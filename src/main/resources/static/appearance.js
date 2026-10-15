/** Original layered SVG portrait. Every interpolated token comes from these fixed palettes. */
const skinColors = {porcelain:'#edcfb9',sand:'#d9b291',warm:'#bc8b70',copper:'#a56c51',umber:'#76503e',ebony:'#49352f'};
const hairColors = {black:'#20232e',silver:'#d8dde2',cyan:'#48d8df',violet:'#a28bfb',red:'#dc5968',gold:'#e7b66c'};
const accents = {cyan:'#58f0d4',acid:'#dcf76a',magenta:'#fa78ca',amber:'#ffb35a'};
export const DEFAULT_APPEARANCE = Object.freeze({face:'angular',skin:'warm',hair:'undercut',hairColor:'silver',eyes:'optic',outfit:'runner',accent:'cyan'});
export const OPTIONS = Object.freeze({
  face:{label:'Formato do rosto',options:[['angular','Angular'],['soft','Suave'],['broad','Largo']]},
  skin:{label:'Tom de pele',options:[['porcelain','Porcelana'],['sand','Areia'],['warm','Dourado'],['copper','Cobre'],['umber','Castanho'],['ebony','Ébano']],colors:skinColors},
  hair:{label:'Corte de cabelo',options:[['undercut','Undercut'],['mohawk','Moicano'],['bob','Chanel'],['braids','Tranças'],['shaved','Raspado']]},
  hairColor:{label:'Cor do cabelo',options:[['black','Preto'],['silver','Prata'],['cyan','Ciano'],['violet','Violeta'],['red','Vermelho'],['gold','Dourado']],colors:hairColors},
  eyes:{label:'Implante facial',options:[['natural','Sem implante'],['optic','Óptica Íris'],['visor','Visor neural']]},
  outfit:{label:'Vestuário',options:[['runner','Jaqueta urbana'],['armor','Colete tático'],['nomad','Nômade']]},
  accent:{label:'Assinatura neon',options:[['cyan','Ciano'],['acid','Lima'],['magenta','Magenta'],['amber','Âmbar']],colors:accents}
});
export function normalizeAppearance(value) {
  return Object.fromEntries(Object.entries(OPTIONS).map(([key,group])=>[key,group.options.some(([id])=>id===value?.[key])?value[key]:DEFAULT_APPEARANCE[key]]));
}
export function randomAppearance() {
  const numbers = crypto.getRandomValues(new Uint32Array(7));
  return Object.fromEntries(Object.entries(OPTIONS).map(([key,group],index)=>[key,group.options[numbers[index]%group.options.length][0]]));
}
export function mountCustomizer(host, value, onChange, prefix) {
  const appearance=normalizeAppearance(value); host.replaceChildren();
  for (const [key,group] of Object.entries(OPTIONS)) {
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
      } else { const text=document.createElement('span');text.textContent=label;item.append(text); }
      options.append(item);
    }
    field.append(options);host.append(field);
  }
}
export function renderPortrait(host, value, label='Retrato do personagem') {
  const a=normalizeAppearance(value),skin=skinColors[a.skin],hair=hairColors[a.hairColor],neon=accents[a.accent];
  const faces={angular:'M135 126Q137 91 180 90Q223 91 225 126L219 193 198 219 180 227 161 219 141 193Z',soft:'M135 131Q134 91 180 90Q226 91 225 131L222 179Q218 215 180 228Q142 215 138 179Z',broad:'M127 129Q130 91 180 90Q230 91 233 129L229 185 216 213 180 227 144 213 131 185Z'};
  const hairs={
    undercut:`<path d="M131 147L128 115Q132 73 179 75Q224 67 235 113L224 145 215 117Q176 127 144 113L140 147Z" fill="${hair}"/><path d="M147 91Q179 73 215 94M146 101Q178 84 222 104" stroke="#111b2b" stroke-width="3" opacity=".5"/>`,
    mohawk:`<path d="M132 147L130 120Q136 84 167 85L175 51 190 45 197 83Q227 91 230 123L225 146 215 122Q183 104 145 124L139 148Z" fill="${hair}"/><path d="M181 54L184 104" stroke="${neon}" stroke-width="3"/>`,
    bob:`<path d="M119 191L122 117Q124 70 180 72Q236 70 239 119L242 201 215 188 214 116Q182 136 143 119L145 198Z" fill="${hair}"/><path d="M131 126L128 189M229 126L232 187" stroke="#0c1422" stroke-width="3" opacity=".4"/>`,
    braids:`<path d="M128 139L129 111Q135 77 178 76Q225 73 232 114L229 140 218 117Q182 102 143 119L140 145Z" fill="${hair}"/><g fill="none" stroke="${hair}" stroke-width="13" stroke-linecap="round"><path d="M133 124Q115 166 124 218M225 122Q244 164 235 223M145 92Q173 120 217 95"/></g><g stroke="${neon}" stroke-width="5"><path d="M119 185l11 2M230 191l11-2M121 208l11 1"/></g>`,
    shaved:`<path d="M132 140L130 120Q134 86 179 85Q225 86 230 122L225 140 218 117Q180 99 141 119L139 141Z" fill="${hair}" opacity=".65"/><path d="M143 108l61-8M140 115l72-8" stroke="#15212c" stroke-width="2" opacity=".5"/>`
  };
  const outfits={
    runner:`<path d="M73 382L84 298 136 269 157 253H205L225 271 278 298 291 382Z" fill="#202d43" stroke="#556078" stroke-width="2"/><path d="M136 269L157 253 177 297 144 330 120 285ZM205 253L225 271 241 288 214 327 183 297Z" fill="#354459" stroke="#758399" stroke-width="2"/><path d="M176 300V381M90 327l39-18M273 327l-37-19" stroke="${neon}" stroke-width="3"/><path d="M152 335h-35v29h35Z" fill="#131e2c" stroke="#67788b"/><path d="M219 343h40" stroke="${neon}" stroke-width="6"/><path d="M99 318l-9 49M264 316l9 51" stroke="#82919e" stroke-width="3"/>`,
    armor:`<path d="M68 382L84 298 141 265H219L277 298 294 382Z" fill="#14242f" stroke="#607989" stroke-width="2"/><path d="M124 271L143 257H216L238 277 232 374H129Z" fill="#2c3b46" stroke="#7c8e93" stroke-width="2"/><path d="M143 291h74v39h-74zM143 344h74v28h-74z" fill="#172932" stroke="#617680"/><path d="M154 306h52M171 358h20" stroke="${neon}" stroke-width="4"/><path d="M89 307l30 8M241 314l30-7" stroke="${neon}" stroke-width="6"/><path d="M94 328l-7 42M268 328l7 42" stroke="#6b8090" stroke-width="4"/>`,
    nomad:`<path d="M68 382L87 299 138 269H220L274 300 293 382Z" fill="#62503f" stroke="#a38e6c" stroke-width="2"/><path d="M133 261L153 247H207L230 268 213 310 144 308Z" fill="#8f7255" stroke="#c8ad7d" stroke-width="2"/><path d="M150 267l64 19M145 281l64 20" stroke="#3e3532" stroke-width="3"/><path d="M177 313v68" stroke="${neon}" stroke-width="3"/><path d="M104 325h40v35h-40zM218 325h40v35h-40z" fill="#423e36" stroke="#b69a70"/><path d="M111 334h25M226 334h25" stroke="${neon}" stroke-width="3"/>`
  };
  const eyes={natural:'',optic:`<path d="M196 143h25v18h-25z" fill="#263b46" stroke="${neon}" stroke-width="1.5"/><circle cx="205" cy="151" r="5" fill="${neon}"/><path d="M214 160v28l-12 13" fill="none" stroke="${neon}" stroke-width="2"/><circle cx="202" cy="202" r="2" fill="${neon}"/>`,visor:`<path d="M132 140L227 138 225 162 137 167Z" fill="#112d3a" stroke="${neon}" stroke-width="2"/><path d="M143 150l69-3" stroke="${neon}" stroke-width="4"/><path d="M145 155l50-2" stroke="#e9fffb" opacity=".65" stroke-width="1"/>`};
  host.replaceChildren();
  // All fragments and colors above are constants selected by normalizeAppearance; player text never enters SVG markup.
  const wrapper=document.createElement('div');wrapper.innerHTML=`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 400" fill="none" class="character-svg"><path d="M0 0h360v400H0z" fill="#101a2c"/><circle cx="180" cy="171" r="117" stroke="${neon}" opacity=".15"/><circle cx="180" cy="171" r="99" stroke="${neon}" opacity=".3" stroke-dasharray="3 8"/><path d="M25 171h310M180 21v310" stroke="${neon}" opacity=".12"/><path d="M27 45h32M27 45v32M301 45h32v32M27 333v32h32M333 333v32h-32" stroke="${neon}" stroke-width="2" opacity=".6"/><path d="M155 199v57l25 26 26-26v-57" fill="${skin}" stroke="#31232e" stroke-width="2"/><path d="M155 212q25 26 51 0v20q-26 20-51 0" fill="#211d2b" opacity=".25"/>${outfits[a.outfit]}<path d="M135 144q-18-9-13 15l14 20M225 144q18-9 13 15l-14 20" fill="${skin}" stroke="#6b4645" stroke-width="2"/><path d="${faces[a.face]}" fill="${skin}" stroke="#67474b" stroke-width="2"/><path d="M143 171l5 24 18 18M217 171l-5 25-17 18" stroke="#352e3c" opacity=".25" stroke-width="3"/><path d="M146 141l20-3M193 138l20 3" stroke="${hair}" stroke-width="5"/><path d="M146 152h19M196 152h19" stroke="#3b303b" stroke-width="3"/><circle cx="155" cy="151" r="3" fill="#e9d8c5"/><circle cx="205" cy="151" r="3" fill="#e9d8c5"/><circle cx="155" cy="151" r="1.8" fill="#18202a"/><circle cx="205" cy="151" r="1.8" fill="#18202a"/><path d="M179 151l-5 28 11 1M168 197q12 4 25-1" stroke="#68484c" stroke-width="2.5" stroke-linecap="round"/><path d="M148 170l11 6M146 175l10 5" stroke="${neon}" stroke-width="1.5" opacity=".7"/>${hairs[a.hair]}${eyes[a.eyes]}<path d="M28 376h67M265 376h67" stroke="${neon}" opacity=".6"/><path d="M0 396h360" stroke="${neon}" stroke-width="4"/></svg>`;
  const svg=wrapper.firstElementChild;svg.setAttribute('role','img');svg.setAttribute('aria-label',label);host.append(svg);
  if(host.id==='profile-portrait') svg.setAttribute('viewBox','95 60 170 230');
  host.dataset.accent=a.accent;
}
