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

import org.onap.sdc.toscaparser.api.elements.enums.FileSize;

public class ScalarUnitSize extends ScalarUnit {



    public ScalarUnitSize(Object value) {
        super(value);

        setScalarUnitDefault("B");
        putToScalarUnitDict("B", FileSize.B);
        putToScalarUnitDict("kB", FileSize.KB);
        putToScalarUnitDict("MB", FileSize.MB);
        putToScalarUnitDict("GB", FileSize.GB);
        putToScalarUnitDict("TB", FileSize.TB);
        putToScalarUnitDict("kiB", FileSize.KIB);
        putToScalarUnitDict("MiB", FileSize.MIB);
        putToScalarUnitDict("GiB", FileSize.GIB);
        putToScalarUnitDict("TiB", FileSize.TIB);
    }
}
