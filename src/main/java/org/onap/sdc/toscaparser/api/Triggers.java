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
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.ValidateUtils;

import java.util.LinkedHashMap;

public class Triggers extends EntityTemplate {

    private static final String DESCRIPTION = "description";
    private static final String EVENT = "event_type";
    private static final String SCHEDULE = "schedule";
    private static final String TARGET_FILTER = "target_filter";
    private static final String CONDITION = "condition";
    private static final String ACTION = "action";

    private static final String[] SECTIONS = {
            DESCRIPTION, EVENT, SCHEDULE, TARGET_FILTER, CONDITION, ACTION
    };

    private static final String METER_NAME = "meter_name";
    private static final String CONSTRAINT = "constraint";
    private static final String PERIOD = "period";
    private static final String EVALUATIONS = "evaluations";
    private static final String METHOD = "method";
    private static final String THRESHOLD = "threshold";
    private static final String COMPARISON_OPERATOR = "comparison_operator";

    private static final String[] CONDITION_KEYNAMES = {
            METER_NAME, CONSTRAINT, PERIOD, EVALUATIONS, METHOD, THRESHOLD, COMPARISON_OPERATOR
    };

    private String name;
    private LinkedHashMap<String, Object> triggerTpl;

    public Triggers(String name, LinkedHashMap<String, Object> triggerTpl) {
        super(); // dummy. don't want super
        this.name = name;
        this.triggerTpl = triggerTpl;
        validateKeys();
        validateCondition();
        validateInput();
    }

    public String getDescription() {
        return (String) triggerTpl.get("description");
    }

    public String getEvent() {
        return (String) triggerTpl.get("event_type");
    }

    public LinkedHashMap<String, Object> getSchedule() {
        return (LinkedHashMap<String, Object>) triggerTpl.get("schedule");
    }

    public LinkedHashMap<String, Object> getTargetFilter() {
        return (LinkedHashMap<String, Object>) triggerTpl.get("target_filter");
    }

    public LinkedHashMap<String, Object> getCondition() {
        return (LinkedHashMap<String, Object>) triggerTpl.get("condition");
    }

    public LinkedHashMap<String, Object> getAction() {
        return (LinkedHashMap<String, Object>) triggerTpl.get("action");
    }

    private void validateKeys() {
        for (String key : triggerTpl.keySet()) {
            boolean bFound = false;
            for (int i = 0; i < SECTIONS.length; i++) {
                if (key.equals(SECTIONS[i])) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE249", String.format(
                        "UnknownFieldError: Triggers \"%s\" contains unknown field \"%s\"",
                        name, key)));
            }
        }
    }

    private void validateCondition() {
        for (String key : getCondition().keySet()) {
            boolean bFound = false;
            for (int i = 0; i < CONDITION_KEYNAMES.length; i++) {
                if (key.equals(CONDITION_KEYNAMES[i])) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE250", String.format(
                        "UnknownFieldError: Triggers \"%s\" contains unknown field \"%s\"",
                        name, key)));
            }
        }
    }

    private void validateInput() {
        for (String key : getCondition().keySet()) {
            Object value = getCondition().get(key);
            if (key.equals(PERIOD) || key.equals(EVALUATIONS)) {
                ValidateUtils.validateInteger(value);
            } else if (key.equals(THRESHOLD)) {
                ValidateUtils.validateNumeric(value);
            } else if (key.equals(METER_NAME) || key.equals(METHOD)) {
                ValidateUtils.validateString(value);
            }
        }
    }

    @Override
    public String toString() {
        return "Triggers{"
                + "name='" + name + '\''
                + ", triggerTpl=" + triggerTpl
                + '}';
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.entity_template import EntityTemplate

SECTIONS = (DESCRIPTION, EVENT, SCHEDULE, TARGET_FILTER, CONDITION, ACTION) = \
           ('description', 'event_type', 'schedule',
            'target_filter', 'condition', 'action')
CONDITION_KEYNAMES = (CONTRAINT, PERIOD, EVALUATIONS, METHOD) = \
                     ('constraint', 'period', 'evaluations', 'method')
log = logging.getLogger('tosca')


class Triggers(EntityTemplate):

    '''Triggers defined in policies of topology template'''

    def __init__(self, name, trigger_tpl):
        self.name = name
        self.trigger_tpl = trigger_tpl
        self._validate_keys()
        self._validate_condition()

    def get_description(self):
        return self.trigger_tpl['description']

    def get_event(self):
        return self.trigger_tpl['event_type']

    def get_schedule(self):
        return self.trigger_tpl['schedule']

    def get_target_filter(self):
        return self.trigger_tpl['target_filter']

    def get_condition(self):
        return self.trigger_tpl['condition']

    def get_action(self):
        return self.trigger_tpl['action']

    def _validate_keys(self):
        for key in self.trigger_tpl.keys():
            if key not in SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Triggers "%s"' % self.name,
                                      field=key))

    def _validate_condition(self):
        for key in self.get_condition():
            if key not in CONDITION_KEYNAMES:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Triggers "%s"' % self.name,
                                      field=key))
*/
