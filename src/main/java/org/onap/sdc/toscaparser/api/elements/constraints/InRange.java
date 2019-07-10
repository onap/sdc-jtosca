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

import java.util.Arrays;
import java.util.Date;

import java.util.ArrayList;

public class InRange extends Constraint {
    // Constraint class for "in_range"

    //Constrains a property or parameter to a value in range of (inclusive)
    //the two values declared.

    private static final String UNBOUNDED = "UNBOUNDED";

    private Object min, max;

    protected void setValues() {

        setConstraintKey(IN_RANGE);

        // timestamps are loaded as Date objects
        addValidTypes(Arrays.asList("Integer", "Double", "Float", "String", "Date"));
        //validTypes.add("datetime.date");
        //validTypes.add("datetime.time");
        //validTypes.add("datetime.datetime");

        validPropTypes.add(Schema.INTEGER);
        validPropTypes.add(Schema.FLOAT);
        validPropTypes.add(Schema.TIMESTAMP);
        validPropTypes.add(Schema.SCALAR_UNIT_SIZE);
        validPropTypes.add(Schema.SCALAR_UNIT_FREQUENCY);
        validPropTypes.add(Schema.SCALAR_UNIT_TIME);
        validPropTypes.add(Schema.RANGE);

    }

    @SuppressWarnings("unchecked")
    public InRange(String name, String type, Object c) {
        super(name, type, c);

        if (!(constraintValue instanceof ArrayList) || ((ArrayList<Object>) constraintValue).size() != 2) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE106", "InvalidSchemaError: The property \"in_range\" expects a list"));

        }

        ArrayList<Object> alcv = (ArrayList<Object>) constraintValue;
        String msg = "The property \"in_range\" expects comparable values";
        for (Object vo : alcv) {
            if (!validTypes.contains(vo.getClass().getSimpleName())) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE107", "InvalidSchemaError: " + msg));
            }
            // The only string we allow for range is the special value 'UNBOUNDED'
            if ((vo instanceof String) && !((String) vo).equals(UNBOUNDED)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE108", "InvalidSchemaError: " + msg));
            }
        }
        min = alcv.get(0);
        max = alcv.get(1);

    }

    @Override
    protected boolean isValid(Object value) {

        // timestamps
        if (value instanceof Date) {
            if (min instanceof Date && max instanceof Date) {
                return !((Date) value).before((Date) min)
                        && !((Date) value).after((Date) max);
            }
            return false;
        }

        Double dvalue = new Double(value.toString());
        if (!(min instanceof String)) {
            if (dvalue < new Double(min.toString())) {
                return false;
            }
        } else if (!((String) min).equals(UNBOUNDED)) {
            return false;
        }
        if (!(max instanceof String)) {
            if (dvalue > new Double(max.toString())) {
                return false;
            }
        } else if (!((String) max).equals(UNBOUNDED)) {
            return false;
        }
        return true;
    }

    @Override
    protected String errMsg(Object value) {
        return String.format("The value \"%s\" of property \"%s\" is out of range \"(min:%s, max:%s)\"",
                valueMsg, propertyName, min.toString(), max.toString());
    }

}

/*python

class InRange(Constraint):
    """Constraint class for "in_range"

    Constrains a property or parameter to a value in range of (inclusive)
    the two values declared.
    """
    UNBOUNDED = 'UNBOUNDED'

    constraint_key = Constraint.IN_RANGE

    valid_types = (int, float, datetime.date,
                   datetime.time, datetime.datetime, str)

    valid_prop_types = (Schema.INTEGER, Schema.FLOAT, Schema.TIMESTAMP,
                        Schema.SCALAR_UNIT_SIZE, Schema.SCALAR_UNIT_FREQUENCY,
                        Schema.SCALAR_UNIT_TIME, Schema.RANGE)

    def __init__(self, property_name, property_type, constraint):
        super(InRange, self).__init__(property_name, property_type, constraint)
        if(not isinstance(self.constraint_value, collections.Sequence) or
           (len(constraint[self.IN_RANGE]) != 2)):
            ValidationIssueCollector.appendException(
                InvalidSchemaError(message=_('The property "in_range" '
                                             'expects a list.')))

        msg = _('The property "in_range" expects comparable values.')
        for value in self.constraint_value:
            if not isinstance(value, self.valid_types):
                ValidationIssueCollector.appendException(
                    InvalidSchemaError(message=msg))
            # The only string we allow for range is the special value
            # 'UNBOUNDED'
            if(isinstance(value, str) and value != self.UNBOUNDED):
                ValidationIssueCollector.appendException(
                    InvalidSchemaError(message=msg))

        self.min = self.constraint_value[0]
        self.max = self.constraint_value[1]

    def _is_valid(self, value):
        if not isinstance(self.min, str):
            if value < self.min:
                return False
        elif self.min != self.UNBOUNDED:
            return False
        if not isinstance(self.max, str):
            if value > self.max:
                return False
        elif self.max != self.UNBOUNDED:
            return False
        return True

    def _err_msg(self, value):
        return (_('The value "%(pvalue)s" of property "%(pname)s" is out of '
                  'range "(min:%(vmin)s, max:%(vmax)s)".') %
                dict(pname=self.property_name,
                     pvalue=self.value_msg,
                     vmin=self.constraint_value_msg[0],
                     vmax=self.constraint_value_msg[1]))

*/
