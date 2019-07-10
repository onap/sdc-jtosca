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

import org.onap.sdc.toscaparser.api.EntityTemplate;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.RelationshipTemplate;
import org.onap.sdc.toscaparser.api.TopologyTemplate;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.InterfacesDef;
import org.onap.sdc.toscaparser.api.elements.RelationshipType;
import org.onap.sdc.toscaparser.api.elements.StatefulEntityType;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;


public class GetOperationOutput extends Function {

    public GetOperationOutput(TopologyTemplate ttpl, Object context, String name, ArrayList<Object> args) {
        super(ttpl, context, name, args);
    }

    @Override
    public void validate() {
        if (args.size() == 4) {
            _findNodeTemplate((String) args.get(0));
            String interfaceName = _findInterfaceName((String) args.get(1));
            _findOperationName(interfaceName, (String) args.get(2));
        } else {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE159",
                    "ValueError: Illegal arguments for function \"get_operation_output\". " +
                            "Expected arguments: \"template_name\",\"interface_name\"," +
                            "\"operation_name\",\"output_variable_name\""));
        }
    }

    private String _findInterfaceName(String _interfaceName) {
        boolean bFound = false;
        for (String sect : InterfacesDef.SECTIONS) {
            if (sect.equals(_interfaceName)) {
                bFound = true;
                break;
            }
        }
        if (bFound) {
            return _interfaceName;
        } else {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE160", String.format(
                    "ValueError: invalid interface name \"%s\" in \"get_operation_output\"",
                    _interfaceName)));
            return null;
        }
    }

    private String _findOperationName(String interfaceName, String operationName) {

        if (interfaceName.equals("Configure") ||
                interfaceName.equals("tosca.interfaces.node.relationship.Configure")) {
            boolean bFound = false;
            for (String sect : StatefulEntityType.INTERFACE_RELATIONSHIP_CONFIGURE_OPERATIONS) {
                if (sect.equals(operationName)) {
                    bFound = true;
                    break;
                }
            }
            if (bFound) {
                return operationName;
            } else {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE161", String.format(
                        "ValueError: Invalid operation of Configure interface \"%s\" in \"get_operation_output\"",
                        operationName)));
                return null;
            }
        }
        if (interfaceName.equals("Standard") ||
                interfaceName.equals("tosca.interfaces.node.lifecycle.Standard")) {
            boolean bFound = false;
            for (String sect : StatefulEntityType.INTERFACE_NODE_LIFECYCLE_OPERATIONS) {
                if (sect.equals(operationName)) {
                    bFound = true;
                    break;
                }
            }
            if (bFound) {
                return operationName;
            } else {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE162", String.format(
                        "ValueError: Invalid operation of Configure interface \"%s\" in \"get_operation_output\"",
                        operationName)));
                return null;
            }
        } else {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE163", String.format(
                    "ValueError: Invalid interface name \"%s\" in \"get_operation_output\"",
                    interfaceName)));
            return null;
        }
    }

    private NodeTemplate _findNodeTemplate(String nodeTemplateName) {
        if (nodeTemplateName.equals(TARGET)) {
            if (!(((EntityTemplate) context).getTypeDefinition() instanceof RelationshipType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE164",
                        "KeyError: \"TARGET\" keyword can only be used in context " +
                                " to \"Relationships\" target node"));
                return null;
            }
            return ((RelationshipTemplate) context).getTarget();
        }
        if (nodeTemplateName.equals(SOURCE)) {
            if (!(((EntityTemplate) context).getTypeDefinition() instanceof RelationshipType)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE165",
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
            if (nodeTemplateName.equals(name)) {
                return nt;
            }
        }
        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE166", String.format(
                "KeyError: Node template \"%s\" was not found", nodeTemplateName)));
        return null;
    }

    @Override
    public Object result() {
        return this;
    }

}

/*python 

class GetOperationOutput(Function):
def validate(self):
    if len(self.args) == 4:
        self._find_node_template(self.args[0])
        interface_name = self._find_interface_name(self.args[1])
        self._find_operation_name(interface_name, self.args[2])
    else:
        ValidationIssueCollector.appendException(
            ValueError(_('Illegal arguments for function "{0}". Expected '
                         'arguments: "template_name","interface_name",'
                         '"operation_name","output_variable_name"'
                         ).format(GET_OPERATION_OUTPUT)))
        return

def _find_interface_name(self, interface_name):
    if interface_name in toscaparser.elements.interfaces.SECTIONS:
        return interface_name
    else:
        ValidationIssueCollector.appendException(
            ValueError(_('Enter a valid interface name'
                         ).format(GET_OPERATION_OUTPUT)))
        return

def _find_operation_name(self, interface_name, operation_name):
    if(interface_name == 'Configure' or
       interface_name == 'tosca.interfaces.node.relationship.Configure'):
        if(operation_name in
           StatefulEntityType.
           interfaces_relationship_configure_operations):
            return operation_name
        else:
            ValidationIssueCollector.appendException(
                ValueError(_('Enter an operation of Configure interface'
                             ).format(GET_OPERATION_OUTPUT)))
            return
    elif(interface_name == 'Standard' or
         interface_name == 'tosca.interfaces.node.lifecycle.Standard'):
        if(operation_name in
           StatefulEntityType.interfaces_node_lifecycle_operations):
            return operation_name
        else:
            ValidationIssueCollector.appendException(
                ValueError(_('Enter an operation of Standard interface'
                             ).format(GET_OPERATION_OUTPUT)))
            return
    else:
        ValidationIssueCollector.appendException(
            ValueError(_('Enter a valid operation name'
                         ).format(GET_OPERATION_OUTPUT)))
        return

def _find_node_template(self, node_template_name):
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

def result(self):
    return self
*/
