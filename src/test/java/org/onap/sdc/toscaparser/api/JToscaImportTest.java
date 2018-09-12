package org.onap.sdc.toscaparser.api;

import org.junit.Test;
import org.onap.sdc.toscaparser.api.common.JToscaException;
import org.onap.sdc.toscaparser.api.parameters.Annotation;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

        ArrayList<String> valueList = (ArrayList<String>)nodeTemplate.getPropertyValueFromTemplatesByName("vmxvpfe_sriov41_0_port_vlanfilter");
        assertEquals(4, valueList.size());

        assertEquals("vPE", (String) nodeTemplate.getPropertyValueFromTemplatesByName("nf_role"));

        assertNull(nodeTemplate.getPropertyValueFromTemplatesByName("test"));
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

}
