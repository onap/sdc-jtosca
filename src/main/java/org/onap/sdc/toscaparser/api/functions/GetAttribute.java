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

package org.onap.sdc.toscaparser.api.functions;

import org.onap.sdc.toscaparser.api.*;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.onap.sdc.toscaparser.api.*;
import org.onap.sdc.toscaparser.api.elements.AttributeDef;
import org.onap.sdc.toscaparser.api.elements.CapabilityTypeDef;
import org.onap.sdc.toscaparser.api.elements.DataType;
import org.onap.sdc.toscaparser.api.elements.EntityType;
import org.onap.sdc.toscaparser.api.elements.NodeType;
import org.onap.sdc.toscaparser.api.elements.PropertyDef;
import org.onap.sdc.toscaparser.api.elements.RelationshipType;
import org.onap.sdc.toscaparser.api.elements.StatefulEntityType;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;

public class GetAttribute extends Function {
    // Get an attribute value of an entity defined in the service template

    // Node template attributes values are set in runtime and therefore its the
    // responsibility of the Tosca engine to implement the evaluation of
    // get_attribute functions.

    // Arguments:

    // * Node template name | HOST.
    // * Attribute name.

    // If the HOST keyword is passed as the node template name argument the
    // function will search each node template along the HostedOn relationship
    // chain until a node which contains the attribute is found.

    // Examples:

    // * { get_attribute: [ server, private_address ] }
    // * { get_attribute: [ HOST, private_address ] }
    // * { get_attribute: [ HOST, private_address, 0 ] }
    // * { get_attribute: [ HOST, private_address, 0, some_prop] }

    public GetAttribute(TopologyTemplate ttpl, Object context, String name, ArrayList<Object> args) {
        super(ttpl, context, name, args);
    }

