// tiniq/src/*.html -> tiniq/out/*.png (yuqori sifat). Ishlatish: NODE_PATH=$(npm root -g) node tiniq/build.js [filtr] [scale]
const fs=require('fs'),path=require('path');const {chromium}=require('playwright');
const D=__dirname,R=path.join(D,'..'),only=process.argv[2]||'',scale=+(process.argv[3]||3);
const part=f=>fs.readFileSync(path.join(R,'parts',f),'utf8');
(async()=>{
  fs.mkdirSync(path.join(D,'out'),{recursive:true});fs.mkdirSync(path.join(D,'html'),{recursive:true});
  const b=await chromium.launch();
  for(const f of fs.readdirSync(path.join(D,'src')).filter(f=>f.endsWith('.html')&&f.includes(only))){
    let h=fs.readFileSync(path.join(D,'src',f),'utf8').replace(/\{\{LOGO\}\}/g,part('logo_urspi.svg')).replace(/\{\{FOOTER\}\}/g,part('footer_kok.html')).replace(/\{\{ROSETTE\}\}/g,part('rosette.svg'));
    const [w,hh]=h.match(/name="size" content="(\d+)x(\d+)"/).slice(1).map(Number);
    const out=path.join(D,'html',f);fs.writeFileSync(out,h);
    const p=await b.newPage({viewport:{width:w,height:hh},deviceScaleFactor:scale});
    await p.goto('file://'+out,{waitUntil:'networkidle'});await p.evaluate(()=>document.fonts.ready);await p.waitForTimeout(200);
    const base=path.join(D,'out',f.replace('.html',''));
    await p.screenshot({path:base+'.png'});await p.screenshot({path:base+'.jpg',type:'jpeg',quality:93});
    await p.close();console.log('ok',f,w*scale+'x'+hh*scale);
  }
  await b.close();
})();
