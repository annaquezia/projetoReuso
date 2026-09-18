package usuario;

public class Universidade extends Usuario {
    private String endereco;
    private static Universidade instancia;

    private Universidade() {
    }

    public static Universidade getInstancia() {

        if (instancia == null) {

            instancia = new Universidade();

        }

        return instancia;

    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
