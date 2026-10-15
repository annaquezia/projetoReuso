package br.edu.projetoreuso.patterns.observer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Subject de um rascunho. Cada requisição possui sua instância, sem compartilhar dados entre usuários. */
public final class FormularioSubject<T> {
    private final Set<FormularioObserver<T>> observers = new LinkedHashSet<>();

    public void registrarObserver(FormularioObserver<T> observer) {
        observers.add(Objects.requireNonNull(observer));
    }
    public void removerObserver(FormularioObserver<T> observer) { observers.remove(observer); }
    public void alterar(T dados) {
        Objects.requireNonNull(dados);
        // Uma cópia permite cancelar uma inscrição durante a notificação.
        for (FormularioObserver<T> observer : List.copyOf(observers)) observer.atualizar(dados);
    }
}
