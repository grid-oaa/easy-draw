package com.easydraw.backend.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight Mermaid flowchart to draw.io converter.
 * Supports edges like: A[Start] -->|Yes| B{Decision}
 */
public final class MermaidToDrawioConverter {

  private MermaidToDrawioConverter() {}

  // Capture A[Start] --> B[Process] or A{Decision} -->|Yes| B
  private static final Pattern EDGE_PATTERN =
      Pattern.compile(
          "([A-Za-z0-9_]+)(?:\\[([^\\]]*)\\]|\\{([^}]*)\\})?\\s*-{1,2}>\\s*(?:\\|([^|]*)\\|\\s*)?([A-Za-z0-9_]+)(?:\\[([^\\]]*)\\]|\\{([^}]*)\\})?");

  public static String toDrawioXml(String mermaid) {
    if (mermaid == null || mermaid.isBlank()) {
      return DrawioXmlFactory.fromText("Mermaid content is empty");
    }

    List<String> lines = mermaid.lines().toList();
    List<Edge> edges = new ArrayList<>();
    Set<String> nodes = new HashSet<>();
    List<String> nodeOrder = new ArrayList<>();
    Map<String, NodeInfo> nodeInfo = new HashMap<>();
    Map<String, List<String>> outgoing = new HashMap<>();
    Map<String, Integer> indegree = new HashMap<>();

    for (String raw : lines) {
      String line = raw.trim();
      Matcher m = EDGE_PATTERN.matcher(line);
      if (m.find()) {
        String from = m.group(1);
        String fromLabel = pickLabel(m.group(2), m.group(3));
        NodeType fromType = m.group(3) != null ? NodeType.DECISION : NodeType.PROCESS;
        String edgeLabel = m.group(4);
        String to = m.group(5);
        String toLabel = pickLabel(m.group(6), m.group(7));
        NodeType toType = m.group(7) != null ? NodeType.DECISION : NodeType.PROCESS;

        if (nodes.add(from)) {
          nodeOrder.add(from);
        }
        if (nodes.add(to)) {
          nodeOrder.add(to);
        }
        nodeInfo.putIfAbsent(from, new NodeInfo(labelOrDefault(fromLabel, from), fromType));
        nodeInfo.putIfAbsent(to, new NodeInfo(labelOrDefault(toLabel, to), toType));
        outgoing.computeIfAbsent(from, key -> new ArrayList<>()).add(to);
        indegree.put(to, indegree.getOrDefault(to, 0) + 1);
        indegree.putIfAbsent(from, indegree.getOrDefault(from, 0));
        edges.add(new Edge(from, to, edgeLabel));
      }
    }

    if (nodes.isEmpty()) {
      return DrawioXmlFactory.fromText(mermaid);
    }

    Map<String, Position> positions = layeredLayout(nodeOrder, outgoing, indegree);

    StringBuilder xml = new StringBuilder();
    xml.append("<mxfile host=\"app.diagrams.net\"><diagram name=\"Generated\"><mxGraphModel><root>");
    xml.append("<mxCell id=\"0\"/><mxCell id=\"1\" parent=\"0\"/>");

    int id = 2;
    Map<String, Integer> nodeId = new HashMap<>();
    for (String n : nodeOrder) {
      NodeInfo info = nodeInfo.getOrDefault(n, new NodeInfo(n, NodeType.PROCESS));
      String label = info.label();
      Position p = positions.get(n);
      int width = info.type() == NodeType.DECISION ? 110 : 120;
      int height = info.type() == NodeType.DECISION ? 80 : 50;
      String style =
          info.type() == NodeType.DECISION
              ? "shape=rhombus;perimeter=rhombusPerimeter;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontSize=12;"
              : "shape=rectangle;rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#000000;fontSize=12;";
      xml.append("<mxCell id=\"")
          .append(id)
          .append("\" value=\"")
          .append(escape(label))
          .append("\" style=\"")
          .append(style)
          .append("\" vertex=\"1\" parent=\"1\">");
      xml.append("<mxGeometry x=\"")
          .append(p.x)
          .append("\" y=\"")
          .append(p.y)
          .append("\" width=\"")
          .append(width)
          .append("\" height=\"")
          .append(height)
          .append("\" as=\"geometry\"/>");
      xml.append("</mxCell>");
      nodeId.put(n, id);
      id++;
    }

    for (Edge e : edges) {
      Integer src = nodeId.get(e.from());
      Integer tgt = nodeId.get(e.to());
      if (src == null || tgt == null) {
        continue;
      }
      boolean curved = outgoing.getOrDefault(e.from(), List.of()).size() > 1;
      String edgeStyle =
          curved
              ? "edgeStyle=orthogonalEdgeStyle;rounded=1;curved=1;endArrow=block;strokeColor=#000000;html=1;"
              : "edgeStyle=orthogonalEdgeStyle;rounded=1;endArrow=block;strokeColor=#000000;html=1;";
      xml.append("<mxCell id=\"")
          .append(id)
          .append("\" edge=\"1\" parent=\"1\" source=\"")
          .append(src)
          .append("\" target=\"")
          .append(tgt)
          .append("\" value=\"")
          .append(escape(labelOrDefault(e.label(), "")))
          .append("\" style=\"")
          .append(edgeStyle)
          .append("\">");
      xml.append("<mxGeometry relative=\"1\" as=\"geometry\"/>");
      xml.append("</mxCell>");
      id++;
    }

    xml.append("</root></mxGraphModel></diagram></mxfile>");
    return xml.toString();
  }

