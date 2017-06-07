package org.openecomp.sdc.toscaparser.api.elements;

import java.util.Map;

public class Metadata {
	
	private final Map<String, Object> metadataMap;

	public Metadata(Map<String, Object> metadataMap) {
        this.metadataMap = metadataMap;
    }

	public String getValue(String key)  {
		return !isEmpty() ? String.valueOf(this.metadataMap.get(key)) : null;
	}
	
	public void setValue(String key, Object value)  {
		if (!isEmpty())  {
			this.metadataMap.put(key, value);
		}
	}


	private boolean isEmpty() {
		return this.metadataMap == null || this.metadataMap.size() == 0;
	}

	@Override
	public String toString() {
		return "Metadata{" +
				"metadataMap=" + metadataMap +
				'}';
	}

}
