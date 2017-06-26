package org.openecomp.sdc.toscaparser.api.functions;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.openecomp.sdc.toscaparser.api.DataEntity;
import org.openecomp.sdc.toscaparser.api.TopologyTemplate;
import org.openecomp.sdc.toscaparser.api.common.ExceptionCollector;
import org.openecomp.sdc.toscaparser.api.parameters.Input;
import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class GetInput extends Function {
	
	public GetInput(TopologyTemplate toscaTpl,Object context,String name,ArrayList<Object> _args) {
		super(toscaTpl,context,name,_args);
		
	}

	@Override
	void validate() {
//	    if(args.size() != 1) {
//	    	//PA - changed to WARNING from CRITICAL after talking to Renana, 22/05/2017
//	        ThreadLocalsHolder.getCollector().appendWarning(String.format(
//	            "ValueError: Expected one argument for function \"get_input\" but received \"%s\"",
//	            args.toString()));
//	    }
		if(args.size() > 2) {
			ThreadLocalsHolder.getCollector().appendWarning(String.format(
					"ValueError: Expected max 2 arguments for function \"get_input\" but received \"%s\"",
					args.size()));
		}
	    boolean bFound = false;
	    for(Input inp: toscaTpl.getInputs()) {
	    	if(inp.getName().equals(args.get(0))) {
	    		bFound = true;
	    		break;
	    	}
	    }
	    if(!bFound) {
	        ThreadLocalsHolder.getCollector().appendException(String.format(
	            "UnknownInputError: Unknown input \"%s\"",args.get(0)));
	    }
	}

	public 	Object result() {
		if(toscaTpl.getParsedParams() != null && 
				toscaTpl.getParsedParams().get(getInputName()) != null) {
			LinkedHashMap<String,Object> ttinp = (LinkedHashMap<String,Object>)toscaTpl.getTpl().get("inputs");
			LinkedHashMap<String,Object> ttinpinp = (LinkedHashMap<String,Object>)ttinp.get(getInputName());
			String type = (String)ttinpinp.get("type");
			
			return DataEntity.validateDatatype(
					type, toscaTpl.getParsedParams().get(getInputName()),null,null,null);
		}
		
		Input inputDef = null;
		for(Input inpDef: toscaTpl.getInputs()) {
			if(getInputName().equals(inpDef.getName())) {
				inputDef = inpDef;
				break;
			}
		}
		if(inputDef != null) {
			if (args.size() == 2 && args.get(1) instanceof Integer) {
				if (inputDef.getDefault() != null && inputDef.getDefault() instanceof ArrayList) {
					return ((ArrayList) inputDef.getDefault()).get(((Integer)args.get(1)).intValue());
				}
			}
			return inputDef.getDefault();
		}
		return null;
	}
	
	public String getInputName() {
		return (String)args.get(0);
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
        ExceptionCollector.appendException(
            ValueError(_(
                'Expected one argument for function "get_input" but '
                'received "%s".') % self.args))
    inputs = [input.name for input in self.tosca_tpl.inputs]
    if self.args[0] not in inputs:
        ExceptionCollector.appendException(
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