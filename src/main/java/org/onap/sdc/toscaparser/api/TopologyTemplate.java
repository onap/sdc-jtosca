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
import org.onap.sdc.toscaparser.api.elements.InterfacesDef;
import org.onap.sdc.toscaparser.api.elements.NodeType;
import org.onap.sdc.toscaparser.api.elements.RelationshipType;
import org.onap.sdc.toscaparser.api.functions.Function;
import org.onap.sdc.toscaparser.api.functions.GetAttribute;
import org.onap.sdc.toscaparser.api.functions.GetInput;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.toscaparser.api.parameters.Output;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class TopologyTemplate {

    private static final String DESCRIPTION = "description";
    private static final String INPUTS = "inputs";
    private static final String NODE_TEMPLATES = "node_templates";
    private static final String RELATIONSHIP_TEMPLATES = "relationship_templates";
    private static final String OUTPUTS = "outputs";
    private static final String GROUPS = "groups";
    private static final String SUBSTITUTION_MAPPINGS = "substitution_mappings";
    private static final String POLICIES = "policies";
    private static final String METADATA = "metadata";

    private static String[] SECTIONS = {
            DESCRIPTION, INPUTS, NODE_TEMPLATES, RELATIONSHIP_TEMPLATES,
            OUTPUTS, GROUPS, SUBSTITUTION_MAPPINGS, POLICIES, METADATA
    };

    private LinkedHashMap<String, Object> tpl;
    LinkedHashMap<String, Object> metaData;
    private ArrayList<Input> inputs;
    private ArrayList<Output> outputs;
    private ArrayList<RelationshipTemplate> relationshipTemplates;
    private ArrayList<NodeTemplate> nodeTemplates;
    private LinkedHashMap<String, Object> customDefs;
    private LinkedHashMap<String, Object> relTypes;//TYPE
    private NodeTemplate subMappedNodeTemplate;
    private ArrayList<Group> groups;
    private ArrayList<Policy> policies;
    private LinkedHashMap<String, Object> parsedParams = null;//TYPE
    private String description;
    private ToscaGraph graph;
    private SubstitutionMappings substitutionMappings;
    private boolean resolveGetInput;

    public TopologyTemplate(
            LinkedHashMap<String, Object> _template,
            LinkedHashMap<String, Object> _customDefs,
            LinkedHashMap<String, Object> _relTypes,//TYPE
            LinkedHashMap<String, Object> _parsedParams,
            NodeTemplate _subMappedNodeTemplate,
            boolean _resolveGetInput) {

        tpl = _template;
        if (tpl != null) {
            subMappedNodeTemplate = _subMappedNodeTemplate;
            metaData = _metaData();
            customDefs = _customDefs;
            relTypes = _relTypes;
            parsedParams = _parsedParams;
            resolveGetInput = _resolveGetInput;
            _validateField();
            description = _tplDescription();
            inputs = _inputs();
            relationshipTemplates = _relationshipTemplates();
            //todo: pass subMappedNodeTemplate to ET constractor
            nodeTemplates = _nodeTemplates();
            outputs = _outputs();
            if (nodeTemplates != null) {
                graph = new ToscaGraph(nodeTemplates);
            }
            groups = _groups();
            policies = _policies();
            _processIntrinsicFunctions();
            substitutionMappings = _substitutionMappings();
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Input> _inputs() {
        ArrayList<Input> alInputs = new ArrayList<>();
        for (String name : _tplInputs().keySet()) {
            Object attrs = _tplInputs().get(name);
            Input input = new Input(name, (LinkedHashMap<String, Object>) attrs, customDefs);
            if (parsedParams != null && parsedParams.get(name) != null) {
                input.validate(parsedParams.get(name));
            } else {
                Object _default = input.getDefault();
                if (_default != null) {
                    input.validate(_default);
                }
            }
            if ((parsedParams != null && parsedParams.get(input.getName()) == null || parsedParams == null)
                    && input.isRequired() && input.getDefault() == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE003",
                    String.format("MissingRequiredFieldError: The required input \"%s\" was not provided"
                        , input.getName()))
                );
            }
            alInputs.add(input);
        }
        return alInputs;

    }

    private LinkedHashMap<String, Object> _metaData() {
        if (tpl.get(METADATA) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(METADATA);
        } else {
            return new LinkedHashMap<String, Object>();
        }

    }

    private ArrayList<NodeTemplate> _nodeTemplates() {
        ArrayList<NodeTemplate> alNodeTemplates = new ArrayList<>();
        LinkedHashMap<String, Object> tpls = _tplNodeTemplates();
        if (tpls != null) {
            for (String name : tpls.keySet()) {
                NodeTemplate tpl = new NodeTemplate(name,
                        tpls,
                        customDefs,
                        relationshipTemplates,
                        relTypes,
                        subMappedNodeTemplate);
                if (tpl.getTypeDefinition() != null) {
                    boolean b = NodeType.TOSCA_DEF.get(tpl.getType()) != null;
                    if (b || (tpl.getCustomDef() != null && !tpl.getCustomDef().isEmpty())) {
                        tpl.validate();
                        alNodeTemplates.add(tpl);
                    }
                }
            }
        }
        return alNodeTemplates;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<RelationshipTemplate> _relationshipTemplates() {
        ArrayList<RelationshipTemplate> alRelationshipTemplates = new ArrayList<>();
        LinkedHashMap<String, Object> tpls = _tplRelationshipTemplates();
        if (tpls != null) {
            for (String name : tpls.keySet()) {
                RelationshipTemplate tpl = new RelationshipTemplate(
                        (LinkedHashMap<String, Object>) tpls.get(name), name, customDefs, null, null, subMappedNodeTemplate);

                alRelationshipTemplates.add(tpl);
            }
        }
        return alRelationshipTemplates;
    }

    private ArrayList<Output> _outputs() {
        ArrayList<Output> alOutputs = new ArrayList<>();
        for (Map.Entry<String, Object> me : _tplOutputs().entrySet()) {
            String oname = me.getKey();
            LinkedHashMap<String, Object> oattrs = (LinkedHashMap<String, Object>) me.getValue();
            Output o = new Output(oname, oattrs);
            o.validate();
            alOutputs.add(o);
        }
        return alOutputs;
    }

    private SubstitutionMappings _substitutionMappings() {
        LinkedHashMap<String, Object> tplSubstitutionMapping = (LinkedHashMap<String, Object>) _tplSubstitutionMappings();

        //*** the commenting-out below and the weaker condition are in the Python source
        // #if tpl_substitution_mapping and self.sub_mapped_node_template:
        if (tplSubstitutionMapping != null && tplSubstitutionMapping.size() > 0) {
            return new SubstitutionMappings(tplSubstitutionMapping,
                    nodeTemplates,
                    inputs,
                    outputs,
                    groups,
                    subMappedNodeTemplate,
                    customDefs);
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    private ArrayList<Policy> _policies() {
        ArrayList<Policy> alPolicies = new ArrayList<>();
        for (Map.Entry<String, Object> me : _tplPolicies().entrySet()) {
            String policyName = me.getKey();
            LinkedHashMap<String, Object> policyTpl = (LinkedHashMap<String, Object>) me.getValue();
            ArrayList<String> targetList = (ArrayList<String>) policyTpl.get("targets");
            ArrayList<NodeTemplate> targetNodes = new ArrayList<>();
            ArrayList<Object> targetObjects = new ArrayList<>();
            ArrayList<Group> targetGroups = new ArrayList<>();
            String targetsType = "groups";
            if (targetList != null && targetList.size() >= 1) {
                targetGroups = _getPolicyGroups(targetList);
                if (targetGroups == null || targetGroups.isEmpty()) {
                    targetsType = "node_templates";
                    targetNodes = _getGroupMembers(targetList);
                    for (NodeTemplate nt : targetNodes) {
                        targetObjects.add(nt);
                    }
                } else {
                    for (Group gr : targetGroups) {
                        targetObjects.add(gr);
                    }
                }
            }
            Policy policyObj = new Policy(policyName,
                    policyTpl,
                    targetObjects,
                    targetsType,
                    customDefs,
                    subMappedNodeTemplate);
            alPolicies.add(policyObj);
        }
        return alPolicies;
    }

    private ArrayList<Group> _groups() {
        ArrayList<Group> groups = new ArrayList<>();
        ArrayList<NodeTemplate> memberNodes = null;
        for (Map.Entry<String, Object> me : _tplGroups().entrySet()) {
            String groupName = me.getKey();
            LinkedHashMap<String, Object> groupTpl = (LinkedHashMap<String, Object>) me.getValue();
            ArrayList<String> memberNames = (ArrayList<String>) groupTpl.get("members");
            if (memberNames != null) {
                DataEntity.validateDatatype("list", memberNames, null, null, null);
                if (memberNames.size() < 1 ||
                        (new HashSet<String>(memberNames)).size() != memberNames.size()) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE005", String.format(
                            "InvalidGroupTargetException: Member nodes \"%s\" should be >= 1 and not repeated",
                            memberNames.toString())));
                } else {
                    memberNodes = _getGroupMembers(memberNames);
                }
            }
            Group group = new Group(groupName,
                    groupTpl,
                    memberNodes,
                    customDefs, subMappedNodeTemplate);
            groups.add(group);
        }
        return groups;
    }

    private ArrayList<NodeTemplate> _getGroupMembers(ArrayList<String> memberNames) {
        ArrayList<NodeTemplate> memberNodes = new ArrayList<>();
        _validateGroupMembers(memberNames);
        for (String member : memberNames) {
            for (NodeTemplate node : nodeTemplates) {
                if (member.equals(node.getName())) {
                    memberNodes.add(node);
                }
            }
        }
        return memberNodes;
    }

    private ArrayList<Group> _getPolicyGroups(ArrayList<String> memberNames) {
        ArrayList<Group> memberGroups = new ArrayList<>();
        for (String member : memberNames) {
            for (Group group : groups) {
                if (member.equals(group.getName())) {
                    memberGroups.add(group);
                }
            }
        }
        return memberGroups;
    }

    private void _validateGroupMembers(ArrayList<String> members) {
        ArrayList<String> nodeNames = new ArrayList<>();
        for (NodeTemplate node : nodeTemplates) {
            nodeNames.add(node.getName());
        }
        for (String member : members) {
            if (!nodeNames.contains(member)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE239", String.format(
                        "InvalidGroupTargetException: Target member \"%s\" is not found in \"nodeTemplates\"", member)));
            }
        }
    }

    // topology template can act like node template
    // it is exposed by substitution_mappings.

    public String nodetype() {
        return substitutionMappings.getNodeType();
    }

    public LinkedHashMap<String, Object> capabilities() {
        return substitutionMappings.getCapabilities();
    }

    public LinkedHashMap<String, Object> requirements() {
        return substitutionMappings.getRequirements();
    }

    private String _tplDescription() {
        return (String) tpl.get(DESCRIPTION);
        //if description:
        //	return description.rstrip()
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplInputs() {
        if (tpl.get(INPUTS) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(INPUTS);
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplNodeTemplates() {
        return (LinkedHashMap<String, Object>) tpl.get(NODE_TEMPLATES);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplRelationshipTemplates() {
        if (tpl.get(RELATIONSHIP_TEMPLATES) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(RELATIONSHIP_TEMPLATES);
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplOutputs() {
        if (tpl.get(OUTPUTS) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(OUTPUTS);
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplSubstitutionMappings() {
        if (tpl.get(SUBSTITUTION_MAPPINGS) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(SUBSTITUTION_MAPPINGS);
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplGroups() {
        if (tpl.get(GROUPS) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(GROUPS);
        }
        return new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> _tplPolicies() {
        if (tpl.get(POLICIES) != null) {
            return (LinkedHashMap<String, Object>) tpl.get(POLICIES);
        }
        return new LinkedHashMap<>();
    }

    private void _validateField() {
        for (String name : tpl.keySet()) {
            boolean bFound = false;
            for (String section : SECTIONS) {
                if (name.equals(section)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE240", String.format(
                        "UnknownFieldError: TopologyTemplate contains unknown field \"%s\"", name)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void _processIntrinsicFunctions() {
        // Process intrinsic functions

        // Current implementation processes functions within node template
        // properties, requirements, interfaces inputs and template outputs.

        if (nodeTemplates != null) {
            for (NodeTemplate nt : nodeTemplates) {
                for (Property prop : nt.getPropertiesObjects()) {
                    prop.setValue(Function.getFunction(this, nt, prop.getValue(), resolveGetInput));
                }
                for (InterfacesDef ifd : nt.getInterfaces()) {
                    LinkedHashMap<String, Object> ifin = ifd.getInputs();
                    if (ifin != null) {
                        for (Map.Entry<String, Object> me : ifin.entrySet()) {
                            String name = me.getKey();
                            Object value = Function.getFunction(this, nt, me.getValue(), resolveGetInput);
                            ifd.setInput(name, value);
                        }
                    }
                }
                if (nt.getRequirements() != null) {
                    for (RequirementAssignment req : nt.getRequirements().getAll()) {
                        LinkedHashMap<String, Object> rel;
                        Object t = req.getRelationship();
                        // it can be a string or a LHM...
                        if (t instanceof LinkedHashMap) {
                            rel = (LinkedHashMap<String, Object>) t;
                        } else {
                            // we set it to null to fail the next test
                            // and avoid the get("proprties")
                            rel = null;
                        }

                        if (rel != null && rel.get("properties") != null) {
                            LinkedHashMap<String, Object> relprops =
                                    (LinkedHashMap<String, Object>) rel.get("properties");
                            for (String key : relprops.keySet()) {
                                Object value = relprops.get(key);
                                Object func = Function.getFunction(this, req, value, resolveGetInput);
                                relprops.put(key, func);
                            }
                        }
                    }
                }
                if (nt.getCapabilitiesObjects() != null) {
                    for (CapabilityAssignment cap : nt.getCapabilitiesObjects()) {
                        if (cap.getPropertiesObjects() != null) {
                            for (Property prop : cap.getPropertiesObjects()) {
                                Object propvalue = Function.getFunction(this, nt, prop.getValue(), resolveGetInput);
                                if (propvalue instanceof GetInput) {
                                    propvalue = ((GetInput) propvalue).result();
                                    for (String p : cap.getProperties().keySet()) {
                                        //Object v = cap.getProperties().get(p);
                                        if (p.equals(prop.getName())) {
                                            cap.setProperty(p, propvalue);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (RelationshipType rel : nt.getRelationships().keySet()) {
                    NodeTemplate node = nt.getRelationships().get(rel);
                    ArrayList<RelationshipTemplate> relTpls = node.getRelationshipTemplate();
                    if (relTpls != null) {
                        for (RelationshipTemplate relTpl : relTpls) {
                            // TT 5
                            for (InterfacesDef iface : relTpl.getInterfaces()) {
                                if (iface.getInputs() != null) {
                                    for (String name : iface.getInputs().keySet()) {
                                        Object value = iface.getInputs().get(name);
                                        Object func = Function.getFunction(
                                                this,
                                                relTpl,
                                                value,
                                                resolveGetInput);
                                        iface.setInput(name, func);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Output output : outputs) {
            Object func = Function.getFunction(this, outputs, output.getValue(), resolveGetInput);
            if (func instanceof GetAttribute) {
                output.setAttr(Output.VALUE, func);
            }
        }
    }

    public static String getSubMappingNodeType(LinkedHashMap<String, Object> topologyTpl) {
        if (topologyTpl != null && topologyTpl instanceof LinkedHashMap) {
            Object submapTpl = topologyTpl.get(SUBSTITUTION_MAPPINGS);
            return SubstitutionMappings.stGetNodeType((LinkedHashMap<String, Object>) submapTpl);
        }
        return null;
    }

    // getters

    public LinkedHashMap<String, Object> getTpl() {
        return tpl;
    }

    public LinkedHashMap<String, Object> getMetadata() {
        return metaData;
    }

    public ArrayList<Input> getInputs() {
        return inputs;
    }

    public ArrayList<Output> getOutputs() {
        return outputs;
    }

    public ArrayList<Policy> getPolicies() {
        return policies;
    }

    public ArrayList<RelationshipTemplate> getRelationshipTemplates() {
        return relationshipTemplates;
    }

    public ArrayList<NodeTemplate> getNodeTemplates() {
        return nodeTemplates;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public SubstitutionMappings getSubstitutionMappings() {
        return substitutionMappings;
    }

    public LinkedHashMap<String, Object> getParsedParams() {
        return parsedParams;
    }

    public boolean getResolveGetInput() {
        return resolveGetInput;
    }

    public LinkedHashMap<String, Object> getCustomDefs() {
        return customDefs;
    }
}

/*python

#    Licensed under the Apache License, Version 2.0 (the "License"); you may
#    not use this file except in compliance with the License. You may obtain
#    a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#    License for the specific language governing permissions and limitations
#    under the License.


import logging

from toscaparser.common import exception
from toscaparser.dataentity import DataEntity
from toscaparser import functions
from toscaparser.groups import Group
from toscaparser.nodetemplate import NodeTemplate
from toscaparser.parameters import Input
from toscaparser.parameters import Output
from toscaparser.policy import Policy
from toscaparser.relationship_template import RelationshipTemplate
from toscaparser.substitution_mappings import SubstitutionMappings
from toscaparser.tpl_relationship_graph import ToscaGraph
from toscaparser.utils.gettextutils import _


# Topology template key names
SECTIONS = (DESCRIPTION, INPUTS, NODE_TEMPLATES,
            RELATIONSHIP_TEMPLATES, OUTPUTS, GROUPS,
            SUBSTITUION_MAPPINGS, POLICIES) = \
           ('description', 'inputs', 'node_templates',
            'relationship_templates', 'outputs', 'groups',
            'substitution_mappings', 'policies')

log = logging.getLogger("tosca.model")


class TopologyTemplate(object):

    '''Load the template data.'''
    def __init__(self, template, custom_defs,
                 rel_types=None, parsed_params=None,
                 sub_mapped_node_template=None):
        self.tpl = template
        self.sub_mapped_node_template = sub_mapped_node_template
        if self.tpl:
            self.custom_defs = custom_defs
            self.rel_types = rel_types
            self.parsed_params = parsed_params
            self._validate_field()
            self.description = self._tpl_description()
            self.inputs = self._inputs()
            self.relationship_templates = self._relationship_templates()
            self.nodetemplates = self._nodetemplates()
            self.outputs = self._outputs()
            if hasattr(self, 'nodetemplates'):
                self.graph = ToscaGraph(self.nodetemplates)
            self.groups = self._groups()
            self.policies = self._policies()
            self._process_intrinsic_functions()
            self.substitution_mappings = self._substitution_mappings()

    def _inputs(self):
        inputs = []
        for name, attrs in self._tpl_inputs().items():
            input = Input(name, attrs)
            if self.parsed_params and name in self.parsed_params:
                input.validate(self.parsed_params[name])
            else:
                default = input.default
                if default:
                    input.validate(default)
            if (self.parsed_params and input.name not in self.parsed_params
                or self.parsed_params is None) and input.required \
                    and input.default is None:
                log.warning(_('The required parameter %s '
                              'is not provided') % input.name)

            inputs.append(input)
        return inputs

    def _nodetemplates(self):
        nodetemplates = []
        tpls = self._tpl_nodetemplates()
        if tpls:
            for name in tpls:
                tpl = NodeTemplate(name, tpls, self.custom_defs,
                                   self.relationship_templates,
                                   self.rel_types)
                if (tpl.type_definition and
                    (tpl.type in tpl.type_definition.TOSCA_DEF or
                     (tpl.type not in tpl.type_definition.TOSCA_DEF and
                      bool(tpl.custom_def)))):
                    tpl.validate(self)
                    nodetemplates.append(tpl)
        return nodetemplates

    def _relationship_templates(self):
        rel_templates = []
        tpls = self._tpl_relationship_templates()
        for name in tpls:
            tpl = RelationshipTemplate(tpls[name], name, self.custom_defs)
            rel_templates.append(tpl)
        return rel_templates

    def _outputs(self):
        outputs = []
        for name, attrs in self._tpl_outputs().items():
            output = Output(name, attrs)
            output.validate()
            outputs.append(output)
        return outputs

    def _substitution_mappings(self):
        tpl_substitution_mapping = self._tpl_substitution_mappings()
        # if tpl_substitution_mapping and self.sub_mapped_node_template:
        if tpl_substitution_mapping:
            return SubstitutionMappings(tpl_substitution_mapping,
                                        self.nodetemplates,
                                        self.inputs,
                                        self.outputs,
                                        self.sub_mapped_node_template,
                                        self.custom_defs)

    def _policies(self):
        policies = []
        for policy in self._tpl_policies():
            for policy_name, policy_tpl in policy.items():
                target_list = policy_tpl.get('targets')
                if target_list and len(target_list) >= 1:
                    target_objects = []
                    targets_type = "groups"
                    target_objects = self._get_policy_groups(target_list)
                    if not target_objects:
                        targets_type = "node_templates"
                        target_objects = self._get_group_members(target_list)
                    policyObj = Policy(policy_name, policy_tpl,
                                       target_objects, targets_type,
                                       self.custom_defs)
                    policies.append(policyObj)
        return policies

    def _groups(self):
        groups = []
        member_nodes = None
        for group_name, group_tpl in self._tpl_groups().items():
            member_names = group_tpl.get('members')
            if member_names is not None:
                DataEntity.validate_datatype('list', member_names)
                if len(member_names) < 1 or \
                        len(member_names) != len(set(member_names)):
                    exception.ValidationIssueCollector.appendException(
                        exception.InvalidGroupTargetException(
                            message=_('Member nodes "%s" should be >= 1 '
                                      'and not repeated') % member_names))
                else:
                    member_nodes = self._get_group_members(member_names)
            group = Group(group_name, group_tpl,
                          member_nodes,
                          self.custom_defs)
            groups.append(group)
        return groups

    def _get_group_members(self, member_names):
        member_nodes = []
        self._validate_group_members(member_names)
        for member in member_names:
            for node in self.nodetemplates:
                if node.name == member:
                    member_nodes.append(node)
        return member_nodes

    def _get_policy_groups(self, member_names):
        member_groups = []
        for member in member_names:
            for group in self.groups:
                if group.name == member:
                    member_groups.append(group)
        return member_groups

    def _validate_group_members(self, members):
        node_names = []
        for node in self.nodetemplates:
            node_names.append(node.name)
        for member in members:
            if member not in node_names:
                exception.ValidationIssueCollector.appendException(
                    exception.InvalidGroupTargetException(
                        message=_('Target member "%s" is not found in '
                                  'node_templates') % member))

    # topology template can act like node template
    # it is exposed by substitution_mappings.
    def nodetype(self):
        return self.substitution_mappings.node_type \
            if self.substitution_mappings else None

    def capabilities(self):
        return self.substitution_mappings.capabilities \
            if self.substitution_mappings else None

    def requirements(self):
        return self.substitution_mappings.requirements \
            if self.substitution_mappings else None

    def _tpl_description(self):
        description = self.tpl.get(DESCRIPTION)
        if description:
            return description.rstrip()

    def _tpl_inputs(self):
        return self.tpl.get(INPUTS) or {}

    def _tpl_nodetemplates(self):
        return self.tpl.get(NODE_TEMPLATES)

    def _tpl_relationship_templates(self):
        return self.tpl.get(RELATIONSHIP_TEMPLATES) or {}

    def _tpl_outputs(self):
        return self.tpl.get(OUTPUTS) or {}

    def _tpl_substitution_mappings(self):
        return self.tpl.get(SUBSTITUION_MAPPINGS) or {}

    def _tpl_groups(self):
        return self.tpl.get(GROUPS) or {}

    def _tpl_policies(self):
        return self.tpl.get(POLICIES) or {}

    def _validate_field(self):
        for name in self.tpl:
            if name not in SECTIONS:
                exception.ValidationIssueCollector.appendException(
                    exception.UnknownFieldError(what='Template', field=name))

    def _process_intrinsic_functions(self):
        """Process intrinsic functions

        Current implementation processes functions within node template
        properties, requirements, interfaces inputs and template outputs.
        """
        if hasattr(self, 'nodetemplates'):
            for node_template in self.nodetemplates:
                for prop in node_template.get_properties_objects():
                    prop.value = functions.get_function(self,
                                                        node_template,
                                                        prop.value)
                for interface in node_template.interfaces:
                    if interface.inputs:
                        for name, value in interface.inputs.items():
                            interface.inputs[name] = functions.get_function(
                                self,
                                node_template,
                                value)
                if node_template.requirements and \
                   isinstance(node_template.requirements, list):
                    for req in node_template.requirements:
                        rel = req
                        for req_name, req_item in req.items():
                            if isinstance(req_item, dict):
                                rel = req_item.get('relationship')
                                break
                        if rel and 'properties' in rel:
                            for key, value in rel['properties'].items():
                                rel['properties'][key] = \
                                    functions.get_function(self,
                                                           req,
                                                           value)
                if node_template.get_capabilities_objects():
                    for cap in node_template.get_capabilities_objects():
                        if cap.get_properties_objects():
                            for prop in cap.get_properties_objects():
                                propvalue = functions.get_function(
                                    self,
                                    node_template,
                                    prop.value)
                                if isinstance(propvalue, functions.GetInput):
                                    propvalue = propvalue.result()
                                    for p, v in cap._properties.items():
                                        if p == prop.name:
                                            cap._properties[p] = propvalue
                for rel, node in node_template.relationships.items():
                    rel_tpls = node.relationship_tpl
                    if rel_tpls:
                        for rel_tpl in rel_tpls:
                            for interface in rel_tpl.interfaces:
                                if interface.inputs:
                                    for name, value in \
                                            interface.inputs.items():
                                        interface.inputs[name] = \
                                            functions.get_function(self,
                                                                   rel_tpl,
                                                                   value)
        for output in self.outputs:
            func = functions.get_function(self, self.outputs, output.value)
            if isinstance(func, functions.GetAttribute):
                output.attrs[output.VALUE] = func

    @classmethod
    def get_sub_mapping_node_type(cls, topology_tpl):
        if topology_tpl and isinstance(topology_tpl, dict):
            submap_tpl = topology_tpl.get(SUBSTITUION_MAPPINGS)
            return SubstitutionMappings.get_node_type(submap_tpl)
*/
