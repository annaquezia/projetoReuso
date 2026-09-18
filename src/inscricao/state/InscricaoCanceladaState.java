package inscricao.state;

import inscricao.Inscricao;

public class InscricaoCanceladaState implements InscricaoState{
    @Override
    public void cancelar(Inscricao inscricao) {
        throw new IllegalStateException("A inscrição já está cancelada");
    }

    @Override
    public void reativar(Inscricao inscricao) {
        inscricao.setInscricaoState(new InscricaoAtivaState());
    }

    @Override
    public String getNome() {
        return "Cancelada";
    }
}
