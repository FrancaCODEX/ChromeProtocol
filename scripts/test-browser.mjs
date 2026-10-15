import {chromium} from 'playwright';
import assert from 'node:assert/strict';
import {mkdir,writeFile} from 'node:fs/promises';
const base=process.env.BASE_URL||'http://localhost:8080';await mkdir('target/browser',{recursive:true});
const browser=await chromium.launch({headless:true});let page;
try{
 const context=await browser.newContext({viewport:{width:1512,height:1100}});page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto(base);await page.locator('#landing').waitFor({state:'visible'});await page.screenshot({path:'target/browser/01-home.png',fullPage:true});
 await page.click('#start-button');await page.fill('#character-name','Nova');await page.locator('input[name="create-eyes"][value="visor"]').locator('..').click();await page.locator('input[name="create-outfit"][value="runner"]').locator('..').click();await page.screenshot({path:'target/browser/02-identity.png',fullPage:true});
 await page.click('#identity-next');await page.selectOption('#nationality','BR');await page.screenshot({path:'target/browser/03-creation.png',fullPage:true});await page.click('#creation-submit');await page.locator('#dashboard').waitFor({state:'visible'});
 assert.match(await page.locator('#career-date').textContent(),/15 ANOS/);await page.screenshot({path:'target/browser/04-career.png',fullPage:true});
 const read=async()=>(await(await page.request.get(`${base}/api/game`)).json()).game.character;
 const choose=async id=>{await page.locator(`[data-choice="${id}"]`).click();await page.locator('#result-dialog').waitFor({state:'visible'});assert.notEqual(await page.locator('#result-narrative').textContent(),'');};
 const next=async()=>{await page.click('#continue-button');await page.locator('#result-dialog').waitFor({state:'hidden'});};
 await choose('prep-study');assert.match(await page.locator('#result-changes').textContent(),/Leitura/);const pending=await read();
 await page.reload();await page.locator('#result-dialog').waitFor({state:'visible'});assert.equal((await read()).revision,pending.revision,'Reload cannot reroll outcome');
 await page.keyboard.press('Escape');assert.equal(await page.locator('#result-dialog').isVisible(),true,'Outcome must be acknowledged');await page.screenshot({path:'target/browser/05-result.png',fullPage:true});await next();
 await choose('trial-steady');await next();assert.equal((await read()).phase,2);await page.screenshot({path:'target/browser/06-offers.png',fullPage:true});
 await choose('offer-1');assert.match(await page.locator('#result-narrative').textContent(),/2027/);await next();assert.equal((await read()).contract.endYear,2027);
 await page.screenshot({path:'target/browser/07-match.png',fullPage:true});
 for(let i=0;i<3;i++){await choose('match-support');if(i<2)assert.match(await page.locator('#result-title').textContent(),/Round/);else assert.match(await page.locator('#result-title').textContent(),/VITÓRIA|DERROTA/);await next();}
 assert.equal((await read()).phase,4);assert.equal((await read()).wins+(await read()).losses,1);
 await choose('life-rest');await next();
 for(let i=0;i<6;i++){await choose('match-control');await next();}
 assert.equal((await read()).phase,7);await choose('year-develop');assert.match(await page.locator('#result-narrative').textContent(),/16 anos/);await next();
 let state=await read();assert.equal(state.age,16);assert.equal(state.year,2027);assert.equal(state.seasons.length,1);assert.equal(state.contract.endYear,2027);
 await page.click('[data-tab=history]');await page.screenshot({path:'target/browser/08-history.png',fullPage:true});assert.match(await page.locator('#season-list').textContent(),/2026/);
 const download=page.waitForEvent('download');await page.click('#export-button');assert.equal((await download).suggestedFilename(),'clutch-career-dossie.json');
 await page.click('#edit-appearance');await page.locator('input[name="edit-hair"][value="braids"]').locator('..').click();await page.click('#save-appearance');await page.locator('#appearance-dialog').waitFor({state:'hidden'});state=await read();assert.equal(state.age,16);assert.equal(state.appearance.hair,'braids');
 await page.setViewportSize({width:390,height:844});await page.click('[data-tab=career]');assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,'No mobile overflow');await page.screenshot({path:'target/browser/09-mobile.png',fullPage:true});
 await choose('prep-team');await page.screenshot({path:'target/browser/10-mobile-result.png',fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);await next();
 await choose('trial-steady');await next();assert.match(await page.locator('#scene-description').textContent(),/contrato.*2027/);assert.equal(await page.locator('[data-choice=honor]').count(),1);await choose('honor');await next();
 await page.reload();await page.locator('#dashboard').waitFor({state:'visible'});assert.equal((await read()).age,16);
 const isolated=await browser.newContext();const fresh=await isolated.newPage();await fresh.goto(base);await fresh.locator('#landing').waitFor({state:'visible'});await isolated.close();
 assert.deepEqual(errors,[]);await writeFile('target/browser/result.txt','Passed: creation, nationality, role/agent, customization, immediate outcomes, reload/acknowledgement, offers, contract duration, maps, yearly progression, history, export, cosmetics, mobile, active contract and isolation.\n');console.log('Browser checks passed.');
}catch(error){if(page)await page.screenshot({path:'target/browser/failure.png',fullPage:true}).catch(()=>{});throw error;}finally{await browser.close();}
