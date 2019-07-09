/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.toscaparser.api.elements;

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
