package inscricao.state;

import br.edu.projetoreuso.inscricao.InscricaoEvento;

public interface InscricaoState {

    void cancelar(InscricaoEvento inscricao);
    void reativar(InscricaoEvento inscricao);

    String getNome();
    boolean isAtiva();
}
