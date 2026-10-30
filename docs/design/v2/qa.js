const {chromium}=require('playwright');
const path=require('path');const {pathToFileURL}=require('url');
const executable=process.env.CHROMIUM_PATH||require('child_process').execFileSync('which',['chromium'],{encoding:'utf8'}).trim();
(async()=>{
 const b=await chromium.launch({executablePath:executable,headless:true,args:['--no-sandbox']});
 const p=await b.newPage({viewport:{width:1920,height:1080},reducedMotion:'reduce'});let errors=[];p.on('pageerror',e=>errors.push(e.message));
 await p.goto(pathToFileURL(path.join(__dirname,'index.html')).href);await p.waitForTimeout(100);
 if(await p.locator('.focused').getAttribute('aria-label')!=='YouTube')throw Error('Initial focus mismatch');
 await p.keyboard.press('ArrowRight');if(await p.locator('.focused').getAttribute('aria-label')!=='Netflix')throw Error('Right key mismatch');
 await p.keyboard.press('End');if(await p.locator('.focused').getAttribute('aria-label')!=='Semua app')throw Error('End key mismatch');
 await p.keyboard.press('ArrowRight');if(await p.locator('.focused').getAttribute('aria-label')!=='YouTube')throw Error('Focus wrap mismatch');
 await p.keyboard.press('Enter');await p.waitForTimeout(100);if(!await p.locator('.notice').isVisible())throw Error('Preview notice missing');
 await p.keyboard.press('Escape');if(await p.locator('.notice').isVisible())throw Error('Notice dismissal failed');
 let tv=await p.evaluate(()=>({animations:[...document.getAnimations()].filter(a=>a.playState==='running').length,overflow:document.documentElement.scrollWidth>innerWidth}));
 if(tv.animations!==0||tv.overflow)throw Error(JSON.stringify(tv));
 await p.setViewportSize({width:390,height:844});if(await p.evaluate(()=>document.documentElement.scrollWidth>innerWidth))throw Error('Mobile horizontal overflow');
 await p.setViewportSize({width:1920,height:1080});await p.emulateMedia({reducedMotion:'no-preference'});await p.waitForTimeout(80);
 if(await p.evaluate(()=>document.getAnimations().filter(a=>a.playState==='running').length)<1)throw Error('Ambient animation not running');
 if(errors.length)throw Error(errors.join('\n'));
 console.log('PASS: initial focus, D-pad arrows, wrap, End, Enter demo notice, Escape, reduced-motion stop, ambient animation and no horizontal overflow.');
 await b.close();process.exit(0)
})().catch(e=>{console.error(e);process.exit(1)});
