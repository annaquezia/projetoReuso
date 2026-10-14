package usuario;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "universidade")
@DiscriminatorValue("UNIVERSIDADE")
public class Universidade extends Usuario {
    @Column(length = 255)
    private String endereco;

    public Universidade() { }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}
