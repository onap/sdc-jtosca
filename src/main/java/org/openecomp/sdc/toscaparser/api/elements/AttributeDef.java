package org.openecomp.sdc.toscaparser.api.elements;

import java.util.LinkedHashMap;

public class AttributeDef {
    // TOSCA built-in Attribute type
	
	private String name;
	private Object value;
	private LinkedHashMap<String,Object> schema;

    public AttributeDef(String adName, Object adValue, LinkedHashMap<String,Object> adSchema) {
        name = adName;
        value = adValue;
        schema = adSchema;
    }
    
    public String getName() {
    	return name;
    }

    public Object getValue() {
    	return value;
    }

    public LinkedHashMap<String,Object> getSchema() {
    	return schema;
    }
}

/*python

class AttributeDef(object):
    '''TOSCA built-in Attribute type.'''

    def __init__(self, name, value=None, schema=None):
        self.name = name
        self.value = value
        self.schema = schema
*/