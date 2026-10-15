const KEYS=['aim','tactics','communication','discipline','composure'];
const LABELS=['MIRA','LEITURA','COMUN.','DISCIPLINA','CONTROLE'];
function svgNode(tag,attributes={}){const n=document.createElementNS('http://www.w3.org/2000/svg',tag);for(const [key,value] of Object.entries(attributes))n.setAttribute(key,String(value));return n;}
export function startingAttributes(role,background,mode,igl){
 const stats=Object.fromEntries(KEYS.map(k=>[k,30]));stats[role.attribute]+=7;
 stats[background==='ranked'?'aim':background==='school'?'discipline':'communication']+=5;
 if(igl){stats.communication+=3;stats.tactics+=2;}if(mode==='prodigy')KEYS.forEach(k=>stats[k]+=20);return stats;
}
export function renderRadar(container,stats,label='Atributos do jogador'){
 const svg=svgNode('svg',{viewBox:'0 0 280 245',role:'img','aria-label':label});const title=svgNode('title');title.textContent=KEYS.map((k,i)=>`${LABELS[i]} ${stats[k]}`).join(', ');svg.append(title);
 const point=(i,ratio)=>{const angle=-Math.PI/2+i*2*Math.PI/5;return [140+Math.cos(angle)*78*ratio,117+Math.sin(angle)*78*ratio];};
 const polygon=ratios=>ratios.map((v,i)=>point(i,v).map(n=>n.toFixed(2)).join(',')).join(' ');
 for(const fraction of [.25,.5,.75,1])svg.append(svgNode('polygon',{points:polygon(KEYS.map(()=>fraction)),class:'radar-ring'}));
 KEYS.forEach((k,i)=>{const p=point(i,1);svg.append(svgNode('line',{x1:140,y1:117,x2:p[0],y2:p[1],class:'radar-axis'}));const t=point(i,1.36),text=svgNode('text',{x:t[0],y:t[1]+4,'text-anchor':'middle',class:'radar-label'});text.textContent=LABELS[i];svg.append(text);});
 svg.append(svgNode('polygon',{points:polygon(KEYS.map(k=>Math.max(0,Math.min(99,stats[k]??0))/99)),class:'radar-shape'}));
 KEYS.forEach((k,i)=>{const p=point(i,(stats[k]??0)/99);svg.append(svgNode('circle',{cx:p[0],cy:p[1],r:3,class:'radar-point'}));});container.replaceChildren(svg);
}
