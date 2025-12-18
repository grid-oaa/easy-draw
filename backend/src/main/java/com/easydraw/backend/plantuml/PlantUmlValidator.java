package com.easydraw.backend.plantuml;

import com.easydraw.backend.dto.DiagramError;
import java.util.List;

public interface PlantUmlValidator {
  List<DiagramError> validate(String plantUml);
}

