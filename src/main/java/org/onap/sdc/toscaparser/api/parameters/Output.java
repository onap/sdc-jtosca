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

package org.onap.sdc.toscaparser.api.parameters;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.LinkedHashMap;

public class Output {

    private static final String DESCRIPTION = "description";
    public static final String VALUE = "value";
    private static final String[] OUTPUT_FIELD = {DESCRIPTION, VALUE};

    private String name;
    private LinkedHashMap<String, Object> attributes;

    public Output(String name, LinkedHashMap<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getDescription() {
        return (String) attributes.get(DESCRIPTION);
    }

    public Object getValue() {
        return attributes.get(VALUE);
    }

    public void validate() {
        validateField();
    }

    private void validateField() {
        if (attributes == null) {
            //TODO wrong error message...
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE216", String.format(
                    "ValidationError: Output \"%s\" has wrong type. Expecting a dict",
                    name)));
        }

        if (getValue() == null) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE217", String.format(
                    "MissingRequiredFieldError: Output \"%s\" is missing required \"%s\"",
                    name, VALUE)));
        }
        for (String key : attributes.keySet()) {
            boolean bFound = false;
            for (String of : OUTPUT_FIELD) {
                if (key.equals(of)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE218", String.format(
                        "UnknownFieldError: Output \"%s\" contains unknown field \"%s\"",
                        name, key)));
            }
        }
    }

    // getter/setter

    public String getName() {
        return name;
    }

    public void setAttr(String name, Object value) {
        attributes.put(name, value);
    }
}

/*python

class Output(object):

    OUTPUT_FIELD = (DESCRIPTION, VALUE) = ('description', 'value')

    def __init__(self, name, attributes):
        self.name = name
        self.attributes = attributes

    @property
    def description(self):
        return self.attributes.get(self.DESCRIPTION)

    @property
    def value(self):
        return self.attributes.get(self.VALUE)

    def validate(self):
        self._validate_field()

    def _validate_field(self):
        if not isinstance(self.attributes, dict):
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(what='Output "%s"' % self.name,
                                          required=self.VALUE))
        if self.value is None:
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(what='Output "%s"' % self.name,
                                          required=self.VALUE))
        for name in self.attributes:
            if name not in self.OUTPUT_FIELD:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Output "%s"' % self.name,
                                      field=name))
*/
