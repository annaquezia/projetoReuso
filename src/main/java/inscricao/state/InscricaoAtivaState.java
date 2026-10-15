package inscricao.state;

import br.edu.projetoreuso.inscricao.InscricaoEvento;

public final class InscricaoAtivaState implements InscricaoState {
    private static final InscricaoAtivaState INSTANCE = new InscricaoAtivaState();
    private final String nome;

    private InscricaoAtivaState() { this.nome = "Ativa"; }

    public static InscricaoAtivaState getInstance() { return INSTANCE; }



    @Override
    public void cancelar(InscricaoEvento inscricao) {
        inscricao.mudarEstado(InscricaoCanceladaState.getInstance());
    }

    @Override
    public void reativar(InscricaoEvento inscricao) {
        throw new IllegalStateException("A inscrição já está ativa");
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public boolean isAtiva() { return true; }
}
