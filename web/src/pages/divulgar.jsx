import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Copy, Download, MessageCircle, Printer } from 'lucide-react'
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
