import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Copy, Download, FileSignature, Landmark, MessageCircle, Printer } from 'lucide-react'
import { useQrCode } from '../components/qr'
import { estilos } from '../components/ui'
import { CATEGORIAS, chamar } from '../lib/api'

const linkDa = (i) => `${window.location.origin}/i/${i.id}`
const nomeArquivo = (titulo) =>
  `qr-${titulo.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 40) || 'pulso'}.png`

/** Link, WhatsApp, QR Code e cartaz para imprimir: quem vê pode apoiar em segundos. */
export function Divulgar({ iniciativa: i }) {
  const url = linkDa(i)
  const qr = useQrCode(url)
  const [copiado, setCopiado] = useState(false)
  const textoWhats = `Apoie no Valtrea Pulso: "${i.titulo}" (${i.bairro.nome}). Quanto mais vizinhos, mais força: ${url}`

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(url)
      setCopiado(true)
      setTimeout(() => setCopiado(false), 2500)
    } catch {
      window.prompt('Copie o link:', url)
    }
  }

  return (
    <div className="grid items-start gap-5 sm:grid-cols-[1fr_auto]">
      <div>
        <p className="text-sm text-muted">
          Mande no grupo do prédio ou da rua, ou imprima o cartaz e cole na portaria e no comércio do bairro.
        </p>
        <div className="mt-3 flex flex-wrap gap-2">
          <a className={estilos.secundario} href={`https://wa.me/?text=${encodeURIComponent(textoWhats)}`} target="_blank" rel="noreferrer">
            <MessageCircle className="h-4 w-4" aria-hidden="true" /> Enviar no WhatsApp
          </a>
          <button type="button" className={estilos.secundario} onClick={copiar}>
            <Copy className="h-4 w-4" aria-hidden="true" /> {copiado ? 'Link copiado' : 'Copiar link'}
          </button>
          <Link className={estilos.secundario} to={`/i/${i.id}/cartaz`}>
            <Printer className="h-4 w-4" aria-hidden="true" /> Cartaz para imprimir
          </Link>
          <Link className={estilos.secundario} to={`/i/${i.id}/abaixo-assinado`}>
            <FileSignature className="h-4 w-4" aria-hidden="true" /> Abaixo-assinado para imprimir
          </Link>
          {qr && (
            <a className={estilos.secundario} href={qr} download={nomeArquivo(i.titulo)}>
              <Download className="h-4 w-4" aria-hidden="true" /> Baixar QR Code
            </a>
          )}
        </div>
        <span className="sr-only" aria-live="polite">{copiado ? 'Link copiado' : ''}</span>
      </div>
      {qr ? (
        <img src={qr} alt={`QR Code da iniciativa ${i.titulo}`} className="h-32 w-32 rounded-lg bg-white p-1" />
      ) : (
        <div className="h-32 w-32 animate-pulse rounded-lg bg-surface-2" aria-hidden="true" />
      )}
    </div>
  )
}

/** Cartaz em A4, preto e branco, para imprimir e colar no bairro. */
export function CartazPage() {
  const { id } = useParams()
  const [i, setI] = useState(null)
  const [erro, setErro] = useState('')
  useEffect(() => {
    chamar('GET', `/publico/iniciativas/${id}`, null, { autenticar: false }).then(setI).catch((e) => setErro(e.message))
  }, [id])
  const qr = useQrCode(i ? linkDa(i) : null)

  if (!i) return <main className="p-8 text-muted">{erro || 'Carregando…'}</main>
  return (
    <main className="min-h-screen bg-white px-4 py-6 text-black print:p-0">
      <div className="mx-auto mb-6 flex max-w-[210mm] flex-wrap gap-2 print:hidden">
        <button type="button" onClick={() => window.print()}
          className="inline-flex h-11 items-center gap-2 rounded-lg bg-black px-5 font-semibold text-white">
          <Printer className="h-4 w-4" aria-hidden="true" /> Imprimir
        </button>
        <Link to={`/i/${i.id}`} className="inline-flex h-11 items-center rounded-lg border border-black/30 px-5 font-semibold">
          Voltar
        </Link>
      </div>
      <article className="mx-auto flex max-w-[210mm] flex-col items-center border-2 border-black p-[12mm] text-center print:border-0">
        <p className="text-lg font-bold uppercase tracking-widest">Vizinhos do {i.bairro.nome}</p>
        <p className="mt-1 text-sm">{CATEGORIAS[i.categoria]}</p>
        <h1 className="mt-6 text-4xl font-black leading-tight">{i.titulo}</h1>
        <p className="mt-6 max-w-[160mm] whitespace-pre-line text-lg leading-relaxed">
          {i.descricao.length > 420 ? `${i.descricao.slice(0, 420).trim()}…` : i.descricao}
        </p>
        <p className="mt-8 text-2xl font-black">
          {i.apoios === 1 ? '1 vizinho já apoia' : `${i.apoios} vizinhos já apoiam`}
        </p>
        {qr && <img src={qr} alt="QR Code para apoiar a iniciativa" className="mt-6 h-[70mm] w-[70mm]" />}
        <p className="mt-4 text-xl font-bold">Aponte a câmera e apoie também</p>
        <p className="mt-1 break-all text-sm">{linkDa(i)}</p>
        <p className="mt-8 text-xs">
          Valtrea Pulso · pulso.valtrea.com.br · Não é canal oficial da prefeitura nem de nenhum órgão público.
        </p>
      </article>
    </main>
  )
}

