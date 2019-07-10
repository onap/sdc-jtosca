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

package org.onap.sdc.toscaparser.api;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.NodeType;
import org.onap.sdc.toscaparser.api.elements.PropertyDef;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.toscaparser.api.parameters.Output;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;


public class SubstitutionMappings {
    // SubstitutionMappings class declaration

    // SubstitutionMappings exports the topology template as an
    // implementation of a Node type.

    private static final String NODE_TYPE = "node_type";
    private static final String REQUIREMENTS = "requirements";
    private static final String CAPABILITIES = "capabilities";

    private static final String SECTIONS[] = {NODE_TYPE, REQUIREMENTS, CAPABILITIES};

    private static final String OPTIONAL_OUTPUTS[] = {"tosca_id", "tosca_name", "state"};

    private LinkedHashMap<String, Object> subMappingDef;
    private ArrayList<NodeTemplate> nodetemplates;
    private ArrayList<Input> inputs;
    private ArrayList<Output> outputs;
    private ArrayList<Group> groups;
    private NodeTemplate subMappedNodeTemplate;
    private LinkedHashMap<String, Object> customDefs;
    private LinkedHashMap<String, Object> _capabilities;
    private LinkedHashMap<String, Object> _requirements;

    public SubstitutionMappings(LinkedHashMap<String, Object> smsubMappingDef,
                                ArrayList<NodeTemplate> smnodetemplates,
                                ArrayList<Input> sminputs,
                                ArrayList<Output> smoutputs,
                                ArrayList<Group> smgroups,
                                NodeTemplate smsubMappedNodeTemplate,
                                LinkedHashMap<String, Object> smcustomDefs) {

        subMappingDef = smsubMappingDef;
        nodetemplates = smnodetemplates;
        inputs = sminputs != null ? sminputs : new ArrayList<Input>();
        outputs = smoutputs != null ? smoutputs : new ArrayList<Output>();
        groups = smgroups != null ? smgroups : new ArrayList<Group>();
        subMappedNodeTemplate = smsubMappedNodeTemplate;
        customDefs = smcustomDefs != null ? smcustomDefs : new LinkedHashMap<String, Object>();
        _validate();

        _capabilities = null;
        _requirements = null;
    }

    public String getType() {
        if (subMappingDef != null) {
            return (String) subMappingDef.get(NODE_TYPE);
        }
        return null;
    }

    public ArrayList<NodeTemplate> getNodeTemplates() {
        return nodetemplates;
    }

	/*
    @classmethod
    def get_node_type(cls, sub_mapping_def):
        if isinstance(sub_mapping_def, dict):
            return sub_mapping_def.get(cls.NODE_TYPE)
	*/

    public static String stGetNodeType(LinkedHashMap<String, Object> _subMappingDef) {
        if (_subMappingDef instanceof LinkedHashMap) {
            return (String) _subMappingDef.get(NODE_TYPE);
        }
        return null;
    }

    public String getNodeType() {
        return (String) subMappingDef.get(NODE_TYPE);
    }

