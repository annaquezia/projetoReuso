package inscricao.state;

import inscricao.Inscricao;

public interface InscricaoState {

    void cancelar (Inscricao inscricao);
    void reativar (Inscricao inscricao);

    String getNome();
}
