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
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;

public class Concat extends Function {
    // Validate the function and provide an instance of the function

    // Concatenation of values are supposed to be produced at runtime and
    // therefore its the responsibility of the TOSCA engine to implement the
    // evaluation of Concat functions.

    // Arguments:

    // * List of strings that needs to be concatenated

    // Example:

    //  [ 'http://',
    //    get_attribute: [ server, public_address ],
    //    ':' ,
    //    get_attribute: [ server, port ] ]


    public Concat(TopologyTemplate ttpl, Object context, String name, ArrayList<Object> args) {
        super(ttpl, context, name, args);
    }

    @Override
    public Object result() {
        return this;
    }

    @Override
    void validate() {
        if (args.size() < 1) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE145",
                    "ValueError: Invalid arguments for function \"concat\". " +
                            "Expected at least one argument"));
        }
    }

}

/*python

class Concat(Function):
"""Validate the function and provide an instance of the function

Concatenation of values are supposed to be produced at runtime and
therefore its the responsibility of the TOSCA engine to implement the
evaluation of Concat functions.

Arguments:

* List of strings that needs to be concatenated

Example:

  [ 'http://',
    get_attribute: [ server, public_address ],
    ':' ,
    get_attribute: [ server, port ] ]
"""

def validate(self):
    if len(self.args) < 1:
        ValidationIsshueCollector.appendException(
            ValueError(_('Invalid arguments for function "{0}". Expected '
                         'at least one arguments.').format(CONCAT)))

def result(self):
    return self
*/
