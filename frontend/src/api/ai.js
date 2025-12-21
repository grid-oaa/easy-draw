import { http } from './http';

export async function requestAiPatch(payload) {
  const res = await http.post('/ai/patch', payload);
  return res.data;
}

/**
 * 生成绘图语言文本（后端 /api/ai/diagram）
 * @param {{ language?: string, diagramType: string, prompt: string }} payload
 * @returns {Promise<{ language: string, diagramType: string, content: string, validation: object, explain: string }>}
 */
export async function generateDiagram(payload) {
  const res = await http.post('/ai/diagram', payload);
  return res.data;
}
