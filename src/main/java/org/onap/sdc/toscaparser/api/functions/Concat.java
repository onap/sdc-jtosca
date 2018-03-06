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
		super(ttpl,context,name,args);
	}
	
	@Override
	public Object result() {
		return this;
	}

	@Override
	void validate() {
		if(args.size() < 1) {
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