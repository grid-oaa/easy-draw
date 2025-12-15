import { http } from './http';

export async function listDiagrams(params) {
  const res = await http.get('/diagrams', { params });
  return res.data;
}

export async function getDiagram(id) {
  const res = await http.get(`/diagrams/${encodeURIComponent(id)}`);
  return res.data;
}

export async function createDiagram(payload) {
  const res = await http.post('/diagrams', payload);
  return res.data;
}

export async function updateDiagram(id, payload) {
  const res = await http.put(`/diagrams/${encodeURIComponent(id)}`, payload);
  return res.data;
}
