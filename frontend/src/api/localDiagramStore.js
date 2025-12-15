const STORAGE_KEY = 'easy-draw:diagram:draft:v1';

function safeJsonParse(value, fallback) {
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
}

export function loadDraftDiagram() {
  const raw = localStorage.getItem(STORAGE_KEY);
  const data = safeJsonParse(raw, null);
  if (!data || typeof data !== 'object') return null;
  if (typeof data.drawioXml !== 'string' || !data.drawioXml) return null;
  return data;
}

export function saveDraftDiagram(diagram) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(diagram));
}
