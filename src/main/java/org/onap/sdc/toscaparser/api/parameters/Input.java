package org.onap.sdc.toscaparser.api.parameters;

import org.onap.sdc.toscaparser.api.DataEntity;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.onap.sdc.toscaparser.api.elements.EntityType;
import org.onap.sdc.toscaparser.api.elements.constraints.Constraint;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;

public class Input {
	
	private static final String TYPE = "type";
	private static final String DESCRIPTION = "description";
	private static final String DEFAULT = "default";
	private static final String CONSTRAINTS = "constraints";
	private static final String REQUIRED = "required";
	private static final String STATUS = "status";
	private static final String ENTRY_SCHEMA = "entry_schema";
	
	public static final String INTEGER = "integer";
	public static final String STRING = "string";
	public static final String BOOLEAN = "boolean";
	public static final String FLOAT = "float";
	public static final String LIST = "list";
	public static final String MAP = "map";
	public static final String JSON = "json";
    
	private static String INPUTFIELD[] = {
    		TYPE, DESCRIPTION, DEFAULT, CONSTRAINTS, REQUIRED,STATUS, ENTRY_SCHEMA
    };
	
	private static String PRIMITIVE_TYPES[] = {
			INTEGER, STRING, BOOLEAN, FLOAT, LIST, MAP, JSON
    };
    
    private String name;
    private Schema schema;
	private LinkedHashMap<String,Object> customDefs;
	
	public Input(){
		/**
		 * Added to support Input serialization
		 */
	}
	
	public Input(String _name,LinkedHashMap<String,Object> _schemaDict,LinkedHashMap<String,Object> _customDefs) {
		name = _name;
		schema = new Schema(_name,_schemaDict);
		customDefs = _customDefs;
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return schema.getType();
	}

	public String getDescription() {
		return schema.getDescription();
	}

	public boolean isRequired() {
		return schema.isRequired();
	}

	public Object getDefault() {
		return schema.getDefault();
	}

	public ArrayList<Constraint> getConstraints() {
		return schema.getConstraints();
	}

    public void validate(Object value) {
        _validateField();
        _validateType(getType());
        if(value != null) {
            _validateValue(value);
        }
    }

    private void _validateField() {
    	for(String key: schema.getSchema().keySet()) {
    		boolean bFound = false;
    		for(String ifld: INPUTFIELD) {
    			if(key.equals(ifld)) {
    				bFound = true;
    				break;
    			}
    		}
    		if(!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE214", String.format(
                		"UnknownFieldError: Input \"%s\" contains unknown field \"%s\"",
                		name,key))); 
    		}
    	}   		
    }
    
    private void _validateType(String inputType) {
		boolean bFound = false;
		for(String pt: Schema.PROPERTY_TYPES) {
			if(pt.equals(inputType)) {
				bFound = true;
				break;
			}
		}
		
		if(!bFound) {
			if(customDefs.get(inputType) != null) {
				bFound = true;
			}
		}
		
		if(!bFound) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE215", String.format(
                    "ValueError: Invalid type \"%s\"",inputType))); 
		}
    }
    
    private void _validateValue(Object value) {
    	Object datatype = null;
    	if(EntityType.TOSCA_DEF.get(getType()) != null) {
    		datatype = EntityType.TOSCA_DEF.get(getType());
    	}
    	else if(EntityType.TOSCA_DEF.get(EntityType.DATATYPE_NETWORK_PREFIX + getType()) != null) {
    		datatype = EntityType.TOSCA_DEF.get(EntityType.DATATYPE_NETWORK_PREFIX + getType());
    	}
    	
    	String type = getType();
    	// if it's one of the basic types DON'T look in customDefs
    	if(Arrays.asList(PRIMITIVE_TYPES).contains(type)) {
        	DataEntity.validateDatatype(getType(), value, null, (LinkedHashMap<String,Object>)datatype, null);
        	return;	
    	}
    	else if(customDefs.get(getType()) != null) {
    		datatype = customDefs.get(getType());
        	DataEntity.validateDatatype(getType(), value, (LinkedHashMap<String,Object>)datatype, customDefs, null);
        	return;
    	}
    	
    	DataEntity.validateDatatype(getType(), value, null, (LinkedHashMap<String,Object>)datatype, null);
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import MissingRequiredFieldError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.dataentity import DataEntity
from toscaparser.elements.constraints import Schema
from toscaparser.elements.entity_type import EntityType
from toscaparser.utils.gettextutils import _


log = logging.getLogger('tosca')


class Input(object):

    INPUTFIELD = (TYPE, DESCRIPTION, DEFAULT, CONSTRAINTS, REQUIRED, STATUS,
                  ENTRY_SCHEMA) = ('type', 'description', 'default',
                                   'constraints', 'required', 'status',
                                   'entry_schema')

    def __init__(self, name, schema_dict):
        self.name = name
        self.schema = Schema(name, schema_dict)

        self._validate_field()
        self.validate_type(self.type)

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
    def status(self):
        return self.schema.status

    def validate(self, value=None):
        if value is not None:
            self._validate_value(value)

    def _validate_field(self):
        for name in self.schema.schema:
            if name not in self.INPUTFIELD:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Input "%s"' % self.name,
                                      field=name))

    def validate_type(self, input_type):
        if input_type not in Schema.PROPERTY_TYPES:
            ValidationIssueCollector.appendException(
                ValueError(_('Invalid type "%s".') % type))

    # tODO(anyone) Need to test for any built-in datatype not just network
    # that is, tosca.datatypes.* and not assume tosca.datatypes.network.*
    # tODO(anyone) Add support for tosca.datatypes.Credential
    def _validate_value(self, value):
        tosca = EntityType.TOSCA_DEF
        datatype = None
        if self.type in tosca:
            datatype = tosca[self.type]
        elif EntityType.DATATYPE_NETWORK_PREFIX + self.type in tosca:
            datatype = tosca[EntityType.DATATYPE_NETWORK_PREFIX + self.type]

        DataEntity.validate_datatype(self.type, value, None, datatype)

*/
