/*-
 * ============LICENSE_START=======================================================
 * Copyright (c) 2019 Fujitsu Limited.
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
package org.onap.sdc.toscaparser.api.functions;

import org.junit.Test;
import org.onap.sdc.toscaparser.api.*;
import org.onap.sdc.toscaparser.api.common.JToscaException;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class GetInputTest {

    private static final String TEST_FILENAME = "csars/listed_input.csar";
    private static final String TEST_FILENAME_NG = "csars/listed_input_ng.csar";
    private static final String TEST_PROPERTY_ROLE = "role";
    private static final String TEST_PROPERTY_LONGITUDE = "longitude";
    private static final String TEST_DEFAULT_VALUE = "dsvpn-hub";
    private static final String TEST_DESCRIPTION_VALUE = "This is used for SDWAN only";
    private static final String TEST_INPUT_TYPE = "type";
    private static final String TEST_INPUT_SCHEMA_TYPE = "tosca.datatypes.siteresource.site";
    private static final String TEST_TOSTRING = "get_input:[sites, 1, longitude]";
    private static final String TEST_INPUT_SITES = "sites";

    @Test
    public void validate() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource(TEST_FILENAME).getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null, false);
        NodeTemplate nodeTemplate = toscaTemplate.getNodeTemplates().get(1).getSubMappingToscaTemplate().getNodeTemplates().get(0);
        ArrayList<Input> inputs = toscaTemplate.getNodeTemplates().get(1).getSubMappingToscaTemplate().getInputs();
        LinkedHashMap<String, Property> properties = nodeTemplate.getProperties();
        assertThat(properties, notNullValue());
        assertThat(properties.size(), is(14));

        Property property = properties.get(TEST_PROPERTY_ROLE);
        assertThat(properties, notNullValue());
        assertThat(property.getName(), is(TEST_PROPERTY_ROLE));
        assertThat(property.getType(), is(Schema.STRING));
        assertThat(property.getDefault(), is(TEST_DEFAULT_VALUE));
        assertThat(property.getDescription(), is(TEST_DESCRIPTION_VALUE));
        GetInput getInput = (GetInput) property.getValue();
        assertThat(getInput.getEntrySchema().get(TEST_INPUT_TYPE).toString(), is(TEST_INPUT_SCHEMA_TYPE));

        property = properties.get(TEST_PROPERTY_LONGITUDE);
        assertThat(properties, notNullValue());
        assertThat(property.getName(), is(TEST_PROPERTY_LONGITUDE));
        assertThat(property.getValue().toString(), is(TEST_TOSTRING));
        getInput = (GetInput) property.getValue();
        ArrayList<Object> getInputArguments = getInput.getArguments();
        assertThat(getInputArguments.size(), is(3));
        assertThat(getInputArguments.get(0).toString(), is(TEST_INPUT_SITES));
        assertThat(getInputArguments.get(1).toString(), is("1"));
        assertThat(getInputArguments.get(2).toString(), is(TEST_PROPERTY_LONGITUDE));

        Input in = inputs.get(10);
        assertThat(in.getEntrySchema().get(TEST_INPUT_TYPE), is(TEST_INPUT_SCHEMA_TYPE));
        assertThat(in.getName(), is(TEST_INPUT_SITES));
        assertThat(in.getType(), is(Input.LIST));
    }

    @Test
    public void validate_ng() throws JToscaException {
        //invalid file
        String fileStr = JToscaImportTest.class.getClassLoader().getResource(TEST_FILENAME_NG).getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null, false);

        List<String> issues = ThreadLocalsHolder.getCollector().getValidationIssueReport();
        assertTrue(issues.stream().anyMatch(x -> x.contains("JE282")));
    }
}
