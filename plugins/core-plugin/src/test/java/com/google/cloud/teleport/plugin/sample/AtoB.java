package com.google.cloud.teleport.plugin.sample;

import com.google.cloud.teleport.metadata.Template;
import com.google.cloud.teleport.metadata.TemplateCategory;
import com.google.cloud.teleport.metadata.TemplateParameter;
import com.google.cloud.teleport.plugin.sample.AtoB.AtoBOptions;

@Template(
    name = "AtoB",
    displayName = "A to B",
    description = "Send A to B",
    category = TemplateCategory.STREAMING,
    optionsClass = AtoBOptions.class)
public class AtoB {

  interface AtoBOptions {
    @TemplateParameter.BigQueryTable(
        order = 2,
        name = "to",
        description = "to",
        helpText = "Table to send data to")
    String to();


    @TemplateParameter.Text(
        order = 1,
        name = "from",
        description = "from",
        helpText = "Define where to get data from")
    String from();
  }
}
