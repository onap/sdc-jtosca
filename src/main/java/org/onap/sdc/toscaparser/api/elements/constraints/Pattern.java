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
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class Pattern extends Constraint {

    @Override
    protected void setValues() {

        setConstraintKey(PATTERN);

        addValidTypes(Collections.singletonList("String"));

        validPropTypes.add(Schema.STRING);

    }


    public Pattern(String name, String type, Object c) {
        super(name, type, c);

        if (!validTypes.contains(constraintValue.getClass().getSimpleName())) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE114", "InvalidSchemaError: The property \"pattern\" expects a string"));
        }
    }

    @Override
    protected boolean isValid(Object value) {
        try {
            if (!(value instanceof String)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE115", String.format("ValueError: Input value \"%s\" to \"pattern\" property \"%s\" must be a string",
                        value.toString(), propertyName)));
                return false;
            }
            String strp = constraintValue.toString();
            String strm = value.toString();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(strp);
            Matcher matcher = pattern.matcher(strm);
            if (matcher.find() && matcher.end() == strm.length()) {
                return true;
            }
            return false;
        } catch (PatternSyntaxException pse) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE116", String.format("ValueError: Invalid regex \"%s\" in \"pattern\" property \"%s\"",
                    constraintValue.toString(), propertyName)));
            return false;
        }
    }

    @Override
    protected String errMsg(Object value) {
        return String.format("The value \"%s\" of property \"%s\" does not match the pattern \"%s\"",
                value.toString(), propertyName, constraintValue.toString());
    }

}

/*python

class Pattern(Constraint):
    """Constraint class for "pattern"

    Constrains the property or parameter to a value that is allowed by
    the provided regular expression.
    """

    constraint_key = Constraint.PATTERN

    valid_types = (str, )

    valid_prop_types = (Schema.STRING, )

    def __init__(self, property_name, property_type, constraint):
        super(Pattern, self).__init__(property_name, property_type, constraint)
        if not isinstance(self.constraint_value, self.valid_types):
            ValidationIsshueCollector.appendException(
                InvalidSchemaError(message=_('The property "pattern" '
                                             'expects a string.')))
        self.match = re.compile(self.constraint_value).match

    def _is_valid(self, value):
        match = self.match(value)
        return match is not None and match.end() == len(value)

    def _err_msg(self, value):
        return (_('The value "%(pvalue)s" of property "%(pname)s" does not '
                  'match pattern "%(cvalue)s".') %
                dict(pname=self.property_name,
                     pvalue=value,
                     cvalue=self.constraint_value))
*/
