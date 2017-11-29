package org.openecomp.sdc.toscaparser.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.junit.Test;
import org.openecomp.sdc.toscaparser.api.common.JToscaException;
import org.openecomp.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;

public class JToscaMetadataParse {

    @Test
    public void testMetadataParsedCorrectly() throws JToscaException {
        String fileStr = JToscaMetadataParse.class.getClassLoader().getResource("csars/csar_hello_world.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        LinkedHashMap<String, Object> metadataProperties = toscaTemplate.getMetaProperties("TOSCA.meta");
        assertNotNull(metadataProperties);
        Object entryDefinition = metadataProperties.get("Entry-Definitions");
        assertNotNull(entryDefinition);
        assertEquals("tosca_helloworld.yaml", entryDefinition);
    }

    @Test
    public void noWarningsAfterParse() throws JToscaException {
        String fileStr = JToscaMetadataParse.class.getClassLoader().getResource("csars/tmpCSAR_Huawei_vSPGW_fixed.csar.csar").getFile();
        File file = new File(fileStr);
        ToscaTemplate toscaTemplate = new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        int validationIssuesCaught = ThreadLocalsHolder.getCollector().validationIssuesCaught();
        assertTrue(validationIssuesCaught == 0 );
    }

}
