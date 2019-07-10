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

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EntityTypeTest {

    private static final Map<String, Object> origMap = EntityType.TOSCA_DEF;

    @Test
    public void testUpdateDefinitions() throws Exception {

        Map<String, Object> testData = new HashMap<>();
        testData.put("tosca.nodes.nfv.VNF", "{derived_from=tosca.nodes.Root, properties={id={type=string, description=ID of this VNF}, vendor={type=string, description=name of the vendor who generate this VNF}, version={type=version, description=version of the software for this VNF}}, requirements=[{virtualLink={capability=tosca.capabilities.nfv.VirtualLinkable, relationship=tosca.relationships.nfv.VirtualLinksTo, node=tosca.nodes.nfv.VL}}]}");
        testData.put("tosca.nodes.nfv.VDU", "{derived_from=tosca.nodes.Compute, capabilities={high_availability={type=tosca.capabilities.nfv.HA}, virtualbinding={type=tosca.capabilities.nfv.VirtualBindable}, monitoring_parameter={type=tosca.capabilities.nfv.Metric}}, requirements=[{high_availability={capability=tosca.capabilities.nfv.HA, relationship=tosca.relationships.nfv.HA, node=tosca.nodes.nfv.VDU, occurrences=[0, 1]}}]}");
        testData.put("tosca.nodes.nfv.CP", "{derived_from=tosca.nodes.network.Port, properties={type={type=string, required=false}}, requirements=[{virtualLink={capability=tosca.capabilities.nfv.VirtualLinkable, relationship=tosca.relationships.nfv.VirtualLinksTo, node=tosca.nodes.nfv.VL}}, {virtualBinding={capability=tosca.capabilities.nfv.VirtualBindable, relationship=tosca.relationships.nfv.VirtualBindsTo, node=tosca.nodes.nfv.VDU}}], attributes={address={type=string}}}");
        testData.put("tosca.nodes.nfv.VL", "{derived_from=tosca.nodes.network.Network, properties={vendor={type=string, required=true, description=name of the vendor who generate this VL}}, capabilities={virtual_linkable={type=tosca.capabilities.nfv.VirtualLinkable}}}");
        testData.put("tosca.nodes.nfv.VL.ELine", "{derived_from=tosca.nodes.nfv.VL, capabilities={virtual_linkable={occurrences=2}}}");
        testData.put("tosca.nodes.nfv.VL.ELAN", "{derived_from=tosca.nodes.nfv.VL}");
        testData.put("tosca.nodes.nfv.VL.ETree", "{derived_from=tosca.nodes.nfv.VL}");
        testData.put("tosca.nodes.nfv.FP", "{derived_from=tosca.nodes.Root, properties={policy={type=string, required=false, description=name of the vendor who generate this VL}}, requirements=[{forwarder={capability=tosca.capabilities.nfv.Forwarder, relationship=tosca.relationships.nfv.ForwardsTo}}]}");
        testData.put("tosca.groups.nfv.VNFFG", "{derived_from=tosca.groups.Root, properties={vendor={type=string, required=true, description=name of the vendor who generate this VNFFG}, version={type=string, required=true, description=version of this VNFFG}, number_of_endpoints={type=integer, required=true, description=count of the external endpoints included in this VNFFG}, dependent_virtual_link={type=list, entry_schema={type=string}, required=true, description=Reference to a VLD  used in this Forwarding Graph}, connection_point={type=list, entry_schema={type=string}, required=true, description=Reference to Connection Points forming the VNFFG}, constituent_vnfs={type=list, entry_schema={type=string}, required=true, description=Reference to a list of  VNFD used in this VNF Forwarding Graph}}}");
        testData.put("tosca.relationships.nfv.VirtualLinksTo", "{derived_from=tosca.relationships.network.LinksTo, valid_target_types=[tosca.capabilities.nfv.VirtualLinkable]}");
        testData.put("tosca.relationships.nfv.VirtualBindsTo", "{derived_from=tosca.relationships.network.BindsTo, valid_target_types=[tosca.capabilities.nfv.VirtualBindable]}");
        testData.put("tosca.relationships.nfv.HA", "{derived_from=tosca.relationships.Root, valid_target_types=[tosca.capabilities.nfv.HA]}");
        testData.put("tosca.relationships.nfv.Monitor", "{derived_from=tosca.relationships.ConnectsTo, valid_target_types=[tosca.capabilities.nfv.Metric]}");
        testData.put("tosca.relationships.nfv.ForwardsTo", "{derived_from=tosca.relationships.root, valid_target_types=[tosca.capabilities.nfv.Forwarder]}");
        testData.put("tosca.capabilities.nfv.VirtualLinkable", "{derived_from=tosca.capabilities.network.Linkable}");
        testData.put("tosca.capabilities.nfv.VirtualBindable", "{derived_from=tosca.capabilities.network.Bindable}");
        testData.put("tosca.capabilities.nfv.HA", "{derived_from=tosca.capabilities.Root, valid_source_types=[tosca.nodes.nfv.VDU]}");
        testData.put("tosca.capabilities.nfv.HA.ActiveActive", "{derived_from=tosca.capabilities.nfv.HA}");
        testData.put("tosca.capabilities.nfv.HA.ActivePassive", "{derived_from=tosca.capabilities.nfv.HA}");
        testData.put("tosca.capabilities.nfv.Metric", "{derived_from=tosca.capabilities.Root}");
        testData.put("tosca.capabilities.nfv.Forwarder", "{derived_from=tosca.capabilities.Root}");

        Map<String, Object> expectedDefMap = origMap;
        expectedDefMap.putAll(testData);
        EntityType.updateDefinitions("tosca_simple_profile_for_nfv_1_0_0");

        assertEquals(expectedDefMap, EntityType.TOSCA_DEF);

    }

    @After
    public void tearDown() throws Exception {
        EntityType.TOSCA_DEF = (LinkedHashMap<String, Object>) origMap;
    }

}
