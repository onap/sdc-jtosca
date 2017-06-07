package org.openecomp.sdc.toscaparser.api.elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.openecomp.sdc.toscaparser.api.common.ExceptionCollector;
import org.openecomp.sdc.toscaparser.api.extensions.ExtTools;
import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class TypeValidation {

	private static final String DEFINITION_VERSION = "tosca_definitions_version";
	private static final String DESCRIPTION = "description";
	private static final String IMPORTS = "imports";
	private static final String DSL_DEFINITIONS = "dsl_definitions";
	private static final String NODE_TYPES = "node_types";
	private static final String REPOSITORIES = "repositories";
	private static final String DATA_TYPES = "data_types";
	private static final String ARTIFACT_TYPES = "artifact_types";
	private static final String GROUP_TYPES = "group_types";
	private static final String RELATIONSHIP_TYPES = "relationship_types";
	private static final String CAPABILITY_TYPES = "capability_types";
	private static final String INTERFACE_TYPES = "interface_types";
	private static final String POLICY_TYPES = "policy_types";
	private static final String TOPOLOGY_TEMPLATE = "topology_template";
	//Pavel
	private static final String METADATA = "metadata";

    private String ALLOWED_TYPE_SECTIONS[] = { 
    		DEFINITION_VERSION, DESCRIPTION, IMPORTS,
            DSL_DEFINITIONS, NODE_TYPES, REPOSITORIES,
            DATA_TYPES, ARTIFACT_TYPES, GROUP_TYPES,
            RELATIONSHIP_TYPES, CAPABILITY_TYPES,
            INTERFACE_TYPES, POLICY_TYPES,
            TOPOLOGY_TEMPLATE, METADATA
    };

    private static ArrayList<String> VALID_TEMPLATE_VERSIONS = _getVTV();
    
    private static ArrayList<String> _getVTV() {
    	ArrayList<String> vtv = new ArrayList<>();
    	vtv.add("tosca_simple_yaml_1_0");
    	ExtTools exttools = new ExtTools();
        vtv.addAll(exttools.getVersions());
        return vtv;
    }
    
    //private LinkedHashMap<String,Object> customTypes;
    private Object importDef;
    //private String version;
    
    public TypeValidation(LinkedHashMap<String,Object> _customTypes,
    					  Object _importDef) {
        importDef = _importDef;
        _validateTypeKeys(_customTypes);
    }
    
    private void _validateTypeKeys(LinkedHashMap<String,Object> customTypes) {
    	
        String sVersion = (String)customTypes.get(DEFINITION_VERSION);
        if(sVersion != null) {
        	_validateTypeVersion(sVersion);
            //version = sVersion;
        }
        for(String name: customTypes.keySet()) {
        	boolean bFound = false;
        	for(String ats: ALLOWED_TYPE_SECTIONS) {
        		if(name.equals(ats)) {
        			bFound = true;
        			break;
        		}
        	}
        	if(!bFound) {
                ThreadLocalsHolder.getCollector().appendException(String.format(
                    "UnknownFieldError: Template \"%s\" contains unknown field \"%s\"",
                    importDef.toString(),name));
        	}
        }
    }
    
    private void _validateTypeVersion(String sVersion) {
    	boolean bFound = false;
    	String allowed = "";
    	for(String atv: VALID_TEMPLATE_VERSIONS) {
    		allowed += "\"" + atv + "\" ";
    		if(sVersion.equals(atv)) {
    			bFound = true;
    			break;
    		}
    	}
    	if(!bFound) {
            ThreadLocalsHolder.getCollector().appendException(String.format(
                "InvalidTemplateVersion: version \"%s\" in \"%s\" is not supported\n" +
                "Allowed versions: [%s]",
                sVersion,importDef.toString(),allowed));
    	}    	
    }
}

/*python

from toscaparser.common.exception import ExceptionCollector
from toscaparser.common.exception import InvalidTemplateVersion
from toscaparser.common.exception import UnknownFieldError
from toscaparser.extensions.exttools import ExtTools


class TypeValidation(object):

    ALLOWED_TYPE_SECTIONS = (DEFINITION_VERSION, DESCRIPTION, IMPORTS,
                             DSL_DEFINITIONS, NODE_TYPES, REPOSITORIES,
                             DATA_TYPES, ARTIFACT_TYPES, GROUP_TYPES,
                             RELATIONSHIP_TYPES, CAPABILITY_TYPES,
                             INTERFACE_TYPES, POLICY_TYPES,
                             TOPOLOGY_TEMPLATE) = \
        ('tosca_definitions_version', 'description', 'imports',
         'dsl_definitions', 'node_types', 'repositories',
         'data_types', 'artifact_types', 'group_types',
         'relationship_types', 'capability_types',
         'interface_types', 'policy_types', 'topology_template')
    VALID_TEMPLATE_VERSIONS = ['tosca_simple_yaml_1_0']
    exttools = ExtTools()
    VALID_TEMPLATE_VERSIONS.extend(exttools.get_versions())

    def __init__(self, custom_types, import_def):
        self.import_def = import_def
        self._validate_type_keys(custom_types)

    def _validate_type_keys(self, custom_type):
        version = custom_type[self.DEFINITION_VERSION] \
            if self.DEFINITION_VERSION in custom_type \
            else None
        if version:
            self._validate_type_version(version)
            self.version = version

        for name in custom_type:
            if name not in self.ALLOWED_TYPE_SECTIONS:
                ExceptionCollector.appendException(
#                    UnknownFieldError(what='Template ' + (self.import_def),
                    UnknownFieldError(what= (self.import_def),
                                      field=name))

    def _validate_type_version(self, version):
        if version not in self.VALID_TEMPLATE_VERSIONS:
            ExceptionCollector.appendException(
                InvalidTemplateVersion(
#                    what=version + ' in ' + self.import_def,
                    what=self.import_def,
                    valid_versions=', '. join(self.VALID_TEMPLATE_VERSIONS)))    
*/
