package com.easydraw.backend.plantuml;

import com.easydraw.backend.dto.DiagramError;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BasicPlantUmlValidator implements PlantUmlValidator {

  @Override
  public List<DiagramError> validate(String plantUml) {
    List<DiagramError> errors = new ArrayList<>();
    if (plantUml == null || plantUml.trim().isEmpty()) {
      errors.add(DiagramError.of("EMPTY", "PlantUML 内容为空"));
      return errors;
    }

    String trimmed = plantUml.trim();
    if (!trimmed.contains("@startuml") || !trimmed.contains("@enduml")) {
      errors.add(DiagramError.of("BOUNDARY_MISSING", "PlantUML 缺少 @startuml / @enduml 边界"));
    }
    return errors;
  }
}

