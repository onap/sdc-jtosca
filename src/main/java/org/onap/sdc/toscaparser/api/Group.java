package org.onap.sdc.toscaparser.api;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.ValidateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Group extends EntityTemplate {
	
	private static final String TYPE = "type";
	private static final String METADATA = "metadata";
	private static final String DESCRIPTION = "description";
	private static final String PROPERTIES = "properties";
	private static final String MEMBERS = "members";
	private static final String INTERFACES = "interfaces";
	private static final String SECTIONS[] = {
			TYPE, METADATA, DESCRIPTION, PROPERTIES, MEMBERS, INTERFACES};

	private String name;
	LinkedHashMap<String,Object> tpl; 
	ArrayList<NodeTemplate> memberNodes;
	LinkedHashMap<String,Object> customDef;
	Metadata metaData;


	public Group(String _name, LinkedHashMap<String, Object> _templates,
				 ArrayList<NodeTemplate> _memberNodes,
				 LinkedHashMap<String, Object> _customDef){
		this(_name, _templates, _memberNodes, _customDef, null);
	}

	public Group(String _name, LinkedHashMap<String, Object> _templates, 
					ArrayList<NodeTemplate> _memberNodes,
					LinkedHashMap<String, Object> _customDef, NodeTemplate parentNodeTemplate) {
		super(_name, _templates, "group_type", _customDef, parentNodeTemplate);

		name = _name;
        tpl = _templates;
        if(tpl.get(METADATA) != null) {
        	Object metadataObject = tpl.get(METADATA);
        	ValidateUtils.validateMap(metadataObject);
        	metaData = new Metadata((Map<String,Object>)metadataObject);
        }
        memberNodes = _memberNodes;
        _validateKeys();
        getCapabilities();
	}

	public Metadata getMetadata() {
		return metaData;
	}
	
	public ArrayList<String> getMembers() {
		return (ArrayList<String>)entityTpl.get("members");
	}
 
	public String getDescription() {
		return (String)entityTpl.get("description");
		
	}

	public ArrayList<NodeTemplate> getMemberNodes() {
		return memberNodes;
	}

	private void _validateKeys() {
		for(String key: entityTpl.keySet()) {
    		boolean bFound = false;
    		for(String sect: SECTIONS) {
    			if(key.equals(sect)) {
    				bFound = true;
    				break;
    			}
    		}
    		if(!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE183", String.format(
                        "UnknownFieldError: Groups \"%s\" contains unknown field \"%s\"",
                        name,key))); 
    		}
		}
	}

	@Override
	public String toString() {
		return "Group{" +
				"name='" + name + '\'' +
				", tpl=" + tpl +
				", memberNodes=" + memberNodes +
				", customDef=" + customDef +
				", metaData=" + metaData +
				'}';
	}
	
	public int compareTo(Group other){
		if(this.equals(other))
			return 0;
		return this.getName().compareTo(other.getName()) == 0 ? this.getType().compareTo(other.getType()) : this.getName().compareTo(other.getName());
	}
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.entity_template import EntityTemplate
from toscaparser.utils import validateutils

SECTIONS = (TYPE, METADATA, DESCRIPTION, PROPERTIES, MEMBERS, INTERFACES) = \
           ('type', 'metadata', 'description',
            'properties', 'members', 'interfaces')


class Group(EntityTemplate):

    def __init__(self, name, group_templates, member_nodes, custom_defs=None):
        super(Group, self).__init__(name,
                                    group_templates,
                                    'group_type',
                                    custom_defs)
        self.name = name
        self.tpl = group_templates
        self.meta_data = None
        if self.METADATA in self.tpl:
            self.meta_data = self.tpl.get(self.METADATA)
            validateutils.validate_map(self.meta_data)
        self.member_nodes = member_nodes
        self._validate_keys()

    @property
    def members(self):
        return self.entity_tpl.get('members')

    @property
    def description(self):
        return self.entity_tpl.get('description')

    def get_member_nodes(self):
        return self.member_nodes

    def _validate_keys(self):
        for key in self.entity_tpl.keys():
            if key not in SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Groups "%s"' % self.name,
                                      field=key))
*/