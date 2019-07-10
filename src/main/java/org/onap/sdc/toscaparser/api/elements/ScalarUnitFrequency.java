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

public class ScalarUnitFrequency extends ScalarUnit {

    private static final Long HZ = 1L;
    private static final Long KHZ = 1000L;
    private static final Long MHZ = 1000000L;
    private static final Long GHZ = 1000000000L;

    public ScalarUnitFrequency(Object value) {
        super(value);
        setScalarUnitDefault("GHz");
        putToScalarUnitDict("Hz", HZ);
        putToScalarUnitDict("kHz", KHZ);
        putToScalarUnitDict("MHz", MHZ);
        putToScalarUnitDict("GHz", GHZ);
    }

}