    public ArrayList<Input> getInputs() {
        return inputs;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public LinkedHashMap<String, Object> getCapabilities() {
        return (LinkedHashMap<String, Object>) subMappingDef.get(CAPABILITIES);
    }

    public LinkedHashMap<String, Object> getRequirements() {
        return (LinkedHashMap<String, Object>) subMappingDef.get(REQUIREMENTS);
    }

    public NodeType getNodeDefinition() {
        return new NodeType(getNodeType(), customDefs);
    }

    private void _validate() {
        // Basic validation
        _validateKeys();
        _validateType();

        // SubstitutionMapping class syntax validation
        _validateInputs();
        _validateCapabilities();
        _validateRequirements();
        _validateOutputs();
    }

    private void _validateKeys() {
        // validate the keys of substitution mappings
        for (String key : subMappingDef.keySet()) {
            boolean bFound = false;
            for (String s : SECTIONS) {
                if (s.equals(key)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE232", String.format(
                        "UnknownFieldError: SubstitutionMappings contain unknown field \"%s\"",
                        key)));
            }
        }
    }

    private void _validateType() {
        // validate the node_type of substitution mappings
        String nodeType = (String) subMappingDef.get(NODE_TYPE);
        if (nodeType == null) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE233", String.format(
                    "MissingRequiredFieldError: SubstitutionMappings used in topology_template is missing required field \"%s\"",
                    NODE_TYPE)));
        }
        Object nodeTypeDef = customDefs.get(nodeType);
        if (nodeTypeDef == null) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE234", String.format(
                    "InvalidNodeTypeError: \"%s\" is invalid", nodeType)));
        }
    }

    private void _validateInputs() {
        // validate the inputs of substitution mappings.

        // The inputs defined by the topology template have to match the
        // properties of the node type or the substituted node. If there are
        // more inputs than the substituted node has properties, default values
        //must be defined for those inputs.

        HashSet<String> allInputs = new HashSet<>();
        for (Input inp : inputs) {
            allInputs.add(inp.getName());
        }
        HashSet<String> requiredProperties = new HashSet<>();
        for (PropertyDef pd : getNodeDefinition().getPropertiesDefObjects()) {
            if (pd.isRequired() && pd.getDefault() == null) {
                requiredProperties.add(pd.getName());
            }
        }
        // Must provide inputs for required properties of node type.
        for (String property : requiredProperties) {
            // Check property which is 'required' and has no 'default' value
            if (!allInputs.contains(property)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE235", String.format(
                        "MissingRequiredInputError: SubstitutionMappings with node_type \"%s\" is missing required input \"%s\"",
                        getNodeType(), property)));
            }
        }
        // If the optional properties of node type need to be customized by
        // substituted node, it also is necessary to define inputs for them,
        // otherwise they are not mandatory to be defined.
        HashSet<String> customizedParameters = new HashSet<>();
        if (subMappedNodeTemplate != null) {
            customizedParameters.addAll(subMappedNodeTemplate.getProperties().keySet());
        }
        HashSet<String> allProperties = new HashSet<String>(
                getNodeDefinition().getPropertiesDef().keySet());
        HashSet<String> diffset = customizedParameters;
        diffset.removeAll(allInputs);
        for (String parameter : diffset) {
            if (allProperties.contains(parameter)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE236", String.format(
                        "MissingRequiredInputError: SubstitutionMappings with node_type \"%s\" is missing required input \"%s\"",
                        getNodeType(), parameter)));
            }
        }
        // Additional inputs are not in the properties of node type must
        // provide default values. Currently the scenario may not happen
        // because of parameters validation in nodetemplate, here is a
        // guarantee.
        for (Input inp : inputs) {
            diffset = allInputs;
            diffset.removeAll(allProperties);
            if (diffset.contains(inp.getName()) && inp.getDefault() == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE237", String.format(
                        "MissingRequiredInputError: SubstitutionMappings with node_type \"%s\" is missing rquired input \"%s\"",
                        getNodeType(), inp.getName())));
            }
        }
    }

    private void _validateCapabilities() {
        // validate the capabilities of substitution mappings

        // The capabilities must be in node template which be mapped.
        LinkedHashMap<String, Object> tplsCapabilities =
                (LinkedHashMap<String, Object>) subMappingDef.get(CAPABILITIES);
        List<CapabilityAssignment> nodeCapabilities = null;
        if (subMappedNodeTemplate != null) {
            nodeCapabilities = subMappedNodeTemplate.getCapabilities().getAll();
        }
        if (nodeCapabilities != null) {
            for (CapabilityAssignment cap : nodeCapabilities) {
                if (tplsCapabilities != null && tplsCapabilities.get(cap.getName()) == null) {
                    ; //pass
                    // ValidationIssueCollector.appendException(
                    //    UnknownFieldError(what='SubstitutionMappings',
                    //                      field=cap))
                }
            }
        }
    }

    private void _validateRequirements() {
        // validate the requirements of substitution mappings
        //*****************************************************
        //TO-DO - Different from Python code!! one is a bug...
        //*****************************************************
        // The requirements must be in node template which be mapped.
        LinkedHashMap<String, Object> tplsRequirements =
                (LinkedHashMap<String, Object>) subMappingDef.get(REQUIREMENTS);
        List<RequirementAssignment> nodeRequirements = null;
        if (subMappedNodeTemplate != null) {
            nodeRequirements = subMappedNodeTemplate.getRequirements().getAll();
        }
        if (nodeRequirements != null) {
            for (RequirementAssignment ro : nodeRequirements) {
                String cap = ro.getName();
                if (tplsRequirements != null && tplsRequirements.get(cap) == null) {
                    ; //pass
                    // ValidationIssueCollector.appendException(
                    //    UnknownFieldError(what='SubstitutionMappings',
                    //                      field=cap))
                }
            }
        }
    }

    private void _validateOutputs() {
        // validate the outputs of substitution mappings.

        // The outputs defined by the topology template have to match the
        // attributes of the node type or the substituted node template,
        // and the observable attributes of the substituted node template
        // have to be defined as attributes of the node type or outputs in
        // the topology template.

        // The outputs defined by the topology template have to match the
        // attributes of the node type according to the specification, but
        // it's reasonable that there are more inputs than the node type
        // has properties, the specification will be amended?

        for (Output output : outputs) {
            Object ado = getNodeDefinition().getAttributesDef();
            if (ado != null && ((LinkedHashMap<String, Object>) ado).get(output.getName()) == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE238", String.format(
                        "UnknownOutputError: Unknown output \"%s\" in SubstitutionMappings with node_type \"%s\"",
                        output.getName(), getNodeType())));
            }
        }
    }

    @Override
    public String toString() {
        return "SubstitutionMappings{" +
//				"subMappingDef=" + subMappingDef +
//				", nodetemplates=" + nodetemplates +
//				", inputs=" + inputs +
//				", outputs=" + outputs +
//				", groups=" + groups +
                ", subMappedNodeTemplate=" + (subMappedNodeTemplate == null ? "" : subMappedNodeTemplate.getName()) +
//				", customDefs=" + customDefs +
//				", _capabilities=" + _capabilities +
//				", _requirements=" + _requirements +
                '}';
    }

    @Deprecated
    public String toLimitedString() {
        return "SubstitutionMappings{" +
                "subMappingDef=" + subMappingDef +
                ", nodetemplates=" + nodetemplates +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", groups=" + groups +
                ", subMappedNodeTemplate=" + (subMappedNodeTemplate == null ? "" : subMappedNodeTemplate.getName()) +
                ", customDefs=" + customDefs +
                ", _capabilities=" + _capabilities +
                ", _requirements=" + _requirements +
                '}';
    }
}


