package org.onap.sdc.toscaparser.api.elements.constraints;

public class Equal extends Constraint {

	protected void _setValues() {

		constraintKey = EQUAL;
		
		for(String s: Schema.PROPERTY_TYPES) {
			validPropTypes.add(s);
		}
		
	}
	
	public Equal(String name,String type,Object c) {
		super(name,type,c);
		
	}
	
	protected boolean _isValid(Object val) {
		// equality of objects is tricky so we're comparing 
		// the toString() representation
		if(val.toString().equals(constraintValue.toString())) {
			return true;
		}
		return false;
	}
	
	protected String _errMsg(Object value) {
	    return String.format("The value \"%s\" of property \"%s\" is not equal to \"%s\"",
	    		valueMsg,propertyName,constraintValueMsg);
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