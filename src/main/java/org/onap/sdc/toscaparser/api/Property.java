package org.onap.sdc.toscaparser.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.onap.sdc.toscaparser.api.elements.constraints.Constraint;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;
import org.onap.sdc.toscaparser.api.functions.Function;

public class Property {
    // TOSCA built-in Property type

	private static final String TYPE = "type";
	private static final String REQUIRED = "required";
	private static final String DESCRIPTION = "description";
	private static final String DEFAULT = "default";
	private static final String CONSTRAINTS = "constraints";
	
	private static final String[] PROPERTY_KEYS = {
			TYPE, REQUIRED, DESCRIPTION, DEFAULT, CONSTRAINTS};

	private static final String ENTRYTYPE = "type";
	private static final String ENTRYPROPERTIES = "properties";
	private static final String[] ENTRY_SCHEMA_KEYS = {
        ENTRYTYPE, ENTRYPROPERTIES};
	
	private String name;
	private Object value;
	private Schema schema;
	private LinkedHashMap<String,Object> customDef;

	public Property(String propname,
					Object propvalue,
					LinkedHashMap<String,Object> propschemaDict,
					LinkedHashMap<String,Object> propcustomDef) {
		
        name = propname;
        value = propvalue;
        customDef = propcustomDef;
        schema = new Schema(propname, propschemaDict);
	}
	
	public String getType() {
		return schema.getType();
	}

	public boolean isRequired() {
		return schema.isRequired();
	}
	
	public String getDescription() {
		return schema.getDescription();
	}

	public Object getDefault() {
		return schema.getDefault();
	}

	public ArrayList<Constraint> getConstraints() {
		return schema.getConstraints();
	}

	public LinkedHashMap<String,Object> getEntrySchema() {
		return schema.getEntrySchema();
	}
 

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	
	// setter
	public Object setValue(Object vob) {
		value = vob;
		return value;
	}
	
	public void validate() {
		// Validate if not a reference property
		if(!Function.isFunction(value)) {
			if(getType().equals(Schema.STRING)) {
				value = value.toString();
			}
			value = DataEntity.validateDatatype(getType(),value,
                    							 getEntrySchema(),
                    							 customDef,
                    							 name);
			_validateConstraints();
		}
	}

	private void _validateConstraints() {
		if(getConstraints() != null) {
			for(Constraint constraint: getConstraints()) {
				constraint.validate(value);
			}
		}
	}

	@Override
	public String toString() {
		return "Property{" +
				"name='" + name + '\'' +
				", value=" + value +
				", schema=" + schema +
				", customDef=" + customDef +
				'}';
	}
}

/*python

class Property(object):
    '''TOSCA built-in Property type.'''

    PROPERTY_KEYS = (
        TYPE, REQUIRED, DESCRIPTION, DEFAULT, CONSTRAINTS
    ) = (
        'type', 'required', 'description', 'default', 'constraints'
    )

    ENTRY_SCHEMA_KEYS = (
        ENTRYTYPE, ENTRYPROPERTIES
    ) = (
        'type', 'properties'
    )

    def __init__(self, property_name, value, schema_dict, custom_def=None):
        self.name = property_name
        self.value = value
        self.custom_def = custom_def
        self.schema = Schema(property_name, schema_dict)

    @property
    def type(self):
        return self.schema.type

    @property
    def required(self):
        return self.schema.required

    @property
    def description(self):
        return self.schema.description

    @property
    def default(self):
        return self.schema.default

    @property
    def constraints(self):
        return self.schema.constraints

    @property
    def entry_schema(self):
        return self.schema.entry_schema

    def validate(self):
        '''Validate if not a reference property.'''
        if not is_function(self.value):
            if self.type == Schema.STRING:
                self.value = str(self.value)
            self.value = DataEntity.validate_datatype(self.type, self.value,
                                                      self.entry_schema,
                                                      self.custom_def,
                                                      self.name)
            self._validate_constraints()

    def _validate_constraints(self):
        if self.constraints:
            for constraint in self.constraints:
                constraint.validate(self.value)
*/
