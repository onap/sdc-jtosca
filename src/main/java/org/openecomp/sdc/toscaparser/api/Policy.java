package org.openecomp.sdc.toscaparser.api;

import org.openecomp.sdc.toscaparser.api.common.JToscaValidationIssue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.openecomp.sdc.toscaparser.api.utils.ValidateUtils;

public class Policy extends EntityTemplate {
	

	private static final String TYPE = "type";
	private static final String METADATA = "metadata";
	private static final String DESCRIPTION = "description";
	private static final String PROPERTIES = "properties";
	private static final String TARGETS = "targets";
	private static final String TRIGGERS = "triggers";
	private static final String SECTIONS[] = {
			TYPE, METADATA, DESCRIPTION, PROPERTIES, TARGETS, TRIGGERS};
	
	LinkedHashMap<String,Object> metaData;
	ArrayList<Object> targetsList; // *** a list of NodeTemplate OR a list of Group ***
	String targetsType;
	ArrayList<Object> triggers;
	LinkedHashMap<String,Object> properties;
	
	public Policy(String _name,
				  LinkedHashMap<String,Object> _policy,
//				  ArrayList<NodeTemplate> targetObjects,
				  ArrayList<Object> targetObjects,
				  String _targetsType,
				  LinkedHashMap<String,Object> _customDef) {
		super(_name,_policy,"policy_type",_customDef);

        metaData = null;
        if(_policy.get(METADATA) != null) {
        	metaData = (LinkedHashMap<String,Object>)_policy.get(METADATA);
        	ValidateUtils.validateMap(metaData);
        }

        targetsList = targetObjects;
        targetsType = _targetsType;
        triggers = _triggers((LinkedHashMap<String,Object>)_policy.get(TRIGGERS));
        properties = null;
        if(_policy.get("properties") != null) {
        	properties = (LinkedHashMap<String,Object>)_policy.get("properties");
        }
        _validateKeys();
	}

	public ArrayList<String> getTargets() {
		return (ArrayList<String>)entityTpl.get("targets");
	}

	public ArrayList<String> getDescription() {
		return (ArrayList<String>)entityTpl.get("description");
	}

	public ArrayList<String> getmetadata() {
		return (ArrayList<String>)entityTpl.get("metadata");
	}

	public String getTargetsType() {
		return targetsType;
	}
 
//	public ArrayList<NodeTemplate> getTargetsList() {
	public ArrayList<Object> getTargetsList() {
		return targetsList;
	}
	
	// entityTemplate already has a different getProperties...
	// this is to access the local properties variable
	public LinkedHashMap<String,Object> getPolicyProperties() {
		return properties;
	}
	
	private ArrayList<Object> _triggers(LinkedHashMap<String,Object> triggers) {
		ArrayList<Object> triggerObjs = new ArrayList<>();
		if(triggers != null) {
			for(Map.Entry<String,Object> me: triggers.entrySet()) {
				String tname = me.getKey();
				LinkedHashMap<String,Object> ttriggerTpl = 
						(LinkedHashMap<String,Object>)me.getValue();
				Triggers triggersObj = new Triggers(tname,ttriggerTpl);
                triggerObjs.add(triggersObj);
			}
		}
		return triggerObjs;
	}

	private void _validateKeys() {	
		for(String key: entityTpl.keySet()) {
			boolean bFound = false;
			for(int i=0; i<SECTIONS.length; i++) {
				if(key.equals(SECTIONS[i])) {
					bFound = true;
					break;
				}
			}
			if(!bFound) {
	            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE219", String.format(
	                    "UnknownFieldError: Policy \"%s\" contains unknown field \"%s\"",
	                    name,key))); 
			}
		}
	}

	@Override
	public String toString() {
		return "Policy{" +
				"metaData=" + metaData +
				", targetsList=" + targetsList +
				", targetsType='" + targetsType + '\'' +
				", triggers=" + triggers +
				", properties=" + properties +
				'}';
	}
	
	public int compareTo(Policy other){
		if(this.equals(other))
			return 0;
		return this.getName().compareTo(other.getName()) == 0 ? this.getType().compareTo(other.getType()) : this.getName().compareTo(other.getName());
	}
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.entity_template import EntityTemplate
from toscaparser.triggers import Triggers
from toscaparser.utils import validateutils


SECTIONS = (TYPE, METADATA, DESCRIPTION, PROPERTIES, TARGETS, TRIGGERS) = \
           ('type', 'metadata', 'description',
            'properties', 'targets', 'triggers')

log = logging.getLogger('tosca')


class Policy(EntityTemplate):
    '''Policies defined in Topology template.'''
    def __init__(self, name, policy, targets, targets_type, custom_def=None):
        super(Policy, self).__init__(name,
                                     policy,
                                     'policy_type',
                                     custom_def)
        self.meta_data = None
        if self.METADATA in policy:
            self.meta_data = policy.get(self.METADATA)
            validateutils.validate_map(self.meta_data)
        self.targets_list = targets
        self.targets_type = targets_type
        self.triggers = self._triggers(policy.get(TRIGGERS))
        self._validate_keys()

    @property
    def targets(self):
        return self.entity_tpl.get('targets')

    @property
    def description(self):
        return self.entity_tpl.get('description')

    @property
    def metadata(self):
        return self.entity_tpl.get('metadata')

    def get_targets_type(self):
        return self.targets_type

    def get_targets_list(self):
        return self.targets_list

    def _triggers(self, triggers):
        triggerObjs = []
        if triggers:
            for name, trigger_tpl in triggers.items():
                triggersObj = Triggers(name, trigger_tpl)
                triggerObjs.append(triggersObj)
        return triggerObjs

    def _validate_keys(self):
        for key in self.entity_tpl.keys():
            if key not in SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Policy "%s"' % self.name,
                                      field=key))
*/