package org.openecomp.sdc.toscaparser.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.sdc.toscaparser.api.common.ExceptionCollector;
import org.openecomp.sdc.toscaparser.api.common.JToscaException;
import org.openecomp.sdc.toscaparser.api.elements.EntityType;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.sdc.toscaparser.api.extensions.ExtTools;
import org.openecomp.sdc.toscaparser.api.parameters.Input;
import org.openecomp.sdc.toscaparser.api.parameters.Output;
import org.openecomp.sdc.toscaparser.api.prereq.CSAR;
import org.openecomp.sdc.toscaparser.api.utils.JToscaErrorCodes;
import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ToscaTemplate extends Object {

	private static Logger log = LoggerFactory.getLogger(ToscaTemplate.class.getName());

	// TOSCA template key names
	private static final String DEFINITION_VERSION = "tosca_definitions_version"; 
	private static final String DEFAULT_NAMESPACE = "tosca_default_namespace"; 
	private static final String TEMPLATE_NAME = "template_name";
	private static final String TOPOLOGY_TEMPLATE = "topology_template"; 
	private static final String TEMPLATE_AUTHOR = "template_author"; 
	private static final String TEMPLATE_VERSION = "template_version";
	private static final String DESCRIPTION = "description"; 
	private static final String IMPORTS = "imports";
	private static final String DSL_DEFINITIONS = "dsl_definitions"; 
	private static final String NODE_TYPES = "node_types";
	private static final String RELATIONSHIP_TYPES = "relationship_types";
	private static final String RELATIONSHIP_TEMPLATES = "relationship_templates";
	private static final String CAPABILITY_TYPES = "capability_types";
	private static final String ARTIFACT_TYPES = "artifact_types"; 
	private static final String DATA_TYPES = "data_types";
	private static final String INTERFACE_TYPES = "interface_types"; 
	private static final String POLICY_TYPES = "policy_types"; 
	private static final String GROUP_TYPES = "group_types"; 
	private static final String REPOSITORIES = "repositories";
	
	private static String SECTIONS[] = {
			DEFINITION_VERSION, DEFAULT_NAMESPACE, TEMPLATE_NAME,
            TOPOLOGY_TEMPLATE, TEMPLATE_AUTHOR, TEMPLATE_VERSION,
            DESCRIPTION, IMPORTS, DSL_DEFINITIONS, NODE_TYPES,
            RELATIONSHIP_TYPES, RELATIONSHIP_TEMPLATES,
            CAPABILITY_TYPES, ARTIFACT_TYPES, DATA_TYPES,
            INTERFACE_TYPES, POLICY_TYPES, GROUP_TYPES, REPOSITORIES
	};

	// Sections that are specific to individual template definitions
	private static final String METADATA = "metadata";
	private static ArrayList<String> SPECIAL_SECTIONS;
	
    private ExtTools exttools = new ExtTools();

    private ArrayList<String> VALID_TEMPLATE_VERSIONS;
    private LinkedHashMap<String,ArrayList<String>> ADDITIONAL_SECTIONS;

	private boolean isFile;
	private String path;
	private String inputPath;
	private LinkedHashMap<String,Object> parsedParams;
	private LinkedHashMap<String,Object> tpl;
    private String version;
    private ArrayList<Object> imports;
    private LinkedHashMap<String,Object> relationshipTypes;
    private Metadata metaData;
    private String description;
    private TopologyTemplate topologyTemplate;
    private ArrayList<Repository> repositories;
    private ArrayList<Input> inputs;
    private ArrayList<RelationshipTemplate> relationshipTemplates;
    private ArrayList<NodeTemplate> nodeTemplates;
    private ArrayList<Output> outputs;
	private ArrayList<Policy> policies;
    private ConcurrentHashMap<String,Object> nestedToscaTplsWithTopology;
    private ArrayList<TopologyTemplate> nestedToscaTemplatesWithTopology;
    private ToscaGraph graph;
    private String csarTempDir;
    private int nestingLoopCounter;
	private LinkedHashMap<String, LinkedHashMap<String, Object>> metaProperties;

	@SuppressWarnings("unchecked")
	public ToscaTemplate(String _path,
			 			 LinkedHashMap<String,Object> _parsedParams,
						 boolean aFile,
						 LinkedHashMap<String,Object> yamlDictTpl) throws JToscaException {

		ThreadLocalsHolder.setCollector(new ExceptionCollector(_path));

		VALID_TEMPLATE_VERSIONS = new ArrayList<>();
		VALID_TEMPLATE_VERSIONS.add("tosca_simple_yaml_1_0");
	    VALID_TEMPLATE_VERSIONS.addAll(exttools.getVersions());
		ADDITIONAL_SECTIONS = new LinkedHashMap<>();
		SPECIAL_SECTIONS = new ArrayList<>();
		SPECIAL_SECTIONS.add(METADATA);
		ADDITIONAL_SECTIONS.put("tosca_simple_yaml_1_0",SPECIAL_SECTIONS);
	    ADDITIONAL_SECTIONS.putAll(exttools.getSections());

		//long startTime = System.nanoTime();
		
		
		isFile = aFile;
		inputPath = null;
		path = null;
		tpl = null;
		csarTempDir = null;
		nestedToscaTplsWithTopology = new ConcurrentHashMap<>();
		nestedToscaTemplatesWithTopology = new ArrayList<TopologyTemplate>();

		if(_path != null && !_path.isEmpty()) {
			// save the original input path
			inputPath = _path;
			// get the actual path (will change with CSAR)
			path = _getPath(_path);
			// load the YAML template
			if (path != null && !path.isEmpty()) {
				try {
					//System.out.println("Loading YAML file " + path);
					log.debug("ToscaTemplate Loading YAMEL file {}", path);
					InputStream input = new FileInputStream(new File(path));
					Yaml yaml = new Yaml();
					Object data = yaml.load(input);
					this.tpl = (LinkedHashMap<String,Object>) data;
				} 
				catch (FileNotFoundException e) {
					log.error("ToscaTemplate - Exception loading yaml: {}", e.getMessage());
					return;
				}
				catch(Exception e) {
					log.error("ToscaTemplate - Error loading yaml, aborting");
					return;
				}
				
		        if(yamlDictTpl != null) {
		            //msg = (_('Both path and yaml_dict_tpl arguments were '
		            //         'provided. Using path and ignoring yaml_dict_tpl.'))
		            //log.info(msg)
		            log.debug("ToscaTemplate - Both path and yaml_dict_tpl arguments were provided. Using path and ignoring yaml_dict_tpl");
		        }
			}
			else {
				// no input to process...
				_abort();
			}
		} 
		else {
			if(yamlDictTpl != null) {
                tpl = yamlDictTpl;
			}
            else {
				ThreadLocalsHolder.getCollector().appendException(
	                    "ValueError: No path or yaml_dict_tpl was provided. There is nothing to parse");
				log.debug("ToscaTemplate ValueError: No path or yaml_dict_tpl was provided. There is nothing to parse");

			}
		}

        if(tpl != null) {
            parsedParams = _parsedParams;
            _validateField();
            this.version = _tplVersion();
            this.metaData = _tplMetaData();
            this.relationshipTypes = _tplRelationshipTypes();
            this.description = _tplDescription();
            this.topologyTemplate = _topologyTemplate();
            this.repositories = _tplRepositories();
            if(topologyTemplate.getTpl() != null) {
                this.inputs = _inputs();
                this.relationshipTemplates = _relationshipTemplates();
                this.nodeTemplates = _nodeTemplates();
                this.outputs = _outputs();
                this.policies = _policies();
//                _handleNestedToscaTemplatesWithTopology();
				_handleNestedToscaTemplatesWithTopology(topologyTemplate);
                graph = new ToscaGraph(nodeTemplates);
            }
        }

        if(csarTempDir != null) {
        	CSAR.deleteDir(new File(csarTempDir));
        	csarTempDir = null;
        }
        
		verifyTemplate();

	}
	
	private void _abort() throws JToscaException {
		// print out all exceptions caught
		verifyTemplate();
		throw new JToscaException("jtosca aborting", JToscaErrorCodes.PATH_NOT_VALID.getValue());
	}
	private TopologyTemplate _topologyTemplate() {
		return new TopologyTemplate(
				_tplTopologyTemplate(),
				_getAllCustomDefs(imports),
				relationshipTypes,
				parsedParams,
				null);
	}

	private ArrayList<Input> _inputs() {
		return topologyTemplate.getInputs();
	}

	private ArrayList<NodeTemplate> _nodeTemplates() {
		return topologyTemplate.getNodeTemplates();
	}

	private ArrayList<RelationshipTemplate> _relationshipTemplates() {
		return topologyTemplate.getRelationshipTemplates();
	}

	private ArrayList<Output> _outputs() {
		return topologyTemplate.getOutputs();
	}

	private String _tplVersion() {
		return (String)tpl.get(DEFINITION_VERSION);
	}

	@SuppressWarnings("unchecked")
	private Metadata _tplMetaData() {
		Object mdo = tpl.get(METADATA);
		if(mdo instanceof LinkedHashMap) {
			return new Metadata((Map<String, Object>)mdo);
		}
		else {
			return null;
		}
	}

	private String _tplDescription() {
		return (String)tpl.get(DESCRIPTION);
	}

	private ArrayList<Object> _tplImports() {
		return (ArrayList<Object>)tpl.get(IMPORTS);
	}

	private ArrayList<Repository> _tplRepositories() {
		LinkedHashMap<String,Object> repositories = 
				(LinkedHashMap<String,Object>)tpl.get(REPOSITORIES);
		ArrayList<Repository> reposit = new ArrayList<>();
		if(repositories != null) {
			for(Map.Entry<String,Object> me: repositories.entrySet()) {
				Repository reposits = new Repository(me.getKey(),me.getValue());
				reposit.add(reposits);
			}
		}
		return reposit;
	}

	private LinkedHashMap<String,Object> _tplRelationshipTypes() {
		return (LinkedHashMap<String,Object>)_getCustomTypes(RELATIONSHIP_TYPES,null);
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String,Object> _tplRelationshipTemplates() {
		return (LinkedHashMap<String,Object>)_tplTopologyTemplate().get(RELATIONSHIP_TEMPLATES);
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String,Object> _tplTopologyTemplate() {
		return (LinkedHashMap<String,Object>)tpl.get(TOPOLOGY_TEMPLATE);
	}

	private ArrayList<Policy> _policies() {
		return topologyTemplate.getPolicies();
	}
	
	private LinkedHashMap<String,Object> _getAllCustomDefs(ArrayList<Object> alImports) {
		
		String types[] = {
			IMPORTS, NODE_TYPES, CAPABILITY_TYPES, RELATIONSHIP_TYPES, 
			DATA_TYPES, INTERFACE_TYPES, POLICY_TYPES, GROUP_TYPES
		};
		LinkedHashMap<String,Object> customDefsFinal = new LinkedHashMap<String,Object>(); 
		LinkedHashMap<String,Object> customDefs = _getCustomTypes(types,alImports);
		if(customDefs != null) {
			customDefsFinal.putAll(customDefs);
			if(customDefs.get(IMPORTS) != null) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String,Object> importDefs = _getAllCustomDefs((ArrayList<Object>)customDefs.get(IMPORTS));
				customDefsFinal.putAll(importDefs);
			}
		}
		
        // As imports are not custom_types, remove from the dict
        customDefsFinal.remove(IMPORTS);

		return customDefsFinal;
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String,Object> _getCustomTypes(Object typeDefinitions,ArrayList<Object> alImports) {
		
        // Handle custom types defined in imported template files
        // This method loads the custom type definitions referenced in "imports"
        // section of the TOSCA YAML template.
		
		LinkedHashMap<String,Object> customDefs = new LinkedHashMap<String,Object>();
        ArrayList<String> typeDefs = new ArrayList<String>();
        if(typeDefinitions  instanceof String[]) {
        	for(String s: (String[])typeDefinitions) {
        		typeDefs.add(s);
        	}
        }
        else {
        	typeDefs.add((String)typeDefinitions);
        }

        if(alImports == null) {
            alImports = _tplImports();
        }

        if(alImports != null) {
        	ImportsLoader customService = new ImportsLoader(alImports,path,typeDefs,tpl);
        	ArrayList<LinkedHashMap<String,Object>> nestedToscaTpls = customService.getNestedToscaTpls();
        	_updateNestedToscaTplsWithTopology(nestedToscaTpls);

        	customDefs = customService.getCustomDefs();
        	if(customDefs == null) {
        		return null;
        	}
        }

        //Handle custom types defined in current template file
        for(String td: typeDefs) {
        	if(!td.equals(IMPORTS)) {
        		LinkedHashMap<String,Object>  innerCustomTypes = (LinkedHashMap<String,Object> )tpl.get(td);
        		if(innerCustomTypes != null) {
        			customDefs.putAll(innerCustomTypes);
        		}
        	}
        }
        return customDefs;
	}

	private void _updateNestedToscaTplsWithTopology(ArrayList<LinkedHashMap<String,Object>> nestedToscaTpls) {
		for(LinkedHashMap<String,Object> ntpl: nestedToscaTpls) {
			// there is just one key:value pair in ntpl
			for(Map.Entry<String,Object> me: ntpl.entrySet()) {
				String fileName = me.getKey();
				@SuppressWarnings("unchecked")
				LinkedHashMap<String,Object> toscaTpl = (LinkedHashMap<String,Object>)me.getValue();
				if(toscaTpl.get(TOPOLOGY_TEMPLATE) != null) {
					if(nestedToscaTplsWithTopology.get(fileName) == null) {
						nestedToscaTplsWithTopology.putAll(ntpl);
					}
				}
			}
		}
	}

	// multi level nesting - RECURSIVE
	private void _handleNestedToscaTemplatesWithTopology(TopologyTemplate tt) {
		if(++nestingLoopCounter > 10) {
			log.error("ToscaTemplate - _handleNestedToscaTemplatesWithTopology - Nested Topologies Loop: too many levels, aborting");
			return;
		}
		for(Map.Entry<String,Object> me: nestedToscaTplsWithTopology.entrySet()) {
			String fname = me.getKey();
			LinkedHashMap<String,Object> toscaTpl = 
							(LinkedHashMap<String,Object>)me.getValue();
			for(NodeTemplate nt: tt.getNodeTemplates()) {
				if(_isSubMappedNode(nt,toscaTpl)) {
					parsedParams = _getParamsForNestedTemplate(nt);
					ArrayList<Object> alim = (ArrayList<Object>)toscaTpl.get(IMPORTS);
					LinkedHashMap<String,Object> topologyTpl = 
							(LinkedHashMap<String,Object>)toscaTpl.get(TOPOLOGY_TEMPLATE);
					TopologyTemplate topologyWithSubMapping = 
						new TopologyTemplate(topologyTpl,
											 _getAllCustomDefs(alim),
											 relationshipTypes, 
											 parsedParams,
											 nt);
					if(topologyWithSubMapping.getSubstitutionMappings() != null) {
                        // Record nested topology templates in top level template
                        //nestedToscaTemplatesWithTopology.add(topologyWithSubMapping);
                        // Set substitution mapping object for mapped node
                        nt.setSubMappingToscaTemplate(
                        		topologyWithSubMapping.getSubstitutionMappings());
                        _handleNestedToscaTemplatesWithTopology(topologyWithSubMapping);
					}
				}
			}
		}
	}
	
//	private void _handleNestedToscaTemplatesWithTopology() {
//		for(Map.Entry<String,Object> me: nestedToscaTplsWithTopology.entrySet()) {
//			String fname = me.getKey();
//			LinkedHashMap<String,Object> toscaTpl =
//							(LinkedHashMap<String,Object>)me.getValue();
//			for(NodeTemplate nt: nodeTemplates) {
//				if(_isSubMappedNode(nt,toscaTpl)) {
//					parsedParams = _getParamsForNestedTemplate(nt);
//                    ArrayList<Object> alim = (ArrayList<Object>)toscaTpl.get(IMPORTS);
//					LinkedHashMap<String,Object> topologyTpl =
//							(LinkedHashMap<String,Object>)toscaTpl.get(TOPOLOGY_TEMPLATE);
//					TopologyTemplate topologyWithSubMapping =
//							new TopologyTemplate(topologyTpl,
//												//_getAllCustomDefs(null),
//												_getAllCustomDefs(alim),
//												relationshipTypes,
//												parsedParams,
//												nt);
//					if(topologyWithSubMapping.getSubstitutionMappings() != null) {
//                        // Record nested topology templates in top level template
//                        nestedToscaTemplatesWithTopology.add(topologyWithSubMapping);
//                        // Set substitution mapping object for mapped node
//                        nt.setSubMappingToscaTemplate(
//                        		topologyWithSubMapping.getSubstitutionMappings());
//					}
//				}
//			}
//		}
//	}

	private void _validateField() {
		String sVersion = _tplVersion();
		if(sVersion == null) {
			ThreadLocalsHolder.getCollector().appendException(String.format(
					"MissingRequiredField: Template is missing required field \"%s\"",DEFINITION_VERSION));
		}
		else {
			_validateVersion(sVersion);
			this.version = sVersion;
		}
		
		for (String sKey : tpl.keySet()) {
			boolean bFound = false;
			for (String sSection: SECTIONS) {
				if(sKey.equals(sSection)) {
					bFound = true;
					break;
				}
			}
			// check ADDITIONAL_SECTIONS
			if(!bFound) {
				if(ADDITIONAL_SECTIONS.get(version) != null &&
						ADDITIONAL_SECTIONS.get(version).contains(sKey)) {
					bFound = true;
				}
			}
			if(!bFound) {
				ThreadLocalsHolder.getCollector().appendException(String.format(
						"UnknownFieldError: Template contains unknown field \"%s\"",
						sKey));
			}
		}
	}

	private void _validateVersion(String sVersion) {
		boolean bFound = false;
		for(String vtv: VALID_TEMPLATE_VERSIONS) {
			if(sVersion.equals(vtv)) {
				bFound = true;
				break;
			}
		}
		if(!bFound) {
			ThreadLocalsHolder.getCollector().appendException(String.format(
				"InvalidTemplateVersion: \"%s\" is invalid. Valid versions are %s",
				sVersion,VALID_TEMPLATE_VERSIONS.toString()));
		}
		else if(!sVersion.equals("tosca_simple_yaml_1_0")) {
			EntityType.updateDefinitions(sVersion);
		}
	}

	private String _getPath(String _path) throws JToscaException {
		if (_path.toLowerCase().endsWith(".yaml") || _path.toLowerCase().endsWith(".yml")) {
			return _path;
		} 
		else if (_path.toLowerCase().endsWith(".zip") || _path.toLowerCase().endsWith(".csar")) {
			// a CSAR archive
			CSAR csar = new CSAR(_path, isFile);
			if (csar.validate()) {
				try {
					csar.decompress();
					metaProperties = csar.getMetaProperties();
				} 
				catch (IOException e) {
					log.error("ToscaTemplate - _getPath - IOException trying to decompress {}", _path);
					return null;
				}
				isFile = true; // the file has been decompressed locally
				csar.cleanup();
				csarTempDir = csar.getTempDir();
				return csar.getTempDir() + File.separator + csar.getMainTemplate();
			}
		} 
		else {
			ThreadLocalsHolder.getCollector().appendException("ValueError: " + _path + " is not a valid file");
			return null;
		}
		return null;
	}

	private void verifyTemplate() throws JToscaException {
		ThreadLocalsHolder.getCollector().setWantTrace(false);

		//Warnings
		int warningsCount = ThreadLocalsHolder.getCollector().warningsCaught();
		if (warningsCount > 0) {
			List<String> warningsStrings = ThreadLocalsHolder.getCollector().getWarningsReport();
			log.warn("####################################################################################################");
			log.warn("CSAR Warnings found! CSAR name - {}", inputPath);
			log.warn("ToscaTemplate - verifyTemplate - {} Parsing Warning{} occurred...", warningsCount, (warningsCount > 1 ? "s" : ""));
			for (String s : warningsStrings) {
				log.warn("{}. CSAR name - {}", s, inputPath);
			}
			log.warn("####################################################################################################");
		}

		//Criticals
		int criticalsCount = ThreadLocalsHolder.getCollector().criticalsCaught();
		if (criticalsCount > 0) {
			List<String> criticalStrings = ThreadLocalsHolder.getCollector().getCriticalsReport();
			log.error("####################################################################################################");
			log.error("ToscaTemplate - verifyTemplate - {} Parsing Critical{} occurred...", criticalsCount, (criticalsCount > 1 ? "s" : ""));
			for (String s : criticalStrings) {
				log.error("{}. CSAR name - {}", s, inputPath);
			}
			throw new JToscaException(String.format("CSAR Validation Failed. CSAR name - {}. Please check logs for details.", inputPath), JToscaErrorCodes.CSAR_TOSCA_VALIDATION_ERROR.getValue());
		}
	}

	public String getPath() {
		return path;
	}

	public String getVersion() {
		return version;
	}
	
	public String getDescription() {
		return description;
	}
	
	public TopologyTemplate getTopologyTemplate() {
		return topologyTemplate;
	}
	
	public Metadata getMetaData() {
		return metaData;
	}
	
	public ArrayList<Input> getInputs() {
		return inputs;
	}
	
	public ArrayList<Output> getOutputs() {
		return outputs;
	}
	
	public ArrayList<Policy> getPolicies() {
		return policies;
	}
	
	public ArrayList<NodeTemplate> getNodeTemplates() {
		return nodeTemplates;
	}

	public LinkedHashMap<String, Object> getMetaProperties(String propertiesFile) {
		return metaProperties.get(propertiesFile);
	}
	
//	private boolean _isSubMappedNode(NodeTemplate nt,LinkedHashMap<String,Object> toscaTpl) {
//		// Return True if the nodetemple is substituted
//		if(nt != null && nt.getSubMappingToscaTemplate() == null &&
//				getSubMappingNodeType(toscaTpl).equals(nt.getType()) &&
//				nt.getInterfaces().size() < 1) {
//			return true;
//		}
//		return false;
//	}

	private boolean _isSubMappedNode(NodeTemplate nt, LinkedHashMap<String,Object> toscaTpl) {
		// Return True if the nodetemple is substituted
		if(nt != null && nt.getSubMappingToscaTemplate() == null &&
				getSubMappingNodeType(toscaTpl).equals(nt.getType()) &&
				nt.getInterfaces().size() < 1) {
			return true;
		}
		return false;
	}

	private LinkedHashMap<String,Object> _getParamsForNestedTemplate(NodeTemplate nt) {
		// Return total params for nested_template
		LinkedHashMap<String,Object> pparams;
		if(parsedParams != null) {
			pparams = parsedParams;
		}
		else {
			pparams = new LinkedHashMap<String,Object>();
		}
		if(nt != null) {
			for(String pname: nt.getProperties().keySet()) {
				pparams.put(pname,nt.getPropertyValue(pname));
			}
		}
		return pparams;
	}

	private String getSubMappingNodeType(LinkedHashMap<String,Object> toscaTpl) {
		// Return substitution mappings node type
		if(toscaTpl != null) {
			return TopologyTemplate.getSubMappingNodeType(
					(LinkedHashMap<String,Object>)toscaTpl.get(TOPOLOGY_TEMPLATE));
		}
		return null;
	}

	private boolean _hasSubstitutionMapping() {
        // Return True if the template has valid substitution mappings
        return topologyTemplate != null &&
            topologyTemplate.getSubstitutionMappings() != null;
	}

	public boolean hasNestedTemplates() {
        // Return True if the tosca template has nested templates
        return nestedToscaTemplatesWithTopology != null &&
        		nestedToscaTemplatesWithTopology.size() >= 1;
		
	}
	
	public ArrayList<TopologyTemplate> getNestedTemplates() {
		return nestedToscaTemplatesWithTopology;
	}

	@Override
	public String toString() {
		return "ToscaTemplate{" +
				"exttools=" + exttools +
				", VALID_TEMPLATE_VERSIONS=" + VALID_TEMPLATE_VERSIONS +
				", ADDITIONAL_SECTIONS=" + ADDITIONAL_SECTIONS +
				", isFile=" + isFile +
				", path='" + path + '\'' +
				", inputPath='" + inputPath + '\'' +
				", parsedParams=" + parsedParams +
				", tpl=" + tpl +
				", version='" + version + '\'' +
				", imports=" + imports +
				", relationshipTypes=" + relationshipTypes +
				", metaData=" + metaData +
				", description='" + description + '\'' +
				", topologyTemplate=" + topologyTemplate +
				", repositories=" + repositories +
				", inputs=" + inputs +
				", relationshipTemplates=" + relationshipTemplates +
				", nodeTemplates=" + nodeTemplates +
				", outputs=" + outputs +
				", policies=" + policies +
				", nestedToscaTplsWithTopology=" + nestedToscaTplsWithTopology +
				", nestedToscaTemplatesWithTopology=" + nestedToscaTemplatesWithTopology +
				", graph=" + graph +
				", csarTempDir='" + csarTempDir + '\'' +
				", nestingLoopCounter=" + nestingLoopCounter +
				'}';
	}
}

/*python

import logging
import os

from copy import deepcopy
from toscaparser.common.exception import ExceptionCollector.collector
from toscaparser.common.exception import InvalidTemplateVersion
from toscaparser.common.exception import MissingRequiredFieldError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.common.exception import ValidationError
from toscaparser.elements.entity_type import update_definitions
from toscaparser.extensions.exttools import ExtTools
import org.openecomp.sdc.toscaparser.api.imports
from toscaparser.prereq.csar import CSAR
from toscaparser.repositories import Repository
from toscaparser.topology_template import TopologyTemplate
from toscaparser.tpl_relationship_graph import ToscaGraph
from toscaparser.utils.gettextutils import _
import org.openecomp.sdc.toscaparser.api.utils.yamlparser


# TOSCA template key names
SECTIONS = (DEFINITION_VERSION, DEFAULT_NAMESPACE, TEMPLATE_NAME,
            TOPOLOGY_TEMPLATE, TEMPLATE_AUTHOR, TEMPLATE_VERSION,
            DESCRIPTION, IMPORTS, DSL_DEFINITIONS, NODE_TYPES,
            RELATIONSHIP_TYPES, RELATIONSHIP_TEMPLATES,
            CAPABILITY_TYPES, ARTIFACT_TYPES, DATA_TYPES, INTERFACE_TYPES,
            POLICY_TYPES, GROUP_TYPES, REPOSITORIES) = \
           ('tosca_definitions_version', 'tosca_default_namespace',
            'template_name', 'topology_template', 'template_author',
            'template_version', 'description', 'imports', 'dsl_definitions',
            'node_types', 'relationship_types', 'relationship_templates',
            'capability_types', 'artifact_types', 'data_types',
            'interface_types', 'policy_types', 'group_types', 'repositories')
# Sections that are specific to individual template definitions
SPECIAL_SECTIONS = (METADATA) = ('metadata')

log = logging.getLogger("tosca.model")

YAML_LOADER = toscaparser.utils.yamlparser.load_yaml


class ToscaTemplate(object):
    exttools = ExtTools()

    VALID_TEMPLATE_VERSIONS = ['tosca_simple_yaml_1_0']

    VALID_TEMPLATE_VERSIONS.extend(exttools.get_versions())

    ADDITIONAL_SECTIONS = {'tosca_simple_yaml_1_0': SPECIAL_SECTIONS}

    ADDITIONAL_SECTIONS.update(exttools.get_sections())

    '''Load the template data.'''
    def __init__(self, path=None, parsed_params=None, a_file=True,
                 yaml_dict_tpl=None):

        ExceptionCollector.collector.start()
        self.a_file = a_file
        self.input_path = None
        self.path = None
        self.tpl = None
        self.nested_tosca_tpls_with_topology = {}
        self.nested_tosca_templates_with_topology = []
        if path:
            self.input_path = path
            self.path = self._get_path(path)
            if self.path:
                self.tpl = YAML_LOADER(self.path, self.a_file)
            if yaml_dict_tpl:
                msg = (_('Both path and yaml_dict_tpl arguments were '
                         'provided. Using path and ignoring yaml_dict_tpl.'))
                log.info(msg)
                print(msg)
        else:
            if yaml_dict_tpl:
                self.tpl = yaml_dict_tpl
            else:
                ExceptionCollector.collector.appendException(
                    ValueError(_('No path or yaml_dict_tpl was provided. '
                                 'There is nothing to parse.')))

        if self.tpl:
            self.parsed_params = parsed_params
            self._validate_field()
            self.version = self._tpl_version()
            self.relationship_types = self._tpl_relationship_types()
            self.description = self._tpl_description()
            self.topology_template = self._topology_template()
            self.repositories = self._tpl_repositories()
            if self.topology_template.tpl:
                self.inputs = self._inputs()
                self.relationship_templates = self._relationship_templates()
                self.nodetemplates = self._nodetemplates()
                self.outputs = self._outputs()
                self._handle_nested_tosca_templates_with_topology()
                self.graph = ToscaGraph(self.nodetemplates)

        ExceptionCollector.collector.stop()
        self.verify_template()

    def _topology_template(self):
        return TopologyTemplate(self._tpl_topology_template(),
                                self._get_all_custom_defs(),
                                self.relationship_types,
                                self.parsed_params,
                                None)

    def _inputs(self):
        return self.topology_template.inputs

    def _nodetemplates(self):
        return self.topology_template.nodetemplates

    def _relationship_templates(self):
        return self.topology_template.relationship_templates

    def _outputs(self):
        return self.topology_template.outputs

    def _tpl_version(self):
        return self.tpl.get(DEFINITION_VERSION)

    def _tpl_description(self):
        desc = self.tpl.get(DESCRIPTION)
        if desc:
            return desc.rstrip()

    def _tpl_imports(self):
        return self.tpl.get(IMPORTS)

    def _tpl_repositories(self):
        repositories = self.tpl.get(REPOSITORIES)
        reposit = []
        if repositories:
            for name, val in repositories.items():
                reposits = Repository(name, val)
                reposit.append(reposits)
        return reposit

    def _tpl_relationship_types(self):
        return self._get_custom_types(RELATIONSHIP_TYPES)

    def _tpl_relationship_templates(self):
        topology_template = self._tpl_topology_template()
        return topology_template.get(RELATIONSHIP_TEMPLATES)

    def _tpl_topology_template(self):
        return self.tpl.get(TOPOLOGY_TEMPLATE)

    def _get_all_custom_defs(self, imports=None):
        types = [IMPORTS, NODE_TYPES, CAPABILITY_TYPES, RELATIONSHIP_TYPES,
                 DATA_TYPES, INTERFACE_TYPES, POLICY_TYPES, GROUP_TYPES]
        custom_defs_final = {}
        custom_defs = self._get_custom_types(types, imports)
        if custom_defs:
            custom_defs_final.update(custom_defs)
            if custom_defs.get(IMPORTS):
                import_defs = self._get_all_custom_defs(
                    custom_defs.get(IMPORTS))
                custom_defs_final.update(import_defs)

        # As imports are not custom_types, removing from the dict
        custom_defs_final.pop(IMPORTS, None)
        return custom_defs_final

    def _get_custom_types(self, type_definitions, imports=None):
        """Handle custom types defined in imported template files

        This method loads the custom type definitions referenced in "imports"
        section of the TOSCA YAML template.
        """
        custom_defs = {}
        type_defs = []
        if not isinstance(type_definitions, list):
            type_defs.append(type_definitions)
        else:
            type_defs = type_definitions

        if not imports:
            imports = self._tpl_imports()

        if imports:
            custom_service = toscaparser.imports.\
                ImportsLoader(imports, self.path,
                              type_defs, self.tpl)

            nested_tosca_tpls = custom_service.get_nested_tosca_tpls()
            self._update_nested_tosca_tpls_with_topology(nested_tosca_tpls)

            custom_defs = custom_service.get_custom_defs()
            if not custom_defs:
                return

        # Handle custom types defined in current template file
        for type_def in type_defs:
            if type_def != IMPORTS:
                inner_custom_types = self.tpl.get(type_def) or {}
                if inner_custom_types:
                    custom_defs.update(inner_custom_types)
        return custom_defs

    def _update_nested_tosca_tpls_with_topology(self, nested_tosca_tpls):
        for tpl in nested_tosca_tpls:
            filename, tosca_tpl = list(tpl.items())[0]
            if (tosca_tpl.get(TOPOLOGY_TEMPLATE) and
                filename not in list(
                    self.nested_tosca_tpls_with_topology.keys())):
                self.nested_tosca_tpls_with_topology.update(tpl)

    def _handle_nested_tosca_templates_with_topology(self):
        for fname, tosca_tpl in self.nested_tosca_tpls_with_topology.items():
            for nodetemplate in self.nodetemplates:
                if self._is_sub_mapped_node(nodetemplate, tosca_tpl):
                    parsed_params = self._get_params_for_nested_template(
                        nodetemplate)
                    topology_tpl = tosca_tpl.get(TOPOLOGY_TEMPLATE)
                    topology_with_sub_mapping = TopologyTemplate(
                        topology_tpl,
                        self._get_all_custom_defs(),
                        self.relationship_types,
                        parsed_params,
                        nodetemplate)
                    if topology_with_sub_mapping.substitution_mappings:
                        # Record nested topo templates in top level template
                        self.nested_tosca_templates_with_topology.\
                            append(topology_with_sub_mapping)
                        # Set substitution mapping object for mapped node
                        nodetemplate.sub_mapping_tosca_template = \
                            topology_with_sub_mapping.substitution_mappings

    def _validate_field(self):
        version = self._tpl_version()
        if not version:
            ExceptionCollector.collector.appendException(
                MissingRequiredFieldError(what='Template',
                                          required=DEFINITION_VERSION))
        else:
            self._validate_version(version)
            self.version = version

        for name in self.tpl:
            if (name not in SECTIONS and
               name not in self.ADDITIONAL_SECTIONS.get(version, ())):
                ExceptionCollector.collector.appendException(
                    UnknownFieldError(what='Template', field=name))

    def _validate_version(self, version):
        if version not in self.VALID_TEMPLATE_VERSIONS:
            ExceptionCollector.collector.appendException(
                InvalidTemplateVersion(
                    what=version,
                    valid_versions=', '. join(self.VALID_TEMPLATE_VERSIONS)))
        else:
            if version != 'tosca_simple_yaml_1_0':
                update_definitions(version)

    def _get_path(self, path):
        if path.lower().endswith(('.yaml','.yml')):
            return path
        elif path.lower().endswith(('.zip', '.csar')):
            # a CSAR archive
            csar = CSAR(path, self.a_file)
            if csar.validate():
                csar.decompress()
                self.a_file = True  # the file has been decompressed locally
                return os.path.join(csar.temp_dir, csar.get_main_template())
        else:
            ExceptionCollector.collector.appendException(
                ValueError(_('"%(path)s" is not a valid file.')
                           % {'path': path}))

    def verify_template(self):
        if ExceptionCollector.collector.exceptionsCaught():
            if self.input_path:
                raise ValidationError(
                    message=(_('\nThe input "%(path)s" failed validation with '
                               'the following error(s): \n\n\t')
                             % {'path': self.input_path}) +
                    '\n\t'.join(ExceptionCollector.collector.getExceptionsReport()))
            else:
                raise ValidationError(
                    message=_('\nThe pre-parsed input failed validation with '
                              'the following error(s): \n\n\t') +
                    '\n\t'.join(ExceptionCollector.collector.getExceptionsReport()))
        else:
            if self.input_path:
                msg = (_('The input "%(path)s" successfully passed '
                         'validation.') % {'path': self.input_path})
            else:
                msg = _('The pre-parsed input successfully passed validation.')

            log.info(msg)

    def _is_sub_mapped_node(self, nodetemplate, tosca_tpl):
        """Return True if the nodetemple is substituted."""
        if (nodetemplate and not nodetemplate.sub_mapping_tosca_template and
                self.get_sub_mapping_node_type(tosca_tpl) == nodetemplate.type
                and len(nodetemplate.interfaces) < 1):
            return True
        else:
            return False

    def _get_params_for_nested_template(self, nodetemplate):
        """Return total params for nested_template."""
        parsed_params = deepcopy(self.parsed_params) \
            if self.parsed_params else {}
        if nodetemplate:
            for pname in nodetemplate.get_properties():
                parsed_params.update({pname:
                                      nodetemplate.get_property_value(pname)})
        return parsed_params

    def get_sub_mapping_node_type(self, tosca_tpl):
        """Return substitution mappings node type."""
        if tosca_tpl:
            return TopologyTemplate.get_sub_mapping_node_type(
                tosca_tpl.get(TOPOLOGY_TEMPLATE))

    def _has_substitution_mappings(self):
        """Return True if the template has valid substitution mappings."""
        return self.topology_template is not None and \
            self.topology_template.substitution_mappings is not None

    def has_nested_templates(self):
        """Return True if the tosca template has nested templates."""
        return self.nested_tosca_templates_with_topology is not None and \
            len(self.nested_tosca_templates_with_topology) >= 1
*/