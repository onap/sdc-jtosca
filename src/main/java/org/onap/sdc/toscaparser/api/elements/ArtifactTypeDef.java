package org.onap.sdc.toscaparser.api.elements;

import java.util.LinkedHashMap;

public class ArtifactTypeDef extends StatefulEntityType {
	
	private String type;
	private LinkedHashMap<String,Object> customDef;
	private LinkedHashMap<String,Object> properties;
	private LinkedHashMap<String,Object> parentArtifacts;
 


	public ArtifactTypeDef(String atype,LinkedHashMap<String,Object> _customDef) {
		super(atype,ARTIFACT_PREFIX,_customDef);

		type = atype;
		customDef = _customDef;
		properties = null;
		if(defs != null) {
			properties = (LinkedHashMap<String,Object>)defs.get(PROPERTIES);
		}
		parentArtifacts = _getParentArtifacts();
	}
	
	private LinkedHashMap<String,Object> _getParentArtifacts() {
		LinkedHashMap<String,Object> artifacts = new LinkedHashMap<>();
		String parentArtif = null;
		if(getParentType() != null) {
			parentArtif = getParentType().getType();
		}
		if(parentArtif != null && !parentArtif.isEmpty()) {
            while(!parentArtif.equals("tosca.artifacts.Root")) {
            	Object ob = TOSCA_DEF.get(parentArtif);
            	artifacts.put(parentArtif,ob);
                parentArtif = 
                	(String)((LinkedHashMap<String,Object>)ob).get("derived_from");
            }
		}
		return artifacts;
	}
	
	public ArtifactTypeDef getParentType() {
        // Return a artifact entity from which this entity is derived
        if(defs == null) {
        	return null;
        }
        String partifactEntity = derivedFrom(defs);
        if(partifactEntity != null) {
            return new ArtifactTypeDef(partifactEntity,customDef);
        }
        return null;
	}
	
	public Object getArtifact(String name) {
        // Return the definition of an artifact field by name
        if(defs != null) {
            return defs.get(name);
        }
        return null;
	}
	
	public String getType() {
		return type;
	}
	
}

/*python
class ArtifactTypeDef(StatefulEntityType):
    '''TOSCA built-in artifacts type.'''

    def __init__(self, atype, custom_def=None):
        super(ArtifactTypeDef, self).__init__(atype, self.ARTIFACT_PREFIX,
                                              custom_def)
        self.type = atype
        self.custom_def = custom_def
        self.properties = None
        if self.PROPERTIES in self.defs:
            self.properties = self.defs[self.PROPERTIES]
        self.parent_artifacts = self._get_parent_artifacts()

    def _get_parent_artifacts(self):
        artifacts = {}
        parent_artif = self.parent_type.type if self.parent_type else None
        if parent_artif:
            while parent_artif != 'tosca.artifacts.Root':
                artifacts[parent_artif] = self.TOSCA_DEF[parent_artif]
                parent_artif = artifacts[parent_artif]['derived_from']
        return artifacts

    @property
    def parent_type(self):
        '''Return a artifact entity from which this entity is derived.'''
        if not hasattr(self, 'defs'):
            return None
        partifact_entity = self.derived_from(self.defs)
        if partifact_entity:
            return ArtifactTypeDef(partifact_entity, self.custom_def)

    def get_artifact(self, name):
        '''Return the definition of an artifact field by name.'''
        if name in self.defs:
            return self.defs[name]
*/