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

package org.onap.sdc.toscaparser.api.elements.constraints;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.Collections;

public class Length extends Constraint {
    // Constraint class for "length"

    // Constrains the property or parameter to a value of a given length.

    @Override
    protected void setValues() {

        setConstraintKey(LENGTH);
        addValidTypes(Collections.singletonList("Integer"));

        validPropTypes.add(Schema.STRING);

    }

    public Length(String name, String type, Object c) {
        super(name, type, c);

        if (!validTypes.contains(constraintValue.getClass().getSimpleName())) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE109", "InvalidSchemaError: The property \"length\" expects an integer"));
        }
    }

    @Override
    protected boolean isValid(Object value) {
        if (value instanceof String && constraintValue instanceof Integer &&
                ((String) value).length() == (Integer) constraintValue) {
            return true;
        }
        return false;
    }

    @Override
    protected String errMsg(Object value) {
        return String.format("Length of value \"%s\" of property \"%s\" must be equal to \"%s\"",
                value.toString(), propertyName, constraintValue.toString());
    }

}

/*python
	class Length(Constraint):
	"""Constraint class for "length"
	
	Constrains the property or parameter to a value of a given length.
	"""
	
	constraint_key = Constraint.LENGTH
	
	valid_types = (int, )
	
	valid_prop_types = (Schema.STRING, )
	
	def __init__(self, property_name, property_type, constraint):
	    super(Length, self).__init__(property_name, property_type, constraint)
	    if not isinstance(self.constraint_value, self.valid_types):
	        ValidationIsshueCollector.appendException(
	            InvalidSchemaError(message=_('The property "length" expects '
	                                         'an integer.')))
	
	def _is_valid(self, value):
	    if isinstance(value, str) and len(value) == self.constraint_value:
	        return True
	
	    return False
	
	def _err_msg(self, value):
	    return (_('Length of value "%(pvalue)s" of property "%(pname)s" '
	              'must be equal to "%(cvalue)s".') %
	            dict(pname=self.property_name,
	                 pvalue=value,
	                 cvalue=self.constraint_value))
*/
