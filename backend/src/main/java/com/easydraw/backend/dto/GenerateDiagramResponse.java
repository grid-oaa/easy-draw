package com.easydraw.backend.dto;

import java.util.List;

public class GenerateDiagramResponse {

  private String language;
  private String diagramType;
  private String content;
  private ValidationResult validation;
  private String explain;

  public static GenerateDiagramResponse of(
      String language,
      String diagramType,
      String content,
      ValidationResult validation,
      String explain) {
    GenerateDiagramResponse res = new GenerateDiagramResponse();
    res.setLanguage(language);
    res.setDiagramType(diagramType);
    res.setContent(content);
    res.setValidation(validation);
    res.setExplain(explain);
    return res;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(String diagramType) {
    this.diagramType = diagramType;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public ValidationResult getValidation() {
    return validation;
  }

  public void setValidation(ValidationResult validation) {
    this.validation = validation;
  }

  public String getExplain() {
    return explain;
  }

  public void setExplain(String explain) {
    this.explain = explain;
  }

  public static class ValidationResult {
    private boolean valid;
    private List<DiagramError> errors;

    public static ValidationResult ok() {
      ValidationResult v = new ValidationResult();
      v.setValid(true);
      v.setErrors(List.of());
      return v;
    }

    public static ValidationResult fail(List<DiagramError> errors) {
      ValidationResult v = new ValidationResult();
      v.setValid(false);
      v.setErrors(errors == null ? List.of() : errors);
      return v;
    }

    public boolean isValid() {
      return valid;
    }

    public void setValid(boolean valid) {
      this.valid = valid;
    }

    public List<DiagramError> getErrors() {
      return errors;
    }

    public void setErrors(List<DiagramError> errors) {
      this.errors = errors;
    }
  }
}