/** Onde levar o pedido: os canais oficiais. O Pulso organiza os vizinhos, mas não substitui o pedido formal. */
export function CanaisOficiais() {
  return (
    <div className="flex gap-3">
      <Landmark className="mt-0.5 h-5 w-5 shrink-0 text-primary" aria-hidden="true" />
      <div className="text-sm leading-relaxed text-muted">
        <p>
          O Pulso junta os vizinhos, mas o pedido só vira protocolo quando chega ao órgão responsável. Com os apoios em
          mãos, registre o pedido em um destes canais e guarde o número do protocolo:
        </p>
        <ul className="mt-2 list-disc space-y-1 pl-5">
          <li><strong className="text-foreground">Central de atendimento da prefeitura</strong>: em muitas cidades é o telefone 156, o site ou o app da prefeitura.</li>
          <li><strong className="text-foreground">Ouvidoria da prefeitura</strong>, quando o pedido não foi atendido no prazo.</li>
          <li><strong className="text-foreground">Vereadores e Câmara Municipal</strong>, que podem cobrar a prefeitura e propor melhorias.</li>
          <li><strong className="text-foreground">Fala.BR</strong> (falabr.cgu.gov.br), para assuntos de órgãos federais.</li>
        </ul>
        <p className="mt-2">Depois, conte o número do protocolo nas novidades da iniciativa, para os vizinhos acompanharem.</p>
      </div>
    </div>
  )
}

/** Abaixo-assinado em A4: o pedido, quantos já apoiam no Pulso e linhas para colher assinaturas no papel. */
export function AbaixoAssinadoPage() {
  const { id } = useParams()
  const [i, setI] = useState(null)
  const [erro, setErro] = useState('')
  useEffect(() => {
    chamar('GET', `/publico/iniciativas/${id}`, null, { autenticar: false }).then(setI).catch((e) => setErro(e.message))
  }, [id])

  if (!i) return <main className="p-8 text-muted">{erro || 'Carregando…'}</main>
  const linhas = Array.from({ length: 18 }, (_, n) => n + 1)
  return (
    <main className="min-h-screen bg-white px-4 py-6 text-black print:p-0">
      <div className="mx-auto mb-6 flex max-w-[210mm] flex-wrap gap-2 print:hidden">
        <button type="button" onClick={() => window.print()}
          className="inline-flex h-11 items-center gap-2 rounded-lg bg-black px-5 font-semibold text-white">
          <Printer className="h-4 w-4" aria-hidden="true" /> Imprimir
        </button>
        <Link to={`/i/${i.id}`} className="inline-flex h-11 items-center rounded-lg border border-black/30 px-5 font-semibold">
          Voltar
        </Link>
      </div>
      <article className="mx-auto max-w-[210mm] border border-black/30 p-[12mm] print:border-0 print:p-0">
        <p className="text-sm font-bold uppercase tracking-widest">Abaixo-assinado · {i.bairro.nome}, {i.bairro.cidade} - {i.bairro.uf}</p>
        <h1 className="mt-2 text-2xl font-black leading-tight">{i.titulo}</h1>
        <p className="mt-3 whitespace-pre-line text-sm leading-relaxed">
          {i.descricao.length > 700 ? `${i.descricao.slice(0, 700).trim()}…` : i.descricao}
        </p>
        <p className="mt-3 text-sm">
          <strong>{i.apoios === 1 ? '1 morador já apoia' : `${i.apoios} moradores já apoiam`}</strong> pelo Valtrea Pulso em {linkDa(i)}.
          Nós, abaixo assinados, apoiamos este pedido.
        </p>
        <table className="mt-5 w-full border-collapse text-sm">
          <thead>
            <tr>
              <th className="w-8 border border-black p-1.5 text-left">Nº</th>
              <th className="border border-black p-1.5 text-left">Nome completo</th>
              <th className="w-[38%] border border-black p-1.5 text-left">Rua e número</th>
              <th className="w-[26%] border border-black p-1.5 text-left">Assinatura</th>
            </tr>
          </thead>
          <tbody>
            {linhas.map((n) => (
              <tr key={n} className="h-[9mm]">
                <td className="border border-black p-1.5 text-center">{n}</td>
                <td className="border border-black" /><td className="border border-black" /><td className="border border-black" />
              </tr>
            ))}
          </tbody>
        </table>
        <p className="mt-4 text-[10px] leading-snug">
          Os dados desta folha servem só para apresentar este pedido ao órgão público responsável. Quem recolhe as
          assinaturas guarda as folhas com cuidado e não as usa para outro fim. Valtrea Pulso · pulso.valtrea.com.br ·
          Não é canal oficial da prefeitura nem de nenhum órgão público.
        </p>
      </article>
    </main>
  )
}
