package org.openecomp.sdc.toscaparser.api.elements;

public class ScalarUnitSize extends ScalarUnit {

	public ScalarUnitSize(Object value) {
		super(value);
		
	    SCALAR_UNIT_DEFAULT = "B";
	    SCALAR_UNIT_DICT.put("B",1L);
	    SCALAR_UNIT_DICT.put("kB",1000L);
	    SCALAR_UNIT_DICT.put("kiB",1024L);
	    SCALAR_UNIT_DICT.put("MB",1000000L);
	    SCALAR_UNIT_DICT.put("MiB",1048576L);
	    SCALAR_UNIT_DICT.put("GB",1000000000L);
	    SCALAR_UNIT_DICT.put("GiB",1073741824L);
	    SCALAR_UNIT_DICT.put("TB",1000000000000L);
	    SCALAR_UNIT_DICT.put("TiB",1099511627776L);
	}
}
