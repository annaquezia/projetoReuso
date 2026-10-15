CREATE TABLE categoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(160) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    centro_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_categoria_centro FOREIGN KEY (centro_id) REFERENCES centro_academico(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE espaco (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(160) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    capacidade INT NOT NULL,
    centro_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_espaco_capacidade CHECK (capacidade > 0),
    CONSTRAINT fk_espaco_centro FOREIGN KEY (centro_id) REFERENCES centro_academico(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE edicao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(160) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    centro_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_edicao_periodo CHECK (data_fim >= data_inicio),
    CONSTRAINT fk_edicao_centro FOREIGN KEY (centro_id) REFERENCES centro_academico(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

ALTER TABLE evento
    ADD COLUMN categoria_id BIGINT,
    ADD COLUMN espaco_id BIGINT,
    ADD COLUMN edicao_id BIGINT,
    ADD CONSTRAINT fk_evento_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_evento_espaco FOREIGN KEY (espaco_id) REFERENCES espaco(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_evento_edicao FOREIGN KEY (edicao_id) REFERENCES edicao(id) ON DELETE RESTRICT;

CREATE TABLE comunicado (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(160) NOT NULL,
    mensagem VARCHAR(2000) NOT NULL,
    evento_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comunicado_evento FOREIGN KEY (evento_id) REFERENCES evento(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE material (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(160) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    url VARCHAR(500) NOT NULL,
    evento_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_material_evento FOREIGN KEY (evento_id) REFERENCES evento(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE presenca (
    id BIGINT NOT NULL AUTO_INCREMENT,
    inscricao_id BIGINT NOT NULL,
    registrada_em DATETIME(6) NOT NULL,
    observacao VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_presenca_inscricao UNIQUE (inscricao_id),
    CONSTRAINT fk_presenca_inscricao FOREIGN KEY (inscricao_id) REFERENCES inscricao_evento(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
