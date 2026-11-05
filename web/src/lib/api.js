// Cliente da API do Valtrea Pulso. O token de acesso dura 15 minutos e é renovado sozinho com o token
// de sessão (cada token de sessão vale uma vez).
const BASE = `${(import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')}/api/v1`

const KEY = 'valtrea-pulso:sessao'

export const sessao = {
  ler() {
    try {
      return JSON.parse(localStorage.getItem(KEY)) || null
    } catch {
      return null
    }
  },
  salvar(tokens) {
    try {
      localStorage.setItem(KEY, JSON.stringify({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken }))
    } catch {
      // navegação privada sem armazenamento: a sessão dura até fechar a aba
    }
    memoria = tokens
  },
  limpar() {
    try {
      localStorage.removeItem(KEY)
    } catch {
      // nada a fazer
    }
    memoria = null
  },
}

let memoria = sessao.ler()
let renovando = null

export class ErroApi extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem)
    this.status = status
    this.campos = campos
  }
}

async function renovar() {
  const refreshToken = memoria?.refreshToken
  if (!refreshToken) throw new ErroApi(401, 'Sua sessão expirou. Entre novamente.')
  const res = await fetch(`${BASE}/auth/renovar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!res.ok) {
    sessao.limpar()
    window.dispatchEvent(new Event('pulso:saiu'))
    throw new ErroApi(401, 'Sua sessão expirou. Entre novamente.')
  }
  sessao.salvar(await res.json())
}

async function mensagemDe(res) {
  const corpo = await res.json().catch(() => ({}))
  const primeiroCampo = corpo.campos && Object.entries(corpo.campos)[0]
  const texto = primeiroCampo ? `${primeiroCampo[1]}` : corpo.detail || corpo.message
  if (res.status === 429) return new ErroApi(429, texto || 'Muitas tentativas. Aguarde alguns minutos.')
  return new ErroApi(res.status, texto || 'Não foi possível concluir agora. Tente de novo em instantes.', corpo.campos)
}

export async function chamar(metodo, rota, corpo, { autenticar = true, tentativa = 0 } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (autenticar && memoria?.accessToken) headers.Authorization = `Bearer ${memoria.accessToken}`
  let res
  try {
    res = await fetch(`${BASE}${rota}`, { method: metodo, headers, body: corpo ? JSON.stringify(corpo) : undefined })
  } catch {
    throw new ErroApi(0, 'Sem conexão com o servidor. Confira a internet e tente de novo. Na primeira vez do dia, ele pode levar até 1 minuto para acordar.')
  }
  if (res.status === 401 && autenticar && tentativa === 0 && memoria?.refreshToken) {
    renovando ??= renovar().finally(() => (renovando = null))
    await renovando
    return chamar(metodo, rota, corpo, { autenticar, tentativa: 1 })
  }
  if (!res.ok) throw await mensagemDe(res)
  if (res.status === 204 || res.status === 202) return null
  const tipo = res.headers.get('Content-Type') || ''
  return tipo.includes('json') ? res.json() : res.text()
}

export const estaLogado = () => Boolean(memoria?.refreshToken)
export const tokenDeSessao = () => memoria?.refreshToken

export const CATEGORIAS = {
  ILUMINACAO: 'Iluminação',
  LIMPEZA: 'Limpeza e lixo',
  SEGURANCA: 'Segurança',
  TRANSITO: 'Trânsito',
  CALCADAS_E_RUAS: 'Calçadas e ruas',
  AREAS_VERDES: 'Praças e áreas verdes',
  SAUDE: 'Saúde',
  EDUCACAO: 'Educação',
  LAZER: 'Esporte e lazer',
  TRANSPORTE: 'Transporte público',
  OUTROS: 'Outros',
}

export const FASES = {
  ABERTA: 'Aberta',
  EM_ANDAMENTO: 'Em andamento',
  RESOLVIDA: 'Resolvida',
  ARQUIVADA: 'Arquivada',
}

export const MOTIVOS = {
  OFENSIVO: 'Xingamento, ataque ou preconceito',
  DADOS_PESSOAIS: 'Expõe uma pessoa (nome, endereço, telefone, foto)',
  PROPAGANDA: 'Propaganda, venda ou pedido de voto',
  SPAM: 'Repetida, sem sentido ou de outro bairro',
  FALSO: 'Informação falsa ou golpe',
  OUTRO: 'Outro motivo',
}

export const UFS = ['AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE',
  'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO']

export const dataCurta = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
