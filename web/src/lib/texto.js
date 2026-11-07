// Ajudas de texto feitas no próprio navegador, sem serviço de IA: nada do que a pessoa escreve sai daqui
// antes de ela publicar.

const PALAVRAS_VAZIAS = new Set(
  ('a o as os um uma uns umas de da do das dos em na no nas nos para pra por com sem que e ou se ao aos ' +
    'mais muito muita esta este essa esse isso aqui ali la lá rua avenida av bairro ha há tem ter sobre ' +
    'nao não precisa precisamos favor pedido pedir quero queremos todo toda todos').split(' '),
)

const normalizar = (texto) =>
  texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().replace(/[^a-z0-9\s]/g, ' ')

// "lampadas" e "lampada", "buracos" e "buraco" contam como a mesma palavra
const raiz = (p) => (p.length > 4 ? p.replace(/(oes|aes|ais|eis|is|es|s)$/, '') : p)

export const palavras = (texto) =>
  new Set(normalizar(texto).split(/\s+/).filter((p) => p.length >= 3 && !PALAVRAS_VAZIAS.has(p)).map(raiz))

/** Semelhança entre dois títulos (0 a 1): quanto das palavras importantes do mais curto aparece no outro. */
export function semelhanca(a, b) {
  const pa = palavras(a)
  const pb = palavras(b)
  if (pa.size === 0 || pb.size === 0) return 0
  let comuns = 0
  for (const p of pa) if (pb.has(p)) comuns += 1
  return comuns / Math.min(pa.size, pb.size)
}

/** Até 3 iniciativas do bairro parecidas com o título digitado (mesma categoria pesa um pouco mais). */
export function parecidas(titulo, categoria, lista) {
  if (palavras(titulo).size < 2) return []
  return lista
    .map((i) => ({ i, nota: semelhanca(titulo, i.titulo) + (i.categoria === categoria ? 0.15 : 0) }))
    .filter(({ nota }) => nota >= 0.5)
    .sort((x, y) => y.nota - x.nota)
    .slice(0, 3)
    .map(({ i }) => i)
}

/** Avisos (não bloqueiam) quando o texto parece expor dados de alguém ou fazer campanha política. */
export function avisosDoTexto(texto) {
  const avisos = []
  if (/\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b/.test(texto)) avisos.push('Parece ter um CPF no texto.')
  if (/\b9?\d{4}-\d{4}\b|\(\d{2}\)\s?9?\d{4}\s?\d{4}|\b\d{2}\s?9\d{8}\b/.test(texto)) avisos.push('Parece ter um número de telefone no texto.')
  if (/[\w.+-]+@[\w-]+\.[\w.]+/.test(texto)) avisos.push('Parece ter um e-mail no texto.')
  if (/\b(vote|votem|votar em|candidat[oa]|deputad[oa]|vereador[a]?|prefeit[oa] [A-Z]|partido|elei[cç][aã]o|elei[cç][oõ]es)\b/i.test(texto)) {
    avisos.push('Parece falar de candidato, partido ou eleição. Campanha política não é permitida no Pulso.')
  }
  return avisos
}
