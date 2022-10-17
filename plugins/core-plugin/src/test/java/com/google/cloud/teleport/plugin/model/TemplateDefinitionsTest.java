package com.google.cloud.teleport.plugin.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.teleport.metadata.Template;
import com.google.cloud.teleport.plugin.sample.AtoB;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TemplateDefinitionsTest {

  @Test
  public void testSampleAtoB() {
    TemplateDefinitions definitions =
        new TemplateDefinitions(AtoB.class, AtoB.class.getAnnotation(Template.class));
    assertNotNull(definitions);

    ImageSpec imageSpec = definitions.buildSpecModel(false);
    assertNotNull(imageSpec);

    ImageSpecMetadata metadata = imageSpec.getMetadata();
    assertNotNull(metadata);

    assertEquals("AtoB", metadata.getName());
    assertEquals("Send A to B", metadata.getDescription());
    assertEquals("com.google.cloud.teleport.plugin.sample.AtoB", metadata.getMainClass());

    List<ImageSpecParameter> parameters = metadata.getParameters();
    assertEquals(2, parameters.size());

    // Make sure metadata follows stable order
    assertEquals("from", metadata.getParameters().get(0).getName());

    ImageSpecParameter from = metadata.getParameter("from").get();
    assertNotNull(from);
    assertEquals(ImageSpecParameterType.TEXT, from.getParamType());

    ImageSpecParameter to = metadata.getParameter("to").get();
    assertNotNull(to);
    assertEquals(ImageSpecParameterType.TEXT, to.getParamType());

  }
}
