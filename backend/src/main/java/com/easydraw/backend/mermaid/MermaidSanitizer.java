package com.easydraw.backend.mermaid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MermaidSanitizer {

  private MermaidSanitizer() {}

  public static String clean(String raw) {
    if (raw == null) {
      return "";
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      return trimmed;
    }

    String normalized = trimmed.replace("\r\n", "\n").replace("\r", "\n");
    String[] lines = normalized.split("\n", -1);

    List<String> out = new ArrayList<>();
    boolean headerHandled = false;

    for (String lineRaw : lines) {
      String line = lineRaw.trim();
      if (line.isEmpty()) {
        if (headerHandled) {
          out.add("");
        }
        continue;
      }
      if (line.startsWith("```")) {
        continue;
      }
      if (line.equalsIgnoreCase("mermaid")) {
        continue;
      }

      if (!headerHandled) {
        String header = normalizeHeader(line);
        if (header != null) {
          out.add(header);
          headerHandled = true;
          continue;
        }
        out.add("graph TB");
        headerHandled = true;
      }

      out.add(line);
    }

    if (!headerHandled) {
      out.add("graph TB");
    }

    List<String> normalizedLines = normalizeFlowchartEdges(out);
    return String.join("\n", normalizedLines).trim();
  }

  private static String normalizeHeader(String line) {
    String lower = line.toLowerCase();
    if (lower.startsWith("flowchart")) {
      String[] parts = line.split("\\s+");
      if (parts.length == 1) {
        return "graph TB";
      }
      return "graph " + parts[1];
    }
    if (lower.startsWith("graph")) {
      String[] parts = line.split("\\s+");
      if (parts.length == 1) {
        return "graph TB";
      }
      return "graph " + parts[1];
    }
    if (lower.startsWith("sequencediagram")
        || lower.startsWith("statediagram")
        || lower.startsWith("classdiagram")
        || lower.startsWith("erdiagram")) {
      return line;
    }
    return null;
  }

  private static List<String> normalizeFlowchartEdges(List<String> lines) {
    String header = firstNonEmpty(lines);
    if (header == null) {
      return lines;
    }
    String lower = header.toLowerCase();
    if (!lower.startsWith("graph") && !lower.startsWith("flowchart")) {
      return lines;
    }

    Map<String, String> labelToId = new HashMap<>();
    int[] counter = new int[] {1};
    List<String> out = new ArrayList<>(lines.size());

    for (String line : lines) {
      if (line == null || line.isBlank() || line == header || line.startsWith("%%")) {
        out.add(line);
        continue;
      }
      out.add(normalizeEdgeLine(line, labelToId, counter));
    }

    return out;
  }

  private static String normalizeEdgeLine(String line, Map<String, String> labelToId, int[] counter) {
    int arrowIdx = line.indexOf("-->");
    if (arrowIdx < 0) {
      return line;
    }

    int nonSpace = 0;
    while (nonSpace < line.length() && Character.isWhitespace(line.charAt(nonSpace))) {
      nonSpace++;
    }
    String indent = line.substring(0, nonSpace);
    String content = line.substring(nonSpace);

    int contentArrow = content.indexOf("-->");
    if (contentArrow < 0) {
      return line;
    }

    String leftRaw = content.substring(0, contentArrow).trim();
    String rightRaw = content.substring(contentArrow + 3).trim();
    String edgeLabel = null;

    if (rightRaw.startsWith("|")) {
      int end = rightRaw.indexOf('|', 1);
      if (end > 0) {
        edgeLabel = rightRaw.substring(0, end + 1).trim();
        rightRaw = rightRaw.substring(end + 1).trim();
      }
    }

    String left = normalizeToken(leftRaw, labelToId, counter);
    String right = normalizeToken(rightRaw, labelToId, counter);

    StringBuilder sb = new StringBuilder(indent);
    sb.append(left).append(" -->");
    if (edgeLabel != null) {
      sb.append(edgeLabel).append(" ");
    } else {
      sb.append(" ");
    }
    sb.append(right);
    return sb.toString();
  }

  private static String normalizeToken(String token, Map<String, String> labelToId, int[] counter) {
    if (token == null) {
      return "";
    }
    String trimmed = token.trim();
    if (trimmed.isEmpty()) {
      return trimmed;
    }
    if (trimmed.matches("^[A-Za-z0-9_]+$")) {
      return trimmed;
    }
    if (trimmed.matches("^[A-Za-z0-9_]+\\s*[\\[{(].*")) {
      return trimmed;
    }

    String id = labelToId.get(trimmed);
    if (id == null) {
      id = "N" + counter[0]++;
      labelToId.put(trimmed, id);
    }

    String label = trimmed.replace("\\", "\\\\").replace("\"", "\\\"");
    return id + "[\"" + label + "\"]";
  }

  private static String firstNonEmpty(List<String> lines) {
    for (String line : lines) {
      if (line != null && !line.isBlank()) {
        return line.trim();
      }
    }
    return null;
  }
}
