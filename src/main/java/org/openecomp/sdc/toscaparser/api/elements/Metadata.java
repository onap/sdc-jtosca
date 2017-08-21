package org.openecomp.sdc.toscaparser.api.elements;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Metadata {
	
	private final Map<String, Object> metadataMap;

	public Metadata(Map<String, Object> metadataMap) {
        this.metadataMap = metadataMap != null ? metadataMap : new HashMap<>();
    }

	public String getValue(String key)  {

		Object obj = this.metadataMap.get(key);
		if (obj != null){
			return String.valueOf(obj);
		}
		return null;
	}

	/**
	 * Get all properties of a Metadata object.<br>
	 * This object represents the "metadata" section of some entity.
	 * @return all properties of this Metadata, as a key-value.
	 */
	public Map<String, String> getAllProperties()  {
		return metadataMap.entrySet().stream().map(e-> new AbstractMap.SimpleEntry<String, String>(e.getKey(), String.valueOf(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
	}

	@Override
	public String toString() {
		return "Metadata{" +
				"metadataMap=" + metadataMap +
				'}';
	}

}
