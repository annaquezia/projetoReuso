package inscricao.state;

import inscricao.Inscricao;

public class InscricaoAtivaState implements InscricaoState {


    @Override
    public void cancelar(Inscricao inscricao) {
        inscricao.setInscricaoState(new InscricaoCanceladaState());
    }

    @Override
    public void reativar(Inscricao inscricao) {
        throw new IllegalStateException("A inscrição já está ativa");
    }

    @Override
    public String getNome() {
        return "Ativa";
    }
}