    @Override
    void validate() {
        if (args.size() < 2) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE146",
                    "ValueError: Illegal arguments for function \"get_attribute\". Expected arguments: \"node-template-name\", \"req-or-cap\" (optional), \"property name.\""));
            return;
        } else if (args.size() == 2) {
            _findNodeTemplateContainingAttribute();
        } else {
            NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
            if (nodeTpl == null) {
                return;
            }
            int index = 2;
            AttributeDef attr = nodeTpl.getTypeDefinition().getAttributeDefValue((String) args.get(1));
            if (attr != null) {
                // found
            } else {
                index = 3;
                // then check the req or caps
                if (!(args.get(1) instanceof String) || !(args.get(2) instanceof String)) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE146", "ValueError: Illegal arguments for function \"get_attribute\". Expected a String argument"));
                }

                attr = _findReqOrCapAttribute(args.get(1).toString(), args.get(2).toString());
                if (attr == null) {
                    return;
                }
            }


            String valueType = (String) attr.getSchema().get("type");
            if (args.size() > index) {
                for (Object elem : args.subList(index, args.size())) {
                    if (valueType.equals("list")) {
                        if (!(elem instanceof Integer)) {
                            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE147", String.format(
                                    "ValueError: Illegal arguments for function \"get_attribute\" \"%s\". Expected positive integer argument",
                                    elem.toString())));
                        }
                        Object ob = attr.getSchema().get("entry_schema");
                        valueType = (String)
                                ((LinkedHashMap<String, Object>) ob).get("type");
                    } else if (valueType.equals("map")) {
                        Object ob = attr.getSchema().get("entry_schema");
                        valueType = (String)
                                ((LinkedHashMap<String, Object>) ob).get("type");
                    } else {
                        boolean bFound = false;
                        for (String p : Schema.PROPERTY_TYPES) {
                            if (p.equals(valueType)) {
                                bFound = true;
                                break;
                            }
                        }
                        if (bFound) {
                            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE148", String.format(
                                    "ValueError: 'Illegal arguments for function \"get_attribute\". Unexpected attribute/index value \"%s\"",
                                    elem)));
                            return;
                        } else {  // It is a complex type
                            DataType dataType = new DataType(valueType, null);
                            LinkedHashMap<String, PropertyDef> props =
                                    dataType.getAllProperties();
                            PropertyDef prop = props.get((String) elem);
                            if (prop != null) {
                                valueType = (String) prop.getSchema().get("type");
                            } else {
                                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE149", String.format(
                                        "KeyError: Illegal arguments for function \"get_attribute\". Attribute name \"%s\" not found in \"%\"",
                                        elem, valueType)));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object result() {
        return this;
    }

    private NodeTemplate getReferencedNodeTemplate() {
        // Gets the NodeTemplate instance the get_attribute function refers to

        // If HOST keyword was used as the node template argument, the node
        // template which contains the attribute along the HostedOn relationship
        // chain will be returned.

        return _findNodeTemplateContainingAttribute();

    }

    // Attributes can be explicitly created as part of the type definition
    // or a property name can be implicitly used as an attribute name
    private NodeTemplate _findNodeTemplateContainingAttribute() {
        NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
        if (nodeTpl != null &&
                !_attributeExistsInType(nodeTpl.getTypeDefinition()) &&
                !nodeTpl.getProperties().keySet().contains(getAttributeName())) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE150", String.format(
                    "KeyError: Attribute \"%s\" was not found in node template \"%s\"",
                    getAttributeName(), nodeTpl.getName())));
        }
        return nodeTpl;
    }

    private boolean _attributeExistsInType(StatefulEntityType typeDefinition) {
        LinkedHashMap<String, AttributeDef> attrsDef = typeDefinition.getAttributesDef();
        return attrsDef.get(getAttributeName()) != null;
    }

    private NodeTemplate _findHostContainingAttribute(String nodeTemplateName) {
        NodeTemplate nodeTemplate = _findNodeTemplate(nodeTemplateName);
        if (nodeTemplate != null) {
            LinkedHashMap<String, Object> hostedOnRel =
                    (LinkedHashMap<String, Object>) EntityType.TOSCA_DEF.get(HOSTED_ON);
            for (RequirementAssignment r : nodeTemplate.getRequirements().getAll()) {
                String targetName = r.getNodeTemplateName();
                NodeTemplate targetNode = _findNodeTemplate(targetName);
                NodeType targetType = (NodeType) targetNode.getTypeDefinition();
                for (CapabilityTypeDef capability : targetType.getCapabilitiesObjects()) {
//							if(((ArrayList<String>)hostedOnRel.get("valid_target_types")).contains(capability.getType())) {
                    if (capability.inheritsFrom((ArrayList<String>) hostedOnRel.get("valid_target_types"))) {
                        if (_attributeExistsInType(targetType)) {
                            return targetNode;
                        }
                        return _findHostContainingAttribute(targetName);
                    }
                }
            }
        }
        return null;
    }


    private NodeTemplate _findNodeTemplate(String nodeTemplateName) {
        if (nodeTemplateName.equals(HOST)) {
            // Currently this is the only way to tell whether the function
            // is used within the outputs section of the TOSCA template.
            if (context instanceof ArrayList) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE151",
                        "ValueError: \"get_attribute: [ HOST, ... ]\" is not allowed in \"outputs\" section of the TOSCA template"));
                return null;
            }
            NodeTemplate nodeTpl = _findHostContainingAttribute(SELF);
            if (nodeTpl == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE152", String.format(
                        "ValueError: \"get_attribute: [ HOST, ... ]\" was used in " +
                                "node template \"%s\" but \"%s\" was not found in " +
                                "the relationship chain", ((NodeTemplate) context).getName(), HOSTED_ON)));
                return null;
            }
            return nodeTpl;
        }
        if (nodeTemplateName.equals(TARGET)) {
            if (!(((EntityTemplate) context).getTypeDefinition() instanceof RelationshipType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE153",
                        "KeyError: \"TARGET\" keyword can only be used in context " +
                                " to \"Relationships\" target node"));
                return null;
            }
            return ((RelationshipTemplate) context).getTarget();
        }
        if (nodeTemplateName.equals(SOURCE)) {
            if (!(((EntityTemplate) context).getTypeDefinition() instanceof RelationshipType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE154",
                        "KeyError: \"SOURCE\" keyword can only be used in context " +
                                " to \"Relationships\" source node"));
                return null;
            }
            return ((RelationshipTemplate) context).getTarget();
        }
        String name;
        if (nodeTemplateName.equals(SELF) && !(context instanceof ArrayList)) {
            name = ((NodeTemplate) context).getName();
        } else {
            name = nodeTemplateName;
        }
        for (NodeTemplate nt : toscaTpl.getNodeTemplates()) {
            if (nt.getName().equals(name)) {
                return nt;
            }
        }
        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE155", String.format(
                "KeyError: Node template \"%s\" was not found", nodeTemplateName)));
        return null;
    }

    public AttributeDef _findReqOrCapAttribute(String reqOrCap, String attrName) {

        NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
        // Find attribute in node template's requirements
        for (RequirementAssignment r : nodeTpl.getRequirements().getAll()) {
            String nodeName = r.getNodeTemplateName();
            if (r.getName().equals(reqOrCap)) {
                NodeTemplate nodeTemplate = _findNodeTemplate(nodeName);
                return _getCapabilityAttribute(nodeTemplate, r.getName(), attrName);
            }
        }
        // If requirement was not found, look in node template's capabilities
        return _getCapabilityAttribute(nodeTpl, reqOrCap, attrName);
    }

    private AttributeDef _getCapabilityAttribute(NodeTemplate nodeTemplate,
                                                 String capabilityName,
                                                 String attrName) {
        // Gets a node template capability attribute
        CapabilityAssignment cap = nodeTemplate.getCapabilities().getCapabilityByName(capabilityName);

        if (cap != null) {
            AttributeDef attribute = null;
            LinkedHashMap<String, AttributeDef> attrs =
                    cap.getDefinition().getAttributesDef();
            if (attrs != null && attrs.keySet().contains(attrName)) {
                attribute = attrs.get(attrName);
            }
            if (attribute == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE156", String.format(
                        "KeyError: Attribute \"%s\" was not found in capability \"%s\" of node template \"%s\" referenced from node template \"%s\"",
                        attrName, capabilityName, nodeTemplate.getName(), ((NodeTemplate) context).getName())));
            }
            return attribute;
        }
        String msg = String.format(
                "Requirement/CapabilityAssignment \"%s\" referenced from node template \"%s\" was not found in node template \"%s\"",
                capabilityName, ((NodeTemplate) context).getName(), nodeTemplate.getName());
        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE157", "KeyError: " + msg));
        return null;
    }

    String getNodeTemplateName() {
        return (String) args.get(0);
    }

    String getAttributeName() {
        return (String) args.get(1);
    }

}

