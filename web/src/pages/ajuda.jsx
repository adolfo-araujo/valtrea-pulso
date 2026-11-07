import { Link } from 'react-router-dom'
import { Pagina, estilos } from '../components/ui'
import { LEGAL } from '../lib/legal'

// Central de ajuda: perguntas frequentes do Valtrea Pulso. Edite os textos aqui.
const SECOES = [
  [
    "Como funciona",
    [
      [
        "O que é o Pulso?",
        "Um espaço para moradores mostrarem o que o bairro precisa (um poste apagado, uma calçada quebrada) e reunirem o apoio dos vizinhos, contando depois o que aconteceu."
      ],
      [
        "O Pulso é da prefeitura?",
        "Não. O Pulso não é canal oficial de nenhum órgão público e não tem ligação com partidos. Publicar aqui não abre pedido na prefeitura: para isso, use os canais oficiais da sua cidade e conte as novidades na iniciativa."
      ],
      [
        "Quem vê o que eu publico?",
        "Qualquer pessoa, com o seu primeiro nome. Ninguém vê quem apoiou nem quem denunciou."
      ]
    ]
  ],
  [
    "Iniciativas",
    [
      [
        "Como publico uma iniciativa?",
        "Escolha o seu bairro, toque em Nova iniciativa, escreva um título claro e explique onde é, desde quando e quem é afetado. É preciso ter o e-mail confirmado, e cada pessoa pode publicar até 3 por dia."
      ],
      [
        "Apareceu \"já existe algo parecido\".",
        "Já há uma iniciativa parecida no bairro. Apoiar a que já existe junta mais vizinhos no mesmo pedido. Se for outra coisa, pode publicar normalmente."
      ],
      [
        "Como chamo mais vizinhos?",
        "Na página da iniciativa, em \"Chame mais vizinhos\": envie no WhatsApp, copie o link ou imprima o cartaz com QR Code para colar na portaria e no comércio do bairro."
      ],
      [
        "Como conto que algo mudou?",
        "Na sua iniciativa, escreva uma novidade e, se for o caso, mude a fase para Em andamento ou Resolvida."
      ]
    ]
  ],
  [
    "Respeito e denúncias",
    [
      [
        "O que não pode ser publicado?",
        "Ofensas, dados de outras pessoas (nome, telefone, endereço, CPF), propaganda, golpes e campanha política. Os Termos de uso têm a lista completa."
      ],
      [
        "Como denuncio uma iniciativa?",
        "Na página da iniciativa, toque em Denunciar e escolha o motivo. Com 3 denúncias, ela fica oculta até a revisão da administração."
      ],
      [
        "Minha iniciativa foi ocultada.",
        "Ela recebeu denúncias e está em revisão. Só você a vê. Se discordar, escreva para o e-mail de contato."
      ]
    ]
  ],
  [
    "Conta e dados",
    [
      [
        "O site demorou para abrir. É normal?",
        "Quando o serviço fica um tempo sem uso, a primeira ação pode levar até 1 minuto, porque o servidor estava em repouso. Aparece o aviso \"Acordando o servidor\" e, depois disso, fica rápido."
      ],
      [
        "Não recebi o e-mail de confirmação.",
        "Confira a caixa de spam e as abas Promoções ou Atualizações. Se não estiver lá, entre na conta e peça um novo código na tela de confirmação. O código vale pelo tempo informado no e-mail."
      ],
      [
        "Esqueci a senha.",
        "Na tela de entrar, toque em \"Esqueci a senha\" e informe seu e-mail. Você recebe um código para criar uma senha nova; ele vale pelo tempo informado no e-mail."
      ],
      [
        "Como baixo meus dados ou excluo a conta?",
        "Em Conta você baixa uma cópia de tudo o que guardamos sobre você e pode excluir a conta. A exclusão apaga seus dados na hora, exceto o que a lei manda guardar por um tempo (veja a Política de privacidade)."
      ],
      [
        "Dá para usar como app no celular?",
        "Sim. No Android, abra o site no Chrome e toque em \"Instalar app\" (ou baixe o app quando estiver na Play Store). No iPhone, abra no Safari, toque em Compartilhar e depois em \"Adicionar à Tela de Início\"."
      ]
    ]
  ]
]

export function AjudaPage() {
  return (
    <Pagina titulo="Central de ajuda" largura="max-w-3xl">
      <p className="text-muted">Respostas rápidas para as dúvidas mais comuns sobre o Valtrea Pulso.</p>
      {SECOES.map(([titulo, perguntas]) => (
        <section key={titulo} aria-labelledby={`sec-${titulo}`} className="mt-8">
          <h2 id={`sec-${titulo}`} className="text-xl font-bold">{titulo}</h2>
          <div className="mt-3 divide-y divide-border rounded-2xl border border-border bg-surface">
            {perguntas.map(([p, r]) => (
              <details key={p} className="group p-5">
                <summary className="cursor-pointer list-none font-semibold marker:hidden">
                  <span className="flex items-center justify-between gap-4">{p}<span className="text-primary group-open:rotate-45" aria-hidden="true">+</span></span>
                </summary>
                <p className="mt-2 leading-relaxed text-muted">{r}</p>
              </details>
            ))}
          </div>
        </section>
      ))}
      <section className="mt-10 rounded-2xl border border-border bg-surface p-5">
        <h2 className="font-bold">Não achou a resposta?</h2>
        <p className="mt-1 text-sm text-muted">
          Escreva para <a href={`mailto:${LEGAL.email}`} className={estilos.link}>{LEGAL.email}</a>. Respondemos por e-mail.
          Veja também os <Link to="/termos" className={estilos.link}>Termos de uso</Link> e a{' '}
          <Link to="/privacidade" className={estilos.link}>Política de privacidade</Link>.
        </p>
      </section>
    </Pagina>
  )
}
