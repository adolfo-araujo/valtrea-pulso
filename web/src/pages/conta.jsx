import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { CATEGORIAS, MOTIVOS, chamar, dataCurta } from '../lib/api'
import { useAuth } from '../lib/auth'
import { Campo, Mensagem, Pagina, estilos, useEnvio } from '../components/ui'

function Numeros({ titulo, itens }) {
  return (
    <div>
      <h3 className="mb-2 font-semibold">{titulo}</h3>
      <dl className="grid gap-2 sm:grid-cols-2">
        {itens.map(([rotulo, valor]) => (
          <div key={rotulo} className="flex justify-between rounded-lg bg-surface-2 px-3 py-2 text-sm">
            <dt className="text-muted">{rotulo}</dt>
            <dd className="font-bold tabular-nums">{valor}</dd>
          </div>
        ))}
      </dl>
    </div>
  )
}

function Administracao() {
  const [m, setM] = useState(null)
  const [erro, setErro] = useState('')
  useEffect(() => {
    chamar('GET', '/admin/metricas').then(setM).catch((e) => setErro(e.message))
  }, [])
  return (
    <section aria-labelledby="admin-titulo" className={estilos.cartao}>
      <h2 id="admin-titulo" className="text-lg font-bold">Administração</h2>
      <p className="mb-4 text-sm text-muted">Só números somados. Nenhum nome, e-mail ou apoio de alguém aparece aqui.</p>
      <Mensagem tipo="erro">{erro}</Mensagem>
      {m && (
        <div className="space-y-5">
          <Numeros titulo="Contas" itens={[
            ['Total', m.contas.total], ['E-mail confirmado', m.contas.confirmadas],
            ['Novas em 7 dias', m.contas.novas7Dias], ['Novas em 30 dias', m.contas.novas30Dias],
            ['Conectadas em algum aparelho', m.contas.comSessaoAtiva],
          ]} />
          <Numeros titulo="Uso" itens={[
            ['Bairros', m.uso.bairros], ['Bairros com iniciativas', m.uso.bairrosComIniciativas],
            ['Iniciativas', m.uso.iniciativas], ['Abertas ou em andamento', m.uso.ativas],
            ['Resolvidas', m.uso.resolvidas], ['Publicadas em 30 dias', m.uso.iniciativas30Dias],
            ['Apoios', m.uso.apoios], ['Apoios em 30 dias', m.uso.apoios30Dias],
            ['Esperando revisão', m.uso.ocultas],
          ]} />
          <Numeros titulo="Cadastros por semana" itens={m.cadastrosPorSemana.map((s) => [
            `Semana de ${s.semana.slice(8, 10)}/${s.semana.slice(5, 7)}`, s.cadastros,
          ])} />
        </div>
      )}
    </section>
  )
}

