#!/usr/bin/env python3
"""Download public Riot reference material for a reproducible asset refresh."""
import json, urllib.request
from pathlib import Path
root=Path('target/reference-assets');root.mkdir(parents=True,exist_ok=True)
urls={
'teams.jpg':'https://cmsassets.rgpub.io/sanity/images/dsfx7636/news_live/5833eb72c7709bcb0fd094c9f4a5799c38a301d8-1920x1080.jpg',
'teams.html':'https://valorantesports.com/en-US/leagues/vct_americas',
'rankings.html':'https://valorantesports.com/pt-BR/gpr/2026/current',
'vcb.html':'https://valorantesports.com/pt-BR/news/tudo-sobre-a-etapa-2-do-gamers-club-challengers-brazil-2026',
'agents.html':'https://playvalorant.com/pt-br/agents/'
}
for name,url in urls.items():
 try:
  request=urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0'})
  root.joinpath(name).write_bytes(urllib.request.urlopen(request,timeout=45).read())
  print(name,'downloaded')
 except Exception as error: print(name,type(error).__name__,str(error))
root.joinpath('sources.json').write_text(json.dumps(urls,indent=2))

import re,concurrent.futures
teams={}
for name in ['rankings.html','teams.html']:
 text=root.joinpath(name).read_text().replace('\\\"','\"').replace('\\/','/')
 for match in re.finditer(r'\{"__typename":"Team","id":"[^"]+","code":',text):
  try:
   obj=json.JSONDecoder().raw_decode(text[match.start():])[0]
   if obj.get('name') and obj.get('image'): teams[obj['slug']]={key:obj.get(key) for key in ['id','slug','name','code','image','homeLeague']}
  except Exception: pass
root.joinpath('teams.json').write_text(json.dumps(list(teams.values()),indent=2))
root.joinpath('logos').mkdir(exist_ok=True)
def download(team):
 try:
  url=team['image'].replace('http://','https://')
  root.joinpath('logos',team['slug']+'.png').write_bytes(urllib.request.urlopen(url,timeout=35).read())
  return team['slug']
 except Exception as error: return str(error)
with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
 for result in pool.map(download,teams.values()): print(result)

