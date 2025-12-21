package com.easydraw.backend.util;

public final class DrawioXmlFactory {

  private DrawioXmlFactory() {}

  public static String fromText(String text) {
    String safe = escape(text == null ? "" : text);
    return """
        <mxfile host="app.diagrams.net">
          <diagram name="Generated">
            <mxGraphModel>
              <root>
                <mxCell id="0"/>
                <mxCell id="1" parent="0"/>
                <mxCell id="2" value="%s"
                        style="whiteSpace=wrap;html=1;rounded=1;fillColor=#EEF2FF;strokeColor=#4B5563;fontSize=12;"
                        vertex="1" parent="1">
                  <mxGeometry x="160" y="120" width="320" height="200" as="geometry"/>
                </mxCell>
              </root>
            </mxGraphModel>
          </diagram>
        </mxfile>
        """
        .formatted(safe)
        .replaceAll(">\\s+<", "><")
        .trim();
  }

  private static String escape(String raw) {
    return raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}

