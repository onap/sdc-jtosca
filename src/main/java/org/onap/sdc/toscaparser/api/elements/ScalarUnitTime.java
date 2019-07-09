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
