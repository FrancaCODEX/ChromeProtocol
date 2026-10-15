import {createAvatar} from '@dicebear/core';
import * as avataaars from '@dicebear/avataaars';
let renderSequence=0;
// Local, deterministic renderer. All inputs are validated palettes in appearance.js.
export function studioSvg(a,colors) {
 const top={undercut:'shortFlat',mohawk:'shavedSides',bob:'bob',braids:'dreads',shaved:'sides',curly:'shortCurly',long:'straight01',ponytail:'bun',afro:'fro'};
 const beard={stubble:'beardLight',full:'beardMajestic',goatee:'beardMedium',mustache:'moustacheFancy'};
 const options={seed:'clutch-studio-v1',style:['default'],backgroundColor:['transparent'],top:[top[a.hair]],topProbability:a.hair==='shaved'||a.headwear==='cap'?0:100,hairColor:[colors.hair.slice(1)],skinColor:[colors.skin.slice(1)],facialHair:[beard[a.beard]||'beardLight'],facialHairProbability:a.beard==='none'?0:100,facialHairColor:[colors.hair.slice(1)],accessories:[{rectangular:'prescription02',round:'round',sun:'sunglasses'}[a.eyewear]||'round'],accessoriesProbability:a.eyewear==='none'?0:100,accessoriesColor:['26334a'],clothing:[{runner:'shirtCrewNeck',armor:'blazerAndShirt',nomad:'hoodie'}[a.outfit]],clothesColor:[a.outfit==='runner'?'26374c':colors.accent.slice(1)],eyebrows:['defaultNatural'],eyes:['default'],mouth:['smile']};
 let svg=createAvatar(avataaars,options).toString();
 // Customize the two irises in the pinned Avataaars 9.4.2 eye component.
 svg=svg.replace('d="M36 22a6 6 0 1 1-12 0 6 6 0 0 1 12 0ZM88 22a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z" fill="#000" fill-opacity=".6"',`d="M36 22a6 6 0 1 1-12 0 6 6 0 0 1 12 0ZM88 22a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z" fill="${colors.iris}"`);
 svg=svg.replace(`fill="${colors.iris}"/>`,`fill="${colors.iris}"/><circle cx="30" cy="22" r="3" fill="#172333"/><circle cx="82" cy="22" r="3" fill="#172333"/><circle cx="28.5" cy="20.5" r="1" fill="#fff"/><circle cx="80.5" cy="20.5" r="1" fill="#fff"/>`);
 const width={angular:1,soft:.96,broad:1.06}[a.face];
 const body=svg.slice(svg.indexOf('>')+1,svg.lastIndexOf('</svg>'));
 const pattern=a.outfit!=='runner'?'':a.jerseyPattern==='stripes'?`<path d="M107 244v35m18-32v32m30-32v32m18-35v35" stroke="${colors.accent}" stroke-width="7" opacity=".75"/>`:a.jerseyPattern==='diagonal'?`<path d="m99 238 67 42h25l-74-46Z" fill="${colors.accent}" opacity=".75"/>`:`<path d="M117 257h45" stroke="${colors.accent}" stroke-width="4"/>`;
 const headset=a.headwear==='headset'?`<path d="M66 139v-16c0-86 148-86 148 0v16" stroke="#182538" stroke-width="13"/><path d="M66 137v-14c0-86 148-86 148 0v14" stroke="${colors.accent}" stroke-width="3"/><rect x="57" y="130" width="19" height="43" rx="8" fill="#24354b" stroke="${colors.accent}" stroke-width="2"/><rect x="204" y="130" width="19" height="43" rx="8" fill="#24354b" stroke="${colors.accent}" stroke-width="2"/><path d="M216 170q0 29-44 27" stroke="#8a9bb1" stroke-width="4"/><rect x="162" y="191" width="20" height="9" rx="4" fill="#182538"/>`:a.headwear==='cap'?`<path d="M65 105Q66 37 140 37q74 0 75 68Z" fill="#26374c"/><path d="M64 101q78-20 151 0l22 15q-100-16-173 0Z" fill="${colors.accent}"/><path d="m133 58 16 0v20h-16Z" fill="${colors.accent}"/>`:'';
 const result=`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 280 300" fill="none"><path fill="#162032" d="M0 0h280v300H0z"/><circle cx="140" cy="132" r="104" fill="${colors.accent}" opacity=".10"/><circle cx="140" cy="132" r="113" stroke="${colors.accent}" opacity=".3"/><path d="M0 274 280 100v55L0 300Z" fill="${colors.accent}" opacity=".07"/><g transform="translate(${140*(1-width)} 14) scale(${width} 1)">${body}${pattern}${headset}</g><path d="M18 23h28M18 23v28m216-28h28v28M0 298h280" stroke="${colors.accent}" stroke-width="2" opacity=".7"/></svg>`;
 const prefix=`studio-${++renderSequence}-`;
 return result.replace(/id="([^"]+)"/g,(_,id)=>`id="${prefix}${id}"`).replace(/url\(#([^)]*)\)/g,(_,id)=>`url(#${prefix}${id})`).replace(/href="#([^"]+)"/g,(_,id)=>`href="#${prefix}${id}"`);
}
