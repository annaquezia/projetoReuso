CREATE TABLE inscricao_evento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aluno_id BIGINT NOT NULL,
    evento_id BIGINT NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    data_inscricao DATETIME(6) NOT NULL,
    data_cancelamento DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_inscricao_aluno_evento UNIQUE (aluno_id, evento_id),
    INDEX idx_inscricao_evento_ativa (evento_id, ativa),
    CONSTRAINT fk_inscricao_aluno FOREIGN KEY (aluno_id) REFERENCES estudante(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inscricao_evento FOREIGN KEY (evento_id) REFERENCES evento(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
