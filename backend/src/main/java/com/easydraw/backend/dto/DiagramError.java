package com.easydraw.backend.dto;

public class DiagramError {

  private String code;
  private String message;

  public static DiagramError of(String code, String message) {
    DiagramError e = new DiagramError();
    e.setCode(code);
    e.setMessage(message);
    return e;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

