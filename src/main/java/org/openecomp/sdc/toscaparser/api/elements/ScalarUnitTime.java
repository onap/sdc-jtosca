package org.openecomp.sdc.toscaparser.api.elements;

public class ScalarUnitTime extends ScalarUnit {

	public ScalarUnitTime(Object value) {
		super(value);
	    SCALAR_UNIT_DEFAULT = "ms";
	    SCALAR_UNIT_DICT.put("d",86400L);
	    SCALAR_UNIT_DICT.put("h",3600L);
	    SCALAR_UNIT_DICT.put("m",60L);
	    SCALAR_UNIT_DICT.put("s",1L);
	    SCALAR_UNIT_DICT.put("ms",0.001);
	    SCALAR_UNIT_DICT.put("us",0.000001);
	    SCALAR_UNIT_DICT.put("ns",0.000000001);
	}

}
