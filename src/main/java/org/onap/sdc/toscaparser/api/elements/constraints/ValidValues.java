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

import java.util.ArrayList;
import java.util.Collections;

public class ValidValues extends Constraint {


    protected void setValues() {
        setConstraintKey(VALID_VALUES);
        Collections.addAll(validPropTypes, Schema.PROPERTY_TYPES);
    }


    public ValidValues(String name, String type, Object c) {
        super(name, type, c);
    }

    @SuppressWarnings("unchecked")
    protected boolean isValid(Object val) {
        if (!(constraintValue instanceof ArrayList)) {
            return false;
        }
        if (val instanceof ArrayList) {
            boolean bAll = true;
            for (Object v : (ArrayList<Object>) val) {
                if (!((ArrayList<Object>) constraintValue).contains(v)) {
                    bAll = false;
                    break;
                }
            }
            return bAll;
        }
        return ((ArrayList<Object>) constraintValue).contains(val);
    }

    protected String errMsg(Object value) {
        return String.format("The value \"%s\" of property \"%s\" is not valid. Expected a value from \"%s\"",
                value.toString(), propertyName, constraintValue.toString());
    }

}

/*python

class ValidValues(Constraint):
"""Constraint class for "valid_values"

Constrains a property or parameter to a value that is in the list of
declared values.
"""
constraint_key = Constraint.VALID_VALUES

valid_prop_types = Schema.PROPERTY_TYPES

def __init__(self, property_name, property_type, constraint):
    super(ValidValues, self).__init__(property_name, property_type,
                                      constraint)
    if not isinstance(self.constraint_value, collections.Sequence):
        ValidationIsshueCollector.appendException(
            InvalidSchemaError(message=_('The property "valid_values" '
                                         'expects a list.')))

def _is_valid(self, value):
    print '*** payton parser validating ',value,' in ',self.constraint_value#GGG
    if isinstance(value, list):
        return all(v in self.constraint_value for v in value)
    return value in self.constraint_value

def _err_msg(self, value):
    allowed = '[%s]' % ', '.join(str(a) for a in self.constraint_value)
    return (_('The value "%(pvalue)s" of property "%(pname)s" is not '
              'valid. Expected a value from "%(cvalue)s".') %
            dict(pname=self.property_name,
                 pvalue=value,
                 cvalue=allowed))


*/
