package com.easydraw.backend.service;

import com.easydraw.backend.dto.ModifyStyleCommand;
import com.easydraw.backend.dto.StyleModifyRequest;

public interface StyleModificationService {
  ModifyStyleCommand generate(StyleModifyRequest request);
}