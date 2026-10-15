-- V3 já foi aplicada. Seu histórico é preservado; apenas estruturas vazias
-- criadas durante a adequação são removidas. Havendo dados, a validação aborta.
CREATE TEMPORARY TABLE validar_escopo_vazio (
    total BIGINT NOT NULL,
    CONSTRAINT ck_escopo_sem_dados CHECK (total = 0)
);

INSERT INTO validar_escopo_vazio
SELECT (SELECT COUNT(*) FROM edicao)
     + (SELECT COUNT(*) FROM material)
     + (SELECT COUNT(*) FROM evento WHERE edicao_id IS NOT NULL);

ALTER TABLE evento DROP FOREIGN KEY fk_evento_edicao, DROP COLUMN edicao_id;
DROP TABLE material;
DROP TABLE edicao;
DROP TEMPORARY TABLE validar_escopo_vazio;
