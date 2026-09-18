package usuario;

public class Universidade extends Usuario {
    private static Universidade instancia;

    private Universidade() {
    }

    public static Universidade getInstancia() {
        if (instancia == null) {
            instancia = new Universidade();
        }
        return instancia;

    }
}
