package org.onap.sdc.toscaparser.api;

import org.junit.Test;
import org.onap.sdc.toscaparser.api.common.JToscaException;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class JToscaImportTest {

    @Test
    public void testNoMissingTypeValidationError() throws JToscaException {
        String fileStr = JToscaImportTest.class.getClassLoader().getResource
            ("csars/sdc-onboarding_csar.csar").getFile();
        File file = new File(fileStr);
        new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        List<String> missingTypeErrors = ThreadLocalsHolder.getCollector()
            .getValidationIssueReport()
            .stream()
            .filter(s -> s.contains("JE136"))
            .collect(Collectors.toList());
        assertEquals(0, missingTypeErrors.size());
    }

    @Test
    public void testNoStackOverFlowError() {
        Exception jte = null;
        try {
            String fileStr = JToscaImportTest.class.getClassLoader().getResource
                ("csars/sdc-onboarding_csar.csar").getFile();
            File file = new File(fileStr);
            new ToscaTemplate(file.getAbsolutePath(), null, true, null);
        } catch (Exception e){
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
      List<String> invalidImportErrors = ThreadLocalsHolder.getCollector()
          .getValidationIssueReport()
          .stream()
          .filter(s -> s.contains("JE195"))
          .collect(Collectors.toList());
      assertEquals(0, invalidImportErrors.size());
    }
  }

}
