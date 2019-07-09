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

import java.util.LinkedHashMap;

import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class Output {
	
	private static final String DESCRIPTION = "description";
	public static final String VALUE = "value";
	private static final String OUTPUTFIELD[] = {DESCRIPTION, VALUE};
	
	private String name;
	private LinkedHashMap<String,Object> attrs;//TYPE???
	
	public Output(String oname,LinkedHashMap<String,Object> oattrs) {
		name = oname;
		attrs = oattrs;
	}
	
	public String getDescription() {
		return (String)attrs.get(DESCRIPTION);
	}

	public Object getValue() {
		return attrs.get(VALUE);
	}
	
	public void validate() {
		_validateField();
	}
	
	private void _validateField() {
		if(!(attrs instanceof LinkedHashMap)) {
			//TODO wrong error message...
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE216", String.format(
                    "ValidationError: Output \"%s\" has wrong type. Expecting a dict",
                    name))); 
		}
		
		if(getValue() == null) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE217", String.format(
                    "MissingRequiredFieldError: Output \"%s\" is missing required \"%s\"",
                    name,VALUE))); 
		}
        for(String key: attrs.keySet()) {
    		boolean bFound = false;
    		for(String of: OUTPUTFIELD) {
    			if(key.equals(of)) {
    				bFound = true;
    				break;
    			}
    		}
    		if(!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE218", String.format(
                    "UnknownFieldError: Output \"%s\" contains unknown field \"%s\"",
                    name,key))); 
            }
        }
	}
	
	// getter/setter
	
	public String getName() {
		return name;
	}
	
	public void setAttr(String name,Object value) {
		attrs.put(name, value);
	}
}

/*python

class Output(object):

    OUTPUTFIELD = (DESCRIPTION, VALUE) = ('description', 'value')

    def __init__(self, name, attrs):
        self.name = name
        self.attrs = attrs

    @property
    def description(self):
        return self.attrs.get(self.DESCRIPTION)

    @property
    def value(self):
        return self.attrs.get(self.VALUE)

    def validate(self):
        self._validate_field()

    def _validate_field(self):
        if not isinstance(self.attrs, dict):
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(what='Output "%s"' % self.name,
                                          required=self.VALUE))
        if self.value is None:
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(what='Output "%s"' % self.name,
                                          required=self.VALUE))
        for name in self.attrs:
            if name not in self.OUTPUTFIELD:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Output "%s"' % self.name,
                                      field=name))
*/
