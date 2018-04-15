package org.onap.sdc.toscaparser.api.parameters;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.enums.ToscaElementNames;

public class Annotation{
	
	private final static String HEAT = "HEAT";
	
	private String name;
	private String type;
	private ArrayList<Property> properties;
	
	public Annotation(){}
	@SuppressWarnings("unchecked")
	public Annotation(Map.Entry<String,Object> annotationEntry){
		if(annotationEntry != null){
			name = annotationEntry.getKey();
			Map<String, Object> annValue = (Map<String, Object>) annotationEntry.getValue();
			type = (String) annValue.get(ToscaElementNames.TYPE.getName());
			properties = fetchProperties((Map<String, Object>) annValue.get(ToscaElementNames.PROPERTIES.getName()));
		}
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public ArrayList<Property> getProperties() {
		return properties;
	}
	
	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}
	
	private ArrayList<Property> fetchProperties(Map<String, Object> properties) {
		if(properties != null){
			return (ArrayList<Property>) properties.entrySet().stream()
					.map(Property::new)
					.collect(Collectors.toList());
		}
		return null;
	}
	
	public boolean isHeatSourceType(){
		if(properties == null){
			return false;
		}
		Optional<Property> sourceType = properties.stream()
				.filter(p -> p.getName().equals(ToscaElementNames.SOURCE_TYPE.getName()))
				.findFirst();
		if(!sourceType.isPresent()){
			return false;
		}
		return sourceType.get().getValue() != null && ((String)sourceType.get().getValue()).equals(HEAT);
	}
	
}
