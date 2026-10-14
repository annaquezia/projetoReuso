CREATE TABLE usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo_usuario VARCHAR(32) NOT NULL,
    nome VARCHAR(160) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE universidade (
    id BIGINT NOT NULL,
    endereco VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_universidade_usuario FOREIGN KEY (id) REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE centro_academico (
    id BIGINT NOT NULL,
    curso VARCHAR(100) NOT NULL,
    nome_presidente VARCHAR(160),
    vice_presidente VARCHAR(160),
    universidade_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_centro_universidade (universidade_id),
    CONSTRAINT fk_centro_usuario FOREIGN KEY (id) REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_centro_universidade FOREIGN KEY (universidade_id) REFERENCES universidade (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE estudante (
    id BIGINT NOT NULL,
    data_nascimento DATE,
    num_matricula VARCHAR(45) NOT NULL,
    curso VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_estudante_matricula UNIQUE (num_matricula),
    CONSTRAINT fk_estudante_usuario FOREIGN KEY (id) REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE palestrante (
    id BIGINT NOT NULL,
    area_atuacao VARCHAR(120),
    centro_academico_id BIGINT,
    PRIMARY KEY (id),
    INDEX idx_palestrante_centro (centro_academico_id),
    CONSTRAINT fk_palestrante_usuario FOREIGN KEY (id) REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT fk_palestrante_centro FOREIGN KEY (centro_academico_id) REFERENCES centro_academico (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE evento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(180) NOT NULL,
    data DATE NOT NULL,
    horario TIME NOT NULL,
    local VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    quantidade_vagas INT NOT NULL,
    imagem_url VARCHAR(500),
    palestrante_id BIGINT,
    centro_academico_id BIGINT NOT NULL,
    status VARCHAR(24) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    INDEX idx_evento_data (data),
    INDEX idx_evento_centro (centro_academico_id),
    INDEX idx_evento_palestrante (palestrante_id),
    CONSTRAINT ck_evento_vagas CHECK (quantidade_vagas > 0),
    CONSTRAINT fk_evento_palestrante FOREIGN KEY (palestrante_id) REFERENCES palestrante (id) ON DELETE RESTRICT,
    CONSTRAINT fk_evento_centro FOREIGN KEY (centro_academico_id) REFERENCES centro_academico (id) ON DELETE RESTRICT
) ENGINE=InnoDB;
