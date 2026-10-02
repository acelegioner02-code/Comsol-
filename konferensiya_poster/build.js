// src/*.html -> *.html (footer/skyline qo'shiladi) -> png/*.png
const fs=require('fs'),path=require('path');
const { chromium } = require('playwright');
const D=__dirname, only=process.argv[2];
const footer=fs.readFileSync(path.join(D,'parts/footer.html'),'utf8');
const sky=fs.readFileSync(path.join(D,'parts/skyline.svg'),'utf8');
(async()=>{
  const files=fs.readdirSync(path.join(D,'src')).filter(f=>f.endsWith('.html')&&(!only||f.includes(only)));
  const b=await chromium.launch();
  const p=await b.newPage({viewport:{width:1080,height:1620},deviceScaleFactor:2});
  for(const f of files){
    const html=fs.readFileSync(path.join(D,'src',f),'utf8').replace('{{FOOTER}}',footer).replace(/\{\{SKYLINE\}\}/g,sky).replace(/\.\.\//g,'');
    const out=path.join(D,f); fs.writeFileSync(out,html);
    await p.goto('file://'+out,{waitUntil:'networkidle'});
    await p.evaluate(()=>document.fonts.ready);
    await p.screenshot({path:path.join(D,'png',f.replace('.html','.png'))});
    console.log('ok',f);
  }
  await b.close();
})();
