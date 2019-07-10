/*-
 * ============LICENSE_START=======================================================
 * Copyright (c) 2017 AT&T Intellectual Property.
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
 * Modifications copyright (c) 2019 Fujitsu Limited.
 * ================================================================================
 */
package org.onap.sdc.toscaparser.api;

import org.junit.Test;
import org.onap.sdc.toscaparser.api.common.JToscaException;
import org.onap.sdc.toscaparser.api.elements.DataType;
import org.onap.sdc.toscaparser.api.elements.PropertyDef;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;
import org.onap.sdc.toscaparser.api.parameters.Annotation;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

public class JToscaImportTest {

    @Test
    public void testNoMissingTypeValidationError() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/sdc-onboarding_csar.csar")
                .getFile();
        File file = new File(fileStr);
        new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        List<String> missingTypeErrors = ThreadLocalsHolder.getCollector().getValidationIssueReport().stream()
                .filter(s -> s.contains("JE136")).collect(Collectors.toList());
        assertEquals(0, missingTypeErrors.size());
    }

    @Test
    public void testNoStackOverFlowError() {
        Exception jte = null;
        try {
            String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/sdc-onboarding_csar.csar")
                    .getFile();
            File file = new File(fileStr);
            new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        } catch (Exception e) {
            jte = e;
        }
        assertEquals(null, jte);
    }

    @Test
    public void testNoInvalidImports() throws JToscaException {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("csars/tmpCSAR_Huawei_vSPGW_fixed.csar");
        fileNames.add("csars/sdc-onboarding_csar.csar");
        fileNames.add("csars/resource-Spgw-csar-ZTE.csar");

        for (String fileName : fileNames) {
            String fileStr = JToscaImportTest.class.getClassLoader().getResource(fileName).getFile();
            File file = new File(fileStr);
            new ToscaTemplate(file.getAbsolutePath(), null, true, null);
            List<String> invalidImportErrors = ThreadLocalsHolder.getCollector().getValidationIssueReport().stream()
                    .filter(s -> s.contains("JE195")).collect(Collectors.toList());
            assertEquals(0, invalidImportErrors.size());
        }
    }

    @Test
    public void testParseAnnotations() throws JToscaException {

        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-AdiodVmxVpeBvService-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);

        List<Input> inputs = toscaTemplate.getInputs();
        assertNotNull(inputs);
        assertTrue(inputs.stream().filter(i -> i.getAnnotations() != null).collect(Collectors.toList()).isEmpty());

        inputs.forEach(Input::parseAnnotations);
        assertTrue(!inputs.stream().filter(i -> i.getAnnotations() != null).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void testGetInputsWithAndWithoutAnnotations() throws JToscaException {

        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-AdiodVmxVpeBvService-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        List<Input> inputs = toscaTemplate.getInputs();
        assertNotNull(inputs);
        assertTrue(inputs.stream().filter(i -> i.getAnnotations() != null).collect(Collectors.toList()).isEmpty());

        inputs = toscaTemplate.getInputs(true);
        assertNotNull(inputs);
        validateInputsAnnotations(inputs);

        inputs = toscaTemplate.getInputs(false);
        assertNotNull(inputs);
        assertTrue(inputs.stream().filter(i -> i.getAnnotations() != null).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void testGetPropertyNameTest() throws JToscaException {

        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-AdiodVmxVpeBvService-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        NodeTemplate nodeTemplate = toscaTemplate.getNodeTemplates().get(0);

        ArrayList<String> valueList = (ArrayList<String>) nodeTemplate.getPropertyValueFromTemplatesByName("vmxvpfe_sriov41_0_port_vlanfilter");
        assertEquals(4, valueList.size());

        assertEquals("vPE", (String) nodeTemplate.getPropertyValueFromTemplatesByName("nf_role"));

        assertNull(nodeTemplate.getPropertyValueFromTemplatesByName("test"));
    }

    @Test
    public void testGetParentNodeTemplateTest() throws JToscaException {

        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-AdiodVmxVpeBvService-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        NodeTemplate nodeTemplate = toscaTemplate.getNodeTemplates().get(0);
        //parent of this VF is service (null)
        assertNull(nodeTemplate.getParentNodeTemplate());
        List<NodeTemplate> children = nodeTemplate.getSubMappingToscaTemplate().getNodeTemplates();
        assertFalse(children.isEmpty());
        NodeTemplate cVFC = children.get(4);
        //parent is the VF above
        assertEquals("2017-488_ADIOD-vPE 0", cVFC.getParentNodeTemplate().getName());
        List<NodeTemplate> children1 = cVFC.getSubMappingToscaTemplate().getNodeTemplates();
        assertFalse(children1.isEmpty());
        //parent is the CVFC above
        assertEquals(cVFC, children1.get(0).getParentNodeTemplate());

/*

		TopologyTemplate tt = nodeTemplate.getOriginComponentTemplate();
		List<Group> groups = tt.getGroups();
		List<Policy> policies = tt.getPolicies();

		TopologyTemplate tt1 = cVFC.getOriginComponentTemplate();
		groups = tt.getGroups();
		policies = tt.getPolicies();
*/

    }

    @Test
    public void testNullValueHasNoNullPointerException() throws JToscaException {

        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-JennyVtsbcKarunaSvc-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        List<Input> inputs = toscaTemplate.getInputs();
        assertNotNull(inputs);
    }

    @Test
    public void testGetPolicyMetadata() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-NetworkCloudVnfServiceMock-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        ArrayList<Policy> policies = toscaTemplate.getPolicies();
        assertNotNull(policies);
        assertEquals(1, policies.size());
        assertEquals("org.openecomp.policies.External", policies.get(0).getType());
        assertEquals("adf03496-bf87-43cf-b20a-450e47cb44bd", policies.get(0).getMetaData().getOrDefault("UUID", "").toString());
        assertTrue(policies.get(0).getMetaData().getOrDefault("UUID_test", "").toString().isEmpty());
    }

    @Test
    public void testGetPolicyMetadataObj() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource("csars/service-NetworkCloudVnfServiceMock-csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        ArrayList<Policy> policies = toscaTemplate.getPolicies();
        assertNotNull(policies);
        assertEquals(1, policies.size());
        assertEquals("adf03496-bf87-43cf-b20a-450e47cb44bd", policies.get(0).getMetaDataObj().getAllProperties().getOrDefault("UUID", "").toString());
        assertTrue(policies.get(0).getMetaDataObj().getAllProperties().getOrDefault("name_test", "").toString().isEmpty());
    }

    private void validateInputsAnnotations(List<Input> inputs) {
        List<Input> inputsWithAnnotations = inputs.stream().filter(i -> i.getAnnotations() != null)
                .collect(Collectors.toList());
        assertTrue(!inputs.isEmpty());
        inputsWithAnnotations.stream().forEach(i -> validateAnnotations(i));
    }

    private void validateAnnotations(Input input) {
        assertNotNull(input.getAnnotations());
        assertEquals(input.getAnnotations().size(), 1);
        Annotation annotation = input.getAnnotations().get("source");
        assertEquals(annotation.getName(), "source");
        assertEquals(annotation.getType().toLowerCase(), "org.openecomp.annotations.source");
        assertNotNull(annotation.getProperties());
        Optional<Property> source_type = annotation.getProperties().stream()
                .filter(p -> p.getName().equals("source_type")).findFirst();
        assertTrue(source_type.isPresent());
        assertEquals(source_type.get().getValue(), "HEAT");
    }

    private static final String TEST_DATATYPE_FILENAME = "csars/dataTypes-test-service.csar";
    private static final String TEST_DATATYPE_TEST1 = "TestType1";
    private static final String TEST_DATATYPE_TEST2 = "TestType2";
    private static final String TEST_DATATYPE_PROPERTY_STR = "strdata";
    private static final String TEST_DATATYPE_PROPERTY_INT = "intdata";
    private static final String TEST_DATATYPE_PROPERTY_LIST = "listdata";
    private static final String TEST_DATATYPE_PROPERTY_TYPE = "type";
    private static final String TEST_DATATYPE_PROPERTY_ENTRY_SCHEMA = "entry_schema";
    private static final String TEST_DATATYPE_TOSTRING = "data_types=";

    @Test
    public void testGetDataType() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource(TEST_DATATYPE_FILENAME).getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        HashSet<DataType> dataTypes = toscaTemplate.getDataTypes();
        assertThat(dataTypes, notNullValue());
        assertThat(dataTypes.size(), is(2));

        for (DataType dataType : dataTypes) {
            LinkedHashMap<String, PropertyDef> properties;
            PropertyDef property;
            if (dataType.getType().equals(TEST_DATATYPE_TEST1)) {
                properties = dataType.getAllProperties();
                property = properties.get(TEST_DATATYPE_PROPERTY_STR);
                assertThat(property, notNullValue());
                assertThat(property.getName(), is(TEST_DATATYPE_PROPERTY_STR));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_TYPE), is(Schema.STRING));
            }
            if (dataType.getType().equals(TEST_DATATYPE_TEST2)) {
                properties = dataType.getAllProperties();
                property = properties.get(TEST_DATATYPE_PROPERTY_INT);
                assertThat(property, notNullValue());
                assertThat(property.getName(), is(TEST_DATATYPE_PROPERTY_INT));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_TYPE), is(Schema.INTEGER));

                property = properties.get(TEST_DATATYPE_PROPERTY_LIST);
                assertThat(property, notNullValue());
                assertThat(property.getName(), is(TEST_DATATYPE_PROPERTY_LIST));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_TYPE), is(Schema.LIST));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_ENTRY_SCHEMA), is(TEST_DATATYPE_TEST1));

                assertThat((LinkedHashMap<String, Object>) toscaTemplate.getTopologyTemplate().getCustomDefs().get(TEST_DATATYPE_TEST1), notNullValue());
                assertThat((LinkedHashMap<String, Object>) toscaTemplate.getTopologyTemplate().getCustomDefs().get(TEST_DATATYPE_TEST2), notNullValue());
                assertThat(toscaTemplate.toString(), containsString(TEST_DATATYPE_TOSTRING));
            }
        }

    }

    @Test
    public void testGetInputValidate() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource(TEST_DATATYPE_FILENAME).getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        HashSet<DataType> dataTypes = toscaTemplate.getDataTypes();
        assertThat(dataTypes, notNullValue());
        assertThat(dataTypes.size(), is(2));

        for (DataType dataType : dataTypes) {
            LinkedHashMap<String, PropertyDef> properties;
            PropertyDef property;
            if (dataType.getType().equals(TEST_DATATYPE_TEST1)) {
                properties = dataType.getAllProperties();
                property = properties.get(TEST_DATATYPE_PROPERTY_STR);
                assertThat(property, notNullValue());
                assertThat(property.getName(), is(TEST_DATATYPE_PROPERTY_STR));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_TYPE), is(Schema.STRING));
            }
            if (dataType.getType().equals(TEST_DATATYPE_TEST2)) {
                properties = dataType.getAllProperties();
                property = properties.get(TEST_DATATYPE_PROPERTY_INT);
                assertThat(property, notNullValue());
                assertThat(property.getName(), is(TEST_DATATYPE_PROPERTY_INT));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_TYPE), is(Schema.INTEGER));

                property = properties.get(TEST_DATATYPE_PROPERTY_LIST);
                assertThat(property, notNullValue());
                assertThat(property.getName(), is(TEST_DATATYPE_PROPERTY_LIST));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_TYPE), is(Schema.LIST));
                assertThat(property.getSchema().get(TEST_DATATYPE_PROPERTY_ENTRY_SCHEMA), is(TEST_DATATYPE_TEST1));

                assertThat((LinkedHashMap<String, Object>) toscaTemplate.getTopologyTemplate().getCustomDefs().get(TEST_DATATYPE_TEST1), notNullValue());
                assertThat((LinkedHashMap<String, Object>) toscaTemplate.getTopologyTemplate().getCustomDefs().get(TEST_DATATYPE_TEST2), notNullValue());
                assertThat(toscaTemplate.toString(), containsString(TEST_DATATYPE_TOSTRING));
            }
        }
    }
}
