// Transporte e apresentação: as regras da prévia e o padrão Observer são executados em Java.
const campos = {
    usuario: ["nome", "email", "tipoUsuario", "curso", "areaAtuacao"],
    evento: ["nome", "descricao", "data", "horario", "local", "quantidadeVagas"]
};

export function coletarCampos(form) {
    return Object.fromEntries((campos[form.dataset.formObserver] || [])
        .map(nome => [nome, form.elements.namedItem(nome)?.value ?? ""]));
}

export function conectarPrevia(form, solicitar = globalThis.fetch) {
    const root = form.querySelector("[data-form-preview]");
    if (!root || !campos[form.dataset.formObserver] || !form.dataset.previewUrl) return null;
    const status = root.querySelector("[data-preview-status]");
    let versao = 0, timer, pendente, encerrado = false;
    const atualizar = async () => {
        const atual = ++versao;
        pendente?.abort();
        pendente = new AbortController();
        try {
            const resposta = await solicitar(form.dataset.previewUrl, {
                method: "POST", credentials: "same-origin", signal: pendente.signal,
                headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": form.elements.namedItem("_csrf")?.value ?? "" },
                body: JSON.stringify(coletarCampos(form))
            });
            if (!resposta.ok || !resposta.headers.get("content-type")?.includes("application/json")) throw new Error("Prévia indisponível");
            const dados = await resposta.json();
            if (atual !== versao || encerrado) return;
            for (const [nome, valor] of Object.entries(dados)) {
                const elemento = root.querySelector(`[data-previa="${nome}"]`);
                if (elemento) elemento.textContent = valor;
            }
            root.hidden = false;
            if (status) { status.textContent = ""; status.hidden = true; }
        } catch (erro) {
            if (atual !== versao || encerrado || erro.name === "AbortError") return;
            root.hidden = false;
            if (status) {
                status.hidden = false;
                status.textContent = "Não foi possível atualizar a prévia. Você pode salvar o formulário normalmente.";
            }
        }
    };
    const aoAlterar = event => {
        if (!campos[form.dataset.formObserver].includes(event.target.name)) return;
        ++versao; pendente?.abort(); clearTimeout(timer);
        timer = setTimeout(atualizar, 250);
    };
    form.addEventListener("input", aoAlterar);
    form.addEventListener("change", aoAlterar);
    atualizar();
    return { atualizar, destruir() {
        encerrado = true; ++versao; clearTimeout(timer); pendente?.abort();
        form.removeEventListener("input", aoAlterar); form.removeEventListener("change", aoAlterar);
    } };
}

export function conectarPrevias(documentRoot) {
    return [...documentRoot.querySelectorAll("form[data-form-observer]")]
        .map(form => conectarPrevia(form)).filter(Boolean);
}
if (typeof document !== "undefined") {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", () => conectarPrevias(document), { once: true });
    else conectarPrevias(document);
}
