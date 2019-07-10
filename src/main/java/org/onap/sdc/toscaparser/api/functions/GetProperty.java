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

import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RelationshipTemplate;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.TopologyTemplate;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.CapabilityTypeDef;
import org.onap.sdc.toscaparser.api.elements.EntityType;
import org.onap.sdc.toscaparser.api.elements.NodeType;
import org.onap.sdc.toscaparser.api.elements.PropertyDef;
import org.onap.sdc.toscaparser.api.elements.RelationshipType;
import org.onap.sdc.toscaparser.api.elements.StatefulEntityType;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GetProperty extends Function {
    // Get a property value of an entity defined in the same service template

    // Arguments:

    // * Node template name | SELF | HOST | SOURCE | TARGET.
    // * Requirement or capability name (optional).
    // * Property name.

    // If requirement or capability name is specified, the behavior is as follows:
    // The req or cap name is first looked up in the specified node template's
    // requirements.
    // If found, it would search for a matching capability
    // of an other node template and get its property as specified in function
    // arguments.
    // Otherwise, the req or cap name would be looked up in the specified
    // node template's capabilities and if found, it would return  the property of
    // the capability as specified in function arguments.

    // Examples:

    // * { get_property: [ mysql_server, port ] }
    // * { get_property: [ SELF, db_port ] }
    // * { get_property: [ SELF, database_endpoint, port ] }
    // * { get_property: [ SELF, database_endpoint, port, 1 ] }


    public GetProperty(TopologyTemplate ttpl, Object context, String name, ArrayList<Object> args) {
        super(ttpl, context, name, args);
    }

    @Override
    void validate() {
        if (args.size() < 2) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE167",
                    "ValueError: Illegal arguments for function \"get_property\". Expected arguments: \"node-template-name\", \"req-or-cap\" (optional), \"property name.\""));
            return;
        }
        if (args.size() == 2) {
            Property foundProp = _findProperty((String) args.get(1));
            if (foundProp == null) {
                return;
            }
            Object prop = foundProp.getValue();
            if (prop instanceof Function) {
                getFunction(toscaTpl, context, prop, toscaTpl.getResolveGetInput());
            }
        } else if (args.size() >= 3) {
            // do not use _find_property to avoid raise KeyError
            // if the prop is not found
            // First check if there is property with this name
            NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
            LinkedHashMap<String, Property> props;
            if (nodeTpl != null) {
                props = nodeTpl.getProperties();
            } else {
                props = new LinkedHashMap<>();
            }
            int index = 2;
            Object propertyValue;
            if (props.get(args.get(1)) != null) {
                propertyValue = ((Property) props.get(args.get(1))).getValue();
            } else {
                index = 3;
                // then check the req or caps
                propertyValue = _findReqOrCapProperty((String) args.get(1), (String) args.get(2));
            }

            if (args.size() > index) {
                for (Object elem : args.subList(index, args.size() - 1)) {
                    if (propertyValue instanceof ArrayList) {
                        int intElem = (int) elem;
                        propertyValue = _getIndexValue(propertyValue, intElem);
                    } else {
                        propertyValue = _getAttributeValue(propertyValue, (String) elem);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object _findReqOrCapProperty(String reqOrCap, String propertyName) {
        NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
        if (nodeTpl == null) {
            return null;
        }
        // look for property in node template's requirements
        for (RequirementAssignment req : nodeTpl.getRequirements().getAll()) {
            String nodeName = req.getNodeTemplateName();
            if (req.getName().equals(reqOrCap)) {
                NodeTemplate nodeTemplate = _findNodeTemplate(nodeName);
                return _getCapabilityProperty(nodeTemplate, req.getName(), propertyName, true);
            }
        }
        // If requirement was not found, look in node template's capabilities
        return _getCapabilityProperty(nodeTpl, reqOrCap, propertyName, true);
    }

    private Object _getCapabilityProperty(NodeTemplate nodeTemplate,
                                          String capabilityName,
                                          String propertyName,
                                          boolean throwErrors) {

        // Gets a node template capability property
        Object property = null;
        CapabilityAssignment cap = nodeTemplate.getCapabilities().getCapabilityByName(capabilityName);
        if (cap != null) {
            LinkedHashMap<String, Property> props = cap.getProperties();
            if (props != null && props.get(propertyName) != null) {
                property = ((Property) props.get(propertyName)).getValue();
            }
            if (property == null && throwErrors) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE168", String.format(
                        "KeyError: Property \"%s\" was not found in capability \"%s\" of node template \"%s\" referenced from node template \"%s\"",
                        propertyName, capabilityName, nodeTemplate.getName(), ((NodeTemplate) context).getName())));
            }
            return property;
        }
        if (throwErrors) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE169", String.format(
                    "KeyError: Requirement/CapabilityAssignment \"%s\" referenced from node template \"%s\" was not found in node template \"%s\"",
                    capabilityName, ((NodeTemplate) context).getName(), nodeTemplate.getName())));
        }

        return null;
    }

    private Property _findProperty(String propertyName) {
        NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
        if (nodeTpl == null) {
            return null;
        }
        LinkedHashMap<String, Property> props = nodeTpl.getProperties();
        Property found = props.get(propertyName);
        if (found == null) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE170", String.format(
                    "KeyError: Property \"%s\" was not found in node template \"%s\"",
                    propertyName, nodeTpl.getName())));
        }
        return found;
    }

    private NodeTemplate _findNodeTemplate(String nodeTemplateName) {
        if (nodeTemplateName.equals(SELF)) {
            return (NodeTemplate) context;
        }
        // enable the HOST value in the function
        if (nodeTemplateName.equals(HOST)) {
            NodeTemplate node = _findHostContainingProperty(null);
            if (node == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE171", String.format(
                        "KeyError: Property \"%s\" was not found in capability \"%s\" of node template \"%s\" referenced from node template \"%s\"",
                        (String) args.get(2), (String) args.get(1), ((NodeTemplate) context).getName())));
                return null;
            }
            return node;
        }
        if (nodeTemplateName.equals(TARGET)) {
            if (!(((RelationshipTemplate) context).getTypeDefinition() instanceof RelationshipType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE172",
                        "KeyError: \"TARGET\" keyword can only be used in context to \"Relationships\" target node"));
                return null;
            }
            return ((RelationshipTemplate) context).getTarget();
        }
        if (nodeTemplateName.equals(SOURCE)) {
            if (!(((RelationshipTemplate) context).getTypeDefinition() instanceof RelationshipType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE173",
                        "KeyError: \"SOURCE\" keyword can only be used in context to \"Relationships\" target node"));
                return null;
            }
            return ((RelationshipTemplate) context).getSource();
        }
        if (toscaTpl.getNodeTemplates() == null) {
            return null;
        }
        for (NodeTemplate nodeTemplate : toscaTpl.getNodeTemplates()) {
            if (nodeTemplate.getName().equals(nodeTemplateName)) {
                return nodeTemplate;
            }
        }
        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE174", String.format(
                "KeyError: Node template \"%s\" was not found. Referenced from Node Template \"%s\"",
                nodeTemplateName, ((NodeTemplate) context).getName())));

        return null;
    }

    @SuppressWarnings("rawtypes")
    private Object _getIndexValue(Object value, int index) {
        if (value instanceof ArrayList) {
            if (index < ((ArrayList) value).size()) {
                return ((ArrayList) value).get(index);
            } else {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE175", String.format(
                        "KeyError: Property \"%s\" found in capability \"%s\" referenced from node template \"%s\" must have an element with index %d",
                        args.get(2), args.get(1), ((NodeTemplate) context).getName(), index)));

            }
        } else {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE176", String.format(
                    "KeyError: Property \"%s\" found in capability \"%s\" referenced from node template \"%s\" must be a list",
                    args.get(2), args.get(1), ((NodeTemplate) context).getName())));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object _getAttributeValue(Object value, String attribute) {
        if (value instanceof LinkedHashMap) {
            Object ov = ((LinkedHashMap<String, Object>) value).get(attribute);
            if (ov != null) {
                return ov;
            } else {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE177", String.format(
                        "KeyError: Property \"%s\" found in capability \"%s\" referenced from node template \"%s\" must have an attribute named \"%s\"",
                        args.get(2), args.get(1), ((NodeTemplate) context).getName(), attribute)));
            }
        } else {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE178", String.format(
                    "KeyError: Property \"%s\" found in capability \"%s\" referenced from node template \"%s\" must be a dict",
                    args.get(2), args.get(1), ((NodeTemplate) context).getName())));
        }
        return null;
    }

    // Add this functions similar to get_attribute case
    private NodeTemplate _findHostContainingProperty(String nodeTemplateName) {
        if (nodeTemplateName == null) {
            nodeTemplateName = SELF;
        }
        NodeTemplate nodeTemplate = _findNodeTemplate(nodeTemplateName);
        LinkedHashMap<String, Object> hostedOnRel = (LinkedHashMap<String, Object>)
                EntityType.TOSCA_DEF.get(HOSTED_ON);
        for (RequirementAssignment requirement : nodeTemplate.getRequirements().getAll()) {
            String targetName = requirement.getNodeTemplateName();
            NodeTemplate targetNode = _findNodeTemplate(targetName);
            NodeType targetType = (NodeType) targetNode.getTypeDefinition();
            for (CapabilityTypeDef capDef : targetType.getCapabilitiesObjects()) {
                if (capDef.inheritsFrom((ArrayList<String>) hostedOnRel.get("valid_target_types"))) {
                    if (_propertyExistsInType(targetType)) {
                        return targetNode;
                    }
                    // If requirement was not found, look in node
                    // template's capabilities
                    if (args.size() > 2 &&
                            _getCapabilityProperty(targetNode, (String) args.get(1), (String) args.get(2), false) != null) {
                        return targetNode;
                    }

                    return _findHostContainingProperty(targetName);
                }
            }

        }
        return null;
    }

    private boolean _propertyExistsInType(StatefulEntityType typeDefinition) {
        LinkedHashMap<String, PropertyDef> propsDef = typeDefinition.getPropertiesDef();
        return propsDef.keySet().contains((String) args.get(1));
    }

    @Override
    public Object result() {
        Object propertyValue;
        if (args.size() >= 3) {
            // First check if there is property with this name
            NodeTemplate nodeTpl = _findNodeTemplate((String) args.get(0));
            LinkedHashMap<String, Property> props;
            if (nodeTpl != null) {
                props = nodeTpl.getProperties();
            } else {
                props = new LinkedHashMap<>();
            }
            int index = 2;
            if (props.get(args.get(1)) != null) {
                propertyValue = ((Property) props.get(args.get(1))).getValue();
            } else {
                index = 3;
                // then check the req or caps
                propertyValue = _findReqOrCapProperty((String) args.get(1), (String) args.get(2));
            }

            if (args.size() > index) {
                for (Object elem : args.subList(index, args.size() - 1)) {
                    if (propertyValue instanceof ArrayList) {
                        int intElem = (int) elem;
                        propertyValue = _getIndexValue(propertyValue, intElem);
                    } else {
                        propertyValue = _getAttributeValue(propertyValue, (String) elem);
                    }
                }
            }
        } else {
            propertyValue = _findProperty((String) args.get(1)).getValue();
        }
        if (propertyValue instanceof Function) {
            return ((Function) propertyValue).result();
        }
        return getFunction(toscaTpl, context, propertyValue, toscaTpl.getResolveGetInput());
    }

    public String getNodeTemplateName() {
        return (String) args.get(0);
    }

    public String getPropertyName() {
        if (args.size() > 2) {
            return (String) args.get(2);
        }
        return (String) args.get(1);
    }

    public String getReqorCap() {
        if (args.size() > 2) {
            return (String) args.get(1);
        }
        return null;
    }

}

