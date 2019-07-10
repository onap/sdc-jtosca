/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.toscaparser.api.elements;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeType extends StatefulEntityType {
    // TOSCA built-in node type

    private static final String DERIVED_FROM = "derived_from";
    private static final String METADATA = "metadata";
    private static final String PROPERTIES = "properties";
    private static final String VERSION = "version";
    private static final String DESCRIPTION = "description";
    private static final String ATTRIBUTES = "attributes";
    private static final String REQUIREMENTS = "requirements";
    private static final String CAPABILITIES = "capabilities";
    private static final String INTERFACES = "interfaces";
    private static final String ARTIFACTS = "artifacts";

    private static final String SECTIONS[] = {
            DERIVED_FROM, METADATA, PROPERTIES, VERSION, DESCRIPTION, ATTRIBUTES, REQUIREMENTS, CAPABILITIES, INTERFACES, ARTIFACTS
    };

    private String ntype;
    public LinkedHashMap<String, Object> customDef;

    public NodeType(String nttype, LinkedHashMap<String, Object> ntcustomDef) {
        super(nttype, NODE_PREFIX, ntcustomDef);
        ntype = nttype;
        customDef = ntcustomDef;
        _validateKeys();
    }

    public Object getParentType() {
        // Return a node this node is derived from
        if (defs == null) {
            return null;
        }
        String pnode = derivedFrom(defs);
        if (pnode != null && !pnode.isEmpty()) {
            return new NodeType(pnode, customDef);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<RelationshipType, NodeType> getRelationship() {
        // Return a dictionary of relationships to other node types

        // This method returns a dictionary of named relationships that nodes
        // of the current node type (self) can have to other nodes (of specific
        // types) in a TOSCA template.

        LinkedHashMap<RelationshipType, NodeType> relationship = new LinkedHashMap<>();
        ArrayList<LinkedHashMap<String, Object>> requires;
        Object treq = getAllRequirements();
        if (treq != null) {
            // NOTE(sdmonov): Check if requires is a dict.
            // If it is a dict convert it to a list of dicts.
            // This is needed because currently the code below supports only
            // lists as requirements definition. The following check will
            // make sure if a map (dict) was provided it will be converted to
            // a list before proceeding to the parsing.
            if (treq instanceof LinkedHashMap) {
                requires = new ArrayList<>();
                for (Map.Entry<String, Object> me : ((LinkedHashMap<String, Object>) treq).entrySet()) {
                    LinkedHashMap<String, Object> tl = new LinkedHashMap<>();
                    tl.put(me.getKey(), me.getValue());
                    requires.add(tl);
                }
            } else {
                requires = (ArrayList<LinkedHashMap<String, Object>>) treq;
            }

            String keyword = null;
            String nodeType = null;
            for (LinkedHashMap<String, Object> require : requires) {
                String relation = null;
                for (Map.Entry<String, Object> re : require.entrySet()) {
                    String key = re.getKey();
                    LinkedHashMap<String, Object> req = (LinkedHashMap<String, Object>) re.getValue();
                    if (req.get("relationship") != null) {
                        Object trelation = req.get("relationship");
                        // trelation is a string or a dict with "type" mapped to the string we want
                        if (trelation instanceof String) {
                            relation = (String) trelation;
                        } else {
                            if (((LinkedHashMap<String, Object>) trelation).get("type") != null) {
                                relation = (String) ((LinkedHashMap<String, Object>) trelation).get("type");
                            }
                        }
                        nodeType = (String) req.get("node");
                        //BUG meaningless?? LinkedHashMap<String,Object> value = req;
                        if (nodeType != null) {
                            keyword = "node";
                        } else {
                            String getRelation = null;
                            // If nodeTypeByCap is a dict and has a type key
                            // we need to lookup the node type using
                            // the capability type
                            String captype = (String) req.get("capability");
                            nodeType = _getNodeTypeByCap(captype);
                            if (nodeType != null) {
                                getRelation = _getRelation(key, nodeType);
                            } else {
                                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE11", String.format(
                                        "NodeTypeRequirementForCapabilityUnfulfilled: Node type: \"%s\" with requrement \"%s\" for node type with capability type \"%s\" is not found\\unfulfilled", this.ntype, key, captype)));
                            }
                            if (getRelation != null) {
                                relation = getRelation;
                            }
                            keyword = key;
                        }
                    }
                }
                if (relation == null || nodeType == null) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE11", String.format(
                            "NodeTypeForRelationUnfulfilled: Node type \"%s\" - relationship type \"%s\" is unfulfilled", this.ntype, relation)));
                } else {
                    RelationshipType rtype = new RelationshipType(relation, keyword, customDef);
                    NodeType relatednode = new NodeType(nodeType, customDef);
                    relationship.put(rtype, relatednode);
                }
            }
        }
        return relationship;

    }

    @SuppressWarnings("unchecked")
    private String _getNodeTypeByCap(String cap) {
        // Find the node type that has the provided capability

        // This method will lookup all node types if they have the
        // provided capability.
        // Filter the node types
        ArrayList<String> nodeTypes = new ArrayList<>();
        for (String nt : customDef.keySet()) {
            if (nt.startsWith(NODE_PREFIX) || nt.startsWith("org.openecomp") && !nt.equals("tosca.nodes.Root")) {
                nodeTypes.add(nt);
            }
        }
        for (String nt : nodeTypes) {
            LinkedHashMap<String, Object> nodeDef = (LinkedHashMap<String, Object>) customDef.get(nt);
            if (nodeDef instanceof LinkedHashMap && nodeDef.get("capabilities") != null) {
                LinkedHashMap<String, Object> nodeCaps = (LinkedHashMap<String, Object>) nodeDef.get("capabilities");
                if (nodeCaps != null) {
                    for (Object val : nodeCaps.values()) {
                        if (val instanceof LinkedHashMap) {
                            String tp = (String) ((LinkedHashMap<String, Object>) val).get("type");
                            if (tp != null && tp.equals(cap)) {
                                return nt;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String _getRelation(String key, String ndtype) {
        String relation = null;
        NodeType ntype = new NodeType(ndtype, customDef);
        LinkedHashMap<String, CapabilityTypeDef> caps = ntype.getCapabilities();
        if (caps != null && caps.get(key) != null) {
            CapabilityTypeDef c = caps.get(key);
            for (int i = 0; i < RELATIONSHIP_TYPE.length; i++) {
                String r = RELATIONSHIP_TYPE[i];
                if (r != null) {
                    relation = r;
                    break;
                }
                LinkedHashMap<String, Object> rtypedef = (LinkedHashMap<String, Object>) customDef.get(r);
                for (Object o : rtypedef.values()) {
                    LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) o;
                    if (properties.get(c.getType()) != null) {
                        relation = r;
                        break;
                    }
                }
                if (relation != null) {
                    break;
                } else {
                    for (Object o : rtypedef.values()) {
                        LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) o;
                        if (properties.get(c.getParentType()) != null) {
                            relation = r;
                            break;
                        }
                    }
                }
            }
        }
        return relation;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<CapabilityTypeDef> getCapabilitiesObjects() {
        // Return a list of capability objects
        ArrayList<CapabilityTypeDef> typecapabilities = new ArrayList<>();
        LinkedHashMap<String, Object> caps = (LinkedHashMap<String, Object>) getValue(CAPABILITIES, null, true);
        if (caps != null) {
            // 'cname' is symbolic name of the capability
            // 'cvalue' is a dict { 'type': <capability type name> }
            for (Map.Entry<String, Object> me : caps.entrySet()) {
                String cname = me.getKey();
                LinkedHashMap<String, String> cvalue = (LinkedHashMap<String, String>) me.getValue();
                String ctype = cvalue.get("type");
                CapabilityTypeDef cap = new CapabilityTypeDef(cname, ctype, type, customDef);
                typecapabilities.add(cap);
            }
        }
        return typecapabilities;
    }

    public LinkedHashMap<String, CapabilityTypeDef> getCapabilities() {
        // Return a dictionary of capability name-objects pairs
        LinkedHashMap<String, CapabilityTypeDef> caps = new LinkedHashMap<>();
        for (CapabilityTypeDef ctd : getCapabilitiesObjects()) {
            caps.put(ctd.getName(), ctd);
        }
        return caps;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object> getRequirements() {
        return (ArrayList<Object>) getValue(REQUIREMENTS, null, true);
    }

    public ArrayList<Object> getAllRequirements() {
        return getRequirements();
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Object> getInterfaces() {
        return (LinkedHashMap<String, Object>) getValue(INTERFACES, null, false);
    }


    @SuppressWarnings("unchecked")
    public ArrayList<String> getLifecycleInputs() {
        // Return inputs to life cycle operations if found
        ArrayList<String> inputs = new ArrayList<>();
        LinkedHashMap<String, Object> interfaces = getInterfaces();
        if (interfaces != null) {
            for (Map.Entry<String, Object> me : interfaces.entrySet()) {
                String iname = me.getKey();
                LinkedHashMap<String, Object> ivalue = (LinkedHashMap<String, Object>) me.getValue();
                if (iname.equals(InterfacesDef.LIFECYCLE)) {
                    for (Map.Entry<String, Object> ie : ivalue.entrySet()) {
                        if (ie.getKey().equals("input")) {
                            LinkedHashMap<String, Object> y = (LinkedHashMap<String, Object>) ie.getValue();
                            for (String i : y.keySet()) {
                                inputs.add(i);
                            }
                        }
                    }
                }
            }
        }
        return inputs;
    }

    public ArrayList<String> getLifecycleOperations() {
        //  Return available life cycle operations if found
        ArrayList<String> ops = null;
        LinkedHashMap<String, Object> interfaces = getInterfaces();
        if (interfaces != null) {
            InterfacesDef i = new InterfacesDef(this, InterfacesDef.LIFECYCLE, null, null, null);
            ops = i.getLifecycleOps();
        }
        return ops;
    }

    public CapabilityTypeDef getCapability(String name) {
        //BUG?? the python code has to be wrong
        // it refers to a bad attribute 'value'...
        LinkedHashMap<String, CapabilityTypeDef> caps = getCapabilities();
        if (caps != null) {
            return caps.get(name);
        }
        return null;
		/*
	    def get_capability(self, name):
	        caps = self.get_capabilities()
	        if caps and name in caps.keys():
	            return caps[name].value
		*/
    }

    public String getCapabilityType(String name) {
        //BUG?? the python code has to be wrong
        // it refers to a bad attribute 'value'...
        CapabilityTypeDef captype = getCapability(name);
        if (captype != null) {
            return captype.getType();
        }
        return null;
	    /*    	
	    def get_capability_type(self, name):
	        captype = self.get_capability(name)
	        if captype and name in captype.keys():
	            return captype[name].value
	     */
    }

    private void _validateKeys() {
        if (defs != null) {
            for (String key : defs.keySet()) {
                boolean bFound = false;
                for (int i = 0; i < SECTIONS.length; i++) {
                    if (key.equals(SECTIONS[i])) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE124", String.format(
                            "UnknownFieldError: Nodetype \"%s\" has unknown field \"%s\"", ntype, key)));
                }
            }
        }
    }

}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.elements.capabilitytype import CapabilityTypeDef
import org.openecomp.sdc.toscaparser.api.elements.interfaces as ifaces
from toscaparser.elements.interfaces import InterfacesDef
from toscaparser.elements.relationshiptype import RelationshipType
from toscaparser.elements.statefulentitytype import StatefulEntityType


class NodeType(StatefulEntityType):
    '''TOSCA built-in node type.'''
    SECTIONS = (DERIVED_FROM, METADATA, PROPERTIES, VERSION, DESCRIPTION, ATTRIBUTES, REQUIREMENTS, CAPABILITIES, INTERFACES, ARTIFACTS) = \
               ('derived_from', 'metadata', 'properties', 'version',
                'description', 'attributes', 'requirements', 'capabilities',
                'interfaces', 'artifacts')

    def __init__(self, ntype, custom_def=None):
        super(NodeType, self).__init__(ntype, self.NODE_PREFIX, custom_def)
        self.ntype = ntype
        self.custom_def = custom_def
        self._validate_keys()

    @property
    def parent_type(self):
        '''Return a node this node is derived from.'''
        if not hasattr(self, 'defs'):
            return None
        pnode = self.derived_from(self.defs)
        if pnode:
            return NodeType(pnode, self.custom_def)

    @property
    def relationship(self):
        '''Return a dictionary of relationships to other node types.

        This method returns a dictionary of named relationships that nodes
        of the current node type (self) can have to other nodes (of specific
        types) in a TOSCA template.

        '''
        relationship = {}
        requires = self.get_all_requirements()
        if requires:
            # NOTE(sdmonov): Check if requires is a dict.
            # If it is a dict convert it to a list of dicts.
            # This is needed because currently the code below supports only
            # lists as requirements definition. The following check will
            # make sure if a map (dict) was provided it will be converted to
            # a list before proceeding to the parsing.
            if isinstance(requires, dict):
                requires = [{key: value} for key, value in requires.items()]

            keyword = None
            node_type = None
            for require in requires:
                for key, req in require.items():
                    if 'relationship' in req:
                        relation = req.get('relationship')
                        if 'type' in relation:
                            relation = relation.get('type')
                        node_type = req.get('node')
                        value = req
                        if node_type:
                            keyword = 'node'
                        else:
                            # If value is a dict and has a type key
                            # we need to lookup the node type using
                            # the capability type
                            value = req
                            if isinstance(value, dict):
                                captype = value['capability']
                                value = (self.
                                         _get_node_type_by_cap(key, captype))
                            relation = self._get_relation(key, value)
                            keyword = key
                            node_type = value
                rtype = RelationshipType(relation, keyword, self.custom_def)
                relatednode = NodeType(node_type, self.custom_def)
                relationship[rtype] = relatednode
        return relationship

    def _get_node_type_by_cap(self, key, cap):
        '''Find the node type that has the provided capability

        This method will lookup all node types if they have the
        provided capability.
        '''

        # Filter the node types
        node_types = [node_type for node_type in self.TOSCA_DEF.keys()
                      if node_type.startswith(self.NODE_PREFIX) and
                      node_type != 'tosca.nodes.Root']

        for node_type in node_types:
            node_def = self.TOSCA_DEF[node_type]
            if isinstance(node_def, dict) and 'capabilities' in node_def:
                node_caps = node_def['capabilities']
                for value in node_caps.values():
                    if isinstance(value, dict) and \
                            'type' in value and value['type'] == cap:
                        return node_type

    def _get_relation(self, key, ndtype):
        relation = None
        ntype = NodeType(ndtype)
        caps = ntype.get_capabilities()
        if caps and key in caps.keys():
            c = caps[key]
            for r in self.RELATIONSHIP_TYPE:
                rtypedef = ntype.TOSCA_DEF[r]
                for properties in rtypedef.values():
                    if c.type in properties:
                        relation = r
                        break
                if relation:
                    break
                else:
                    for properties in rtypedef.values():
                        if c.parent_type in properties:
                            relation = r
                            break
        return relation

    def get_capabilities_objects(self):
        '''Return a list of capability objects.'''
        typecapabilities = []
        caps = self.get_value(self.CAPABILITIES, None, True)
        if caps:
            # 'name' is symbolic name of the capability
            # 'value' is a dict { 'type': <capability type name> }
            for name, value in caps.items():
                ctype = value.get('type')
                cap = CapabilityTypeDef(name, ctype, self.type,
                                        self.custom_def)
                typecapabilities.append(cap)
        return typecapabilities

    def get_capabilities(self):
        '''Return a dictionary of capability name-objects pairs.'''
        return {cap.name: cap
                for cap in self.get_capabilities_objects()}

    @property
    def requirements(self):
        return self.get_value(self.REQUIREMENTS, None, True)

    def get_all_requirements(self):
        return self.requirements

    @property
    def interfaces(self):
        return self.get_value(self.INTERFACES)

    @property
    def lifecycle_inputs(self):
        '''Return inputs to life cycle operations if found.'''
        inputs = []
        interfaces = self.interfaces
        if interfaces:
            for name, value in interfaces.items():
                if name == ifaces.LIFECYCLE:
                    for x, y in value.items():
                        if x == 'inputs':
                            for i in y.iterkeys():
                                inputs.append(i)
        return inputs

    @property
    def lifecycle_operations(self):
        '''Return available life cycle operations if found.'''
        ops = None
        interfaces = self.interfaces
        if interfaces:
            i = InterfacesDef(self.type, ifaces.LIFECYCLE)
            ops = i.lifecycle_ops
        return ops

    def get_capability(self, name):
        caps = self.get_capabilities()
        if caps and name in caps.keys():
            return caps[name].value

    def get_capability_type(self, name):
        captype = self.get_capability(name)
        if captype and name in captype.keys():
            return captype[name].value

    def _validate_keys(self):
        if self.defs:
            for key in self.defs.keys():
                if key not in self.SECTIONS:
                    ValidationIssueCollector.appendException(
                        UnknownFieldError(what='Nodetype"%s"' % self.ntype,
                                          field=key))
*/
