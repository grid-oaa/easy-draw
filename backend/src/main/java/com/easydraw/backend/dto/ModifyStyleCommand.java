package com.easydraw.backend.dto;

import java.util.Map;

public class ModifyStyleCommand {

  private String action;
  private String target;
  private Map<String, Object> styles;
  private Map<String, Operation> operations;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public Map<String, Object> getStyles() {
    return styles;
  }

  public void setStyles(Map<String, Object> styles) {
    this.styles = styles;
  }

  public Map<String, Operation> getOperations() {
    return operations;
  }

  public void setOperations(Map<String, Operation> operations) {
    this.operations = operations;
  }

  public static class Operation {
    private String op;
    private Double value;

    public String getOp() {
      return op;
    }

    public void setOp(String op) {
      this.op = op;
    }

    public Double getValue() {
      return value;
    }

    public void setValue(Double value) {
      this.value = value;
    }
  }
}