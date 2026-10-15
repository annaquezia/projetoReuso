package br.edu.projetoreuso;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Executa os mesmos cenários no esquema MySQL, com limpeza limitada aos IDs do teste. */
@EnabledIfEnvironmentVariable(named = "VALIDAR_MYSQL", matches = "true")
@ActiveProfiles(value = "mysql", inheritProfiles = false)
@SpringBootTest(properties = {"app.demo.seed=false", "APP_BOOTSTRAP_EMAIL=", "APP_BOOTSTRAP_PASSWORD="})
class MysqlComunicadosTest extends ComunicadosTest { }
