// Teste de carga do Valtrea Pulso (k6: https://k6.io). Rode pelo rodar-teste-de-carga.bat.
import http from 'k6/http'
import { check } from 'k6'
import { API, entrar, espera, ipDe, resumo } from './comum.js'

const USERS = Number(__ENV.USERS || 100)
export const options = {
    setupTimeout: '10m',
    scenarios: {
        uso: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: __ENV.RAMP || '1m', target: USERS },
                { duration: __ENV.HOLD || '3m', target: USERS },
                { duration: '20s', target: 0 },
            ],
            gracefulRampDown: '15s',
        },
    },
    // Mesmo critério do Valtrea Finance e do Pay.
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{tipo:leitura}': ['p(95)<1000'],
        'http_req_duration{tipo:escrita}': ['p(95)<1000'],
        'http_req_duration{tipo:login}': ['p(95)<2000'],
        checks: ['rate>0.99'],
    },
    summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
}

let token = null
const conta = () => (__VU - 1) % USERS
const ip = () => ipDe(__VU + 1000)
const p = (tipo, rota, extra = {}) => ({
    headers: { 'Content-Type': 'application/json', 'CF-Connecting-IP': ip(), Authorization: `Bearer ${token}` },
    tags: { tipo, rota },
    timeout: '60s',
    ...extra,
})
const ok = (r) => r.status >= 200 && r.status < 300

let publicou = false
let noBairro = false

// Antes do teste: a conta carga-0 cria o bairro em que todo mundo vai entrar.
export function setup() {
    const ipSetup = ipDe(999999)
    const t = entrar(0, ipSetup)
    const b = http.post(`${API}/api/v1/bairros`, JSON.stringify({ nome: 'Centro', cidade: 'Cidade Carga', uf: 'RJ' }),
        { headers: { 'Content-Type': 'application/json', 'CF-Connecting-IP': ipSetup, Authorization: `Bearer ${t}` }, timeout: '120s' })
    if (b.status !== 200 && b.status !== 201) throw new Error(`bairro: ${b.status} ${b.body}`)
    return { bairro: b.json('id') }
}

// Cada pessoa: entra no bairro, abre a lista de iniciativas, "lê" por 5 a 15 s e às vezes abre uma,
// apoia e retira o apoio. As 10 primeiras publicam uma iniciativa (o limite é 3 por dia).
export default function (dados) {
    const bairro = dados.bairro
    if (!token) {
        token = entrar(conta(), ip())
        if (!check(token, { 'login ok': (t) => !!t })) return espera(5, 10)
    }
    if (!noBairro) {
        noBairro = check(http.put(`${API}/api/v1/eu/bairro`, JSON.stringify({ bairroId: bairro }), p('escrita', 'PUT /eu/bairro')),
            { 'entrou no bairro': ok })
        if (!noBairro) return espera(5, 10)
    }
    if (!publicou && conta() < 10) {
        publicou = true
        check(http.post(`${API}/api/v1/iniciativas`, JSON.stringify({
            titulo: `Poste apagado na rua ${conta()}`, categoria: 'ILUMINACAO',
            descricao: 'Criada pelo teste de carga: o poste da esquina está apagado há uma semana.',
        }), p('escrita', 'POST /iniciativas')), { 'iniciativa publicada': (r) => r.status === 201 })
    }
    const inicio = http.batch([
        ['GET', `${API}/api/v1/eu`, null, p('leitura', 'GET /eu')],
        ['GET', `${API}/api/v1/bairros/${bairro}/iniciativas`, null, p('leitura', 'GET /bairros/{id}/iniciativas')],
    ])
    check(inicio, { 'início carregou': (rs) => rs.every(ok) })
    let lista = []
    try { lista = (inicio[1].json() || []).filter((i) => !i.minha) } catch (e) { lista = [] }
    espera(5, 15)
    if (lista.length > 0 && Math.random() < 0.4) {
        const i = lista[Math.floor(Math.random() * lista.length)]
        check(http.get(`${API}/api/v1/iniciativas/${i.id}`, p('leitura', 'GET /iniciativas/{id}')), { 'iniciativa abriu': ok })
        if (Math.random() < 0.4) {
            espera(2, 5)
            const metodo = i.apoiei ? 'DELETE' : 'POST'
            const r = http.request(metodo, `${API}/api/v1/iniciativas/${i.id}/apoio`, null, p('escrita', `${metodo} /iniciativas/{id}/apoio`))
            check(r, { 'apoio ok': ok })
        }
    }
}

export function handleSummary(data) {
    return resumo(data, 'Valtrea Pulso')
}
