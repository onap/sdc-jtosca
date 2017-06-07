package org.openecomp.sdc.toscaparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.LinkedHashMap;

import org.junit.Test;
import org.openecomp.sdc.toscaparser.api.ToscaTemplate;
import org.openecomp.sdc.toscaparser.api.common.JToscaException;

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
}
