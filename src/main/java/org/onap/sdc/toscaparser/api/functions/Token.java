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

package org.onap.sdc.toscaparser.api.functions;

import org.onap.sdc.toscaparser.api.TopologyTemplate;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;

import java.util.ArrayList;

import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class Token extends Function {
    // Validate the function and provide an instance of the function

    //The token function is used within a TOSCA service template on a string to
    //parse out (tokenize) substrings separated by one or more token characters
    //within a larger string.

    //Arguments:

    //* The composite string that contains one or more substrings separated by
    //  token characters.
    //* The string that contains one or more token characters that separate
    //  substrings within the composite string.
    //* The integer indicates the index of the substring to return from the
    //  composite string.  Note that the first substring is denoted by using
    //  the '0' (zero) integer value.

    //Example:

    // [ get_attribute: [ my_server, data_endpoint, ip_address ], ':', 1 ]


    public Token(TopologyTemplate ttpl, Object context, String name, ArrayList<Object> args) {
        super(ttpl, context, name, args);
    }

    @Override
    public Object result() {
        return this;
    }

    @Override
    void validate() {
        if (args.size() < 3) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE180",
                    "ValueError: Invalid arguments for function \"token\". " +
                            "Expected at least three arguments"));
        } else {
            if (!(args.get(1) instanceof String) ||
                    ((String) args.get(1)).length() != 1) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE181",
                        "ValueError: Invalid arguments for function \"token\". " +
                                "Expected single char value as second argument"));
            }
            if (!(args.get(2) instanceof Integer)) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE182",
                        "ValueError: Invalid arguments for function \"token\"" +
                                "Expected integer value as third argument"));
            }
        }
    }

}

/*python

class Token(Function):
"""Validate the function and provide an instance of the function

The token function is used within a TOSCA service template on a string to
parse out (tokenize) substrings separated by one or more token characters
within a larger string.


Arguments:

* The composite string that contains one or more substrings separated by
  token characters.
* The string that contains one or more token characters that separate
  substrings within the composite string.
* The integer indicates the index of the substring to return from the
  composite string.  Note that the first substring is denoted by using
  the '0' (zero) integer value.

Example:

 [ get_attribute: [ my_server, data_endpoint, ip_address ], ':', 1 ]

"""

def validate(self):
    if len(self.args) < 3:
        ValidationIssueCollector.appendException(
            ValueError(_('Invalid arguments for function "{0}". Expected '
                         'at least three arguments.').format(TOKEN)))
    else:
        if not isinstance(self.args[1], str) or len(self.args[1]) != 1:
            ValidationIssueCollector.appendException(
                ValueError(_('Invalid arguments for function "{0}". '
                             'Expected single char value as second '
                             'argument.').format(TOKEN)))

        if not isinstance(self.args[2], int):
            ValidationIssueCollector.appendException(
                ValueError(_('Invalid arguments for function "{0}". '
                             'Expected integer value as third '
                             'argument.').format(TOKEN)))

def result(self):
    return self
*/
