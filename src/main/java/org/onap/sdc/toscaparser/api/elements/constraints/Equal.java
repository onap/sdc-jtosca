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

import java.util.Arrays;

public class Equal extends Constraint {

    protected void setValues() {

        setConstraintKey(EQUAL);
        validPropTypes.addAll(Arrays.asList(Schema.PROPERTY_TYPES));

    }

    public Equal(String name, String type, Object c) {
        super(name, type, c);

    }

    protected boolean isValid(Object val) {
        // equality of objects is tricky so we're comparing
        // the toString() representation
        return val.toString().equals(constraintValue.toString());
    }

    protected String errMsg(Object value) {
        return String.format("The value \"%s\" of property \"%s\" is not equal to \"%s\"",
                valueMsg, propertyName, constraintValueMsg);
    }

}

/*python

class Equal(Constraint):
"""Constraint class for "equal"

Constrains a property or parameter to a value equal to ('=')
the value declared.
"""

constraint_key = Constraint.EQUAL

valid_prop_types = Schema.PROPERTY_TYPES

def _is_valid(self, value):
    if value == self.constraint_value:
        return True

    return False

def _err_msg(self, value):
    return (_('The value "%(pvalue)s" of property "%(pname)s" is not '
              'equal to "%(cvalue)s".') %
            dict(pname=self.property_name,
                 pvalue=self.value_msg,
                 cvalue=self.constraint_value_msg))
*/
