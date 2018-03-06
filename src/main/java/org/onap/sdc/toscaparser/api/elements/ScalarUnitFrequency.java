package org.onap.sdc.toscaparser.api.elements;

public class ScalarUnitFrequency extends ScalarUnit {

	public ScalarUnitFrequency(Object value) {
		super(value);
	    SCALAR_UNIT_DEFAULT = "GHz";
	    SCALAR_UNIT_DICT.put("Hz",1L);
	    SCALAR_UNIT_DICT.put("kHz",1000L);
	    SCALAR_UNIT_DICT.put("MHz",1000000L);
	    SCALAR_UNIT_DICT.put("GHz",1000000000L);
	}

}
