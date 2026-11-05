import { Link, Navigate } from 'react-router-dom'
import { Heart, Megaphone, ShieldCheck, TrendingUp } from 'lucide-react'
import { useAuth } from '../lib/auth'
import { Pagina, estilos } from '../components/ui'
import { BuscaBairros } from './bairros'

const PASSOS = [
  { icone: Megaphone, titulo: 'Conte o que o bairro precisa', texto: 'O poste apagado, a calçada quebrada, a praça sem cuidado. Cada pedido vira uma iniciativa do bairro.' },
  { icone: Heart, titulo: 'Os vizinhos apoiam', texto: 'Cada morador apoia uma vez. O número mostra o que mais importa para quem mora ali.' },
  { icone: TrendingUp, titulo: 'Acompanhe até resolver', texto: 'Quem publicou conta as novidades: pedido aberto na prefeitura, obra começou, resolvido.' },
]

export function InicioPage() {
  const { perfil, carregando } = useAuth()
  if (!carregando && perfil) return <Navigate to="/painel" replace />
  return (
    <Pagina>
      <section className="py-8 sm:py-14">
        <p className="text-sm font-bold text-primary">Valtrea Pulso · em testes</p>
        <h1 className="mt-2 max-w-2xl text-4xl font-black leading-tight tracking-tight sm:text-5xl">A voz organizada do seu bairro.</h1>
        <p className="mt-5 max-w-xl text-lg leading-relaxed text-muted">
          Moradores registram o que o bairro precisa, reúnem apoio e acompanham o que acontece com cada iniciativa.
        </p>
        <div className="mt-8 flex flex-col gap-3 sm:flex-row">
          <Link to="/cadastro" className={estilos.primario}>Criar conta grátis</Link>
          <Link to="/entrar" className={estilos.secundario}>Já tenho conta</Link>
        </div>
      </section>
      <section aria-labelledby="ver-bairro" className={estilos.cartao}>
        <h2 id="ver-bairro" className="mb-4 text-xl font-extrabold">Veja o que o seu bairro está pedindo</h2>
        <BuscaBairros />
      </section>
      <section aria-labelledby="como-funciona" className="mt-10 border-t border-border py-10">
        <h2 id="como-funciona" className="text-2xl font-extrabold">Como funciona</h2>
        <ol className="mt-8 grid gap-6 sm:grid-cols-3">
          {PASSOS.map(({ icone: Icone, titulo, texto }) => (
            <li key={titulo} className={estilos.cartao}>
              <Icone className="h-6 w-6 text-primary" aria-hidden="true" />
              <h3 className="mt-3 font-bold">{titulo}</h3>
              <p className="mt-1 text-sm leading-relaxed text-muted">{texto}</p>
            </li>
          ))}
        </ol>
        <p className="mt-8 flex items-start gap-2 text-sm text-muted">
          <ShieldCheck className="h-5 w-5 shrink-0 text-primary" aria-hidden="true" />
          O Pulso não é canal oficial da prefeitura e não tem ligação com partidos. Em público aparece só o seu primeiro nome.
        </p>
      </section>
    </Pagina>
  )
}
