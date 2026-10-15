-- O requisito usa telas de operações, sem exigir seis cadastros completos.
-- Preserva comunicados já utilizados e aborta se os cadastros retirados tiverem dados.
CREATE TEMPORARY TABLE validar_cadastros_vazios (
    total BIGINT NOT NULL,
    CONSTRAINT ck_cadastros_sem_dados CHECK (total = 0)
);

INSERT INTO validar_cadastros_vazios
SELECT (SELECT COUNT(*) FROM categoria)
     + (SELECT COUNT(*) FROM espaco)
     + (SELECT COUNT(*) FROM presenca)
     + (SELECT COUNT(*) FROM evento WHERE categoria_id IS NOT NULL OR espaco_id IS NOT NULL);

ALTER TABLE evento
    DROP FOREIGN KEY fk_evento_categoria,
    DROP FOREIGN KEY fk_evento_espaco,
    DROP COLUMN categoria_id,
    DROP COLUMN espaco_id;
DROP TABLE presenca;
DROP TABLE espaco;
DROP TABLE categoria;
DROP TEMPORARY TABLE validar_cadastros_vazios;
