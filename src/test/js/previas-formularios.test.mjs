import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const source = await readFile(new URL('../../main/resources/static/js/previas-formularios.js', import.meta.url), 'utf8');
const { coletarCampos, conectarPrevia } = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`);
function fixture(kind = 'usuario') {
    const values = { nome: 'Ana', email: 'ana@exemplo.edu.br', tipoUsuario: 'PALESTRANTE', senha: 'segredo', _csrf: 'token' };
    const nodes = new Map(['nome','perfil','email','detalhe','descricao','quando','local','vagas'].map(key => [key, {textContent: ''}]));
    const status = {textContent: '', hidden: true};
    const preview = { hidden: true, querySelector(selector) { return selector === '[data-preview-status]' ? status : nodes.get(/data-previa="([^"]+)"/.exec(selector)?.[1]); } };
    const listeners = new Map();
    const form = { dataset: {formObserver: kind, previewUrl: '/usuarios/previa'},
        elements: {namedItem: name => ({value: values[name] ?? ''})}, querySelector: () => preview,
        addEventListener: (key, fn) => listeners.set(key, fn), removeEventListener: key => listeners.delete(key) };
    return {form, values, nodes, preview, status, listeners};
}
const response = data => ({ok: true, headers: {get: () => 'application/json'}, json: async () => data});
const tick = () => new Promise(resolve => setImmediate(resolve));
test('Bridge sends only preview fields and renders the response computed in Java as text', async () => {
    const f = fixture(); let sent;
    const bridge = conectarPrevia(f.form, async (url, options) => { sent = {url, options}; return response({nome: '<script>literal</script>', perfil: 'Palestrante'}); });
    await tick();
    assert.equal(sent.url, '/usuarios/previa');
    assert.equal(sent.options.headers['X-CSRF-TOKEN'], 'token');
    assert.equal('senha' in JSON.parse(sent.options.body), false);
    assert.equal('_csrf' in JSON.parse(sent.options.body), false);
    assert.equal(coletarCampos(f.form).nome, 'Ana');
    assert.equal(f.nodes.get('nome').textContent, '<script>literal</script>');
    assert.equal(f.preview.hidden, false);
    bridge.destruir(); assert.equal(f.listeners.size, 0);
});
test('A slower old response cannot overwrite the latest preview', async () => {
    const f = fixture(); const pending = [];
    const bridge = conectarPrevia(f.form, () => new Promise(resolve => pending.push(resolve)));
    const latest = bridge.atualizar();
    pending[1](response({nome: 'Atual'})); await latest;
    pending[0](response({nome: 'Antiga'})); await tick();
    assert.equal(f.nodes.get('nome').textContent, 'Atual');
    bridge.destruir();
});
test('Failed preview informs the user without blocking the save form', async () => {
    const f = fixture();
    const bridge = conectarPrevia(f.form, async () => ({ok: false}));
    await tick();
    assert.equal(f.status.hidden, false);
    assert.match(f.status.textContent, /salvar o formulário normalmente/);
    bridge.destruir();
});
test('Event payload is separated from user fields and contains no password', () => {
    const f = fixture('evento'); Object.assign(f.values, {quantidadeVagas: '40', descricao: 'Java'});
    assert.equal(coletarCampos(f.form).quantidadeVagas, '40');
    assert.equal('email' in coletarCampos(f.form), false);
    assert.equal('senha' in coletarCampos(f.form), false);
});
