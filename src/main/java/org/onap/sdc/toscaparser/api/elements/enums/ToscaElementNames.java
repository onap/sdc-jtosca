package org.onap.sdc.toscaparser.api.elements.enums;

public enum ToscaElementNames {
	
	TYPE ("type"),
	PROPERTIES ("properties"),
	ANNOTATIONS ("annotations"),
	SOURCE_TYPE ("source_type");
	
	private String name;
	
	ToscaElementNames(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
