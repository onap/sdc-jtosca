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
import org.onap.sdc.toscaparser.api.utils.TOSCAVersionProperty;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PolicyType extends StatefulEntityType {

    private static final String DERIVED_FROM = "derived_from";
    private static final String METADATA = "metadata";
    private static final String PROPERTIES = "properties";
    private static final String VERSION = "version";
    private static final String DESCRIPTION = "description";
    private static final String TARGETS = "targets";
    private static final String TRIGGERS = "triggers";
    private static final String TYPE = "type";

    private static final String[] SECTIONS = {
            DERIVED_FROM, METADATA, PROPERTIES, VERSION, DESCRIPTION, TARGETS, TRIGGERS, TYPE
    };

    private LinkedHashMap<String, Object> customDef;
    private String policyDescription;
    private Object policyVersion;
    private LinkedHashMap<String, Object> properties;
    private LinkedHashMap<String, Object> parentPolicies;
    private LinkedHashMap<String, Object> metaData;
    private ArrayList<String> targetsList;


    public PolicyType(String type, LinkedHashMap<String, Object> customDef) {
        super(type, POLICY_PREFIX, customDef);

        this.type = type;
        this.customDef = customDef;
        validateKeys();

        metaData = null;
        if (defs != null && defs.get(METADATA) != null) {
            metaData = (LinkedHashMap<String, Object>) defs.get(METADATA);
            validateMetadata(metaData);
        }

        properties = null;
        if (defs != null && defs.get(PROPERTIES) != null) {
            properties = (LinkedHashMap<String, Object>) defs.get(PROPERTIES);
        }
        parentPolicies = getParentPolicies();

        policyVersion = null;
        if (defs != null && defs.get(VERSION) != null) {
            policyVersion = (new TOSCAVersionProperty(
                    defs.get(VERSION).toString())).getVersion();
        }

        policyDescription = null;
        if (defs != null && defs.get(DESCRIPTION) != null) {
            policyDescription = (String) defs.get(DESCRIPTION);
        }

        targetsList = null;
        if (defs != null && defs.get(TARGETS) != null) {
            targetsList = (ArrayList<String>) defs.get(TARGETS);
            validateTargets(targetsList, this.customDef);
        }

    }

    private LinkedHashMap<String, Object> getParentPolicies() {
        LinkedHashMap<String, Object> policies = new LinkedHashMap<>();
        String parentPolicy;
        if (getParentType() != null) {
            parentPolicy = getParentType().getType();
        } else {
            parentPolicy = null;
        }
        if (parentPolicy != null) {
            while (parentPolicy != null && !parentPolicy.equals("tosca.policies.Root")) {
                policies.put(parentPolicy, TOSCA_DEF.get(parentPolicy));
                parentPolicy = (String)
                        ((LinkedHashMap<String, Object>) policies.get(parentPolicy)).get("derived_from);");
            }
        }
        return policies;
    }

    public String getType() {
        return type;
    }

    public PolicyType getParentType() {
        // Return a policy statefulentity of this node is derived from
        if (defs == null) {
            return null;
        }
        String policyEntity = derivedFrom(defs);
        if (policyEntity != null) {
            return new PolicyType(policyEntity, customDef);
        }
        return null;
    }

    public Object getPolicy(String name) {
        // Return the definition of a policy field by name
        if (defs != null && defs.get(name) != null) {
            return defs.get(name);
        }
        return null;
    }

    public ArrayList<String> getTargets() {
        // Return targets
        return targetsList;
    }

    public String getDescription() {
        return policyDescription;
    }

    public Object getVersion() {
        return policyVersion;
    }

    private void validateKeys() {
        for (String key : defs.keySet()) {
            boolean bFound = false;
            for (String sect : SECTIONS) {
                if (key.equals(sect)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE125", String.format(
                        "UnknownFieldError: Policy \"%s\" contains unknown field \"%s\"",
                        type, key)));
            }
        }
    }

    private void validateTargets(ArrayList<String> targetsList,
                                 LinkedHashMap<String, Object> customDef) {
        for (String nodetype : targetsList) {
            if (customDef.get(nodetype) == null) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE126", String.format(
                        "InvalidTypeError: \"%s\" defined in targets for policy \"%s\"",
                        nodetype, type)));

            }
        }
    }

    private void validateMetadata(LinkedHashMap<String, Object> metaData) {
        String mtype = (String) metaData.get("type");
        if (mtype != null && !mtype.equals("map") && !mtype.equals("tosca:map")) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE127", String.format(
                    "InvalidTypeError: \"%s\" defined in policy for metadata",
                    mtype)));
        }
        for (String entrySchema : this.metaData.keySet()) {
            Object estob = this.metaData.get(entrySchema);
            if (estob instanceof LinkedHashMap) {
                String est = (String)
                        ((LinkedHashMap<String, Object>) estob).get("type");
                if (!est.equals("string")) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE128", String.format(
                            "InvalidTypeError: \"%s\" defined in policy for metadata \"%s\"",
                            est, entrySchema)));
                }
            }
        }
    }

}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import InvalidTypeError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.elements.statefulentitytype import StatefulEntityType
from toscaparser.utils.validateutils import TOSCAVersionProperty


