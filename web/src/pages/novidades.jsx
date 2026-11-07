import { Pagina } from '../components/ui'

// O que mudou em cada versão. Acrescente no topo a cada novidade.
const VERSOES = [
  [
    "7 de novembro de 2026",
    [
      "Telas de carregamento, mensagens de erro mais claras e listas vazias com o próximo passo.",
      "Abaixo-assinado em A4 para colher assinaturas no papel e levar à prefeitura.",
      "Na iniciativa, os canais oficiais para registrar o pedido (central de atendimento, ouvidoria, Câmara).",
      "Busca rápida: aperte Ctrl+K (ou / ) no computador para ir a qualquer tela."
    ]
  ],
  [
    "6 de novembro de 2026",
    [
      "Ao escrever uma iniciativa, o Pulso mostra se já existe uma parecida no bairro.",
      "Aviso antes de publicar quando o texto parece ter CPF, telefone, e-mail ou campanha política.",
      "Chame mais vizinhos: WhatsApp, link, QR Code e cartaz A4 para imprimir.",
      "Novo visual da marca Valtrea: fonte Inter e logo própria do produto.",
      "Dá para instalar como app no iPhone: no Safari, Compartilhar → Adicionar à Tela de Início.",
      "Quando o servidor estava parado, aparece \"Acordando o servidor\" em vez de parecer travado.",
      "Central de ajuda com as dúvidas mais comuns e prévia com imagem ao compartilhar o link.",
      "Termos de uso e Política de privacidade revisados (cookies e propriedade intelectual)."
    ]
  ],
  [
    "5 de novembro de 2026",
    [
      "Lançamento da versão de testes: bairros, iniciativas com apoio dos vizinhos, novidades e fases, denúncias, baixar meus dados e excluir conta."
    ]
  ]
]

export function NovidadesPage() {
  return (
    <Pagina titulo="Novidades" largura="max-w-3xl">
      <p className="text-muted">O que mudou no Valtrea Pulso, do mais recente para o mais antigo.</p>
      <ol className="mt-8 space-y-8 border-l-2 border-border pl-6">
        {VERSOES.map(([data, itens]) => (
          <li key={data}>
            <h2 className="text-lg font-bold">{data}</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-muted">
              {itens.map((i) => <li key={i}>{i}</li>)}
            </ul>
          </li>
        ))}
      </ol>
    </Pagina>
  )
}
