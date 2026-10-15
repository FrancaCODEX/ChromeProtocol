import { DEFAULT_APPEARANCE, normalizeAppearance, renderPortrait, mountCustomizer, randomAppearance } from './appearance.js';

'use strict';
const $ = id => document.getElementById(id);
const originNames = {street:'Cria das ruas',corpo:'Ex-corporativo',nomad:'Nômade'};
const specialtyNames = {netrunner:'Netrunner',solo:'Combatente',ghost:'Infiltrador'};
let game = null, csrf = null, busy = false, restarting = false, activeTab = 'mission', pending = null, toastTimer;
let creatorAppearance = {...DEFAULT_APPEARANCE}, editorAppearance = null, creationStep = 'identity';
const credits = value => `₡ ${new Intl.NumberFormat('pt-BR').format(value)}`;
function show(id, visible = true) { $(id).classList.toggle('hidden', !visible); }
function text(id, value) { $(id).textContent = value; }
function element(tag, className, value) {
  const el = document.createElement(tag); if (className) el.className = className;
  if (value !== undefined) el.textContent = value; return el;
}
function toast(message, error = false) {
  clearTimeout(toastTimer); text('toast', message); $('toast').classList.toggle('error', error); show('toast');
  toastTimer = setTimeout(() => show('toast', false), error ? 8500 : 5000);
}
function screen(name) {
  ['loading','landing','creation','dashboard'].forEach(id => show(id, id === name));
}
async function request(path, body) {
  const options = {credentials:'same-origin', headers:{Accept:'application/json'}};
  if (body !== undefined) {
    options.method = 'POST'; options.headers['Content-Type'] = 'application/json';
    options.headers[csrf.header] = csrf.token; options.body = JSON.stringify(body);
  }
  let response;
  try { response = await fetch(`/api/${path}`, options); }
  catch { throw new Error('Conexão interrompida. Atualize para conferir o último salvamento antes de tentar novamente.'); }
  const result = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(result.message || (response.status === 403 ? 'Sua sessão expirou. Atualize a página para continuar.' : 'Não foi possível concluir a ação. Tente atualizar a página.'));
  return result;
}
async function init() {
  screen('loading'); show('retry-button', false);
  try {
    csrf = await request('session');
    const result = await request('game'); game = result.started ? result.game : null;
    text('save-status', game ? 'Carreira sincronizada' : 'Canal seguro');
    if (game) { render(); screen('dashboard'); } else screen('landing');
  } catch (error) {
    text('save-status','Sem conexão'); text('loading-message', error.message); show('retry-button');
  }
}
function setBusy(value) {
  busy = value; document.body.setAttribute('aria-busy', String(value));
  document.querySelectorAll('button').forEach(button => {
    if (value) { button.dataset.wasDisabled = String(button.disabled); button.disabled = true; }
    else if (button.dataset.wasDisabled !== undefined) { button.disabled = button.dataset.wasDisabled === 'true'; delete button.dataset.wasDisabled; }
  });
  text('save-status', value ? 'Salvando…' : 'Carreira sincronizada');
}
async function act(type, target = '') {
  if (busy || !game) return;
  setBusy(true);
  let syncStatus = 'Carreira sincronizada';
  try {
    game = await request('game/action', {type, target, revision:game.character.revision});
    toast(game.character.journal.find(entry => !entry.title.startsWith('Nível'))?.text || 'Carreira salva.');
  } catch (error) {
    toast(error.message, true);
    try { const latest = await request('game'); if (latest.started) game = latest.game; }
    catch { syncStatus = 'Sem conexão'; }
  } finally { setBusy(false); text('save-status', syncStatus); render(); }
}
function selectTab(tab) {
  activeTab = tab;
  document.querySelectorAll('[data-tab]').forEach(button => {
    const active = button.dataset.tab === tab; button.classList.toggle('active', active);
    button.setAttribute('aria-current', active ? 'page' : 'false');
  });
  ['mission','district','clinic','dossier'].forEach(id => show(`${id}-tab`, id === tab));
}
function render() {
  if (!game) return;
  const s = game.character;
  text('day-label', `DIA ${String(s.day).padStart(2,'0')} / NOVA AURORA, 2089`);
  text('welcome', s.ended ? 'Toda lenda deixa uma marca.' : 'Seu próximo movimento.');
  text('chapter-label', s.ended ? 'CARREIRA CONCLUÍDA' : `CONTRATO ${String(s.missionIndex+1).padStart(2,'0')} / ${game.totalMissions}`);
  renderPortrait($('profile-portrait'), s.appearance, `Retrato de ${s.name}`);
  text('profile-name', s.name); text('profile-origin', `${originNames[s.origin]} / ${specialtyNames[s.specialty]}`);
  text('profile-level', `NÍVEL ${s.level}`); text('xp-label', `${s.xp} / 100 XP`);
  text('points-label', `${s.skillPoints} PT${s.skillPoints===1?'':'S'}`); $('xp-bar').value = s.xp;
  for (const stat of ['health','energy']) { text(`${stat}-value`, s[stat]); $(`${stat}-bar`).value = s[stat]; }
  text('money-value', credits(s.money)); text('reputation-value', `REP / ${s.reputation}`); text('heat-value', `PROCURADO / ${s.heat}`);
  $('attributes-list').replaceChildren(); $('training-list').replaceChildren(); $('skill-list').replaceChildren();
  Object.entries(game.attributes).forEach(([id,label]) => {
    const bonus = game.shop.filter(i => s.implants.includes(i.id) && i.attribute===id).reduce((n,i)=>n+i.bonus,0);
    const row = element('div','attribute-row'); row.append(element('span','',label));
    const value = element('strong','',String(s.attributes[id])); if (bonus) value.append(element('span','bonus',` +${bonus}`));
    row.append(value); $('attributes-list').append(row);
    const train = element('button','button',`${label} +1`); train.type = 'button';
    train.disabled = s.ended || s.money<150 || s.energy<20 || s.attributes[id]>=10;
    train.addEventListener('click',()=>act('train',id)); $('training-list').append(train);
    const upgrade = element('button','button',`${label} +1 / 1 PT`);
    upgrade.disabled = s.ended || s.skillPoints<1 || s.attributes[id]>=10;
    upgrade.addEventListener('click',()=>act('upgrade',id)); $('skill-list').append(upgrade);
  });
  $('factions-list').replaceChildren();
  Object.entries(game.factions).forEach(([id,label]) => {
    const affinity = s.factions[id]; const row = element('div','faction-row');
    const top = element('div','faction-top'); top.append(element('span','',label), element('span','',`${affinity>0?'+':''}${affinity}`));
    const track = element('div','faction-track');
    for (let n=0;n<10;n++) track.append(element('span',`faction-segment ${n<Math.ceil(Math.abs(affinity)/10)?(affinity<0?'hostile':'filled'):''}`));
    row.append(top,track); $('factions-list').append(row);
  });
  document.querySelectorAll('[data-action]').forEach(button => {
    const type = button.dataset.action;
    button.disabled = s.ended || (type==='work' && s.energy<15) || (type==='debt' && (!s.debt || s.money<s.debt));
  });
  text('debt-text', s.debt ? `Você deve ${credits(s.debt)}. Quite o valor total quando tiver saldo suficiente.` : `Nenhuma dívida. Próximo aluguel no dia ${s.day+(7-(s.day-1)%7)}.`);
  show('debt-button', s.debt>0); text('slots-label', `${s.implants.length} / 3 SLOTS OCUPADOS`);
  renderShop(); renderMission(); renderJournal(); selectTab(activeTab);
  text('transmission-text', s.ended ? 'O sinal termina. A cidade continua.' : s.heat>=70 ? 'Seu nome está nos rádios das patrulhas. Uma noite de descanso pode mudar o tom.' : s.flags.includes('lia') ? 'Uma mensagem de Lia chega: “Ainda tem gente em quem vale a pena confiar.”' : 'Cada contrato compra mais um dia. Algumas escolhas custam mais do que créditos.');
}
function renderShop() {
  const s = game.character; $('shop-list').replaceChildren();
  game.shop.forEach(i => {
    const installed = s.implants.includes(i.id); const card = element('article',`panel shop-card ${installed?'installed':''}`);
    card.append(element('span','shop-symbol','⌘'),element('h3','',i.name),element('span','shop-bonus',`+${i.bonus} ${game.attributes[i.attribute]}`),element('p','',i.description));
    const bottom = element('div','shop-bottom'); bottom.append(element('strong','',credits(i.price)));
    const button = element('button','button',installed?'INSTALADO':s.implants.length>=3?'SEM SLOT':'Instalar ↗');
    button.disabled = s.ended || installed || s.implants.length>=3 || s.money<i.price;
    button.addEventListener('click',()=>openConfirmation({type:'buy',target:i.id},i.name,i.description,
      `${credits(i.price)} · +${i.bonus} ${game.attributes[i.attribute]} · 1 slot permanente`, 'O implante é permanente. A instalação não avança o dia.', 'Confirmar instalação'));
    bottom.append(button); card.append(bottom); $('shop-list').append(card);
  });
}
function renderMission() {
  const s = game.character, m = game.mission;
  show('mission-panel', !s.ended); show('ending-panel', s.ended);
  const lastContract = s.journal.find(entry => entry.title.includes(' • '));
  show('outcome-panel', Boolean(lastContract) && !s.ended);
  if (lastContract) { text('outcome-title', lastContract.title); text('outcome-text', lastContract.text); }
  if (s.ended) {
    text('ending-title',s.ending); text('ending-text',s.journal[0]?.text || 'Sua história chegou ao fim.');
    $('ending-stats').replaceChildren();
    [['DIAS NA CIDADE',s.day-1],['CRÉDITOS',credits(s.money)],['REPUTAÇÃO',s.reputation],['NÍVEL',s.level]].forEach(([label,value])=>{
      const card = element('div','ending-stat'); card.append(element('span','',label),element('strong','',String(value))); $('ending-stats').append(card);
    }); return;
  }
  text('mission-location',m.district.toUpperCase()); text('mission-tag',m.tag); text('mission-contact',m.contact);
  text('mission-title',m.title); text('mission-description',m.description); text('mission-memory',`↳ ${m.memory}`); show('mission-memory',Boolean(m.memory));
  $('choices-list').replaceChildren();
  m.choices.forEach(c => {
    const button = element('button','choice'); button.disabled = !c.available;
    const top = element('div','choice-top'); top.append(element('h3','',c.title),element('span','chance',c.chance===100?'SEM TESTE':`${c.chance}% CHANCE`));
    button.append(top,element('p','',c.description));
    const meta = element('div','choice-meta'); meta.append(element('span','reward',`ATÉ +${credits(c.reward)}`),element('span','',`${c.energy} ENERGIA`));
    if (c.price) meta.append(element('span','',`CUSTO ${credits(c.price)}`));
    if (c.chance!==100) meta.append(element('span','',c.attribute.toUpperCase()));
    button.append(meta); if (!c.available) button.append(element('p','blocked-reason',c.blockedReason));
    button.addEventListener('click',()=>openConfirmation({type:'choice',target:c.id},c.title,c.description,
      `${c.chance===100?'Sem teste':`${c.chance}% de sucesso · ${c.attribute}`}\n${c.energy} de energia · até +${credits(c.reward)}${c.price?` · custo ${credits(c.price)}`:''}`,
      'O resultado é salvo imediatamente. Esta escolha avança um dia e encerra o contrato atual.', 'Confirmar escolha ↗'));
    $('choices-list').append(button);
  });
}
function renderJournal() {
  $('journal-list').replaceChildren();
  game.character.journal.forEach(entry => {
    const item = element('article',`journal-entry ${entry.tone}`);
    item.append(element('span','',`DIA ${String(entry.day).padStart(2,'0')}`),element('h3','',entry.title),element('p','',entry.text));
    $('journal-list').append(item);
  });
}
function openConfirmation(action, title, description, details, notice, buttonLabel) {
  pending = action; text('confirm-title',title); text('confirm-description',description); text('confirm-details',details);
  $('choice-dialog').querySelector('.micro-copy').textContent = notice; text('confirm-choice',buttonLabel); $('choice-dialog').showModal();
}
function updateCreatorPreview() {
  renderPortrait($('creator-portrait'), creatorAppearance, 'Prévia do seu personagem');
  text('creator-name-preview', $('character-name').value.trim() || 'SEM REGISTRO');
  const data = new FormData($('character-form'));
  text('creator-build-preview', `${originNames[data.get('origin')]} / ${specialtyNames[data.get('specialty')]}`);
}
function mountCreator() {
  mountCustomizer($('creation-customizer'), creatorAppearance, appearance => {
    creatorAppearance = appearance; updateCreatorPreview();
  }, 'create');
  updateCreatorPreview();
}
function validName() {
  const input = $('character-name');
  input.setCustomValidity(input.value.trim() ? '' : 'Digite seu nome ou codinome.');
  return input.reportValidity();
}
function selectCreationStep(step) {
  if (step === 'history' && !validName()) return;
  creationStep = step;
  show('identity-step', step === 'identity'); show('history-step', step === 'history');
  show('identity-next', step === 'identity'); show('creation-submit', step === 'history');
  text('creator-step-note', step === 'identity' ? 'Seu visual não altera os atributos do personagem.' : 'Sua carreira é salva automaticamente neste navegador e servidor.');
  document.querySelectorAll('[data-step]').forEach(button => {
    const active = button.dataset.step === step; button.classList.toggle('active', active);
    if(active) button.setAttribute('aria-current','step'); else button.removeAttribute('aria-current');
  });
}
function createScreen(restart = false) {
  restarting = restart;
  creatorAppearance = {...DEFAULT_APPEARANCE}; $('character-form').reset();
  text('creation-notice', restart ? 'Uma nova carreira substitui a atual somente depois de confirmar. Para mudar apenas a aparência, use “Editar visual” no seu perfil.' : 'O rosto é seu. A reputação, você conquista.');
  text('creation-submit', restart ? 'Substituir carreira e começar ↗' : 'Entrar em Nova Aurora ↗');
  screen('creation'); selectCreationStep('identity'); mountCreator(); $('character-name').focus(); window.scrollTo({top:0,behavior:'instant'});
}
function mountEditor() {
  mountCustomizer($('editor-customizer'), editorAppearance, appearance => {
    editorAppearance = appearance; renderPortrait($('editor-portrait'), editorAppearance, 'Prévia do novo visual');
  }, 'edit');
  renderPortrait($('editor-portrait'), editorAppearance, 'Prévia do novo visual');
}
$('edit-appearance').addEventListener('click', () => {
  if(busy || !game) return;
  editorAppearance = normalizeAppearance(game.character.appearance); mountEditor(); $('appearance-dialog').showModal();
});
$('randomize-appearance').addEventListener('click', () => { creatorAppearance = randomAppearance(); mountCreator(); });
$('randomize-editor').addEventListener('click', () => { editorAppearance = randomAppearance(); mountEditor(); });
$('save-appearance').addEventListener('click', async () => {
  if(busy || !game || !editorAppearance) return;
  setBusy(true);
  try {
    game = await request('game/appearance', {appearance:editorAppearance, revision:game.character.revision});
    $('appearance-dialog').close(); toast('Visual atualizado. Sua história continua.');
  } catch(error) {
    toast(error.message, true);
    try { const latest = await request('game'); if(latest.started)game=latest.game; } catch { /* Keep the editor open for retry. */ }
  } finally { setBusy(false); render(); }
});
$('identity-next').addEventListener('click', () => selectCreationStep('history'));
document.querySelectorAll('[data-step]').forEach(button => button.addEventListener('click', () => selectCreationStep(button.dataset.step)));
$('character-form').addEventListener('change', updateCreatorPreview);
$('character-form').addEventListener('submit',async event => {
  event.preventDefault(); if (busy) return;
  if (creationStep === 'identity') { selectCreationStep('history'); return; }
  const data = new FormData(event.currentTarget);
  const body = {name:data.get('name').trim(),origin:data.get('origin'),specialty:data.get('specialty'), appearance:creatorAppearance};
  if (!body.name) { $('character-name').setCustomValidity('Digite seu nome ou codinome.'); $('character-name').reportValidity(); return; }
  if (restarting) body.revision = game.character.revision;
  setBusy(true);
  try { game = await request(restarting?'game/restart':'game',body); activeTab='mission'; render(); screen('dashboard'); toast('Canal aberto. Bem-vindo a Nova Aurora.'); }
  catch (error) { toast(error.message,true); }
  finally { setBusy(false); if (game && !$('dashboard').classList.contains('hidden')) render(); }
});
$('character-name').addEventListener('input',()=> { $('character-name').setCustomValidity(''); updateCreatorPreview(); });
$('start-button').addEventListener('click',()=>createScreen());
$('creation-back').addEventListener('click',()=>{screen(game?'dashboard':'landing'); if(game)render();});
$('retry-button').addEventListener('click',init);
$('help-button').addEventListener('click',()=> $('help-dialog').showModal());
$('confirm-choice').addEventListener('click',()=>{ $('choice-dialog').close(); if(pending)act(pending.type,pending.target); pending=null; });
$('choice-dialog').addEventListener('close',()=>{ pending=null; });
document.querySelectorAll('[data-tab]').forEach(button=>button.addEventListener('click',()=>selectTab(button.dataset.tab)));
document.querySelectorAll('[data-action]').forEach(button=>button.addEventListener('click',()=>act(button.dataset.action)));
document.querySelectorAll('.new-career-button').forEach(button=>button.addEventListener('click',()=>createScreen(true)));
$('export-button').addEventListener('click',()=>{
  const blob = new Blob([JSON.stringify({title:'ChromeProtocol — dossiê',version:'0.2.0',exportedAt:new Date().toISOString(),character:game.character},null,2)],{type:'application/json'});
  const url = URL.createObjectURL(blob); const link = element('a'); link.href=url; link.download='chromeprotocol-dossie.json'; link.click(); setTimeout(()=>URL.revokeObjectURL(url),1000);
});
init();