class PolicyType(StatefulEntityType):

    '''TOSCA built-in policies type.'''
    SECTIONS = (DERIVED_FROM, METADATA, PROPERTIES, VERSION, DESCRIPTION, TARGETS) = \
               ('derived_from', 'metadata', 'properties', 'version',
                'description', 'targets')

    def __init__(self, ptype, custom_def=None):
        super(PolicyType, self).__init__(ptype, self.POLICY_PREFIX,
                                         custom_def)
        self.type = ptype
        self.custom_def = custom_def
        self._validate_keys()

        self.meta_data = None
        if self.METADATA in self.defs:
            self.meta_data = self.defs[self.METADATA]
            self._validate_metadata(self.meta_data)

        self.properties = None
        if self.PROPERTIES in self.defs:
            self.properties = self.defs[self.PROPERTIES]
        self.parent_policies = self._get_parent_policies()

        self.policy_version = None
        if self.VERSION in self.defs:
            self.policy_version = TOSCAVersionProperty(
                self.defs[self.VERSION]).get_version()

        self.policy_description = self.defs[self.DESCRIPTION] \
            if self.DESCRIPTION in self.defs else None

        self.targets_list = None
        if self.TARGETS in self.defs:
            self.targets_list = self.defs[self.TARGETS]
            self._validate_targets(self.targets_list, custom_def)

    def _get_parent_policies(self):
        policies = {}
        parent_policy = self.parent_type.type if self.parent_type else None
        if parent_policy:
            while parent_policy != 'tosca.policies.Root':
                policies[parent_policy] = self.TOSCA_DEF[parent_policy]
                parent_policy = policies[parent_policy]['derived_from']
        return policies

    @property
    def parent_type(self):
        '''Return a policy statefulentity of this node is derived from.'''
        if not hasattr(self, 'defs'):
            return None
        ppolicy_entity = self.derived_from(self.defs)
        if ppolicy_entity:
            return PolicyType(ppolicy_entity, self.custom_def)

    def get_policy(self, name):
        '''Return the definition of a policy field by name.'''
        if name in self.defs:
            return self.defs[name]

    @property
    def targets(self):
        '''Return targets.'''
        return self.targets_list

    @property
    def description(self):
        return self.policy_description

    @property
    def version(self):
        return self.policy_version

    def _validate_keys(self):
        for key in self.defs.keys():
            if key not in self.SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Policy "%s"' % self.type,
                                      field=key))

    def _validate_targets(self, targets_list, custom_def):
        for nodetype in targets_list:
            if nodetype not in custom_def:
                ValidationIssueCollector.appendException(
                    InvalidTypeError(what='"%s" defined in targets for '
                                     'policy "%s"' % (nodetype, self.type)))

    def _validate_metadata(self, meta_data):
        if not meta_data.get('type') in ['map', 'tosca:map']:
            ValidationIssueCollector.appendException(
                InvalidTypeError(what='"%s" defined in policy for '
                                 'metadata' % (meta_data.get('type'))))

        for entry_schema, entry_schema_type in meta_data.items():
            if isinstance(entry_schema_type, dict) and not \
                    entry_schema_type.get('type') == 'string':
                ValidationIssueCollector.appendException(
                    InvalidTypeError(what='"%s" defined in policy for '
                                     'metadata "%s"'
                                     % (entry_schema_type.get('type'),
                                        entry_schema)))
*/
