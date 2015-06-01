/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.restapi.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.streamsets.pipeline.api.ExecutionMode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StageDefinitionJson {

  private final com.streamsets.pipeline.config.StageDefinition stageDefinition;

  @JsonCreator
  public StageDefinitionJson(
    @JsonProperty("className") String className,
    @JsonProperty("name") String name,
    @JsonProperty("version") String version,
    @JsonProperty("label") String label,
    @JsonProperty("description") String description,
    @JsonProperty("type") StageTypeJson type,
    @JsonProperty("errorStage") boolean errorStage,
    @JsonProperty("preconditions") boolean preconditions,
    @JsonProperty("onRecordError") boolean onRecordError,
    @JsonProperty("configDefinitions") List<ConfigDefinitionJson> configDefinitionJsons,
    @JsonProperty("rawSourceDefinition") RawSourceDefinitionJson rawSourceDefinitionJson,
    @JsonProperty("icon") String icon,
    @JsonProperty("configGroupDefinition") ConfigGroupDefinitionJson configGroupDefinitionJson,
    @JsonProperty("variableOutputStreams") boolean variableOutputStreams,
    @JsonProperty("outputStreams") int outputStreams,
    @JsonProperty("outputStreamLabelProviderClass") String outputStreamLabelProviderClass,
    @JsonProperty("executionModes") List<ExecutionMode> executionModes) {
    this.stageDefinition = new com.streamsets.pipeline.config.StageDefinition(className, name, version, label,
      description, BeanHelper.unwrapStageType(type), errorStage, preconditions,
      onRecordError, BeanHelper.unwrapConfigDefinitions(configDefinitionJsons),
      BeanHelper.unwrapRawSourceDefinition(rawSourceDefinitionJson), icon,
      BeanHelper.unwrapConfigGroupDefinition(configGroupDefinitionJson), variableOutputStreams, outputStreams,
      outputStreamLabelProviderClass, executionModes);
  }

  public StageDefinitionJson(com.streamsets.pipeline.config.StageDefinition stageDefinition) {
    this.stageDefinition = stageDefinition;
  }

  public ConfigGroupDefinitionJson getConfigGroupDefinition() {
    return BeanHelper.wrapConfigGroupDefinition(stageDefinition.getConfigGroupDefinition());
  }

  public String getClassName() {
    return stageDefinition.getClassName();
  }

  public String getName() {
    return stageDefinition.getName();
  }

  public String getVersion() {
    return stageDefinition.getVersion();
  }

  public String getLabel() {
    return stageDefinition.getLabel();
  }

  public RawSourceDefinitionJson getRawSourceDefinition() {
    return BeanHelper.wrapRawSourceDefinition(stageDefinition.getRawSourceDefinition());
  }

  public String getDescription() {
    return stageDefinition.getDescription();
  }

  public StageTypeJson getType() {
    return BeanHelper.wrapStageType(stageDefinition.getType());
  }

  public boolean isErrorStage() {
    return stageDefinition.isErrorStage();
  }

  @JsonProperty("preconditions")
  public boolean hasRequiredFields() {
    return stageDefinition.hasPreconditions();
  }

  @JsonProperty("onRecordError")
  public boolean hasOnRecordError() {
    return stageDefinition.hasOnRecordError();
  }

  public List<ConfigDefinitionJson> getConfigDefinitions() {
    return BeanHelper.wrapConfigDefinitions(stageDefinition.getConfigDefinitions());
  }

  public String getIcon() {
    return stageDefinition.getIcon();
  }

  public boolean isVariableOutputStreams() {
    return stageDefinition.isVariableOutputStreams();
  }

  public int getOutputStreams() {
    return stageDefinition.getOutputStreams();
  }

  public String getOutputStreamLabelProviderClass() {
    return stageDefinition.getOutputStreamLabelProviderClass();
  }

  public String getLibrary() {
    return stageDefinition.getLibrary();
  }

  public String getLibraryLabel() {
    return stageDefinition.getLibraryLabel();
  }

  public List<String> getOutputStreamLabels() {
    return stageDefinition.getOutputStreamLabels();
  }

  public List<ExecutionMode> getExecutionModes() {
    return stageDefinition.getExecutionModes();
  }

  @JsonIgnore
  public com.streamsets.pipeline.config.StageDefinition getStageDefinition() {
    return stageDefinition;
  }
}