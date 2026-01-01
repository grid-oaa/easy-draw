package com.easydraw.backend.style;

import com.easydraw.backend.dto.ModifyStyleCommand;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ModifyStyleValidator {

  private static final Set<String> TARGETS = Set.of("selected", "edges", "vertices", "all");
  private static final Set<String> OPS = Set.of("set", "increase", "decrease", "multiply");

  public void validate(ModifyStyleCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("样式指令为空");
    }
    if (!"modifyStyle".equals(command.getAction())) {
      throw new IllegalArgumentException("action 必须为 modifyStyle");
    }
    if (command.getTarget() == null || !TARGETS.contains(command.getTarget())) {
      throw new IllegalArgumentException("target 不合法");
    }
    boolean hasStyles = command.getStyles() != null && !command.getStyles().isEmpty();
    boolean hasOps = command.getOperations() != null && !command.getOperations().isEmpty();
    if (!hasStyles && !hasOps) {
      throw new IllegalArgumentException("styles 和 operations 至少提供一个");
    }

    if (command.getOperations() != null) {
      for (Map.Entry<String, ModifyStyleCommand.Operation> entry : command.getOperations().entrySet()) {
        ModifyStyleCommand.Operation op = entry.getValue();
        if (op == null || op.getOp() == null || op.getValue() == null) {
          throw new IllegalArgumentException("operations 子项不完整");
        }
        if (!OPS.contains(op.getOp())) {
          throw new IllegalArgumentException("operations.op 不合法");
        }
      }
    }
  }
}