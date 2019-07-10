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

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.sdc.toscaparser.api.JToscaImportTest;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.ToscaTemplate;
import org.onap.sdc.toscaparser.api.common.JToscaException;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculatePropertyByPathTest {
    private static ToscaTemplate toscaTemplate;

    @BeforeClass
    public static void setUpClass() throws JToscaException {
        URL scarUrl = JToscaImportTest.class.getClassLoader().getResource("csars/service-NetworkCloudVnfServiceMock-csar.csar");
        if (scarUrl != null) {
            File file = new File(scarUrl.getFile());
            toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        }

    }

    @Test
    public void testGetPropertyWhenPropertyHasListOfDataTypesAndPathIsNotEmpty() throws JToscaException {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)         //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(0);       //testVM_testVM_SRIOVtrunk1_port

        Property property = cp.getProperties().get("related_networks");
        List<String> propertyValueList = property.getLeafPropertyValue("related_network_role");
        assertEquals(3, propertyValueList.size());
        assertTrue(propertyValueList.contains("cor_direct_2"));
        assertTrue(propertyValueList.contains("sgi_direct_2"));
        assertTrue(propertyValueList.contains("int_imbl_2"));
    }

    @Test
    public void testGetPropertyWhenPropertyHasDataTypeAndPathIsEmpty() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)         //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(1);       //testVM_testVM_SRIOVNonTrunk0_port

        Property property = cp.getProperties().get("exCP_naming");
        List<String> propertyValueList = property.getLeafPropertyValue("");
        assertTrue(propertyValueList.isEmpty());
    }

    @Test
    public void testGetPropertyWhenPropertyHasSimpleTypeAndValueAsGetInputIsNotResolvedCorrectlyAndPathIsEmpty() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)         //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(1);       //testVM_testVM_SRIOVNonTrunk0_port

        Property property = cp.getProperties().get("network");
        List<String> propertyValueList = property.getLeafPropertyValue("");
        assertTrue(propertyValueList.isEmpty());
    }

    @Test
    public void testGetPropertyWhenPropertyHasSimpleTypeAndPathIsEmpty() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)         //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(1);       //testVM_testVM_SRIOVNonTrunk0_port

        Property property = cp.getProperties().get("subinterface_indicator");
        List<String> propertyValueList = property.getLeafPropertyValue("");
        assertEquals(1, propertyValueList.size());
        assertEquals("false", propertyValueList.get(0));
    }


    @Test
    public void testGetPropertyWhenPropertyHasDataTypeAndPathIsNotEmpty() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)        //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(2);       //testVM_testVM_OVS_port

        Property property = cp.getProperties().get("ip_requirements");
        List<String> propertyValueList = property.getLeafPropertyValue("ip_version");
        assertEquals(1, propertyValueList.size());
        assertEquals("4", propertyValueList.get(0));
    }

    @Test
    public void testGetPropertyWhenPropertyHasListOfDataTypesAndPathIsNull() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)        //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(2);       //testVM_testVM_OVS_port

        Property property = cp.getProperties().get("ip_requirements");
        assertTrue(property.getLeafPropertyValue(null).isEmpty());
    }

    @Test
    public void testGetPropertyWhenPropertyHasListOfDataTypesAndPathIsComplex() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)        //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(0);       //testVM_testVM_SRIOVtrunk1_port

        Property property = cp.getProperties().get("ip_requirements");
        List<String> propertyValueList = property.getLeafPropertyValue("ip_count_required#is_required");
        assertEquals(1, propertyValueList.size());
        assertEquals("false", propertyValueList.get(0));
    }

    @Test
    public void testGetPropertyWhenPropertyHasListOfDataTypesAndPathIsWrong() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)        //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(0);       //testVM_testVM_SRIOVtrunk1_port

        Property property = cp.getProperties().get("ip_requirements");
        List<String> propertyValueList = property.getLeafPropertyValue("ip_count_required#is_required_1");
        assertEquals(0, propertyValueList.size());
    }

    @Test
    public void testGetPropertyWhenPropertyHasDataTypeWithoutSchemaAndComplexPath() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)        //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(0);       //testVM_testVM_SRIOVtrunk1_port

        Property property = cp.getProperties().get("mac_requirements");
        List<String> propertyValueList = property.getLeafPropertyValue("mac_count_required#is_required");
        assertEquals(1, propertyValueList.size());
        assertEquals("false", propertyValueList.get(0));
    }

    @Test
    public void testGetPropertyWhenPropertyHasDataTypeWithoutSchemaAndSimplePath() {
        NodeTemplate cp = toscaTemplate.getNodeTemplates().get(0)               //Network Cloud VNF MOCK 0
                .getSubMappingToscaTemplate().getNodeTemplates().get(0)        //abstract_testVM
                .getSubMappingToscaTemplate().getNodeTemplates().get(0);       //testVM_testVM_SRIOVtrunk1_port

        Property property = cp.getProperties().get("mac_requirements");
        List<String> propertyValueList = property.getLeafPropertyValue("mac_count_required");
        assertEquals(0, propertyValueList.size());
    }
}
