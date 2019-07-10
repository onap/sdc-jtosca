/*-
 * ============LICENSE_START=======================================================
 * Copyright (c) 2017 AT&T Intellectual Property.
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
 * Modifications copyright (c) 2019 Fujitsu Limited.
 * ================================================================================
 */
package org.onap.sdc.toscaparser.api.functions;

import org.onap.sdc.toscaparser.api.DataEntity;
import org.onap.sdc.toscaparser.api.TopologyTemplate;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.parameters.Input;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GetInput extends Function {

    public static final String INDEX = "INDEX";
    public static final String INPUTS = "inputs";
    public static final String TYPE = "type";
    public static final String PROPERTIES = "properties";
    public static final String ENTRY_SCHEMA = "entry_schema";

    public GetInput(TopologyTemplate toscaTpl, Object context, String name, ArrayList<Object> _args) {
        super(toscaTpl, context, name, _args);

    }

    @Override
    void validate() {

//	    if(args.size() != 1) {
//	    	//PA - changed to WARNING from CRITICAL after talking to Renana, 22/05/2017
//	        ThreadLocalsHolder.getCollector().appendWarning(String.format(
//	            "ValueError: Expected one argument for function \"get_input\" but received \"%s\"",
//	            args.toString()));
//	    }
        boolean bFound = false;
        for (Input inp : toscaTpl.getInputs()) {
            if (inp.getName().equals(args.get(0))) {
                bFound = true;
                break;
            }
        }
        if (!bFound) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE158", String.format(
                    "UnknownInputError: Unknown input \"%s\"", args.get(0))));
        } else if (args.size() > 2) {
            LinkedHashMap<String, Object> inputs = (LinkedHashMap<String, Object>) toscaTpl.getTpl().get(INPUTS);
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) inputs.get(getInputName());
            String type;

            for (int argumentNumber = 1; argumentNumber < args.size(); argumentNumber++) {
                String dataTypeName = "";
                bFound = false;
                if (INDEX.equals(args.get(argumentNumber).toString()) || (args.get(argumentNumber) instanceof Integer)) {
                    bFound = true;
                } else {
                    type = (String) data.get(TYPE);
                    //get type name
                    if (type.equals("list") || type.equals("map")) {
                        LinkedHashMap<String, Object> schema = (LinkedHashMap<String, Object>) data.get(ENTRY_SCHEMA);
                        dataTypeName = (String) schema.get(TYPE);
                    } else {
                        dataTypeName = type;
                    }
                    //check property name
                    LinkedHashMap<String, Object> dataType = (LinkedHashMap<String, Object>) toscaTpl.getCustomDefs().get(dataTypeName);
                    if (dataType != null) {
                        LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) dataType.get(PROPERTIES);
                        data = (LinkedHashMap<String, Object>) props.get(args.get(argumentNumber).toString());
                        if (data != null) {
                            bFound = true;
                        }
                    }
                }
                if (!bFound) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE282", String.format(
                            "UnknownDataType: Unknown data type \"%s\"", args.get(argumentNumber))));
                }
            }
        }
    }

    public Object result() {
        if (toscaTpl.getParsedParams() != null &&
                toscaTpl.getParsedParams().get(getInputName()) != null) {
            LinkedHashMap<String, Object> ttinp = (LinkedHashMap<String, Object>) toscaTpl.getTpl().get(INPUTS);
            LinkedHashMap<String, Object> ttinpinp = (LinkedHashMap<String, Object>) ttinp.get(getInputName());
            String type = (String) ttinpinp.get("type");

            Object value = DataEntity.validateDatatype(
                    type, toscaTpl.getParsedParams().get(getInputName()), null, toscaTpl.getCustomDefs(), null);
            //SDC resolving Get Input
            if (value instanceof ArrayList) {
                if (args.size() == 2 && args.get(1) instanceof Integer && ((ArrayList) value).size() > (Integer) args.get(1)) {
                    return ((ArrayList) value).get((Integer) args.get(1));
                }
				/* commented out for network cloud (SDNC)
				ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE273",String.format(
							"GetInputError: cannot resolve input name \"%s\", the expected structure is an argument with a name of input type list and a second argument with an index in the list", args.get(0))));
				return null;
*/
            }
            return value;
        }

        Input inputDef = null;
        for (Input inpDef : toscaTpl.getInputs()) {
            if (getInputName().equals(inpDef.getName())) {
                inputDef = inpDef;
                break;
            }
        }
        if (inputDef != null) {
            if (args.size() == 2 && inputDef.getDefault() != null && inputDef.getDefault() instanceof ArrayList) {
                if (args.get(1) instanceof Integer
                        && ((ArrayList) inputDef.getDefault()).size() > ((Integer) args.get(1)).intValue()) {
                    return ((ArrayList) inputDef.getDefault()).get(((Integer) args.get(1)).intValue());
                }
/*
				commented out for network cloud (SDNC)
				ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE274",(String.format(
						"GetInputError: cannot resolve input Def name \"%s\", the expected structure is an argument with a name of input type list and a second argument with an index in the list", args.get(0)))));
				return null;
*/
            }
            return inputDef.getDefault();
        }
        return null;
    }

    public String getInputName() {
        return (String) args.get(0);
    }

    public LinkedHashMap<String, Object> getEntrySchema() {
        LinkedHashMap<String, Object> inputs = (LinkedHashMap<String, Object>) toscaTpl.getTpl().get(INPUTS);
        LinkedHashMap<String, Object> inputValue = (LinkedHashMap<String, Object>) inputs.get(getInputName());
        return (LinkedHashMap<String, Object>) inputValue.get(ENTRY_SCHEMA);
    }

    public ArrayList<Object> getArguments() {
        return args;
    }
}

/*python

class GetInput(Function):
"""Get a property value declared within the input of the service template.

Arguments:

* Input name.

Example:

* get_input: port
"""

def validate(self):
    if len(self.args) != 1:
        ValidationIssueCollector.appendException(
            ValueError(_(
                'Expected one argument for function "get_input" but '
                'received "%s".') % self.args))
    inputs = [input.name for input in self.tosca_tpl.inputs]
    if self.args[0] not in inputs:
        ValidationIssueCollector.appendException(
            UnknownInputError(input_name=self.args[0]))

def result(self):
    if self.tosca_tpl.parsed_params and \
       self.input_name in self.tosca_tpl.parsed_params:
        return DataEntity.validate_datatype(
            self.tosca_tpl.tpl['inputs'][self.input_name]['type'],
            self.tosca_tpl.parsed_params[self.input_name])

    input = [input_def for input_def in self.tosca_tpl.inputs
             if self.input_name == input_def.name][0]
    return input.default

@property
def input_name(self):
    return self.args[0]

*/
