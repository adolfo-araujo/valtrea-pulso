import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './lib/auth'
import { AvisoTermos } from './components/aviso-termos'
import { Pagina } from './components/ui'
import { InicioPage } from './pages/inicio'
import { CadastroPage, ConfirmarEmailPage, EntrarPage, EsqueciSenhaPage } from './pages/auth'
import { BairroPage, EscolherBairroPage } from './pages/bairros'
import { FormIniciativaPage, IniciativaPage, MinhasPage } from './pages/iniciativas'
import { CartazPage } from './pages/divulgar'
import { ContaPage } from './pages/conta'
import { AcessibilidadePage, ExcluirContaPage, PrivacidadePage, TermosPage } from './pages/legal'

function Carregando() {
  return <Pagina><p className="text-muted">Carregando…</p></Pagina>
}

function Protegida({ children }) {
  const { perfil, carregando } = useAuth()
  if (carregando) return <Carregando />
  if (!perfil) return <Navigate to="/entrar" replace />
  return children
}

/** Depois de entrar: o bairro da pessoa ou, se ainda não escolheu, a escolha. */
function Painel() {
  const { perfil, carregando } = useAuth()
  if (carregando) return <Carregando />
  if (!perfil) return <Navigate to="/entrar" replace />
  if (!perfil.emailConfirmado) return <Navigate to="/confirmar-email" replace />
  return <Navigate to={perfil.bairro ? `/b/${perfil.bairro.id}` : '/bairro'} replace />
}

export default function App() {
  return (
    <>
      <AvisoTermos />
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/entrar" element={<EntrarPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/esqueci-senha" element={<EsqueciSenhaPage />} />
        <Route path="/confirmar-email" element={<ConfirmarEmailPage />} />
        <Route path="/painel" element={<Painel />} />
        <Route path="/b/:id" element={<BairroPage />} />
        <Route path="/i/:id" element={<IniciativaPage />} />
        <Route path="/bairro" element={<Protegida><EscolherBairroPage /></Protegida>} />
        <Route path="/nova" element={<Protegida><FormIniciativaPage /></Protegida>} />
        <Route path="/i/:id/cartaz" element={<CartazPage />} />
        <Route path="/i/:id/editar" element={<Protegida><FormIniciativaPage /></Protegida>} />
        <Route path="/minhas" element={<Protegida><MinhasPage /></Protegida>} />
        <Route path="/conta" element={<Protegida><ContaPage /></Protegida>} />
        <Route path="/termos" element={<TermosPage />} />
        <Route path="/privacidade" element={<PrivacidadePage />} />
        <Route path="/acessibilidade" element={<AcessibilidadePage />} />
        <Route path="/excluir-conta" element={<ExcluirContaPage />} />
        <Route path="*" element={<Pagina titulo="Página não encontrada"><p className="text-muted">Confira o endereço.</p></Pagina>} />
      </Routes>
    </>
  )
}
