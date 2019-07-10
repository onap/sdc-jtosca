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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.ValidateUtils;

public class Policy extends EntityTemplate {


    static final String TYPE = "type";
    static final String METADATA = "metadata";
    static final String DESCRIPTION = "description";
    static final String PROPERTIES = "properties";
    static final String TARGETS = "targets";
    private static final String TRIGGERS = "triggers";
    private static final String SECTIONS[] = {
            TYPE, METADATA, DESCRIPTION, PROPERTIES, TARGETS, TRIGGERS};

    Metadata metaDataObject;
    LinkedHashMap<String, Object> metaData = null;
    ArrayList<Object> targetsList; // *** a list of NodeTemplate OR a list of Group ***
    String targetsType;
    ArrayList<Object> triggers;
    LinkedHashMap<String, Object> properties;

    public Policy(String _name,
                  LinkedHashMap<String, Object> _policy,
                  ArrayList<Object> targetObjects,
                  String _targetsType,
                  LinkedHashMap<String, Object> _customDef) {
        this(_name, _policy, targetObjects, _targetsType, _customDef, null);
    }

    public Policy(String _name,
                  LinkedHashMap<String, Object> _policy,
//				  ArrayList<NodeTemplate> targetObjects,
                  ArrayList<Object> targetObjects,
                  String _targetsType,
                  LinkedHashMap<String, Object> _customDef, NodeTemplate parentNodeTemplate) {
        super(_name, _policy, "policy_type", _customDef, parentNodeTemplate);

        if (_policy.get(METADATA) != null) {
            metaData = (LinkedHashMap<String, Object>) _policy.get(METADATA);
            ValidateUtils.validateMap(metaData);
            metaDataObject = new Metadata(metaData);
        }

        targetsList = targetObjects;
        targetsType = _targetsType;
        triggers = _triggers((LinkedHashMap<String, Object>) _policy.get(TRIGGERS));
        properties = null;
        if (_policy.get("properties") != null) {
            properties = (LinkedHashMap<String, Object>) _policy.get("properties");
        }
        _validateKeys();
    }

    public ArrayList<String> getTargets() {
        return (ArrayList<String>) entityTpl.get("targets");
    }

    public ArrayList<String> getDescription() {
        return (ArrayList<String>) entityTpl.get("description");
    }

    public ArrayList<String> getmetadata() {
        return (ArrayList<String>) entityTpl.get("metadata");
    }

    public String getTargetsType() {
        return targetsType;
    }

    public Metadata getMetaDataObj() {
        return metaDataObject;
    }

    public LinkedHashMap<String, Object> getMetaData() {
        return metaData;
    }

    //	public ArrayList<NodeTemplate> getTargetsList() {
    public ArrayList<Object> getTargetsList() {
        return targetsList;
    }

    // entityTemplate already has a different getProperties...
    // this is to access the local properties variable
    public LinkedHashMap<String, Object> getPolicyProperties() {
        return properties;
    }

    private ArrayList<Object> _triggers(LinkedHashMap<String, Object> triggers) {
        ArrayList<Object> triggerObjs = new ArrayList<>();
        if (triggers != null) {
            for (Map.Entry<String, Object> me : triggers.entrySet()) {
                String tname = me.getKey();
                LinkedHashMap<String, Object> ttriggerTpl =
                        (LinkedHashMap<String, Object>) me.getValue();
                Triggers triggersObj = new Triggers(tname, ttriggerTpl);
                triggerObjs.add(triggersObj);
            }
        }
        return triggerObjs;
    }

    private void _validateKeys() {
        for (String key : entityTpl.keySet()) {
            boolean bFound = false;
            for (int i = 0; i < SECTIONS.length; i++) {
                if (key.equals(SECTIONS[i])) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE219", String.format(
                        "UnknownFieldError: Policy \"%s\" contains unknown field \"%s\"",
                        name, key)));
            }
        }
    }

    @Override
    public String toString() {
        return "Policy{" +
                "metaData=" + metaData +
                ", targetsList=" + targetsList +
                ", targetsType='" + targetsType + '\'' +
                ", triggers=" + triggers +
                ", properties=" + properties +
                '}';
    }

    public int compareTo(Policy other) {
        if (this.equals(other))
            return 0;
        return this.getName().compareTo(other.getName()) == 0 ? this.getType().compareTo(other.getType()) : this.getName().compareTo(other.getName());
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.entity_template import EntityTemplate
from toscaparser.triggers import Triggers
from toscaparser.utils import validateutils


SECTIONS = (TYPE, METADATA, DESCRIPTION, PROPERTIES, TARGETS, TRIGGERS) = \
           ('type', 'metadata', 'description',
            'properties', 'targets', 'triggers')

log = logging.getLogger('tosca')


class Policy(EntityTemplate):
    '''Policies defined in Topology template.'''
    def __init__(self, name, policy, targets, targets_type, custom_def=None):
        super(Policy, self).__init__(name,
                                     policy,
                                     'policy_type',
                                     custom_def)
        self.meta_data = None
        if self.METADATA in policy:
            self.meta_data = policy.get(self.METADATA)
            validateutils.validate_map(self.meta_data)
        self.targets_list = targets
        self.targets_type = targets_type
        self.triggers = self._triggers(policy.get(TRIGGERS))
        self._validate_keys()

    @property
    def targets(self):
        return self.entity_tpl.get('targets')

    @property
    def description(self):
        return self.entity_tpl.get('description')

    @property
    def metadata(self):
        return self.entity_tpl.get('metadata')

    def get_targets_type(self):
        return self.targets_type

    def get_targets_list(self):
        return self.targets_list

    def _triggers(self, triggers):
        triggerObjs = []
        if triggers:
            for name, trigger_tpl in triggers.items():
                triggersObj = Triggers(name, trigger_tpl)
                triggerObjs.append(triggersObj)
        return triggerObjs

    def _validate_keys(self):
        for key in self.entity_tpl.keys():
            if key not in SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Policy "%s"' % self.name,
                                      field=key))
*/
