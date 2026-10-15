package br.edu.projetoreuso.config;

import br.edu.projetoreuso.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import usuario.Universidade;

@Component
@Profile("!demo")
public class BootstrapUniversidade implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapUniversidade.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_BOOTSTRAP_EMAIL:universidade@demo.com}")
    private String emailInicial;

    @Value("${APP_BOOTSTRAP_PASSWORD:12345678}")
    private String senhaInicial;

    public BootstrapUniversidade(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (emailInicial == null || emailInicial.isBlank() || senhaInicial == null || senhaInicial.length() < 8) {
            log.warn("Universidade inicial não criada. Defina APP_BOOTSTRAP_EMAIL e APP_BOOTSTRAP_PASSWORD (mínimo 8 caracteres) para o primeiro acesso administrativo.");
            return;
        }
        if (usuarios.findByEmailIgnoreCase(emailInicial).isPresent()) return;

        Universidade universidade = new Universidade();
        universidade.setNome("Universidade");
        universidade.setEmail(emailInicial);
        universidade.setSenha(passwordEncoder.encode(senhaInicial));
        universidade.setEndereco("");
        usuarios.save(universidade);
        log.info("Conta inicial da universidade criada para {}.", emailInicial);
    }
}
