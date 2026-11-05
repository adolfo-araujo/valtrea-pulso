# Plano de Resposta a Incidentes de Segurança: Valtrea Pulso

Documento interno. Base: LGPD arts. 46 a 48; Resolução CD/ANPD nº 15/2024. O plano completo, com o passo a passo, está no repositório do Valtrea Finance (`docs/lgpd/plano-de-resposta-a-incidentes.md`); aqui ficam as ações específicas do Pulso.

**Responsável:** João Adolfo Martins Araújo · contato@valtrea.com.br · **Atualizado em:** 05/11/2026

## Conter (na hora)

| O que vazou ou aconteceu | O que fazer |
| --- | --- |
| Segredo do JWT (`VALTREA_JWT_SECRET`) | Gerar outro no Render: todos os tokens de acesso deixam de valer (o app pede login de novo) |
| Banco (`DATABASE_*`) | Trocar a senha no Neon e atualizar o Render e o segredo `BACKUP_DATABASE_URL` do GitHub |
| Banco inteiro | Além do acima: o banco liga cada pessoa aos apoios e denúncias que fez, o que pode revelar opinião; tratar como risco relevante e comunicar |
| Chave do Resend (`RESEND_API_KEY`) | Revogar no Resend e criar outra |
| Chave de assinatura do app (`*.jks`) | Pedir troca da chave de upload no Play Console |
| Conta de usuário invadida | Pedir "Esqueci a senha": a redefinição desconecta todos os aparelhos |
| Conteúdo ilegal publicado (exposição de pessoa, ameaça) | Remover pela moderação (ou pelo banco) assim que souber; guardar registro do pedido |
| Abuso ou robôs | Os limites por IP já barram; se precisar, reduzir `VALTREA_LIMITE_*` no Render |

## Avaliar e comunicar

Havendo risco ou dano relevante aos titulares, comunicar a **ANPD e os titulares em até 3 dias úteis** (Resolução CD/ANPD nº 15/2024), pelo canal oficial em gov.br/anpd, e registrar o incidente, mesmo os não comunicados, por pelo menos 5 anos.

Lembrete: nome, e-mail, bairro, o que cada pessoa publicou, apoiou e denunciou são dados pessoais.

## Restaurar dados

Veja `docs/BACKUP.md`.