/*python

class GetAttribute(Function):
"""Get an attribute value of an entity defined in the service template

Node template attributes values are set in runtime and therefore its the
responsibility of the Tosca engine to implement the evaluation of
get_attribute functions.

Arguments:

* Node template name | HOST.
* Attribute name.

If the HOST keyword is passed as the node template name argument the
function will search each node template along the HostedOn relationship
chain until a node which contains the attribute is found.

Examples:

* { get_attribute: [ server, private_address ] }
* { get_attribute: [ HOST, private_address ] }
* { get_attribute: [ HOST, private_address, 0 ] }
* { get_attribute: [ HOST, private_address, 0, some_prop] }
"""

def validate(self):
    if len(self.args) < 2:
        ValidationIssueCollector.appendException(
            ValueError(_('Illegal arguments for function "{0}". Expected '
                         'arguments: "node-template-name", "req-or-cap"'
                         '(optional), "property name"'
                         ).format(GET_ATTRIBUTE)))
        return
    elif len(self.args) == 2:
        self._find_node_template_containing_attribute()
    else:
        node_tpl = self._find_node_template(self.args[0])
        if node_tpl is None:
            return
        index = 2
        attrs = node_tpl.type_definition.get_attributes_def()
        found = [attrs[self.args[1]]] if self.args[1] in attrs else []
        if found:
            attr = found[0]
        else:
            index = 3
            # then check the req or caps
            attr = self._find_req_or_cap_attribute(self.args[1],
                                                   self.args[2])

        value_type = attr.schema['type']
        if len(self.args) > index:
            for elem in self.args[index:]:
                if value_type == "list":
                    if not isinstance(elem, int):
                        ValidationIssueCollector.appendException(
                            ValueError(_('Illegal arguments for function'
                                         ' "{0}". "{1}" Expected positive'
                                         ' integer argument'
                                         ).format(GET_ATTRIBUTE, elem)))
                    value_type = attr.schema['entry_schema']['type']
                elif value_type == "map":
                    value_type = attr.schema['entry_schema']['type']
                elif value_type in Schema.PROPERTY_TYPES:
                    ValidationIssueCollector.appendException(
                        ValueError(_('Illegal arguments for function'
                                     ' "{0}". Unexpected attribute/'
                                     'index value "{1}"'
                                     ).format(GET_ATTRIBUTE, elem)))
                    return
                else:  # It is a complex type
                    data_type = DataType(value_type)
                    props = data_type.get_all_properties()
                    found = [props[elem]] if elem in props else []
                    if found:
                        prop = found[0]
                        value_type = prop.schema['type']
                    else:
                        ValidationIssueCollector.appendException(
                            KeyError(_('Illegal arguments for function'
                                       ' "{0}". Attribute name "{1}" not'
                                       ' found in "{2}"'
                                       ).format(GET_ATTRIBUTE,
                                                elem,
                                                value_type)))

def result(self):
    return self

def get_referenced_node_template(self):
    """Gets the NodeTemplate instance the get_attribute function refers to.

    If HOST keyword was used as the node template argument, the node
    template which contains the attribute along the HostedOn relationship
    chain will be returned.
    """
    return self._find_node_template_containing_attribute()

# Attributes can be explicitly created as part of the type definition
# or a property name can be implicitly used as an attribute name
def _find_node_template_containing_attribute(self):
    node_tpl = self._find_node_template(self.args[0])
    if node_tpl and \
            not self._attribute_exists_in_type(node_tpl.type_definition) \
            and self.attribute_name not in node_tpl.get_properties():
        ValidationIssueCollector.appendException(
            KeyError(_('Attribute "%(att)s" was not found in node '
                       'template "%(ntpl)s".') %
                     {'att': self.attribute_name,
                      'ntpl': node_tpl.name}))
    return node_tpl

def _attribute_exists_in_type(self, type_definition):
    attrs_def = type_definition.get_attributes_def()
    found = [attrs_def[self.attribute_name]] \
        if self.attribute_name in attrs_def else []
    return len(found) == 1

def _find_host_containing_attribute(self, node_template_name=SELF):
    node_template = self._find_node_template(node_template_name)
    if node_template:
        hosted_on_rel = EntityType.TOSCA_DEF[HOSTED_ON]
        for r in node_template.requirements:
            for requirement, target_name in r.items():
                target_node = self._find_node_template(target_name)
                target_type = target_node.type_definition
                for capability in target_type.get_capabilities_objects():
                    if capability.type in \
                            hosted_on_rel['valid_target_types']:
                        if self._attribute_exists_in_type(target_type):
                            return target_node
                        return self._find_host_containing_attribute(
                            target_name)

def _find_node_template(self, node_template_name):
    if node_template_name == HOST:
        # Currently this is the only way to tell whether the function
        # is used within the outputs section of the TOSCA template.
        if isinstance(self.context, list):
            ValidationIssueCollector.appendException(
                ValueError(_(
                    '"get_attribute: [ HOST, ... ]" is not allowed in '
                    '"outputs" section of the TOSCA template.')))
            return
        node_tpl = self._find_host_containing_attribute()
        if not node_tpl:
            ValidationIssueCollector.appendException(
                ValueError(_(
                    '"get_attribute: [ HOST, ... ]" was used in node '
                    'template "{0}" but "{1}" was not found in '
                    'the relationship chain.').format(self.context.name,
                                                      HOSTED_ON)))
            return
        return node_tpl
    if node_template_name == TARGET:
        if not isinstance(self.context.type_definition, RelationshipType):
            ValidationIssueCollector.appendException(
                KeyError(_('"TARGET" keyword can only be used in context'
                           ' to "Relationships" target node')))
            return
        return self.context.target
    if node_template_name == SOURCE:
        if not isinstance(self.context.type_definition, RelationshipType):
            ValidationIssueCollector.appendException(
                KeyError(_('"SOURCE" keyword can only be used in context'
                           ' to "Relationships" source node')))
            return
        return self.context.source
    name = self.context.name \
        if node_template_name == SELF and \
        not isinstance(self.context, list) \
        else node_template_name
    for node_template in self.tosca_tpl.nodetemplates:
        if node_template.name == name:
            return node_template
    ValidationIssueCollector.appendException(
        KeyError(_(
            'Node template "{0}" was not found.'
            ).format(node_template_name)))

def _find_req_or_cap_attribute(self, req_or_cap, attr_name):
    node_tpl = self._find_node_template(self.args[0])
    # Find attribute in node template's requirements
    for r in node_tpl.requirements:
        for req, node_name in r.items():
            if req == req_or_cap:
                node_template = self._find_node_template(node_name)
                return self._get_capability_attribute(
                    node_template,
                    req,
                    attr_name)
    # If requirement was not found, look in node template's capabilities
    return self._get_capability_attribute(node_tpl,
                                          req_or_cap,
                                          attr_name)

def _get_capability_attribute(self,
                              node_template,
                              capability_name,
                              attr_name):
    """Gets a node template capability attribute."""
    caps = node_template.get_capabilities()
    if caps and capability_name in caps.keys():
        cap = caps[capability_name]
        attribute = None
        attrs = cap.definition.get_attributes_def()
        if attrs and attr_name in attrs.keys():
            attribute = attrs[attr_name]
        if not attribute:
            ValidationIssueCollector.appendException(
                KeyError(_('Attribute "%(attr)s" was not found in '
                           'capability "%(cap)s" of node template '
                           '"%(ntpl1)s" referenced from node template '
                           '"%(ntpl2)s".') % {'attr': attr_name,
                                              'cap': capability_name,
                                              'ntpl1': node_template.name,
                                              'ntpl2': self.context.name}))
        return attribute
    msg = _('Requirement/CapabilityAssignment "{0}" referenced from node template '
            '"{1}" was not found in node template "{2}".').format(
                capability_name,
                self.context.name,
                node_template.name)
    ValidationIssueCollector.appendException(KeyError(msg))

@property
def node_template_name(self):
    return self.args[0]

@property
def attribute_name(self):
    return self.args[1]
*/
