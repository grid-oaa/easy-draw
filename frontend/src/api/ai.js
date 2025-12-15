import { http } from './http';

export async function requestAiPatch(payload) {
  const res = await http.post('/ai/patch', payload);
  return res.data;
}