  private static Map<String, Position> layeredLayout(
      List<String> nodeOrder,
      Map<String, List<String>> outgoing,
      Map<String, Integer> indegree) {
    Map<String, Integer> level = new HashMap<>();
    Deque<String> queue = new ArrayDeque<>();
    for (String node : nodeOrder) {
      if (indegree.getOrDefault(node, 0) == 0) {
        queue.add(node);
      }
    }
    while (!queue.isEmpty()) {
      String node = queue.remove();
      int base = level.getOrDefault(node, 0);
      for (String next : outgoing.getOrDefault(node, List.of())) {
        level.put(next, Math.max(level.getOrDefault(next, 0), base + 1));
        int nextDeg = indegree.getOrDefault(next, 0) - 1;
        indegree.put(next, nextDeg);
        if (nextDeg == 0) {
          queue.add(next);
        }
      }
    }

    TreeMap<Integer, List<String>> levels = new TreeMap<>();
    for (String node : nodeOrder) {
      int lv = level.getOrDefault(node, 0);
      levels.computeIfAbsent(lv, key -> new ArrayList<>()).add(node);
    }

    Map<String, Position> pos = new HashMap<>();
    int gapX = 150;
    int gapY = 120;
    int centerX = 280;
    for (Map.Entry<Integer, List<String>> entry : levels.entrySet()) {
      int y = 80 + entry.getKey() * gapY;
      List<String> nodesAt = entry.getValue();
      int totalWidth = (nodesAt.size() - 1) * gapX;
      int startX = centerX - totalWidth / 2;
      for (int i = 0; i < nodesAt.size(); i++) {
        int x = startX + i * gapX;
        pos.put(nodesAt.get(i), new Position(x, y));
      }
    }
    return pos;
  }

  private static String pickLabel(String bracketLabel, String braceLabel) {
    if (bracketLabel != null) {
      return bracketLabel.trim();
    }
    if (braceLabel != null) {
      return braceLabel.trim();
    }
    return null;
  }

  private static String labelOrDefault(String label, String fallback) {
    if (label == null || label.isBlank()) {
      return fallback;
    }
    return label.trim();
  }

  private static String escape(String raw) {
    return raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private record Edge(String from, String to, String label) {}

  private record Position(int x, int y) {}

  private enum NodeType {
    PROCESS,
    DECISION
  }

  private record NodeInfo(String label, NodeType type) {}
}
