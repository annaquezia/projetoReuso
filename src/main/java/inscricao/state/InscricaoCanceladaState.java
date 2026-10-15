package inscricao.state;

import br.edu.projetoreuso.inscricao.InscricaoEvento;

public final class InscricaoCanceladaState implements InscricaoState {
    private static final InscricaoCanceladaState INSTANCE = new InscricaoCanceladaState();
    private final String nome;

    private InscricaoCanceladaState() { this.nome = "Cancelada"; }

    public static InscricaoCanceladaState getInstance() { return INSTANCE; }

    @Override
    public void cancelar(InscricaoEvento inscricao) {
        throw new IllegalStateException("A inscrição já está cancelada");
    }

    @Override
    public void reativar(InscricaoEvento inscricao) {
        inscricao.mudarEstado(InscricaoAtivaState.getInstance());
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public boolean isAtiva() { return false; }
}