/*python

class GetProperty(Function):
"""Get a property value of an entity defined in the same service template.

Arguments:

* Node template name | SELF | HOST | SOURCE | TARGET.
* Requirement or capability name (optional).
* Property name.

If requirement or capability name is specified, the behavior is as follows:
The req or cap name is first looked up in the specified node template's
requirements.
If found, it would search for a matching capability
of an other node template and get its property as specified in function
arguments.
Otherwise, the req or cap name would be looked up in the specified
node template's capabilities and if found, it would return  the property of
the capability as specified in function arguments.

Examples:

* { get_property: [ mysql_server, port ] }
* { get_property: [ SELF, db_port ] }
* { get_property: [ SELF, database_endpoint, port ] }
* { get_property: [ SELF, database_endpoint, port, 1 ] }
"""

def validate(self):
    if len(self.args) < 2:
        ValidationIssueCollector.appendException(
            ValueError(_(
                'Expected arguments: "node-template-name", "req-or-cap" '
                '(optional), "property name".')))
        return
    if len(self.args) == 2:
        found_prop = self._find_property(self.args[1])
        if not found_prop:
            return
        prop = found_prop.value
        if not isinstance(prop, Function):
            get_function(self.tosca_tpl, self.context, prop)
    elif len(self.args) >= 3:
        # do not use _find_property to avoid raise KeyError
        # if the prop is not found
        # First check if there is property with this name
        node_tpl = self._find_node_template(self.args[0])
        props = node_tpl.get_properties() if node_tpl else []
        index = 2
        found = [props[self.args[1]]] if self.args[1] in props else []
        if found:
            property_value = found[0].value
        else:
            index = 3
            # then check the req or caps
            property_value = self._find_req_or_cap_property(self.args[1],
                                                            self.args[2])
        if len(self.args) > index:
            for elem in self.args[index:]:
                if isinstance(property_value, list):
                    int_elem = int(elem)
                    property_value = self._get_index_value(property_value,
                                                           int_elem)
                else:
                    property_value = self._get_attribute_value(
                        property_value,
                        elem)

def _find_req_or_cap_property(self, req_or_cap, property_name):
    node_tpl = self._find_node_template(self.args[0])
    # Find property in node template's requirements
    for r in node_tpl.requirements:
        for req, node_name in r.items():
            if req == req_or_cap:
                node_template = self._find_node_template(node_name)
                return self._get_capability_property(
                    node_template,
                    req,
                    property_name)
    # If requirement was not found, look in node template's capabilities
    return self._get_capability_property(node_tpl,
                                         req_or_cap,
                                         property_name)

def _get_capability_property(self,
                             node_template,
                             capability_name,
                             property_name):
    """Gets a node template capability property."""
    caps = node_template.get_capabilities()
    if caps and capability_name in caps.keys():
        cap = caps[capability_name]
        property = None
        props = cap.get_properties()
        if props and property_name in props.keys():
            property = props[property_name].value
        if not property:
            ValidationIssueCollector.appendException(
                KeyError(_('Property "%(prop)s" was not found in '
                           'capability "%(cap)s" of node template '
                           '"%(ntpl1)s" referenced from node template '
                           '"%(ntpl2)s".') % {'prop': property_name,
                                              'cap': capability_name,
                                              'ntpl1': node_template.name,
                                              'ntpl2': self.context.name}))
        return property
    msg = _('Requirement/CapabilityAssignment "{0}" referenced from node template '
            '"{1}" was not found in node template "{2}".').format(
                capability_name,
                self.context.name,
                node_template.name)
    ValidationIssueCollector.appendException(KeyError(msg))

def _find_property(self, property_name):
    node_tpl = self._find_node_template(self.args[0])
    if not node_tpl:
        return
    props = node_tpl.get_properties()
    found = [props[property_name]] if property_name in props else []
    if len(found) == 0:
        ValidationIssueCollector.appendException(
            KeyError(_('Property "%(prop)s" was not found in node '
                       'template "%(ntpl)s".') %
                     {'prop': property_name,
                      'ntpl': node_tpl.name}))
        return None
    return found[0]

def _find_node_template(self, node_template_name):
    if node_template_name == SELF:
        return self.context
    # enable the HOST value in the function
    if node_template_name == HOST:
        return self._find_host_containing_property()
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
    if not hasattr(self.tosca_tpl, 'nodetemplates'):
        return
    for node_template in self.tosca_tpl.nodetemplates:
        if node_template.name == node_template_name:
            return node_template
    ValidationIssueCollector.appendException(
        KeyError(_(
            'Node template "{0}" was not found.'
            ).format(node_template_name)))

def _get_index_value(self, value, index):
    if isinstance(value, list):
        if index < len(value):
            return value[index]
        else:
            ValidationIssueCollector.appendException(
                KeyError(_(
                    "Property '{0}' found in capability '{1}'"
                    " referenced from node template {2}"
                    " must have an element with index {3}.").
                    format(self.args[2],
                           self.args[1],
                           self.context.name,
                           index)))
    else:
        ValidationIssueCollector.appendException(
            KeyError(_(
                "Property '{0}' found in capability '{1}'"
                " referenced from node template {2}"
                " must be a list.").format(self.args[2],
                                           self.args[1],
                                           self.context.name)))

def _get_attribute_value(self, value, attibute):
    if isinstance(value, dict):
        if attibute in value:
            return value[attibute]
        else:
            ValidationIssueCollector.appendException(
                KeyError(_(
                    "Property '{0}' found in capability '{1}'"
                    " referenced from node template {2}"
                    " must have an attribute named {3}.").
                    format(self.args[2],
                           self.args[1],
                           self.context.name,
                           attibute)))
    else:
        ValidationIssueCollector.appendException(
            KeyError(_(
                "Property '{0}' found in capability '{1}'"
                " referenced from node template {2}"
                " must be a dict.").format(self.args[2],
                                           self.args[1],
                                           self.context.name)))

# Add this functions similar to get_attribute case
def _find_host_containing_property(self, node_template_name=SELF):
    node_template = self._find_node_template(node_template_name)
    hosted_on_rel = EntityType.TOSCA_DEF[HOSTED_ON]
    for r in node_template.requirements:
        for requirement, target_name in r.items():
            target_node = self._find_node_template(target_name)
            target_type = target_node.type_definition
            for capability in target_type.get_capabilities_objects():
                if capability.type in hosted_on_rel['valid_target_types']:
                    if self._property_exists_in_type(target_type):
                        return target_node
                    return self._find_host_containing_property(
                        target_name)
    return None

def _property_exists_in_type(self, type_definition):
    props_def = type_definition.get_properties_def()
    found = [props_def[self.args[1]]] \
        if self.args[1] in props_def else []
    return len(found) == 1

def result(self):
    if len(self.args) >= 3:
        # First check if there is property with this name
        node_tpl = self._find_node_template(self.args[0])
        props = node_tpl.get_properties() if node_tpl else []
        index = 2
        found = [props[self.args[1]]] if self.args[1] in props else []
        if found:
            property_value = found[0].value
        else:
            index = 3
            # then check the req or caps
            property_value = self._find_req_or_cap_property(self.args[1],
                                                            self.args[2])
        if len(self.args) > index:
            for elem in self.args[index:]:
                if isinstance(property_value, list):
                    int_elem = int(elem)
                    property_value = self._get_index_value(property_value,
                                                           int_elem)
                else:
                    property_value = self._get_attribute_value(
                        property_value,
                        elem)
    else:
        property_value = self._find_property(self.args[1]).value
    if isinstance(property_value, Function):
        return property_value.result()
    return get_function(self.tosca_tpl,
                        self.context,
                        property_value)

@property
def node_template_name(self):
    return self.args[0]

@property
def property_name(self):
    if len(self.args) > 2:
        return self.args[2]
    return self.args[1]

@property
def req_or_cap(self):
    if len(self.args) > 2:
        return self.args[1]
    return None
*/
