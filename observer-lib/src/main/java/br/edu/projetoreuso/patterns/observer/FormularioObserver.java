package br.edu.projetoreuso.patterns.observer;

/** Contrato Java dos observadores que atualizam a prévia exibida no formulário. */
@FunctionalInterface
public interface FormularioObserver<T> {
    void atualizar(T dados);
}
