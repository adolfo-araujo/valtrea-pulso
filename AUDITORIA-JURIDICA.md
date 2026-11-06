# Auditoria Jurídica: Valtrea Pulso

**Data:** 06/11/2026 · **Gerado com:** skill blindagem-juridica (versão `66c8cef`) · **Modo:** corrigir

> ⚖️ Este relatório é uma auditoria técnica automatizada e **não constitui parecer jurídico**.
> Ele reduz riscos, mas não garante ausência de questionamentos. Termos de uso e Política de privacidade devem ser revisados por advogado(a).

## Placar

| # | Item | Antes | Depois | Observação |
|---|------|-------|--------|-----------|
| 1 | Política de privacidade | ⚠️ | ✅ | Responsável, canal, bases legais por finalidade, o que é público, fornecedores e países, tabela de retenção; nova seção de cookies e armazenamento |
| 2 | Termos de uso | ⚠️ | ✅ | Responsável, condutas proibidas, suspensão com aviso, mudanças sem aceite tácito, foro do consumidor; nova cláusula de propriedade intelectual |
| 3 | Aviso de cookies | ✅ | ✅ | Só armazenamento estritamente necessário (sessão), sem cookies de terceiros: não exige banner de consentimento; agora explicado na Política |
| 4 | Política de reembolso | ➖ | ➖ | Serviço gratuito, sem venda |
| 5 | Formulários (LGPD) | ⚠️ | ✅ | Cadastro com caixa desmarcada: "li e aceito os Termos e **estou ciente** da Política" (a Política informa, não é "aceita"); declaração de 18+ |
| 6 | Só dados necessários | ✅ | ✅ | Só nome, e-mail e senha; bairro escolhido; iniciativas e atualizações (públicas com o primeiro nome); apoios; denúncias |
| 7 | Scripts de rastreamento | ✅ | ✅ | Nenhum rastreador nem anúncio; fontes Inter servidas pelo próprio site; Sentry sem dados pessoais |
| 8 | Acessibilidade geral | ✅ | ✅ | Declaração em `/acessibilidade`; `lang="pt-BR"`; testes axe (WCAG A e AA) no computador e no celular a cada mudança |
| 9 | Alt nas imagens | ✅ | ✅ | Só ícones decorativos, com `aria-hidden` |
| 10 | Contraste | ✅ | ✅ | Verificado pelo axe no tema atual (verde #55B02E, fundo #0D0F12) |
| 11 | Navegação por teclado | ✅ | ✅ | "Pular para o conteúdo", foco visível, botões e links nativos |
| 12 | Avaliações e depoimentos | ✅ | ✅ | Não há depoimentos nem notas |
| 13 | Promessas e publicidade | ✅ | ✅ | Limites do serviço ditos em destaque nos Termos |
| 14 | Direitos autorais | ✅ | ✅ | Logos próprias; fonte Inter (SIL OFL 1.1, licença em `web/public/fonts/` e `android/licencas/`); ícones Lucide (ISC) |
| 15 | Dados da empresa | ⚠️ | ⚠️ | Responsável pessoa física, por decisão sua: nome, cidade e e-mail no rodapé e nos documentos, sem CPF nem endereço |
| 16 | Conformidade LGPD | ✅ | ✅ | "Baixar meus dados", exclusão de conta no site e no app, novo aceite quando os textos mudam, retenção automática, backup criptografado, registro de operações e plano de incidentes |
| 17 | Outros riscos jurídicos | ⚠️ | ⚠️ | Veja os riscos restantes |

Legenda: ✅ Conforme · ⚠️ Parcial · ❌ Não conforme · ➖ Não se aplica

**Riscos restantes mais importantes:**
1. **Conteúdo de usuário e Marco Civil.** Depois do julgamento do STF sobre o art. 19 do Marco Civil (Temas 533 e 987, jun/2025), a plataforma pode responder se não agir após ser notificada em vários casos. O fluxo de denúncia e o e-mail de contato atendem a isso; a chave é revisar a fila de denúncias com frequência (em Conta → Moderação). Em ano eleitoral, conteúdo político é proibido pelos Termos: remova rápido o que aparecer.
2. **Identificação como pessoa física.** Enquanto o serviço for gratuito, o risco é baixo. Se passar a cobrar, o Decreto 7.962/2013 (art. 2º, I e II) exige nome empresarial e CNPJ (ou CPF) e endereço físico em destaque.
3. **Revisão jurídica.** Os textos seguem a LGPD, o CDC e o Marco Civil, mas não foram revisados por advogado(a).

## O que foi corrigido

- **Cadastro (site):** o texto da caixa passou de "li e aceito os Termos de uso e a Política de privacidade" para "li e aceito os Termos de uso e estou ciente da Política de privacidade" (LGPD arts. 8º e 9º: a Política informa; o consentimento não é a base do cadastro). O app Android já usava esse texto. Arquivo: `web/src/pages/auth.jsx`.
- **Política de privacidade:** nova seção "Cookies e armazenamento no navegador": sem cookies de publicidade, análise ou terceiros; só as chaves da sessão no armazenamento local, apagadas ao sair; fontes servidas pelo próprio site. Arquivo: `web/src/pages/legal.jsx`.
- **Termos de uso:** nova cláusula de propriedade intelectual: a marca, o visual e o código são do responsável; o que a pessoa escreve (as iniciativas e atualizações) continua sendo dela, com autorização só para guardar e mostrar no serviço (Lei 9.610/1998). Arquivo: `web/src/pages/legal.jsx`.
- **Nova versão dos textos (2026-11-06):** quem aceitou a versão anterior vê o aviso de nova versão no site e no app e confirma com "Li e concordo". Arquivos: `web/src/lib/legal.js`, `api/.../usuario/Termos.java`, `android/.../componentes/Links.kt`.

Já estava em ordem (conferido no código): Conteúdo de usuário com regras claras (sem exposição de terceiros, sem campanha política, sem golpes), denúncia em cada iniciativa, ocultação automática com 3 denúncias, revisão pela administração com aviso ao autor e canal de contestação. Destaque de que o Pulso não é canal oficial da prefeitura.

## Documentos

| Documento | Arquivo | URL/rota | Linkado em |
|-----------|---------|----------|-----------|
| Política de privacidade | `web/src/pages/legal.jsx` | `/privacidade` | Rodapé, cadastro, app (Conta) |
| Termos de uso | `web/src/pages/legal.jsx` | `/termos` | Rodapé, cadastro, app (Conta) |
| Declaração de acessibilidade | `web/src/pages/legal.jsx` | `/acessibilidade` | Rodapé |
| Como excluir a conta | `web/src/pages/legal.jsx` | `/excluir-conta` | Rodapé; endereço exigido pelo Google Play |
| Registro das operações (interno) | `docs/lgpd/registro-de-operacoes.md` | — | — |
| Plano de resposta a incidentes (interno) | `docs/lgpd/plano-de-resposta-a-incidentes.md` | — | — |

## O que depende de você

- [ ] 🟠 Revisão por advogado(a) dos Termos e da Política. Deixado para depois, por decisão sua em 04/11/2026 (vale para todos os produtos).
- [ ] 🟡 Verificar os termos de tratamento de dados (DPA) de Render, Neon, Vercel, Resend, Sentry, GitHub, ImprovMX e Google (transferência internacional, Res. CD/ANPD 19/2024).
- [ ] 🟡 Conferir se o backup diário está rodando (segredos `BACKUP_DATABASE_URL` e `BACKUP_PASSPHRASE` no GitHub e uma execução manual). A Política promete essa cópia.
- [ ] 🟡 Os testes automáticos do GitHub estão parados por cobrança/limite de minutos; a Declaração de acessibilidade diz que as páginas são verificadas a cada mudança. Volte a rodá-los (ou rode no seu computador) antes da próxima mudança grande.
- [ ] 🔵 Quando possível, testar com leitor de tela (NVDA no Windows ou TalkBack no Android).

### Marcadores pendentes `[[PREENCHER]]`

Nenhum.

### Achados do scanner que não são problema

| Achado | Por quê |
|--------|---------|
| `termos-incompletos` e `declaracao-acessibilidade-ausente` | Os textos ficam em `legal.jsx`, em JSX que o scanner não lê; todos os itens existem nas páginas |
| `reembolso-nao-aplicavel` | Correto: o serviço é gratuito |
| `imagens-confirmar-origem` | O único arquivo é o `favicon.svg`, feito para a marca |
| `alegacao-saude` (crítico) em bairros.jsx | O scanner leu "cura" dentro de "procurar"; não há alegação de saúde |
| `formulario-sem-aviso` em iniciativas.jsx | É o formulário de denúncia/publicação de quem já está logado; a tela avisa "Aparece para todos com o seu primeiro nome" e o rodapé tem a Política |
| `setor-financeiro` | Falso positivo de palavras; não há dinheiro, crédito nem pagamento |

## Perfil e respostas (usado na próxima execução)

- **Tipo:** Serviço gratuito em que moradores publicam o que o bairro precisa, reúnem apoios e contam o que aconteceu; conteúdo público, moderação por denúncia (3 denúncias ocultam até revisão).
- **Stack:** API Spring Boot (Render, EUA) + Postgres (Neon, EUA/Virgínia); site React/Vite/Tailwind (Vercel); app Android (Kotlin/Compose).
- **Responsável:** João Adolfo Martins Araújo, pessoa física, Rio de Janeiro/RJ, contato@valtrea.com.br. Não publicar CPF nem endereço.
- **Encarregado/canal:** sem encarregado indicado (agente de pequeno porte); canal contato@valtrea.com.br.
- **Público:** maiores de 18 anos no Brasil; sem público no exterior.
- **Mapa de dados e prazos:** veja a Política e `docs/lgpd/registro-de-operacoes.md`.
- **Rastreadores:** nenhum. Armazenamento essencial: sessão (localStorage).

## Como manter

- Ao mudar os Termos ou a Política, troque `version` e `updatedAt` em `web/src/lib/legal.js`, `Termos.VERSAO` na API e `Links.VERSAO_TERMOS` no app: o aviso de nova versão aparece sozinho.
- Rode a skill de novo ao adicionar formulários, fornecedores, pagamentos ou cobrança.
- Revise este relatório a cada 12 meses.