function Moderacao() {
  const [fila, setFila] = useState(null)
  const [aviso, setAviso] = useState('')
  const { enviando, erro, executar } = useEnvio()
  const carregar = () => executar(async () => setFila(await chamar('GET', '/admin/moderacao')))
  useEffect(() => {
    carregar()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const decidir = (item, manter) => executar(async () => {
    if (manter) await chamar('POST', `/admin/moderacao/${item.id}/manter`)
    else await chamar('DELETE', `/admin/moderacao/${item.id}`)
    setAviso(manter ? `"${item.titulo}" voltou a aparecer.` : `"${item.titulo}" foi removida e o autor foi avisado.`)
    setFila((f) => f.filter((x) => x.id !== item.id))
  })

  return (
    <section aria-labelledby="moderacao-titulo" className={estilos.cartao}>
      <h2 id="moderacao-titulo" className="text-lg font-bold">Moderação</h2>
      <p className="mb-4 text-sm text-muted">
        Iniciativas ocultas depois de 3 denúncias. Você vê o texto, o primeiro nome do autor e os motivos, nunca quem
        denunciou. Sem decisão em 60 dias, a iniciativa é apagada.
      </p>
      <Mensagem tipo="erro">{erro}</Mensagem>
      <Mensagem>{aviso}</Mensagem>
      {fila?.length === 0 && <p className="text-sm text-muted">Nada esperando revisão.</p>}
      <ul className="space-y-4">
        {fila?.map((item) => (
          <li key={item.id} className="rounded-xl border border-border p-4">
            <h3 className="font-bold">{item.titulo}</h3>
            <p className="text-xs text-muted">
              {CATEGORIAS[item.categoria]} · {item.bairro} · por {item.autor} · oculta em {dataCurta(item.ocultaEm)}
            </p>
            <p className="mt-2 whitespace-pre-line text-sm">{item.descricao}</p>
            <p className="mt-2 text-sm">
              <span className="font-semibold">Denúncias:</span>{' '}
              {Object.entries(item.denuncias).map(([motivo, n]) => `${MOTIVOS[motivo]} (${n})`).join('; ')}
            </p>
            <div className="mt-3 flex flex-col gap-2 sm:flex-row">
              <button type="button" className={estilos.secundario} disabled={enviando} onClick={() => decidir(item, true)}>
                Manter (segue os Termos)
              </button>
              <button type="button" className={estilos.perigo} disabled={enviando} onClick={() => decidir(item, false)}>
                Remover
              </button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}

export function ContaPage() {
  const { perfil, recarregar, sair } = useAuth()
  const navegar = useNavigate()
  const [nome, setNome] = useState(perfil?.nome ?? '')
  const [aviso, setAviso] = useState('')
  const [senha, setSenha] = useState('')
  const [excluindo, setExcluindo] = useState(false)
  const { enviando, erro, executar } = useEnvio()

  const baixar = () => executar(async () => {
    const dados = await chamar('GET', '/eu/dados')
    const url = URL.createObjectURL(new Blob([JSON.stringify(dados, null, 2)], { type: 'application/json' }))
    const a = document.createElement('a')
    a.href = url
    a.download = `valtrea-pulso-meus-dados-${new Date().toISOString().slice(0, 10)}.json`
    a.click()
    URL.revokeObjectURL(url)
  })

  return (
    <Pagina titulo="Minha conta" largura="max-w-3xl">
      <div className="space-y-6">
        <Mensagem tipo="erro">{erro}</Mensagem>
        <Mensagem>{aviso}</Mensagem>

        <section aria-labelledby="perfil-titulo" className={estilos.cartao}>
          <h2 id="perfil-titulo" className="mb-4 text-lg font-bold">Seus dados</h2>
          <p className="mb-4 text-sm text-muted">E-mail: <span className="text-foreground">{perfil?.email}</span>
            {perfil && !perfil.emailConfirmado && <> · <Link to="/confirmar-email" className={estilos.link}>confirmar</Link></>}
          </p>
          <form className="flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={(e) => {
            e.preventDefault()
            executar(async () => {
              await chamar('PATCH', '/eu', { nome })
              await recarregar()
              setAviso('Nome atualizado.')
            })
          }}>
            <Campo rotulo="Nome" className="flex-1" maxLength={120} value={nome} onChange={(e) => setNome(e.target.value)} />
            <button type="submit" className={estilos.secundario} disabled={enviando || !nome.trim()}>Salvar</button>
          </form>
        </section>

        <section aria-labelledby="dados-titulo" className={estilos.cartao}>
          <h2 id="dados-titulo" className="text-lg font-bold">Baixar meus dados</h2>
          <p className="mt-1 text-sm text-muted">
            Uma cópia do cadastro, do bairro, das suas iniciativas, dos seus apoios e das suas denúncias, em um arquivo
            que outros serviços conseguem ler (JSON).
          </p>
          <button type="button" className={`${estilos.secundario} mt-3`} disabled={enviando} onClick={baixar}>Baixar meus dados</button>
        </section>

        <section aria-labelledby="bairro-titulo" className={estilos.cartao}>
          <h2 id="bairro-titulo" className="text-lg font-bold">Seu bairro</h2>
          <p className="mt-1 text-sm text-muted">
            {perfil?.bairro ? `${perfil.bairro.nome}, ${perfil.bairro.cidade} - ${perfil.bairro.uf}` : 'Ainda não escolhido.'}
          </p>
          <Link to="/bairro" className={`${estilos.secundario} mt-3`}>{perfil?.bairro ? 'Mudar de bairro' : 'Escolher bairro'}</Link>
        </section>

        {perfil?.admin && <Administracao />}
        {perfil?.admin && <Moderacao />}

        <section aria-labelledby="excluir-titulo" className={estilos.cartao}>
          <h2 id="excluir-titulo" className="text-lg font-bold">Excluir conta</h2>
          <p className="mt-1 text-sm text-muted">
            Apaga na hora seu cadastro, as iniciativas que você publicou, seus apoios e suas denúncias. Não dá para desfazer.
          </p>
          {excluindo ? (
            <form className="mt-3 space-y-3" onSubmit={(e) => {
              e.preventDefault()
              executar(async () => {
                await chamar('DELETE', '/eu', { senha })
                await sair()
                navegar('/', { replace: true })
              })
            }}>
              <Campo rotulo="Digite sua senha para confirmar" type="password" autoComplete="current-password" value={senha} onChange={(e) => setSenha(e.target.value)} />
              <div className="flex flex-col gap-2 sm:flex-row">
                <button type="submit" className={estilos.perigo} disabled={enviando || !senha}>Excluir minha conta</button>
                <button type="button" className={estilos.secundario} onClick={() => setExcluindo(false)}>Cancelar</button>
              </div>
            </form>
          ) : (
            <button type="button" className={`${estilos.perigo} mt-3`} onClick={() => setExcluindo(true)}>Excluir conta</button>
          )}
        </section>
      </div>
    </Pagina>
  )
}
