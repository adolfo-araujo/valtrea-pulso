# Registro das Operações de Tratamento de Dados Pessoais: Valtrea Pulso

Documento interno (LGPD art. 37), no formato simplificado para agente de tratamento de pequeno porte (Resolução CD/ANPD nº 2/2022). Não publicar.

- **Controlador:** João Adolfo Martins Araújo (pessoa física), Rio de Janeiro/RJ
- **Encarregado / canal:** sem encarregado indicado (dispensa de pequeno porte); canal: contato@valtrea.com.br
- **Elaborado em:** 05/11/2026 · **Atualizado em:** 07/11/2026 · **Revisar:** a cada 12 meses ou a cada nova funcionalidade que trate dados
- **Serviço:** Valtrea Pulso (site pulso.valtrea.com.br, app Android e API valtrea-pulso-api.onrender.com), em testes

## Operações

| # | Operação | Titulares | Dados pessoais | Sensíveis? | Finalidade | Base legal | Onde no sistema | Operadores | Transferência internacional | Retenção | Segurança |
|---|----------|-----------|----------------|-----------|-----------|-----------|-----------------|-----------|-----------------------------|----------|-----------|
| 1 | Cadastro e login | Moradores (18+) | Nome, e-mail, senha (hash), aceite dos Termos | Não | Criar e manter a conta | Art. 7º, V | `/api/v1/auth/*` | Render, Neon | Sim (EUA) | Enquanto a conta existir; nunca confirmados: 30 dias | bcrypt, JWT curto, sessões só como SHA-256, limites por IP, bloqueio por tentativas |
| 2 | Bairro do morador | Moradores | Bairro escolhido (nome, cidade, UF) | Não | Mostrar e publicar no bairro certo | Art. 7º, V | `/api/v1/eu/bairro`, `/api/v1/bairros` | Render, Neon | Sim (EUA) | Enquanto a conta existir; bairros vazios: 30 dias | O bairro de cada pessoa não é público |
| 3 | Iniciativas e atualizações | Autores | Texto publicado, primeiro nome, datas | Não, mas o texto pode trazer opinião política (art. 5º, II): os Termos proíbem propaganda e campanha | Mostrar ao público o que o bairro precisa | Art. 7º, V | `/api/v1/iniciativas` | Render, Neon, Vercel | Sim (EUA) | Até o autor apagar ou excluir a conta; ocultas sem revisão: 60 dias | Só o primeiro nome é público; o formulário avisa para não pôr dados de terceiros |
| 4 | Apoios | Moradores | Qual iniciativa cada pessoa apoiou | Não, mas pode revelar opinião: só a contagem é pública | Contar o apoio sem repetir | Art. 7º, V | `/api/v1/iniciativas/{id}/apoio` | Render, Neon | Sim (EUA) | Até retirar o apoio ou excluir a conta | Ninguém vê quem apoiou, nem a administração |
| 5 | Denúncias e moderação | Moradores e autores | Quem denunciou, motivo; texto revisado | Não | Ocultar e revisar conteúdo que viola os Termos | Art. 7º, IX | `/api/v1/iniciativas/{id}/denuncia`, `/api/v1/admin/moderacao` | Render, Neon | Sim (EUA) | Até a decisão | A administração vê motivos e contagens, nunca quem denunciou nem o e-mail do autor |
| 6 | E-mails | Moradores | E-mail, nome, códigos, título da iniciativa | Não | Confirmar e-mail, nova senha, avisos de segurança e de moderação | Art. 7º, V | `email/*` | Resend | Não (São Paulo) | Códigos: 15 min, apagados 1 dia após vencer | — |
| 7 | Prova do aceite | Moradores | Versão e data do aceite | Não | Comprovar o aceite | Art. 7º, VI | `/api/v1/eu/termos`, cadastro | Render, Neon | Sim (EUA) | Enquanto a conta existir | — |
| 8 | Baixar meus dados | Moradores | Tudo da conta | Não | Acesso e portabilidade | Art. 18, II e V | `/api/v1/eu/dados` | Render | Sim (EUA) | Não guarda | Exige login |
| 9 | Proteção contra abuso | Todos | IP, contagem de pedidos | Não | Bloquear força bruta e spam | Art. 7º, IX | `protecao/*` | Render | Sim (EUA) | IP só em memória, até 1 hora | — |
| 10 | Registro de erros | Todos | Dados técnicos, rota modelo (sem ids) | Não | Corrigir falhas | Art. 7º, IX | `monitoramento/*`, `ValtreaApp.kt` | Sentry | Sim (EUA) | Até 90 dias | Sem usuário, IP, corpo, cabeçalhos |
| 11 | Backup | Todos | Cópia do banco | Não | Recuperar dados | Art. 7º, IX | `.github/workflows/backup.yml` | GitHub | Sim (EUA) | 30 dias | AES-256, senha fora do GitHub |
| 12 | Painel de administração | Todos (em conjunto) | Só contagens | Não | Acompanhar o uso | Art. 7º, IX | `/api/v1/admin/metricas` | Render, Neon | Sim (EUA) | Não guarda | Só administradores |

## Direitos do titular

- Morador: corrigir nome, mudar o bairro, editar ou apagar iniciativas, retirar apoios, baixar os dados e excluir a conta (site ou app). Sem acesso: `/excluir-conta`.
- Pessoa citada numa iniciativa de outra pessoa: denunciar ("Expõe uma pessoa") ou pedir a remoção por contato@valtrea.com.br.
- Resposta em até 15 dias.

## Operadores

| Fornecedor | Serviço | País | Contrato/DPA verificado? |
|-----------|---------|------|--------------------------|
| Render | API | EUA | Pendente |
| Neon | Banco de dados (AWS US East 1) | EUA | Pendente |
| Vercel | Site | EUA e rede global | Pendente |
| Resend | E-mails | Brasil (São Paulo) | Pendente |
| Sentry | Registro de erros | EUA | Pendente |
| GitHub | Backup criptografado | EUA | Pendente |
| ImprovMX / Google (Gmail) | E-mail de contato | EUA | Pendente |

## Pontos de atenção

- Conteúdo político: reclamações sobre o bairro podem citar governos e políticos. Os Termos proíbem propaganda, pedido de voto e ataques; em período eleitoral, revisar a fila de moderação com mais frequência.
- Dados de terceiros: alguém pode citar uma pessoa (vizinho, comerciante). O formulário avisa; a denúncia "Expõe uma pessoa" e a remoção pela administração tratam o caso (Marco Civil, art. 19 e 21).
- Apoios podem revelar opinião: por isso só a contagem é pública e nem a administração vê a lista.

## Revisão de 07/11/2026

- Abaixo-assinado em papel: a página só monta a folha com dados públicos da iniciativa e o número de apoios. Nomes, endereços e assinaturas são colhidos no papel por quem organiza, fora do sistema; o Pulso não recebe nem guarda esses dados. A própria folha avisa que eles servem só para apresentar o pedido ao órgão público.
- Quadro "Leve o pedido ao poder público": texto fixo, sem dados.
- Busca rápida (Ctrl+K), telas de carregamento, de erro e de lista vazia: sem dados novos.
