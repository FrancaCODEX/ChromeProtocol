import {studioSvg} from './vendor/avatar-studio.js';
/** Original layered SVG portrait. Every interpolated token comes from these fixed palettes. */
const skinColors = {porcelain:'#edcfb9',sand:'#d9b291',warm:'#bc8b70',copper:'#a56c51',umber:'#76503e',ebony:'#49352f'};
const hairColors = {black:'#20232e',silver:'#d8dde2',cyan:'#48d8df',violet:'#a28bfb',red:'#dc5968',gold:'#e7b66c',brown:'#624536',auburn:'#a85b3a'};
const accents = {cyan:'#58f0d4',acid:'#dcf76a',magenta:'#fa78ca',amber:'#ffb35a',red:'#ff5967',blue:'#6ca8ff',white:'#ecf4fa'};
export const DEFAULT_APPEARANCE = Object.freeze({face:'angular',skin:'warm',hair:'undercut',hairColor:'black',eyes:'natural',outfit:'runner',accent:'cyan',beard:'none',eyeColor:'brown',eyewear:'none',headwear:'none',jerseyPattern:'diagonal',portraitStyle:'studio'});
export const OPTIONS = Object.freeze({
  portraitStyle:{label:'Estilo do retrato',options:[['studio','Estúdio'],['classic','Clássico']]},
  face:{label:'Formato do rosto',options:[['angular','Angular'],['soft','Suave'],['broad','Largo']]},
  skin:{label:'Tom de pele',options:[['porcelain','Porcelana'],['sand','Areia'],['warm','Dourado'],['copper','Cobre'],['umber','Castanho'],['ebony','Ébano']],colors:skinColors},
  hair:{label:'Corte de cabelo',options:[['undercut','Undercut'],['mohawk','Moicano'],['bob','Chanel'],['braids','Tranças'],['shaved','Raspado'],['curly','Cacheado'],['long','Longo'],['ponytail','Rabo de cavalo'],['afro','Afro']]},
  hairColor:{label:'Cor do cabelo',options:[['black','Preto'],['silver','Prata'],['cyan','Ciano'],['violet','Violeta'],['red','Vermelho'],['gold','Dourado'],['brown','Castanho'],['auburn','Acobreado']],colors:hairColors},
  beard:{label:'Barba',options:[['none','Sem barba'],['stubble','Por fazer'],['full','Cheia'],['goatee','Cavanhaque'],['mustache','Bigode']]},
  eyeColor:{label:'Cor dos olhos',options:[['brown','Castanho'],['hazel','Mel'],['blue','Azul'],['green','Verde'],['gray','Cinza']],colors:{brown:'#63432c',hazel:'#ab854b',blue:'#76b3d4',green:'#78a978',gray:'#a2aebd'}},
  eyewear:{label:'Óculos',options:[['none','Sem óculos'],['rectangular','Retangular'],['round','Redondo'],['sun','Escuros']]},
  headwear:{label:'Acessório de cabeça',options:[['none','Sem acessório'],['headset','Headset'],['cap','Boné']]},
  outfit:{label:'Vestuário',options:[['runner','Jersey'],['armor','Jaqueta esportiva'],['nomad','Moletom']]},
  accent:{label:'Cor da equipe',options:[['cyan','Ciano'],['acid','Lima'],['magenta','Magenta'],['amber','Âmbar'],['red','Vermelho'],['blue','Azul'],['white','Branco']],colors:accents},
  jerseyPattern:{label:'Estampa da jersey',options:[['diagonal','Diagonal'],['minimal','Minimalista'],['stripes','Listras']]}
});
export function normalizeAppearance(value) {
  const legacy={portraitStyle:'classic',eyewear:value?.eyes==='optic'?'rectangular':'none',headwear:value?.eyes==='visor'?'headset':'none'};
  return {...Object.fromEntries(Object.entries(OPTIONS).map(([key,group])=>[key,group.options.some(([id])=>id===value?.[key])?value[key]:legacy[key]||DEFAULT_APPEARANCE[key]])),eyes:'natural'};
}
export function randomAppearance() {
  const numbers = crypto.getRandomValues(new Uint32Array(Object.keys(OPTIONS).length));
  return Object.fromEntries(Object.entries(OPTIONS).map(([key,group],index)=>[key,group.options[numbers[index]%group.options.length][0]]));
}
export function mountCustomizer(host, value, onChange, prefix) {
  const appearance=normalizeAppearance(value); host.replaceChildren();
  const tabs=document.createElement('div');tabs.className='avatar-tabs';tabs.setAttribute('role','tablist');const content=document.createElement('div');host.append(tabs,content);
  const categories={Rosto:['portraitStyle','face','skin','eyeColor','beard'],Cabelo:['hair','hairColor'],Estilo:['eyewear','headwear','outfit','accent','jerseyPattern']};
  Object.entries(categories).forEach(([name,keys],index)=>{const button=document.createElement('button');button.type='button';button.textContent=name;button.className='avatar-tab'+(index===0?' active':'');button.setAttribute('role','tab');button.setAttribute('aria-selected',String(index===0));const pane=document.createElement('div');pane.className='avatar-pane'+(index?' hidden':'');button.addEventListener('click',()=>{tabs.querySelectorAll('button').forEach(b=>{b.classList.toggle('active',b===button);b.setAttribute('aria-selected',String(b===button));});content.querySelectorAll('.avatar-pane').forEach(p=>p.classList.toggle('hidden',p!==pane));});tabs.append(button);content.append(pane);
  for (const key of keys) {const group=OPTIONS[key];
    const field=document.createElement('fieldset'); field.className='appearance-group';
    const legend=document.createElement('legend'); legend.textContent=group.label; field.append(legend);
    const options=document.createElement('div'); options.className=group.colors?'swatch-options':'appearance-options';
    for (const [id,label] of group.options) {
      const item=document.createElement('label'); item.className=group.colors?'swatch-option':'appearance-option';
      const input=document.createElement('input'); input.type='radio'; input.name=`${prefix}-${key}`; input.value=id; input.checked=appearance[key]===id;
      input.setAttribute('aria-label',`${group.label}: ${label}`); item.title=label;
      input.addEventListener('change',()=>{appearance[key]=id;onChange({...appearance});}); item.append(input);
      if(group.colors) {
        const dot=document.createElementNS('http://www.w3.org/2000/svg','svg');dot.setAttribute('viewBox','0 0 32 32');dot.setAttribute('aria-hidden','true');
        const circle=document.createElementNS(dot.namespaceURI,'circle');circle.setAttribute('cx','16');circle.setAttribute('cy','16');circle.setAttribute('r','11');circle.setAttribute('fill',group.colors[id]);dot.append(circle);item.append(dot);
        const sr=document.createElement('span');sr.className='sr-only';sr.textContent=label;item.append(sr);
      } else {if(['portraitStyle','hair','outfit','beard','headwear'].includes(key)){const preview=document.createElement('span');preview.className='avatar-option-preview';renderPortrait(preview,{...appearance,[key]:id},label);item.append(preview);} const text=document.createElement('span');text.textContent=label;item.append(text); }
      options.append(item);
    }
    field.append(options);pane.append(field);
  }});
}
export function renderPortrait(host, value, label='Retrato do personagem') {
  const a=normalizeAppearance(value),skin=skinColors[a.skin],hair=hairColors[a.hairColor],neon=accents[a.accent];
  if(a.portraitStyle==='studio'){
    const wrap=document.createElement('div');wrap.innerHTML=studioSvg(a,{skin,hair,accent:neon,iris:OPTIONS.eyeColor.colors[a.eyeColor]});const svg=wrap.firstElementChild;svg.classList.add('character-svg','studio-svg');svg.setAttribute('role','img');svg.setAttribute('aria-label',label);host.replaceChildren(svg);host.dataset.accent=a.accent;return;
  }
  const faces={angular:'M135 126Q137 91 180 90Q223 91 225 126L219 193 198 219 180 227 161 219 141 193Z',soft:'M135 131Q134 91 180 90Q226 91 225 131L222 179Q218 215 180 228Q142 215 138 179Z',broad:'M127 129Q130 91 180 90Q230 91 233 129L229 185 216 213 180 227 144 213 131 185Z'};
  const hairs={
    curly:`<path d="M129 144V116Q126 81 153 80Q174 62 195 78Q232 76 233 118L224 146 215 120Q177 133 142 117L139 146Z" fill="${hair}"/><g fill="${hair}" stroke="#0e1422" stroke-opacity=".3" stroke-width="2"><circle cx="143" cy="100" r="15"/><circle cx="163" cy="89" r="17"/><circle cx="185" cy="86" r="17"/><circle cx="207" cy="93" r="17"/><circle cx="223" cy="110" r="13"/></g>`,
    long:`<path d="M117 265L119 120Q121 69 180 70Q239 69 241 121L245 265 216 249 215 117Q177 131 144 116L144 251Z" fill="${hair}"/><path d="M128 125v117M232 129v113" stroke="#131b2a" opacity=".35" stroke-width="4"/>`,
    ponytail:`<path d="M220 107Q260 82 263 136L252 236 233 219 242 141 222 133Z" fill="${hair}"/><path d="M129 144V117Q134 75 180 76Q226 73 232 118L225 146 215 117Q180 125 145 116L140 145Z" fill="${hair}"/><path d="M235 118l17 4" stroke="${neon}" stroke-width="7"/>`,
    afro:`<path d="M126 160Q102 153 108 132Q93 111 111 94Q106 71 132 65Q138 43 162 52Q181 31 199 52Q228 44 235 68Q262 73 254 99Q272 121 252 140Q258 163 231 166L217 121Q180 137 143 120L139 163Z" fill="${hair}"/>`,
    undercut:`<path d="M131 147L128 115Q132 73 179 75Q224 67 235 113L224 145 215 117Q176 127 144 113L140 147Z" fill="${hair}"/><path d="M147 91Q179 73 215 94M146 101Q178 84 222 104" stroke="#111b2b" stroke-width="3" opacity=".5"/>`,
    mohawk:`<path d="M132 147L130 120Q136 84 167 85L175 51 190 45 197 83Q227 91 230 123L225 146 215 122Q183 104 145 124L139 148Z" fill="${hair}"/><path d="M181 54L184 104" stroke="${neon}" stroke-width="3"/>`,
    bob:`<path d="M119 191L122 117Q124 70 180 72Q236 70 239 119L242 201 215 188 214 116Q182 136 143 119L145 198Z" fill="${hair}"/><path d="M131 126L128 189M229 126L232 187" stroke="#0c1422" stroke-width="3" opacity=".4"/>`,
    braids:`<path d="M128 139L129 111Q135 77 178 76Q225 73 232 114L229 140 218 117Q182 102 143 119L140 145Z" fill="${hair}"/><g fill="none" stroke="${hair}" stroke-width="13" stroke-linecap="round"><path d="M133 124Q115 166 124 218M225 122Q244 164 235 223M145 92Q173 120 217 95"/></g><g stroke="${neon}" stroke-width="5"><path d="M119 185l11 2M230 191l11-2M121 208l11 1"/></g>`,
    shaved:`<path d="M132 140L130 120Q134 86 179 85Q225 86 230 122L225 140 218 117Q180 99 141 119L139 141Z" fill="${hair}" opacity=".65"/><path d="M143 108l61-8M140 115l72-8" stroke="#15212c" stroke-width="2" opacity=".5"/>`
  };
  const outfits={
    runner:`<path d="M63 400L83 298 143 266H216L277 298 298 400Z" fill="#26374c" stroke="#718298" stroke-width="2"/><path d="M144 266l36 31 36-31-9-9h-54z" fill="${neon}"/><path d="M83 298l37 27-8 29-39-19M277 298l-37 27 8 29 39-19" fill="${neon}"/><path d="M126 302L235 400H267L144 289Z" fill="${neon}" opacity=".35"/><path d="M154 324h52v43h-52z" fill="#172536"/><text x="180" y="351" fill="${neon}" font-family="Arial" font-size="21" font-weight="bold" text-anchor="middle">CC</text>`,
    armor:`<path d="M64 400L84 298 144 264H216L276 298 298 400Z" fill="#233346" stroke="#718298" stroke-width="2"/><path d="M144 264l17-13h38l17 13-35 42z" fill="#111d2e"/><path d="M180 305v95M84 299l44 32M277 299l-44 32" stroke="${neon}" stroke-width="5"/><path d="M115 351h38M208 351h38" stroke="#9dadbc" stroke-width="3"/>`,
    nomad:`<path d="M64 400L85 296 136 263 145 246h70l10 17 51 33 22 104Z" fill="#475466" stroke="#8591a2" stroke-width="2"/><path d="M136 263l10-21q34-14 69 0l10 21-20 39h-48z" fill="#26374b"/><path d="M151 266l29 24 29-24" stroke="#8995a4" stroke-width="3"/><path d="M169 290v49M191 290v49" stroke="${neon}" stroke-width="3"/><path d="M133 355h94l-13 29h-68z" fill="#354559"/>`
  };
  const lens=a.eyewear==='sun'?'#101827':'#b6d4e0';
  const glasses=a.eyewear==='none'?'':a.eyewear==='round'?`<g fill="${lens}" fill-opacity=".2" stroke="#172534" stroke-width="3"><circle cx="156" cy="151" r="16"/><circle cx="205" cy="151" r="16"/><path d="M172 148h17M132 145l8 2M221 147l10-2"/></g>`:`<g fill="${lens}" fill-opacity="${a.eyewear==='sun'?'.95':'.25'}" stroke="#172534" stroke-width="3"><rect x="138" y="138" width="36" height="27" rx="5"/><rect x="187" y="138" width="36" height="27" rx="5"/><path d="M174 148h13M131 144l7 3M223 147l8-3"/></g>`;
  const head=a.headwear==='headset'?`<path d="M121 153v-27q1-62 59-62 60 0 61 62v27" stroke="#192d43" stroke-width="14"/><path d="M121 150v-25q1-54 59-54 58 0 61 54v25" stroke="${neon}" stroke-width="3"/><rect x="115" y="140" width="19" height="44" rx="7" fill="#21354b" stroke="${neon}" stroke-width="2"/><rect x="227" y="140" width="19" height="44" rx="7" fill="#21354b" stroke="${neon}" stroke-width="2"/><path d="M239 179q1 31-33 31" stroke="#8198ae" stroke-width="4"/>`:a.headwear==='cap'?`<path d="M126 119Q126 64 180 65Q232 63 234 119Z" fill="#24374d" stroke="#52667b" stroke-width="2"/><path d="M127 113Q180 99 242 118L256 130Q189 117 126 126Z" fill="${neon}"/><path d="M175 77h11v23h-11z" fill="${neon}"/>`:'';
  const beards={none:'',stubble:`<path d="M144 185Q150 217 180 223Q212 217 218 185L207 203 193 205 180 210 165 205 153 201Z" fill="${hair}" opacity=".4"/>`,full:`<path d="M142 178L154 192 166 188 180 194 194 188 208 192 219 178 215 209 199 225 180 234 161 225 146 209Z" fill="${hair}"/><path d="M164 199q16 6 32 0" stroke="${skin}" stroke-width="3"/>`,goatee:`<path d="M164 200L170 213h20l7-13-5 22-12 5-12-5Z" fill="${hair}"/><path d="M166 190q14-8 28 0l-14 3z" fill="${hair}"/>`,mustache:`<path d="M180 187q-12-5-22 9 13 5 22-1 9 6 22-1-10-11-22-7Z" fill="${hair}"/>`};
  const pattern=a.outfit!=='runner'||a.jerseyPattern==='diagonal'?'':a.jerseyPattern==='minimal'?`<path d="M118 307h124v91H118Z" fill="#26374c"/><path d="M153 327h54" stroke="${neon}" stroke-width="3"/>`:`<path d="M132 304v94M160 312v86M200 312v86M228 304v94" stroke="${neon}" stroke-width="9" opacity=".7"/>`;
  const iris=OPTIONS.eyeColor.colors[a.eyeColor];
  host.replaceChildren();
  // All fragments and colors above are constants selected by normalizeAppearance; player text never enters SVG markup.
  const wrapper=document.createElement('div');wrapper.innerHTML=`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 400" fill="none" class="character-svg"><path d="M0 0h360v400H0z" fill="#182335"/><path d="M0 320L360 110v70L0 390Z" fill="${neon}" opacity=".12"/><circle cx="180" cy="171" r="117" stroke="${neon}" opacity=".15"/><circle cx="180" cy="171" r="99" stroke="${neon}" opacity=".3" stroke-dasharray="3 8"/><path d="M27 45h32M27 45v32M301 45h32v32M27 333v32h32M333 333v32h-32" stroke="${neon}" stroke-width="2" opacity=".6"/><path d="M155 199v57l25 26 26-26v-57" fill="${skin}" stroke="#31232e" stroke-width="2"/><path d="M155 212q25 26 51 0v20q-26 20-51 0" fill="#211d2b" opacity=".25"/>${outfits[a.outfit]}${pattern}<path d="M135 144q-18-9-13 15l14 20M225 144q18-9 13 15l-14 20" fill="${skin}" stroke="#6b4645" stroke-width="2"/><path d="${faces[a.face]}" fill="${skin}" stroke="#67474b" stroke-width="2"/><path d="M143 171l5 24 18 18M217 171l-5 25-17 18" stroke="#352e3c" opacity=".25" stroke-width="3"/><path d="M146 141l20-3M193 138l20 3" stroke="${hair}" stroke-width="5"/><path d="M146 152h19M196 152h19" stroke="#3b303b" stroke-width="3"/><circle cx="155" cy="151" r="3" fill="#e9d8c5"/><circle cx="205" cy="151" r="3" fill="#e9d8c5"/><circle cx="155" cy="151" r="2.6" fill="${iris}"/><circle cx="205" cy="151" r="2.6" fill="${iris}"/><path d="M179 151l-5 28 11 1M168 197q12 4 25-1" stroke="#68484c" stroke-width="2.5" stroke-linecap="round"/>${beards[a.beard]}${hairs[a.hair]}${glasses}${head}<path d="M28 376h67M265 376h67" stroke="${neon}" opacity=".6"/><path d="M0 396h360" stroke="${neon}" stroke-width="4"/></svg>`;
  const svg=wrapper.firstElementChild;svg.setAttribute('role','img');svg.setAttribute('aria-label',label);host.append(svg);
  if(host.id==='profile-portrait') svg.setAttribute('viewBox','55 45 250 315');
  host.dataset.accent=a.accent;
}
