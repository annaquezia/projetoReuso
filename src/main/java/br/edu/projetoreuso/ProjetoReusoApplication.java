package br.edu.projetoreuso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"br.edu.projetoreuso", "usuario", "evento"})
@EntityScan(basePackages = {"usuario", "evento", "br.edu.projetoreuso.inscricao", "br.edu.projetoreuso.organizacao"})
@EnableJpaRepositories(basePackages = {"br.edu.projetoreuso"})
public class ProjetoReusoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjetoReusoApplication.class, args);
    }
}
