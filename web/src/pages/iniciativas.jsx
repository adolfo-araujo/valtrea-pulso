import { useEffect, useId, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { EyeOff, Flag, Heart, MapPin, Pencil } from 'lucide-react'
import { CATEGORIAS, FASES, MOTIVOS, chamar, dataCurta } from '../lib/api'
import { useAuth } from '../lib/auth'
import { Campo, Mensagem, Pagina, estilos, useEnvio } from '../components/ui'

const COR_FASE = {
  ABERTA: 'border-primary/50 text-primary',
  EM_ANDAMENTO: 'border-amber-400/60 text-amber-300',
  RESOLVIDA: 'border-sky-400/60 text-sky-300',
  ARQUIVADA: 'border-border text-muted',
}

export function Fase({ fase }) {
  return <span className={`rounded-full border px-2.5 py-0.5 text-xs font-semibold ${COR_FASE[fase]}`}>{FASES[fase]}</span>
}

const apoiosTexto = (n) => (n === 1 ? '1 apoio' : `${n} apoios`)

export function CartaoIniciativa({ iniciativa: i }) {
  return (
    <Link to={`/i/${i.id}`} className={`${estilos.cartao} block h-full transition-colors hover:border-primary/60`}>
      <div className="flex flex-wrap items-center gap-2">
        <Fase fase={i.fase} />
        <span className="text-xs text-muted">{CATEGORIAS[i.categoria]}</span>
        {i.oculta && <span className="flex items-center gap-1 text-xs text-danger"><EyeOff className="h-3 w-3" aria-hidden="true" /> oculta para revisão</span>}
      </div>
      <h2 className="mt-2 text-lg font-bold leading-snug">{i.titulo}</h2>
      <p className="mt-2 flex items-center gap-1.5 text-sm text-muted">
        <Heart className={`h-4 w-4 ${i.apoiei ? 'fill-primary text-primary' : ''}`} aria-hidden="true" />
        <span>{apoiosTexto(i.apoios)}{i.apoiei ? ' · você apoia' : ''}</span>
        <span aria-hidden="true">·</span>
        <span>{i.minha ? 'sua' : `por ${i.autor}`}, {dataCurta(i.criadoEm)}</span>
      </p>
    </Link>
  )
}

function Denunciar({ iniciativa, aoDenunciar }) {
  const [aberto, setAberto] = useState(false)
  const [motivo, setMotivo] = useState('')
  const { enviando, erro, executar } = useEnvio()
  const nome = useId()
  if (iniciativa.denunciei) return <p className="text-sm text-muted">Você denunciou esta iniciativa. Obrigado por avisar.</p>
  if (!aberto) {
    return (
      <button type="button" className="inline-flex h-10 items-center gap-1.5 text-sm font-semibold text-muted hover:text-danger" onClick={() => setAberto(true)}>
        <Flag className="h-4 w-4" aria-hidden="true" /> Denunciar
      </button>
    )
  }
  return (
    <form className={`${estilos.cartao} space-y-3`} onSubmit={(e) => {
      e.preventDefault()
      executar(async () => {
        await chamar('POST', `/iniciativas/${iniciativa.id}/denuncia`, { motivo })
        aoDenunciar()
      })
    }}>
      <fieldset>
        <legend className="font-semibold">Qual o problema?</legend>
        <p className="mb-2 text-sm text-muted">Com 3 denúncias, a iniciativa some até a nossa revisão. Ninguém fica sabendo que foi você.</p>
        {Object.entries(MOTIVOS).map(([valor, rotulo]) => (
          <label key={valor} className="flex min-h-10 items-center gap-2.5 text-sm">
            <input type="radio" name={nome} value={valor} checked={motivo === valor} onChange={() => setMotivo(valor)} className="h-4 w-4 accent-[#22C55E]" />
            {rotulo}
          </label>
        ))}
      </fieldset>
      <Mensagem tipo="erro">{erro}</Mensagem>
      <div className="flex flex-col gap-2 sm:flex-row">
        <button type="submit" className={estilos.perigo} disabled={enviando || !motivo}>Enviar denúncia</button>
        <button type="button" className={estilos.secundario} onClick={() => setAberto(false)}>Cancelar</button>
      </div>
    </form>
  )
}

function NovaAtualizacao({ iniciativa, aoAtualizar }) {
  const [texto, setTexto] = useState('')
  const [fase, setFase] = useState(iniciativa.fase)
  const { enviando, erro, executar } = useEnvio()
  const idFase = useId()
  return (
    <form className={`${estilos.cartao} space-y-3`} onSubmit={(e) => {
      e.preventDefault()
      executar(async () => {
        aoAtualizar(await chamar('POST', `/iniciativas/${iniciativa.id}/atualizacoes`, { texto, fase }))
        setTexto('')
      })
    }}>
      <h2 className="text-lg font-bold">Contar uma novidade</h2>
      <Campo rotulo="O que aconteceu?" as="textarea" maxLength={1000} value={texto} onChange={(e) => setTexto(e.target.value)}
        dica="Ex.: Abri o pedido 2026-123 no 156. Não coloque dados de outras pessoas." />
      <div>
        <label htmlFor={idFase} className="text-sm font-semibold">Fase</label>
        <select id={idFase} value={fase} onChange={(e) => setFase(e.target.value)}
          className="mt-1 h-11 w-full rounded-lg border border-border bg-background px-3 text-foreground sm:max-w-xs">
          {Object.entries(FASES).map(([v, r]) => <option key={v} value={v}>{r}</option>)}
        </select>
      </div>
      <Mensagem tipo="erro">{erro}</Mensagem>
      <button type="submit" className={estilos.primario} disabled={enviando || texto.trim().length < 5}>Publicar novidade</button>
    </form>
  )
}

export function IniciativaPage() {
  const { id } = useParams()
  const { perfil } = useAuth()
  const navegar = useNavigate()
  const [i, setI] = useState(null)
  const [naoExiste, setNaoExiste] = useState(false)
  const [aviso, setAviso] = useState('')
  const { enviando, erro, executar } = useEnvio()

  useEffect(() => {
    const pedido = perfil ? chamar('GET', `/iniciativas/${id}`) : chamar('GET', `/publico/iniciativas/${id}`, null, { autenticar: false })
    pedido.then(setI).catch((e) => (e.status === 404 ? setNaoExiste(true) : setAviso(e.message)))
  }, [id, perfil?.id]) // eslint-disable-line react-hooks/exhaustive-deps

  if (naoExiste) {
    return (
      <Pagina titulo="Iniciativa não encontrada" largura="max-w-3xl">
        <p className="text-muted">Ela pode ter sido apagada pelo autor ou estar em revisão.</p>
      </Pagina>
    )
  }
  if (!i) return <Pagina largura="max-w-3xl"><p className="text-muted">{aviso || 'Carregando…'}</p></Pagina>

  const podeApoiar = !i.minha && (i.fase === 'ABERTA' || i.fase === 'EM_ANDAMENTO')
  const apoiar = () => {
    if (!perfil) return navegar('/cadastro')
    return executar(async () => setI(await chamar(i.apoiei ? 'DELETE' : 'POST', `/iniciativas/${i.id}/apoio`)))
  }

  return (
    <Pagina largura="max-w-3xl">
      <Link to={`/b/${i.bairro.id}`} className="flex items-center gap-1.5 text-sm font-semibold text-primary hover:underline">
        <MapPin className="h-4 w-4" aria-hidden="true" /> {i.bairro.nome}, {i.bairro.cidade} - {i.bairro.uf}
      </Link>
      {i.oculta && (
        <p role="status" className="mt-4 rounded-lg border border-danger/50 bg-surface-2 px-4 py-3 text-sm">
          Esta iniciativa recebeu denúncias e está oculta até a nossa revisão. Só você a vê.
        </p>
      )}
      <div className="mt-4 flex flex-wrap items-center gap-2">
        <Fase fase={i.fase} />
        <span className="text-sm text-muted">{CATEGORIAS[i.categoria]}</span>
      </div>
      <h1 className="mt-2 text-3xl font-bold leading-tight tracking-tight">{i.titulo}</h1>
      <p className="mt-2 text-sm text-muted">Por {i.minha ? 'você' : i.autor}, em {dataCurta(i.criadoEm)}</p>
      <p className="mt-6 whitespace-pre-line leading-relaxed">{i.descricao}</p>

      <div className="mt-6 flex flex-col gap-3 rounded-2xl border border-border bg-surface p-5 sm:flex-row sm:items-center sm:justify-between">
        <p className="flex items-center gap-2 text-lg font-bold" aria-live="polite">
          <Heart className="h-5 w-5 fill-primary text-primary" aria-hidden="true" /> {apoiosTexto(i.apoios)}
        </p>
        {podeApoiar && (
          <button type="button" onClick={apoiar} disabled={enviando} aria-pressed={i.apoiei}
            className={i.apoiei ? estilos.secundario : estilos.primario}>
            {i.apoiei ? 'Retirar meu apoio' : perfil ? 'Apoiar' : 'Criar conta para apoiar'}
          </button>
        )}
      </div>
      <Mensagem tipo="erro">{erro}</Mensagem>

      <section aria-labelledby="historico" className="mt-8">
        <h2 id="historico" className="text-xl font-bold">Acompanhamento</h2>
        {i.atualizacoes.length === 0 ? (
          <p className="mt-2 text-sm text-muted">Nenhuma novidade ainda.</p>
        ) : (
          <ol className="mt-4 space-y-4 border-l-2 border-border pl-5">
            {i.atualizacoes.map((a) => (
              <li key={a.id}>
                <p className="text-xs text-muted">{dataCurta(a.criadoEm)}{a.fase && <> · passou para <strong>{FASES[a.fase]}</strong></>}</p>
                <p className="mt-1 whitespace-pre-line">{a.texto}</p>
              </li>
            ))}
          </ol>
        )}
      </section>

      {i.minha && (
        <div className="mt-8 space-y-4">
          <NovaAtualizacao iniciativa={i} aoAtualizar={setI} />
          <div className="flex flex-col gap-2 sm:flex-row">
            <Link to={`/i/${i.id}/editar`} className={estilos.secundario}><Pencil className="h-4 w-4" aria-hidden="true" /> Editar</Link>
            <button type="button" className={estilos.perigo} disabled={enviando} onClick={() => {
              if (!window.confirm('Apagar esta iniciativa, os apoios e as novidades? Não dá para desfazer.')) return
              executar(async () => {
                await chamar('DELETE', `/iniciativas/${i.id}`)
                navegar('/minhas', { replace: true })
              })
            }}>Apagar</button>
          </div>
        </div>
      )}
      {perfil?.emailConfirmado && !i.minha && (
        <div className="mt-8"><Denunciar iniciativa={i} aoDenunciar={() => setI({ ...i, denunciei: true })} /></div>
      )}
    </Pagina>
  )
}

export function FormIniciativaPage() {
  const { id } = useParams()
  const { perfil } = useAuth()
  const navegar = useNavigate()
  const [dados, setDados] = useState({ titulo: '', descricao: '', categoria: 'ILUMINACAO' })
  const { enviando, erro, executar } = useEnvio()
  const idCategoria = useId()

  useEffect(() => {
    if (id) chamar('GET', `/iniciativas/${id}`).then((i) => setDados({ titulo: i.titulo, descricao: i.descricao, categoria: i.categoria }))
  }, [id])

  if (!id && !perfil?.bairro) {
    return (
      <Pagina titulo="Nova iniciativa" largura="max-w-2xl">
        <p className="text-muted">Primeiro, escolha o seu bairro.</p>
        <Link to="/bairro" className={`${estilos.primario} mt-4`}>Escolher bairro</Link>
      </Pagina>
    )
  }

  const enviar = (e) => {
    e.preventDefault()
    executar(async () => {
      const salva = await chamar(id ? 'PUT' : 'POST', id ? `/iniciativas/${id}` : '/iniciativas', dados)
      navegar(`/i/${salva.id}`, { replace: true })
    })
  }

  return (
    <Pagina titulo={id ? 'Editar iniciativa' : 'Nova iniciativa'} largura="max-w-2xl">
      {!id && <p className="mb-6 text-muted">No bairro <strong className="text-foreground">{perfil.bairro.nome}</strong>. Aparece para todos com o seu primeiro nome.</p>}
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Campo rotulo="Título" maxLength={120} value={dados.titulo} onChange={(e) => setDados({ ...dados, titulo: e.target.value })}
          placeholder="Ex.: Trocar a lâmpada do poste da Rua das Flores" />
        <div>
          <label htmlFor={idCategoria} className="text-sm font-semibold">Categoria</label>
          <select id={idCategoria} value={dados.categoria} onChange={(e) => setDados({ ...dados, categoria: e.target.value })}
            className="mt-1 h-11 w-full rounded-lg border border-border bg-background px-3 text-foreground">
            {Object.entries(CATEGORIAS).map(([v, r]) => <option key={v} value={v}>{r}</option>)}
          </select>
        </div>
        <Campo rotulo="O que precisa e por quê" as="textarea" maxLength={2000} value={dados.descricao}
          onChange={(e) => setDados({ ...dados, descricao: e.target.value })}
          dica="Conte onde é, desde quando e quem é afetado. Não coloque nome, telefone ou endereço de pessoas, nem propaganda." />
        <Mensagem tipo="erro">{erro}</Mensagem>
        <button type="submit" className={`${estilos.primario} w-full`}
          disabled={enviando || dados.titulo.trim().length < 8 || dados.descricao.trim().length < 20}>
          {id ? 'Salvar' : 'Publicar'}
        </button>
        <p className="text-xs text-muted">Pelo menos 8 caracteres no título e 20 na explicação. Até 3 iniciativas por dia.</p>
      </form>
    </Pagina>
  )
}

export function MinhasPage() {
  const [lista, setLista] = useState(null)
  const { erro, executar } = useEnvio()
  useEffect(() => {
    executar(async () => setLista(await chamar('GET', '/eu/iniciativas')))
  }, []) // eslint-disable-line react-hooks/exhaustive-deps
  return (
    <Pagina titulo="Minhas iniciativas">
      <Mensagem tipo="erro">{erro}</Mensagem>
      {lista?.length === 0 && (
        <p className="text-muted">Você ainda não publicou nada. <Link to="/nova" className={estilos.link}>Publicar a primeira</Link></p>
      )}
      <ul className="grid gap-4 sm:grid-cols-2">
        {lista?.map((i) => <li key={i.id}><CartaoIniciativa iniciativa={i} /></li>)}
      </ul>
    </Pagina>
  )
}
