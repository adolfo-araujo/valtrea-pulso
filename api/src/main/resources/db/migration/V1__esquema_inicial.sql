-- Valtrea Pulso: moradores, bairros, iniciativas, apoios, atualizações e denúncias.
-- Excluir uma conta apaga tudo dela em cascata (LGPD: exclusão na hora).

-- Bairros criados pelos próprios moradores. A chave (UF + cidade + nome, sem acentos e em minúsculas) evita repetidos.
CREATE TABLE bairros (
    id              UUID PRIMARY KEY,
    nome            VARCHAR(80)  NOT NULL,
    cidade          VARCHAR(80)  NOT NULL,
    uf              CHAR(2)      NOT NULL,
    chave           VARCHAR(200) NOT NULL,
    criado_em       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_bairros_chave UNIQUE (chave)
);
CREATE INDEX ix_bairros_uf_cidade ON bairros (uf, cidade);

CREATE TABLE usuarios (
    id                        UUID PRIMARY KEY,
    nome                      VARCHAR(120) NOT NULL,
    email                     VARCHAR(254) NOT NULL,
    senha_hash                VARCHAR(100) NOT NULL,
    papel                     VARCHAR(20)  NOT NULL,
    tentativas_login_falhas   INTEGER      NOT NULL DEFAULT 0,
    bloqueado_ate             TIMESTAMPTZ,
    email_confirmado_em       TIMESTAMPTZ,
    criado_em                 TIMESTAMPTZ  NOT NULL,
    termos_versao             VARCHAR(20),
    termos_aceitos_em         TIMESTAMPTZ,
    bairro_id                 UUID REFERENCES bairros (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

-- Sessões do app e do site. Só o hash do token: quem ler o banco não consegue usá-lo.
CREATE TABLE sessoes (
    id              UUID PRIMARY KEY,
    usuario_id      UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) NOT NULL,
    expira_em       TIMESTAMPTZ NOT NULL,
    criado_em       TIMESTAMPTZ NOT NULL,
    revogado_em     TIMESTAMPTZ,
    CONSTRAINT uk_sessoes_token UNIQUE (token_hash)
);
CREATE INDEX ix_sessoes_usuario ON sessoes (usuario_id);

-- Códigos de 6 dígitos enviados por e-mail (confirmação de e-mail e nova senha), só como HMAC.
CREATE TABLE codigos_email (
    id              UUID PRIMARY KEY,
    usuario_id      UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    finalidade      VARCHAR(20) NOT NULL,
    codigo_hash     VARCHAR(64) NOT NULL,
    tentativas      INTEGER     NOT NULL DEFAULT 0,
    expira_em       TIMESTAMPTZ NOT NULL,
    criado_em       TIMESTAMPTZ NOT NULL,
    usado_em        TIMESTAMPTZ
);
CREATE INDEX ix_codigos_email_usuario ON codigos_email (usuario_id, finalidade, criado_em DESC);

-- O que o bairro precisa. Pública, mas mostra só o primeiro nome do autor.
-- oculta: escondida depois de denúncias, até a revisão. revisada: a administração já olhou e manteve.
CREATE TABLE iniciativas (
    id              UUID PRIMARY KEY,
    bairro_id       UUID          NOT NULL REFERENCES bairros (id),
    autor_id        UUID          NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    titulo          VARCHAR(120)  NOT NULL,
    descricao       VARCHAR(2000) NOT NULL,
    categoria       VARCHAR(30)   NOT NULL,
    fase            VARCHAR(20)   NOT NULL,
    oculta          BOOLEAN       NOT NULL DEFAULT FALSE,
    oculta_em       TIMESTAMPTZ,
    revisada        BOOLEAN       NOT NULL DEFAULT FALSE,
    criado_em       TIMESTAMPTZ   NOT NULL,
    atualizado_em   TIMESTAMPTZ   NOT NULL
);
CREATE INDEX ix_iniciativas_bairro ON iniciativas (bairro_id, oculta, criado_em DESC);
CREATE INDEX ix_iniciativas_autor ON iniciativas (autor_id, criado_em DESC);

-- Um apoio por pessoa em cada iniciativa.
CREATE TABLE apoios (
    iniciativa_id   UUID        NOT NULL REFERENCES iniciativas (id) ON DELETE CASCADE,
    usuario_id      UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    criado_em       TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (iniciativa_id, usuario_id)
);
CREATE INDEX ix_apoios_usuario ON apoios (usuario_id);

-- O autor conta o que aconteceu (e, se quiser, muda a fase).
CREATE TABLE atualizacoes (
    id              UUID PRIMARY KEY,
    iniciativa_id   UUID          NOT NULL REFERENCES iniciativas (id) ON DELETE CASCADE,
    texto           VARCHAR(1000) NOT NULL,
    fase            VARCHAR(20),
    criado_em       TIMESTAMPTZ   NOT NULL
);
CREATE INDEX ix_atualizacoes_iniciativa ON atualizacoes (iniciativa_id, criado_em);

-- Uma denúncia por pessoa em cada iniciativa. Apagadas quando a administração decide.
CREATE TABLE denuncias (
    iniciativa_id   UUID        NOT NULL REFERENCES iniciativas (id) ON DELETE CASCADE,
    usuario_id      UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    motivo          VARCHAR(30) NOT NULL,
    criado_em       TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (iniciativa_id, usuario_id)
);
