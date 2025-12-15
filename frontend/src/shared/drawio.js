export const EMPTY_DRAWIO_XML =
  '<mxfile host="app.diagrams.net"><diagram name="Page-1"><mxGraphModel><root><mxCell id="0"/><mxCell id="1" parent="0"/></root></mxGraphModel></diagram></mxfile>';

export function getDrawioBaseUrl() {
  return process.env.VUE_APP_DRAWIO_BASE_URL || 'https://embed.diagrams.net';
}

export function getDrawioOrigin() {
  const base = getDrawioBaseUrl();
  try {
    return new URL(base, window.location.origin).origin;
  } catch {
    return null;
  }
}
