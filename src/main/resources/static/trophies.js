export const TROPHIES = [
 ['ascension','VCT Ascension'],['champions','VALORANT Champions'],['masters','VALORANT Masters'],['americas','VCT Americas'],['emea','VCT EMEA'],['pacific','VCT Pacific'],['china','VCT China'],['brazil','Challengers Brazil'],['korea','Challengers Korea'],['spain','Challengers Spain']
].map(([id,name])=>({id,name,image:`/trophies/${id}.png`}));
// Unknown circuits keep the generic cup, rather than assigning the wrong regional image.
export function trophyAsset(name){
 const n=name.toLowerCase();const id=n.includes('ascension')?'ascension':n.includes('champions')?'champions':n.includes('masters')?'masters':n.includes('challengers')||n.includes('vcb')?n.includes('brazil')?'brazil':n.includes('korea')?'korea':n.includes('spain')?'spain':null:n.includes('vct')?n.includes('americas')?'americas':n.includes('emea')?'emea':n.includes('pacific')?'pacific':/\b(cn|china)\b/.test(n)?'china':null:null;
 return TROPHIES.find(t=>t.id===id)||{id:'regional',name:'Troféu do circuito',image:'/trophies/regional.svg'};
}

export function awardAsset(title){
 const t=title.toLowerCase();let id=t.includes('revelação')?'rookie':t.includes('igl')?'igl':t.includes('duelista')?'duelist':t.includes('iniciador')?'initiator':t.includes('controlador')?'controller':t.includes('sentinela')?'sentinel':'mvp';
 return {id:`award-${id}`,name:title,image:`/awards/${id}.svg`};
}
