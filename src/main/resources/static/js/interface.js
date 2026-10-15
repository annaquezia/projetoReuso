function icone(nome) {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.classList.add('icon');
    svg.setAttribute('aria-hidden', 'true');
    const use = document.createElementNS(svg.namespaceURI, 'use');
    use.setAttribute('href', `/images/icons/bootstrap-icons.svg#${nome}`);
    svg.append(use);
    return svg;
}

function botaoFechar(rotulo, fechar) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = 'icon-button';
    botao.setAttribute('aria-label', rotulo);
    botao.append(icone('x-lg'));
    botao.addEventListener('click', fechar);
    return botao;
}

function notificar(mensagem, erro = false) {
    let regiao = document.querySelector('.toast-region');
    if (!regiao) {
        regiao = document.createElement('div');
        regiao.className = 'toast-region';
        regiao.setAttribute('aria-label', 'Notificações');
        document.body.append(regiao);
    }
    const toast = document.createElement('div');
    toast.className = erro ? 'toast toast-error' : 'toast';
    toast.setAttribute('role', erro ? 'alert' : 'status');
    const texto = document.createElement('p');
    texto.textContent = mensagem;
    toast.append(icone(erro ? 'exclamation-triangle' : 'check-circle'), texto,
        botaoFechar('Dispensar notificação', () => toast.remove()));
    regiao.append(toast);
    // Erros e mensagens com foco permanecem disponíveis até serem dispensados.
    if (!erro) {
        let prazo;
        const agendar = () => { prazo = setTimeout(() => toast.remove(), 10000); };
        const pausar = () => clearTimeout(prazo);
        toast.addEventListener('pointerenter', pausar);
        toast.addEventListener('pointerleave', agendar);
        toast.addEventListener('focusin', pausar);
        toast.addEventListener('focusout', agendar);
        agendar();
    }
}

function criarConfirmacao() {
    const dialogo = document.createElement('dialog');
    dialogo.className = 'confirm-dialog';
    dialogo.setAttribute('aria-labelledby', 'dialogo-titulo');
    const cabecalho = document.createElement('div');
    cabecalho.className = 'dialog-header';
    const titulo = document.createElement('h2');
    titulo.id = 'dialogo-titulo';
    const conteudo = document.createElement('div');
    const fechar = () => dialogo.close();
    const botao = botaoFechar('Fechar confirmação', fechar);
    cabecalho.append(titulo, botao);
    dialogo.append(cabecalho, conteudo);
    document.body.append(dialogo);
    let origem;
    let requisicao;
    dialogo.addEventListener('close', () => {
        requisicao?.abort();
        origem?.focus();
    });
    dialogo.addEventListener('click', event => {
        if (event.target !== dialogo) return;
        const r = dialogo.getBoundingClientRect();
        if (event.clientX < r.left || event.clientX > r.right || event.clientY < r.top || event.clientY > r.bottom) fechar();
    });
    function abrir(rotulo, elemento) {
        requisicao?.abort();
        origem = elemento;
        titulo.textContent = rotulo;
        conteudo.replaceChildren();
        if (!dialogo.open) dialogo.showModal();
        botao.focus();
    }
    async function carregar(link) {
        const destino = new URL(link.href, location.href);
        if (destino.origin !== location.origin) return;
        abrir('Carregando confirmação…', link);
        requisicao = new AbortController();
        const sinal = requisicao.signal;
        try {
            const resposta = await fetch(destino, { credentials: 'same-origin', signal: sinal });
            if (!resposta.ok) throw new Error('Falha ao obter confirmação');
            const pagina = new DOMParser().parseFromString(await resposta.text(), 'text/html');
            const card = pagina.querySelector('main .confirmation-card');
            const formulario = card?.querySelector('form[method="post"]');
            if (!card || !formulario) {
                location.assign(destino.href);
                return;
            }
            if (sinal.aborted || !dialogo.open) return;
            titulo.textContent = pagina.querySelector('main h1')?.textContent || 'Confirmar alteração';
            const copia = document.importNode(card, true);
            copia.querySelectorAll('.form-actions a').forEach(a => {
                const cancelar = document.createElement('button');
                cancelar.type = 'button';
                cancelar.className = a.className;
                cancelar.textContent = a.textContent;
                cancelar.addEventListener('click', fechar);
                a.replaceWith(cancelar);
            });
            conteudo.replaceChildren(copia);
        } catch (erro) {
            if (erro.name === 'AbortError') return;
            fechar();
            notificar('Não foi possível abrir a confirmação. Tente novamente ou abra a tela pelo link.', true);
        }
    }
    function confirmarFormulario(formulario, elemento, confirmar) {
        abrir(formulario.dataset.confirm, elemento);
        const descricao = document.createElement('p');
        descricao.textContent = formulario.dataset.confirmDescription || 'Confira antes de continuar.';
        const acoes = document.createElement('div');
        acoes.className = 'form-actions';
        const cancelar = document.createElement('button');
        cancelar.type = 'button';
        cancelar.className = 'button button-outline';
        cancelar.textContent = 'Voltar sem alterar';
        cancelar.addEventListener('click', fechar);
        const aceitar = document.createElement('button');
        aceitar.type = 'button';
        aceitar.className = 'button button-danger';
        aceitar.textContent = 'Confirmar cancelamento';
        aceitar.addEventListener('click', () => { fechar(); confirmar(); }, { once: true });
        acoes.append(cancelar, aceitar);
        conteudo.append(descricao, acoes);
    }
    return { carregar, confirmarFormulario };
}

function iniciarInterface() {
    document.querySelectorAll('[data-toast]').forEach(alerta => {
        notificar(alerta.textContent.trim(), alerta.classList.contains('alert-error'));
        alerta.remove();
    });
    const modal = typeof HTMLDialogElement !== 'undefined' ? criarConfirmacao() : null;
    const confirmados = new WeakSet();
    const pendentes = new WeakSet();
    document.addEventListener('click', event => {
        const link = event.target.closest('a[data-modal-confirm]');
        if (!modal || !link || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
        event.preventDefault();
        modal.carregar(link);
    });
    document.addEventListener('submit', event => {
        if (event.defaultPrevented) return;
        const form = event.target;
        if (pendentes.has(form)) { event.preventDefault(); return; }
        if (form.dataset.confirm && !confirmados.has(form)) {
            if (modal) {
                event.preventDefault();
                const submitter = event.submitter;
                modal.confirmarFormulario(form, submitter, () => {
                    confirmados.add(form);
                    form.requestSubmit(submitter);
                    confirmados.delete(form);
                });
                return;
            }
            if (!window.confirm(form.dataset.confirm)) { event.preventDefault(); return; }
        }
        pendentes.add(form);
        setTimeout(() => {
            if (event.defaultPrevented) { pendentes.delete(form); return; }
            form.setAttribute('aria-busy', 'true');
            form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(b => { b.disabled = true; });
        }, 0);
    });
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('form[aria-busy]').forEach(form => {
            pendentes.delete(form);
            form.removeAttribute('aria-busy');
            form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(b => { b.disabled = false; });
        });
    });
}

if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', iniciarInterface, { once: true });
else iniciarInterface();
