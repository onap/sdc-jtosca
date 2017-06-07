package org.openecomp.sdc.toscaparser.api.elements.constraints;

import org.openecomp.sdc.toscaparser.api.common.ExceptionCollector;
import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class Length extends Constraint {
	// Constraint class for "length"
	
	// Constrains the property or parameter to a value of a given length.

	@Override
	protected void _setValues() {

		constraintKey = LENGTH;

		validTypes.add("Integer");
		
		validPropTypes.add(Schema.STRING);
		
	}
	
	public Length(String name,String type,Object c) {
		super(name,type,c);
		
		if(!validTypes.contains(constraintValue.getClass().getSimpleName())) {
	        ThreadLocalsHolder.getCollector().appendException("InvalidSchemaError: The property \"length\" expects an integer");
		}
	}
	
	@Override
	protected boolean _isValid(Object value) {
	    if(value instanceof String && constraintValue instanceof Integer &&
	    		((String)value).length() == (Integer)constraintValue) {
	        return true;
	    }
		return false;
	}

	@Override
	protected String _errMsg(Object value) {
	    return String.format("Length of value \"%s\" of property \"%s\" must be equal to \"%s\"",
	    					 value.toString(),propertyName,constraintValue.toString());
	}

}

/*python
	class Length(Constraint):
	"""Constraint class for "length"
	
	Constrains the property or parameter to a value of a given length.
	"""
	
	constraint_key = Constraint.LENGTH
	
	valid_types = (int, )
	
	valid_prop_types = (Schema.STRING, )
	
	def __init__(self, property_name, property_type, constraint):
	    super(Length, self).__init__(property_name, property_type, constraint)
	    if not isinstance(self.constraint_value, self.valid_types):
	        ExceptionCollector.appendException(
	            InvalidSchemaError(message=_('The property "length" expects '
	                                         'an integer.')))
	
	def _is_valid(self, value):
	    if isinstance(value, str) and len(value) == self.constraint_value:
	        return True
	
	    return False
	
	def _err_msg(self, value):
	    return (_('Length of value "%(pvalue)s" of property "%(pname)s" '
	              'must be equal to "%(cvalue)s".') %
	            dict(pname=self.property_name,
	                 pvalue=value,
	                 cvalue=self.constraint_value))
*/