/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import InvalidNodeTypeError
from toscaparser.common.exception import MissingDefaultValueError
from toscaparser.common.exception import MissingRequiredFieldError
from toscaparser.common.exception import MissingRequiredInputError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.common.exception import UnknownOutputError
from toscaparser.elements.nodetype import NodeType
from toscaparser.utils.gettextutils import _

log = logging.getLogger('tosca')


class SubstitutionMappings(object):
    '''SubstitutionMappings class declaration

    SubstitutionMappings exports the topology template as an
    implementation of a Node type.
    '''

    SECTIONS = (NODE_TYPE, REQUIREMENTS, CAPABILITIES) = \
               ('node_type', 'requirements', 'capabilities')

    OPTIONAL_OUTPUTS = ['tosca_id', 'tosca_name', 'state']

    def __init__(self, sub_mapping_def, nodetemplates, inputs, outputs,
                 sub_mapped_node_template, custom_defs):
        self.nodetemplates = nodetemplates
        self.sub_mapping_def = sub_mapping_def
        self.inputs = inputs or []
        self.outputs = outputs or []
        self.sub_mapped_node_template = sub_mapped_node_template
        self.custom_defs = custom_defs or {}
        self._validate()

        self._capabilities = None
        self._requirements = None

    @property
    def type(self):
        if self.sub_mapping_def:
            return self.sub_mapping_def.get(self.NODE_TYPE)

    @classmethod
    def get_node_type(cls, sub_mapping_def):
        if isinstance(sub_mapping_def, dict):
            return sub_mapping_def.get(cls.NODE_TYPE)

    @property
    def node_type(self):
        return self.sub_mapping_def.get(self.NODE_TYPE)

    @property
    def capabilities(self):
        return self.sub_mapping_def.get(self.CAPABILITIES)

    @property
    def requirements(self):
        return self.sub_mapping_def.get(self.REQUIREMENTS)

    @property
    def node_definition(self):
        return NodeType(self.node_type, self.custom_defs)

    def _validate(self):
        # Basic validation
        self._validate_keys()
        self._validate_type()

        # SubstitutionMapping class syntax validation
        self._validate_inputs()
        self._validate_capabilities()
        self._validate_requirements()
        self._validate_outputs()

    def _validate_keys(self):
        """validate the keys of substitution mappings."""
        for key in self.sub_mapping_def.keys():
            if key not in self.SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what=_('SubstitutionMappings'),
                                      field=key))

    def _validate_type(self):
        """validate the node_type of substitution mappings."""
        node_type = self.sub_mapping_def.get(self.NODE_TYPE)
        if not node_type:
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(
                    what=_('SubstitutionMappings used in topology_template'),
                    required=self.NODE_TYPE))

        node_type_def = self.custom_defs.get(node_type)
        if not node_type_def:
            ValidationIssueCollector.appendException(
                InvalidNodeTypeError(what=node_type))

    def _validate_inputs(self):
        """validate the inputs of substitution mappings.

        The inputs defined by the topology template have to match the
        properties of the node type or the substituted node. If there are
        more inputs than the substituted node has properties, default values
        must be defined for those inputs.
        """

        all_inputs = set([input.name for input in self.inputs])
        required_properties = set([p.name for p in
                                   self.node_definition.
                                   get_properties_def_objects()
                                   if p.required and p.default is None])
        # Must provide inputs for required properties of node type.
        for property in required_properties:
            # Check property which is 'required' and has no 'default' value
            if property not in all_inputs:
                ValidationIssueCollector.appendException(
                    MissingRequiredInputError(
                        what=_('SubstitutionMappings with node_type ')
                        + self.node_type,
                        input_name=property))

        # If the optional properties of node type need to be customized by
        # substituted node, it also is necessary to define inputs for them,
        # otherwise they are not mandatory to be defined.
        customized_parameters = set(self.sub_mapped_node_template
                                    .get_properties().keys()
                                    if self.sub_mapped_node_template else [])
        all_properties = set(self.node_definition.get_properties_def())
        for parameter in customized_parameters - all_inputs:
            if parameter in all_properties:
                ValidationIssueCollector.appendException(
                    MissingRequiredInputError(
                        what=_('SubstitutionMappings with node_type ')
                        + self.node_type,
                        input_name=parameter))

        # Additional inputs are not in the properties of node type must
        # provide default values. Currently the scenario may not happen
        # because of parameters validation in nodetemplate, here is a
        # guarantee.
        for input in self.inputs:
            if input.name in all_inputs - all_properties \
               and input.default is None:
                ValidationIssueCollector.appendException(
                    MissingDefaultValueError(
                        what=_('SubstitutionMappings with node_type ')
                        + self.node_type,
                        input_name=input.name))

    def _validate_capabilities(self):
        """validate the capabilities of substitution mappings."""

        # The capabilites must be in node template wchich be mapped.
        tpls_capabilities = self.sub_mapping_def.get(self.CAPABILITIES)
        node_capabiliteys = self.sub_mapped_node_template.get_capabilities() \
            if self.sub_mapped_node_template else None
        for cap in node_capabiliteys.keys() if node_capabiliteys else []:
            if (tpls_capabilities and
                    cap not in list(tpls_capabilities.keys())):
                pass
                # ValidationIssueCollector.appendException(
                #    UnknownFieldError(what='SubstitutionMappings',
                #                      field=cap))

    def _validate_requirements(self):
        """validate the requirements of substitution mappings."""

        # The requirements must be in node template wchich be mapped.
        tpls_requirements = self.sub_mapping_def.get(self.REQUIREMENTS)
        node_requirements = self.sub_mapped_node_template.requirements \
            if self.sub_mapped_node_template else None
        for req in node_requirements if node_requirements else []:
            if (tpls_requirements and
                    req not in list(tpls_requirements.keys())):
                pass
                # ValidationIssueCollector.appendException(
                #    UnknownFieldError(what='SubstitutionMappings',
                #                      field=req))

    def _validate_outputs(self):
        """validate the outputs of substitution mappings.

        The outputs defined by the topology template have to match the
        attributes of the node type or the substituted node template,
        and the observable attributes of the substituted node template
        have to be defined as attributes of the node type or outputs in
        the topology template.
        """

        # The outputs defined by the topology template have to match the
        # attributes of the node type according to the specification, but
        # it's reasonable that there are more inputs than the node type
        # has properties, the specification will be amended?
        for output in self.outputs:
            if output.name not in self.node_definition.get_attributes_def():
                ValidationIssueCollector.appendException(
                    UnknownOutputError(
                        where=_('SubstitutionMappings with node_type ')
                        + self.node_type,
                        output_name=output.name))*/		
