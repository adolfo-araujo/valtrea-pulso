import { useEffect, useId, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { MapPin, Plus } from 'lucide-react'
import { FASES, UFS, chamar } from '../lib/api'
import { useAuth } from '../lib/auth'
import { Campo, Mensagem, Pagina, PrimeirosPassos, Vazio, estilos, useEnvio } from '../components/ui'
import { CartaoIniciativa } from './iniciativas'

export const nomeDoBairro = (b) => `${b.nome}, ${b.cidade} - ${b.uf}`

function SeletorUf({ valor, aoMudar, rotulo = 'Estado', vazio = 'Todos' }) {
  const id = useId()
  return (
    <div>
      <label htmlFor={id} className="text-sm font-semibold">{rotulo}</label>
      <select id={id} value={valor} onChange={(e) => aoMudar(e.target.value)}
        className="mt-1 h-11 w-full rounded-xl border border-border bg-surface px-3 text-foreground">
        {vazio !== null && <option value="">{vazio}</option>}
        {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
      </select>
    </div>
  )
}

/** Busca de bairros (aberta). {@code aoEscolher} recebe o bairro; sem ele, cada resultado é um link. */
export function BuscaBairros({ aoEscolher, rotuloBotao = 'Ver' }) {
  const [uf, setUf] = useState('')
  const [busca, setBusca] = useState('')
  const [resultados, setResultados] = useState(null)
  const { enviando, erro, executar } = useEnvio()

  const procurar = (e) => {
    e.preventDefault()
    executar(async () => {
      const q = new URLSearchParams()
      if (uf) q.set('uf', uf)
      if (busca.trim()) q.set('busca', busca.trim())
      setResultados(await chamar('GET', `/publico/bairros?${q}`, null, { autenticar: false }))
    })
  }

  return (
    <div>
      <form onSubmit={procurar} className="grid gap-3 sm:grid-cols-[8rem_1fr_auto] sm:items-end" role="search" aria-label="Procurar bairro">
        <SeletorUf valor={uf} aoMudar={setUf} />
        <Campo rotulo="Bairro ou cidade" placeholder="Ex.: Tijuca" value={busca} onChange={(e) => setBusca(e.target.value)} />
        <button type="submit" className={estilos.primario} disabled={enviando}>{enviando ? 'Procurando…' : 'Procurar'}</button>
      </form>
      <div className="mt-4" aria-live="polite">
        <Mensagem tipo="erro">{erro}</Mensagem>
        {resultados?.length === 0 && <p className="text-sm text-muted">Nenhum bairro encontrado com esse nome.</p>}
        {resultados?.length > 0 && (
          <ul className="divide-y divide-border rounded-xl border border-border">
            {resultados.map((b) => (
              <li key={b.id} className="flex items-center justify-between gap-3 px-4 py-3">
                <div>
                  <p className="font-semibold">{nomeDoBairro(b)}</p>
                  <p className="text-xs text-muted">{b.iniciativas === 1 ? '1 iniciativa aberta' : `${b.iniciativas} iniciativas abertas`}</p>
                </div>
                {aoEscolher ? (
                  <button type="button" className={estilos.secundario} onClick={() => aoEscolher(b)}>{rotuloBotao}</button>
                ) : (
                  <Link to={`/b/${b.id}`} className={estilos.secundario}>{rotuloBotao}</Link>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

export function EscolherBairroPage() {
  const { perfil, recarregar } = useAuth()
  const navegar = useNavigate()
  const [novo, setNovo] = useState({ nome: '', cidade: '', uf: 'SP' })
  const { enviando, erro, executar } = useEnvio()

  const morarEm = (bairro) => executar(async () => {
    await chamar('PUT', '/eu/bairro', { bairroId: bairro.id })
    await recarregar()
    navegar(`/b/${bairro.id}`)
  })

  const criar = (e) => {
    e.preventDefault()
    executar(async () => {
      const bairro = await chamar('POST', '/bairros', novo)
      await chamar('PUT', '/eu/bairro', { bairroId: bairro.id })
      await recarregar()
      navegar(`/b/${bairro.id}`)
    })
  }

  return (
    <Pagina titulo="Escolha o seu bairro" largura="max-w-3xl">
      {perfil?.bairro && <p className="mb-4 text-muted">Hoje: <strong className="text-foreground">{nomeDoBairro(perfil.bairro)}</strong>. As iniciativas que você já publicou continuam no bairro delas.</p>}
      {!perfil?.emailConfirmado && (
        <Mensagem>Confirme seu e-mail antes. <Link to="/confirmar-email" className={estilos.link}>Confirmar agora</Link></Mensagem>
      )}
      <Mensagem tipo="erro">{erro}</Mensagem>
      <section aria-labelledby="procurar-titulo" className={`${estilos.cartao} mt-4`}>
        <h2 id="procurar-titulo" className="mb-4 text-lg font-bold">Procure o seu bairro</h2>
        <BuscaBairros aoEscolher={morarEm} rotuloBotao="Moro aqui" />
      </section>
      <section aria-labelledby="criar-titulo" className={`${estilos.cartao} mt-6`}>
        <h2 id="criar-titulo" className="text-lg font-bold">Não achou? Cadastre</h2>
        <p className="mb-4 mt-1 text-sm text-muted">Se alguém já tiver cadastrado com o mesmo nome, você entra no mesmo bairro.</p>
        <form onSubmit={criar} className="grid gap-3 sm:grid-cols-[1fr_1fr_7rem]" noValidate>
          <Campo rotulo="Bairro" maxLength={80} value={novo.nome} onChange={(e) => setNovo({ ...novo, nome: e.target.value })} />
          <Campo rotulo="Cidade" maxLength={80} value={novo.cidade} onChange={(e) => setNovo({ ...novo, cidade: e.target.value })} />
          <SeletorUf valor={novo.uf} aoMudar={(uf) => setNovo({ ...novo, uf })} vazio={null} />
          <button type="submit" className={`${estilos.primario} sm:col-span-3`}
            disabled={enviando || novo.nome.trim().length < 2 || novo.cidade.trim().length < 2}>
            Cadastrar e morar aqui
          </button>
        </form>
      </section>
    </Pagina>
  )
}

export function BairroPage() {
  const { id } = useParams()
  const { perfil } = useAuth()
  const [bairro, setBairro] = useState(null)
  const [lista, setLista] = useState(null)
  const [fase, setFase] = useState('')
  const [ordem, setOrdem] = useState('recentes')
  const [naoExiste, setNaoExiste] = useState(false)
  const { erro, executar } = useEnvio()
  const idFase = useId()
  const idOrdem = useId()

  useEffect(() => {
    chamar('GET', `/publico/bairros/${id}`, null, { autenticar: false })
      .then(setBairro).catch((e) => (e.status === 404 ? setNaoExiste(true) : null))
  }, [id])

  useEffect(() => {
    executar(async () => {
      const q = new URLSearchParams({ ordem })
      if (fase) q.set('fase', fase)
      // Logado, a lista diz o que a pessoa já apoiou.
      setLista(perfil
        ? await chamar('GET', `/bairros/${id}/iniciativas?${q}`)
        : await chamar('GET', `/publico/bairros/${id}/iniciativas?${q}`, null, { autenticar: false }))
    })
  }, [id, fase, ordem, perfil?.id]) // eslint-disable-line react-hooks/exhaustive-deps

  if (naoExiste) {
    return <Pagina titulo="Bairro não encontrado"><Link to="/" className={estilos.link}>Procurar outro bairro</Link></Pagina>
  }
  const meuBairro = perfil?.bairro?.id === id

  return (
    <Pagina>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="flex items-center gap-1.5 text-sm font-semibold text-primary">
            <MapPin className="h-4 w-4" aria-hidden="true" /> {bairro ? `${bairro.cidade} - ${bairro.uf}` : '…'}
            {meuBairro && <span className="ml-2 rounded-full border border-primary/50 px-2 text-xs">seu bairro</span>}
          </p>
          <h1 className="mt-1 text-3xl font-bold tracking-tight">{bairro?.nome ?? 'Carregando…'}</h1>
        </div>
        {meuBairro ? (
          <Link to="/nova" className={estilos.primario}><Plus className="h-4 w-4" aria-hidden="true" /> Nova iniciativa</Link>
        ) : perfil ? (
          <Link to="/bairro" className={estilos.secundario}>{perfil.bairro ? 'Mudar para este bairro' : 'Escolher meu bairro'}</Link>
        ) : (
          <Link to="/cadastro" className={estilos.primario}>Criar conta para participar</Link>
        )}
      </div>

      {meuBairro && lista && (
        <PrimeirosPassos passos={[
          { feito: Boolean(perfil?.emailConfirmado), titulo: 'Confirme seu e-mail', texto: 'Para publicar e apoiar.', para: '/confirmar-email', rotulo: 'Confirmar agora' },
          { feito: true, titulo: 'Escolha o seu bairro' },
          { feito: lista.some((i) => i.apoiei || i.minha), titulo: 'Apoie ou publique uma iniciativa',
            texto: lista.length > 0 ? 'Abra uma iniciativa abaixo e toque em Apoiar, ou publique a sua.' : 'Ainda não há iniciativas aqui: seja a primeira pessoa a publicar.',
            para: '/nova', rotulo: 'Nova iniciativa' },
        ]} />
      )}

      <div className="mb-4 grid grid-cols-2 gap-3 sm:max-w-md">
        <div>
          <label htmlFor={idFase} className="text-sm font-semibold">Mostrar</label>
          <select id={idFase} value={fase} onChange={(e) => setFase(e.target.value)}
            className="mt-1 h-11 w-full rounded-xl border border-border bg-surface px-3 text-foreground">
            <option value="">Abertas e em andamento</option>
            <option value="RESOLVIDA">{FASES.RESOLVIDA}s</option>
            <option value="ARQUIVADA">{FASES.ARQUIVADA}s</option>
          </select>
        </div>
        <div>
          <label htmlFor={idOrdem} className="text-sm font-semibold">Ordenar por</label>
          <select id={idOrdem} value={ordem} onChange={(e) => setOrdem(e.target.value)}
            className="mt-1 h-11 w-full rounded-xl border border-border bg-surface px-3 text-foreground">
            <option value="recentes">Mais recentes</option>
            <option value="apoios">Mais apoiadas</option>
          </select>
        </div>
      </div>

      <Mensagem tipo="erro">{erro}</Mensagem>
      {lista?.length === 0 && (
        <Vazio titulo="Nenhuma iniciativa aqui ainda"
          texto={meuBairro ? 'Seja a primeira pessoa a propor uma melhoria para o bairro.' : 'Quando os moradores publicarem pedidos de melhoria, eles aparecem aqui.'}
          acao={meuBairro ? ['Publicar a primeira', '/nova'] : null} />
      )}
      <ul className="grid gap-4 sm:grid-cols-2">
        {lista?.map((i) => <li key={i.id}><CartaoIniciativa iniciativa={i} /></li>)}
      </ul>
    </Pagina>
  )
}
