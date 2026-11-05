# Valtrea Pulso

Moradores registram o que o bairro precisa, reúnem o apoio dos vizinhos e acompanham o que acontece com cada iniciativa.

| Parte | Pasta | Tecnologia | Onde roda |
| --- | --- | --- | --- |
| API | `api/` | Java 21, Spring Boot 4.1, PostgreSQL, Flyway | Render (`valtrea-pulso-api.onrender.com`) |
| Site | `web/` | React, Vite, Tailwind | Vercel (`pulso.valtrea.com.br`) |
| App | `android/` | Kotlin, Jetpack Compose | Google Play |

Qualquer pessoa vê as iniciativas de um bairro no site, sem conta. Para publicar, apoiar e denunciar, é preciso conta com o e-mail confirmado.

## Como funciona

1. O morador escolhe o bairro (cidade + nome). Se ainda não existir, cria; nomes iguais, sem acento ou com espaços a mais, viram o mesmo bairro.
2. Publica uma iniciativa (título, explicação e categoria) no bairro dele. Até 3 por dia e 20 abertas por pessoa.
3. Os vizinhos apoiam (um apoio por pessoa). A lista do bairro ordena por mais recentes ou mais apoiadas.
4. O autor conta as novidades e muda a fase: aberta → em andamento → resolvida (ou arquivada).
5. Com 3 denúncias de pessoas diferentes, a iniciativa fica oculta até a administração decidir (manter ou remover) no painel. O autor é avisado por e-mail nos dois casos.

## Segurança e privacidade

- Em público aparece só o **primeiro nome** do autor. Ninguém vê quem apoiou (só quantos) nem quem denunciou; nem a administração.
- Publicar, apoiar e denunciar exigem e-mail confirmado; limite por IP para essas ações (60 por hora).
- Iniciativa oculta responde 404 para todos, menos para o autor. Depois que a administração mantém, ela não some mais sozinha (até ser editada).
- Excluir a conta apaga na hora as iniciativas, os apoios e as denúncias da pessoa.
- Retenção: cadastros nunca confirmados 30 dias; ocultas sem revisão 60 dias; bairros sem moradores nem iniciativas 30 dias.
- Os mesmos controles do Valtrea Pay e do Valtrea Volta: bcrypt, sessões de uso único, bloqueio de login, limites por IP, cabeçalhos de segurança, CORS só para o site, Sentry sem dados pessoais, backup diário criptografado, OSV-Scanner e gitleaks no CI.

## Rotas da API

Todas em `/api/v1`. Documentação interativa em `/swagger-ui.html`.

| Método | Rota | O que faz |
| --- | --- | --- |
| POST | `/auth/cadastro`, `/auth/login`, `/auth/renovar`, `/auth/sair` | Conta e sessão |
| POST | `/auth/confirmar-email`, `/auth/reenviar-confirmacao`, `/auth/esqueci-senha`, `/auth/redefinir-senha` | Códigos por e-mail |
| GET, PATCH, DELETE | `/eu` | Perfil, troca do nome e exclusão da conta |
| GET | `/eu/dados` | Baixar meus dados (JSON) |
| POST | `/eu/termos` | Aceite da versão vigente dos Termos |
| PUT | `/eu/bairro` | Escolher ou mudar o bairro |
| GET | `/eu/iniciativas` | As suas iniciativas (inclusive ocultas) |
| POST | `/bairros` | Acha o bairro pelo nome ou cria |
| GET | `/bairros/{id}/iniciativas` | Iniciativas do bairro, dizendo quais você apoiou |
| GET, POST | `/iniciativas/{id}`, `/iniciativas` | Ver e publicar |
| PUT, DELETE | `/iniciativas/{id}` | Editar e apagar (só o autor) |
| POST | `/iniciativas/{id}/atualizacoes` | Novidade e mudança de fase (só o autor) |
| POST, DELETE | `/iniciativas/{id}/apoio` | Apoiar e retirar o apoio |
| POST | `/iniciativas/{id}/denuncia` | Denunciar (com motivo) |
| GET | `/publico/bairros`, `/publico/bairros/{id}`, `/publico/bairros/{id}/iniciativas`, `/publico/iniciativas/{id}` | Leitura sem conta |
| GET, POST, DELETE | `/admin/moderacao`, `/admin/moderacao/{id}/manter`, `/admin/moderacao/{id}` | Fila de revisão (só administradores) |
| GET | `/admin/metricas` | Números de uso somados (só administradores) |

## Rodar no computador

```bash
# Banco
docker run -d --name pulso-db -p 5432:5432 -e POSTGRES_USER=valtrea -e POSTGRES_PASSWORD=valtrea -e POSTGRES_DB=valtrea_pulso postgres:18-alpine
# API (porta 8080)
cd api
VALTREA_JWT_SECRET=$(openssl rand -hex 32) \
VALTREA_URL_SITE=http://localhost:5175 VALTREA_CORS=http://localhost:5175 mvn spring-boot:run
# Site (porta 5175)
cd ../web && npm install && npm run dev
```

Sem SMTP configurado, os e-mails (códigos e avisos) aparecem no log da API. O app Android (variante `debug`) usa `http://10.0.2.2:8080/`.

## Publicar

1. **Banco (Neon):** crie o projeto `valtrea-pulso` na região **AWS US East 1 (N. Virginia)**, a mesma do Render. O teste de capacidade do Pay mostrou que o banco em São Paulo com a API nos EUA deixa cada ida ao banco com ~120 ms.
2. **API (Render):** New → Blueprint → repositório `valtrea-pulso`. Preencha `DATABASE_URL` (`jdbc:postgresql://<host>/<banco>?sslmode=require`), `DATABASE_USER`, `DATABASE_PASSWORD` e `RESEND_API_KEY`.
3. **Site (Vercel):** Add New → Project → repositório `valtrea-pulso`, **Root Directory** `web`, framework Vite. Em Environment Variables, `VITE_API_URL` = `https://valtrea-pulso-api.onrender.com`. Em Domains, adicione `pulso.valtrea.com.br`.
4. **DNS:** crie o registro CNAME que a Vercel mostrar para `pulso`.
5. **App:** a variante `release` já aponta para `https://valtrea-pulso-api.onrender.com/`.

