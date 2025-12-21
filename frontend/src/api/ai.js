import { http } from './http';

export async function requestAiPatch(payload) {
  const res = await http.post('/ai/patch', payload);
  return res.data;
}

/**
 * 生成绘图语言文本（后端 /api/ai/diagram）
 * @param {{ language?: string, diagramType: string, prompt: string, modelConfig?: object }} payload
 * @returns {Promise<{ language: string, diagramType: string, content: string, validation: object, explain: string }>}
 */
export async function generateDiagram(payload) {
  const res = await http.post('/ai/diagram', payload);
  return res.data;
}

/**
 * Edit an existing Mermaid diagram.
 * @param {{ diagramType?: string, prompt: string, mermaid: string, modelConfig?: object }} payload
 * @returns {Promise<{ language: string, diagramType: string, content: string, validation: object, explain: string }>}
 */
export async function editMermaidDiagram(payload) {
  const res = await http.post('/ai/diagram/edit', payload);
  return res.data;
}
