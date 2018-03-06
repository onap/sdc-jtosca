package org.onap.sdc.toscaparser.api.elements.constraints;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class Pattern extends Constraint {

	@Override
	protected void _setValues() {

		constraintKey = PATTERN;

		validTypes.add("String");
		
		validPropTypes.add(Schema.STRING);
		
	}
	
	
	public Pattern(String name,String type,Object c) {
		super(name,type,c);
		
		if(!validTypes.contains(constraintValue.getClass().getSimpleName())) {
	        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE114", "InvalidSchemaError: The property \"pattern\" expects a string")); 
		}
	}

	@Override
	protected boolean _isValid(Object value) {
		try {
			if(!(value instanceof String)) {
		        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE115", String.format("ValueError: Input value \"%s\" to \"pattern\" property \"%s\" must be a string",
		        		value.toString(),propertyName))); 
				return false;
			}
			String strp = constraintValue.toString();
			String strm = value.toString();
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(strp);
			Matcher matcher = pattern.matcher(strm);
			if(matcher.find() && matcher.end() == strm.length()) {
				return true;
			}
			return false;
		}
		catch(PatternSyntaxException pse) {
	        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE116", String.format("ValueError: Invalid regex \"%s\" in \"pattern\" property \"%s\"",
	        		constraintValue.toString(),propertyName))); 
	        return false;
		}
	}

	@Override
	protected String _errMsg(Object value) {
	    return String.format("The value \"%s\" of property \"%s\" does not match the pattern \"%s\"",
				 value.toString(),propertyName,constraintValue.toString());
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