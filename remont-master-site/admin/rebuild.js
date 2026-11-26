(() => {
"use strict";
const OWNER="Romangold999", REPO="smeta-pro", BRANCH="remont-master-pages", ROOT="remont-master-site/data/";
const FILES=["catalog.json","categories.json","site-content.json","portfolio.json","posts.json","concrete-products.json","services.json","diy-media.json"];
const $=id=>document.getElementById(id), api=p=>"https://api.github.com/repos/"+OWNER+"/"+REPO+(p?"/"+p:"");
let token="",head="",data={},pending=new Map(),busy=false;
const headers=()=>({Authorization:"Bearer "+token,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28"});
async function req(path,opts={}){const method=String(opts.method||"GET").toUpperCase();let r,j;try{r=await fetch(api(path),{...opts,headers:{...headers(),...(opts.headers||{})}});j=await r.json().catch(()=>({}));}catch(e){throw Error("Сеть: "+method+" /"+path+" — "+(e?.message||"ошибка соединения"));}if(!r.ok){const detail=j.message||("GitHub API HTTP "+r.status);const errors=Array.isArray(j.errors)? " · "+j.errors.map(x=>typeof x==="string"?x:(x.message||JSON.stringify(x))).join("; "):"";throw Error(detail+" [HTTP "+r.status+" · "+method+" /"+path+"]"+errors);}return j}
const decode=s=>new TextDecoder().decode(Uint8Array.from(atob(String(s)),c=>c.charCodeAt(0)));
const copy=x=>JSON.parse(JSON.stringify(x));
function status(s,bad=false){const n=$("rmAdminStatus");if(n){n.textContent=s;n.style.color=bad?"#ffb4a8":"#e5c98e"}}
function updateSave(){const b=$("rmAdminSave");if(!b)return;b.disabled=busy||!pending.size;b.textContent=busy?"Сохранение…":pending.size?"Сохранить изменения ("+pending.size+")":"Сохранить изменения"}
function stage(file){pending.set(ROOT+file,copy(data[file]));updateSave();status("Изменения подготовлены. Файлов в очереди: "+pending.size+".")}

function stageAsset(path,base64){pending.set(path,{__binary:true,content:base64});updateSave();status("Файл подготовлен: "+path);}
function portfolioTarget(category){return {"decorative-walls":"portfolio-decorative-walls","tile":"portfolio-tile","concrete":"portfolio-concrete","complex-structures":"portfolio-complex-structures"}[category]||"";}
function videoEmbed(url){
 try{
  const u=new URL(url),host=u.hostname.toLowerCase().replace(/^www\\./,"");
  if(host==="rutube.ru"||host==="m.rutube.ru"){
   const path=u.pathname.replace(/\\/+$/,"");
   const m=path.match(/^\\/(?:shorts|video|play\\/embed)\\/([a-zA-Z0-9_-]+)$/i);
   const privateMatch=path.match(/^\\/video\\/private\\/([a-zA-Z0-9_-]+)$/i);
   const id=privateMatch?privateMatch[1]:(m?m[1]:"");
   if(!id)return null;
   const embed=new URL("https://rutube.ru/play/embed/"+encodeURIComponent(id));
   const accessKey=u.searchParams.get("p");
   if(accessKey)embed.searchParams.set("p",accessKey);
   return {src:embed.href,platform:"Rutube"};
  }
  if(host==="youtu.be"){const id=u.pathname.split("/").filter(Boolean)[0]||"";return /^[a-zA-Z0-9_-]{6,}$/.test(id)?{src:"https://www.youtube.com/embed/"+encodeURIComponent(id),platform:"YouTube"}:null;}
  if(["youtube.com","m.youtube.com","music.youtube.com","youtube-nocookie.com"].includes(host)){const m=u.pathname.match(/^\\/(?:shorts|live|embed|v)\\/([a-zA-Z0-9_-]+)/),id=u.pathname==="/watch"?(u.searchParams.get("v")||""):(m?m[1]:"");return /^[a-zA-Z0-9_-]{6,}$/.test(id)?{src:"https://www.youtube.com/embed/"+encodeURIComponent(id),platform:"YouTube"}:null;}
 }catch(_){}
 return null;
}
function renderPortfolioCard(item,target){
 const card=document.createElement("article");card.className="widecard";card.dataset.portfolioId=item.id;
 const title=document.createElement("p");title.textContent=item.title||"Работа";card.append(title);
 if(item.type==="video"){
   const video=videoEmbed(item.url);
   if(video){const frame=document.createElement("iframe");frame.src=video.src;frame.title=item.title||"Видео";frame.allow="clipboard-write; autoplay; encrypted-media; fullscreen";frame.allowFullscreen=true;frame.style.cssText="width:100%;aspect-ratio:16/9;border:0;border-radius:10px";card.prepend(frame)}
   else{const link=document.createElement("a");link.href=item.url;link.target="_blank";link.rel="noopener";link.textContent="Открыть видео";card.append(link)}
 }else if(item.image){const img=document.createElement("img");img.src=new URL(item.image,document.baseURI).href;img.alt=item.title||"Работа";img.loading="lazy";img.style.cssText="width:100%;max-height:320px;object-fit:cover;border-radius:10px";card.prepend(img)}
 const del=document.createElement("button");del.type="button";del.textContent="Удалить материал";del.style.cssText="margin-top:10px;border:1px solid #8b4b43;border-radius:9px;background:#2a1d1b;color:#f0b7ad;padding:8px 10px;font-size:11px";
 del.addEventListener("click",()=>{if(!confirm("Удалить этот материал?"))return;data["portfolio.json"].items=data["portfolio.json"].items.filter(x=>String(x.id)!==String(item.id));stage("portfolio.json");card.remove()});card.append(del);target.append(card);
}
function renderPortfolioControls(){
 (data["portfolio.json"].categories||[]).forEach(cat=>{
  const screen=$( {"decorative-walls":"portfolioDecorativeWalls","tile":"portfolioTile","concrete":"portfolioConcrete","complex-structures":"portfolioComplexStructures"}[cat.id] );
  if(!screen)return;
  let panel=screen.querySelector(".rm-portfolio-editor");
  if(!panel){
   panel=document.createElement("div");panel.className="widecard rm-portfolio-editor";panel.style.cssText="margin:12px 0;border-color:#8a6a3d";
   const heading=document.createElement("strong");heading.textContent="Добавить материал в портфолио";panel.append(heading);
   const title=document.createElement("input");title.placeholder="Название работы";title.dataset.role="title";title.style.cssText="display:block;width:100%;margin:10px 0;padding:11px;border:1px solid #51432d;border-radius:9px;background:#202426;color:#fff";
   const files=document.createElement("input");files.type="file";files.accept="image/*";files.multiple=true;files.dataset.role="files";files.style.cssText="display:block;width:100%;margin:8px 0;color:#ddd";
   const video=document.createElement("input");video.type="url";video.placeholder="Ссылка на видео Rutube или YouTube";video.dataset.role="video";video.style.cssText="display:block;width:100%;margin:8px 0;padding:11px;border:1px solid #51432d;border-radius:9px;background:#202426;color:#fff";
   const add=document.createElement("button");add.type="button";add.textContent="Добавить выбранные материалы";add.className="goldbtn";add.style.cssText="width:100%;margin-top:8px";add.addEventListener("click",()=>addPortfolioContent(cat.id,panel));
   panel.append(title,files,video,add);screen.insertBefore(panel,screen.children[1]||null);
  }
  const target=$(portfolioTarget(cat.id));if(!target)return;
  target.replaceChildren();
  data["portfolio.json"].items.filter(x=>x.category===cat.id).sort((a,b)=>(a.sortOrder||0)-(b.sortOrder||0)).forEach(item=>renderPortfolioCard(item,target));
 });
}
async function addPortfolioContent(category,panel){
 const title=panel.querySelector('[data-role="title"]').value.trim(),files=Array.from(panel.querySelector('[data-role="files"]').files||[]),video=panel.querySelector('[data-role="video"]').value.trim();
 if(!title){status("Укажи название материала.",true);return}
 if(!files.length&&!video){status("Выбери фотографии или добавь ссылку Rutube / YouTube.",true);return}
 let order=Math.max(0,...data["portfolio.json"].items.filter(x=>x.category===category).map(x=>Number(x.sortOrder)||0))+1;
 for(const file of files){
  if(!file.type.startsWith("image/"))continue;
  const id="portfolio-"+Date.now().toString(36)+"-"+Math.random().toString(36).slice(2,7),ext=(file.name.split(".").pop()||"jpg").toLowerCase().replace(/[^a-z0-9]/g,"")||"jpg",path="assets/portfolio/"+id+"."+ext;
  const base64=await new Promise((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(String(reader.result).split(",")[1]||"");reader.onerror=()=>reject(Error("Не удалось прочитать "+file.name));reader.readAsDataURL(file)});
  stageAsset("remont-master-site/"+path,base64);
  data["portfolio.json"].items.push({id,type:"image",title:title+(files.length>1?" · "+file.name:""),category,image:path,sortOrder:order++});
 }
  if(video){const parsed=videoEmbed(video);if(!parsed){status("Поддерживаются ссылки Rutube и YouTube (watch, youtu.be, Shorts).",true);return}data["portfolio.json"].items.push({id:"portfolio-video-"+Date.now().toString(36),type:"video",title,category,url:video,sortOrder:order++})}
 stage("portfolio.json");panel.querySelector('[data-role="title"]').value="";panel.querySelector('[data-role="files"]').value="";panel.querySelector('[data-role="video"]').value="";renderPortfolioControls();status("Материалы подготовлены. Сохрани общей кнопкой.");
}

function genericView(file,item,group){
 if(file==="site-content.json"&&group==="home"){
  const hero=document.querySelector("#home .hero");if(hero){const nodes=hero.querySelectorAll(".eyebrow,h1,p");if(nodes[0])nodes[0].textContent=item.eyebrow||"";if(nodes[1])nodes[1].textContent=item.title||"";if(nodes[2])nodes[2].textContent=item.description||"";const b=hero.querySelector("button");if(b&&item.button)b.textContent=item.button+" ↗"}
 }
 if(file==="site-content.json"&&group==="profile"){
  const name=document.querySelector("#profile .profilebox h2"),sub=document.querySelector("#profile .profilebox > p"),brand=document.querySelector("header .brand small");
  if(name)name.textContent=item.name||"";if(sub)sub.textContent=[item.specialty,item.region].filter(Boolean).join(" · ");if(brand)brand.textContent=[item.name,item.region].filter(Boolean).join(" · ");
  if(item.avatar)document.querySelectorAll("#profile .profilepic img,header .avatar img").forEach(img=>img.src=new URL(item.avatar,document.baseURI).href);
  const phone=document.querySelector("#profile a[href^='tel:']");if(phone&&item.phone){phone.href="tel:"+String(item.phone).replace(/[^+\\d]/g,"");const small=phone.querySelector("small");if(small)small.textContent=item.phone}
 }
 if(file==="services.json"){
  const card=document.querySelector('[data-service-id="'+CSS.escape(String(item.id))+'"]');if(card){const strong=card.querySelector("strong"),small=card.querySelector("small");if(strong)strong.textContent=item.title||"";if(small)small.textContent=item.subtitle||"";const img=card.querySelector(".rowico img");if(img&&item.icon)img.src=new URL(item.icon,document.baseURI).href}
 }
 if(file==="concrete-products.json"){
  const card=document.querySelector('[data-concrete-id="'+CSS.escape(String(item.id))+'"]');if(card){const strong=card.querySelector("strong"),small=card.querySelector("small");if(strong)strong.textContent=item.title||"";if(small)small.textContent=item.description||"";if(item.image)card.style.backgroundImage='url("'+new URL(item.image,document.baseURI).href+'")'}
 }
}
function genericField(file,item,key,group){
 const label=document.createElement("label");label.style.cssText="display:flex;flex-direction:column;gap:5px;min-width:0;color:#d8bd87;font-size:11px";
 const cap=document.createElement("span");cap.textContent=key;label.append(cap);
 let input;
 const value=item[key];
 if(typeof value==="boolean"){input=document.createElement("select");["true","false"].forEach(v=>{const o=document.createElement("option");o.value=v;o.textContent=v==="true"?"Да":"Нет";input.append(o)});input.value=String(value)}
 else if(value!==null&&typeof value==="object"){input=document.createElement("textarea");input.value=JSON.stringify(value);input.rows=3}
 else{input=document.createElement("input");input.type=typeof value==="number"?"number":"text";input.value=value??""}
 input.style.cssText="width:100%;min-width:0;box-sizing:border-box;border:1px solid #66543a;border-radius:9px;background:#151a1c;color:#f3f1eb;padding:9px;font:inherit;font-size:12px";
 const update=()=>{try{if(typeof value==="boolean")item[key]=input.value==="true";else if(value!==null&&typeof value==="object")item[key]=JSON.parse(input.value);else if(typeof value==="number")item[key]=Number(input.value);else item[key]=input.value;stage(file);genericView(file,item,group)}catch(e){input.setCustomValidity("JSON некорректен");return}input.setCustomValidity("")};
 input.addEventListener("input",update);input.addEventListener("change",update);label.append(input);
 if(["avatar","image","icon","cover"].includes(key)){
  const upload=document.createElement("input");upload.type="file";upload.accept="image/*";upload.style.cssText="width:100%;margin-top:5px;color:#aaa";
  upload.addEventListener("change",async()=>{const fileObj=upload.files?.[0];if(!fileObj)return;const ext=(fileObj.name.split(".").pop()||"jpg").toLowerCase().replace(/[^a-z0-9]/g,"")||"jpg",path="assets/uploads/"+String(item.id||group)+"-"+Date.now().toString(36)+"."+ext;const base64=await new Promise((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(String(reader.result).split(",")[1]||"");reader.onerror=()=>reject(Error("Не удалось прочитать изображение"));reader.readAsDataURL(fileObj)});stageAsset("remont-master-site/"+path,base64);item[key]=path;input.value=path;stage(file);genericView(file,item,group)});
  label.append(upload);
 }
 return label;
}
function attachGenericEditor(container,file,item,group,label){
 if(!container||container.querySelector(":scope > .rm-generic-edit"))return;
 const button=document.createElement("button");button.type="button";button.className="ghostbtn rm-generic-edit";button.textContent="Изменить";button.style.cssText="margin-top:10px;padding:8px 11px;border-color:#8a6a3d;color:#f0d49a;font-size:11px";
 const panel=document.createElement("div");panel.className="widecard rm-generic-panel";panel.hidden=true;panel.style.cssText="display:none;grid-template-columns:1fr 1fr;gap:8px;margin-top:10px";
 Object.keys(item).filter(k=>k!=="id").forEach(k=>panel.append(genericField(file,item,k,group)));
 const id=document.createElement("small");id.textContent="ID: "+(item.id||"—")+" · постоянный";id.style.cssText="grid-column:1/-1;color:#9ba2a3;font-size:10px";panel.append(id);
 button.addEventListener("click",()=>{panel.hidden=!panel.hidden;panel.style.display=panel.hidden?"none":"grid";button.textContent=panel.hidden?"Изменить":"Закрыть редактор"});
 container.append(button,panel);
}
function installGenericEditors(){
 const content=data["site-content.json"]?.content||{};
 attachGenericEditor(document.querySelector("#home .hero"),"site-content.json",content.home||{},"home","Главная");
 attachGenericEditor(document.querySelector("#profile .profilebox"),"site-content.json",content.profile||{},"profile","Профиль");
 (data["services.json"]?.items||[]).forEach(item=>{const card=document.querySelector('[data-service-id="'+CSS.escape(String(item.id))+'"]');if(!card)return;let wrap=card.nextElementSibling;if(!wrap||!wrap.classList.contains("rm-service-edit-wrap")){wrap=document.createElement("div");wrap.className="rm-service-edit-wrap";card.after(wrap)}attachGenericEditor(wrap,"services.json",item,"services","Услуга")});
 (data["concrete-products.json"]?.items||[]).forEach(item=>attachGenericEditor(document.querySelector('[data-concrete-id="'+CSS.escape(String(item.id))+'"]'),"concrete-products.json",item,"concrete-products","Изделие"));
 (data["diy-media.json"]?.items||[]).forEach(item=>attachGenericEditor(document.querySelector('[data-diy-id="'+CSS.escape(String(item.id))+'"]'),"diy-media.json",item,"diy-media","Материал"));
 const diy=document.getElementById("diyMaterials");
 if(diy&&!document.getElementById("rmAddDiy")){
  const add=document.createElement("button");add.id="rmAddDiy";add.type="button";add.className="goldbtn";add.textContent="Добавить материал";
  add.addEventListener("click",()=>{
   const item={id:"diy-"+Date.now().toString(36),type:"image",title:"",description:"",image:"",url:"",sortOrder:data["diy-media.json"].items.length+1};
   data["diy-media.json"].items.push(item);stage("diy-media.json");
   const card=document.createElement("article");card.className="widecard";card.dataset.diyId=item.id;
   const heading=document.createElement("h3");heading.textContent="Новый материал";card.append(heading);diy.append(card);
   attachGenericEditor(card,"diy-media.json",item,"diy-media","Материал");
  });
  diy.prepend(add);
 }

}

function installPortfolio(){renderPortfolioControls();}

function validate(){
 const a=data["catalog.json"]?.items,c=data["categories.json"]?.items||[];
 if(!Array.isArray(a)||a.length!==317)throw Error("Ожидалось 317 позиций каталога, получено "+(a?.length??"не массив")+".");
 const ids=a.map(x=>String(x.id??""));if(ids.some(x=>!x)||new Set(ids).size!==ids.length)throw Error("В каталоге есть пустые или повторяющиеся ID.");
 const cats=new Set(c.map(x=>String(x.id)));const bad=a.filter(x=>!cats.has(String(x.category)));
 if(bad.length)throw Error("У "+bad.length+" позиций некорректная категория. Данные не изменялись.");
}
function syncApp(){const items=data["catalog.json"].items;if(typeof workCatalog!=="undefined"&&Array.isArray(workCatalog))workCatalog.splice(0,workCatalog.length,...items.map(x=>({id:String(x.id),cat:String(x.category||"other"),name:String(x.name||""),unit:String(x.unit||""),price:x.price==null?null:Number(x.price)})));if(typeof window.renderCatalog==="function")window.renderCatalog();if(typeof window.renderServiceWorks==="function")window.renderServiceWorks();if(typeof window.renderEstimate==="function")window.renderEstimate();decorateRows()}
function visibleItems(){const cat=$("workCategory")?.value||"all",q=($("workSearch")?.value||"").toLocaleLowerCase("ru");return data["catalog.json"].items.filter(x=>(cat==="all"||String(x.category)===cat)&&String(x.name||"").toLocaleLowerCase("ru").includes(q))}
function field(item,key){
 const label=document.createElement("label");label.style.cssText="display:flex;flex-direction:column;gap:5px;min-width:0;color:#d8bd87;font-size:11px";
 const cap=document.createElement("span");cap.textContent=({category:"Категория",name:"Название",unit:"Единица измерения",price:"Цена, ₽"})[key]||key;label.append(cap);
 let input;
 if(key==="category"){input=document.createElement("select");(data["categories.json"].items||[]).forEach(c=>{const o=document.createElement("option");o.value=c.id;o.textContent=c.title||c.id;input.append(o)});input.value=String(item[key]??"")}
 else{input=document.createElement("input");input.type=key==="price"?"number":"text";input.value=item[key]??"";if(key==="price"){input.min="0";input.step="any"}}
 input.style.cssText="width:100%;min-width:0;box-sizing:border-box;border:1px solid #66543a;border-radius:9px;background:#151a1c;color:#f3f1eb;padding:10px;font:inherit;font-size:13px";
 const update=()=>{if(key==="price"){if(input.value.trim()!==""&&(!Number.isFinite(Number(input.value))||Number(input.value)<0)){input.setCustomValidity("Укажи неотрицательное число");return}input.setCustomValidity("");item.price=input.value.trim()===""?null:Number(input.value)}else item[key]=input.value;stage("catalog.json");const live=typeof workCatalog!=="undefined"?workCatalog.find(x=>String(x.id)===String(item.id)):null;if(live){live.cat=item.category;live.name=item.name;live.unit=item.unit;live.price=item.price}const row=document.querySelector('.catalogitem[data-rm-id="'+CSS.escape(String(item.id))+'"]');const b=row?.querySelector(".rm-rebuild-edit");if(b)b.textContent="Изменено"};
 input.addEventListener("input",update);input.addEventListener("change",update);label.append(input);return label;
}
function edit(row,item){
 if(row.querySelector(".rm-rebuild-editor"))return;
 const display=row.querySelector(".ci");if(display)display.hidden=true;
 const editor=document.createElement("div");editor.className="rm-rebuild-editor";editor.style.cssText="display:grid;grid-template-columns:1fr 1fr;gap:9px;width:100%;padding:10px 0;min-width:0;flex:1 0 100%";row.style.flexWrap="wrap";
 Object.keys(item).filter(k=>k!=="id").forEach(k=>editor.append(field(item,k)));
 const id=document.createElement("small");id.textContent="ID: "+item.id+" · постоянный";id.style.cssText="grid-column:1/-1;color:#9ba2a3;font-size:10px";editor.append(id);
 const del=document.createElement("button");del.type="button";del.textContent="Удалить позицию";del.style.cssText="grid-column:1/-1;border:1px solid #8b4b43;border-radius:9px;background:#2a1d1b;color:#f0b7ad;padding:9px";del.addEventListener("click",()=>{if(!confirm("Удалить позицию «"+item.name+"»? Она будет удалена только после общей кнопки сохранения."))return;data["catalog.json"].items=data["catalog.json"].items.filter(x=>String(x.id)!==String(item.id));stage("catalog.json");syncApp()});editor.append(del);row.append(editor);
}
function decorateRows(){
 const box=$("catalogList");if(!box||!data["catalog.json"])return;
 const items=visibleItems(),rows=Array.from(box.querySelectorAll(".catalogitem"));
 rows.forEach((row,i)=>{const item=items[i];if(!item)return;row.dataset.rmId=String(item.id);if(row.querySelector(".rm-rebuild-edit"))return;
 const b=document.createElement("button");b.type="button";b.className="rm-rebuild-edit";b.textContent="Изменить";b.style.cssText="flex:0 0 auto;border:1px solid #9a7846;border-radius:9px;background:#29251e;color:#f0d49a;padding:8px 10px;font-size:11px";b.addEventListener("click",()=>edit(row,item));row.append(b)});
}
function decorateServiceRows(){
 const box=$("serviceWorkList");if(!box||!data["catalog.json"])return;
 box.querySelectorAll('.catalogitem[data-work-id]').forEach(row=>{
  const id=String(row.dataset.workId||"");
  const item=data["catalog.json"].items.find(x=>String(x.id)===id);
  if(!item||row.querySelector(".rm-rebuild-edit"))return;
  const b=document.createElement("button");b.type="button";b.className="rm-rebuild-edit";b.textContent="Изменить";
  b.style.cssText="flex:0 0 auto;border:1px solid #9a7846;border-radius:9px;background:#29251e;color:#f0d49a;padding:8px 10px;font-size:11px";
  b.addEventListener("click",()=>edit(row,item));row.append(b);
 });
}
function addItem(){const item={id:"rm-"+Date.now().toString(36),category:data["categories.json"].items[0]?.id||"other",name:"",unit:"м²",price:null};data["catalog.json"].items.push(item);stage("catalog.json");if($("workCategory"))$("workCategory").value="all";if($("workSearch"))$("workSearch").value="";syncApp();const row=document.querySelector('.catalogitem[data-rm-id="'+CSS.escape(item.id)+'"]');if(row)edit(row,item)}
function install(){
 const originalService=window.renderServiceWorks;
 if(typeof originalService==="function"&&!originalService.__rmWrapped){
  const wrappedService=function(){const r=originalService.apply(this,arguments);queueMicrotask(decorateServiceRows);return r};
  wrappedService.__rmWrapped=true;window.renderServiceWorks=wrappedService;
 }
 const serviceBox=$("serviceWorkList");if(serviceBox)new MutationObserver(decorateServiceRows).observe(serviceBox,{childList:true,subtree:true});
 const original=window.renderCatalog;if(typeof original==="function"&&!original.__rmWrapped){const wrapped=function(){const r=original.apply(this,arguments);queueMicrotask(decorateRows);return r};wrapped.__rmWrapped=true;window.renderCatalog=wrapped}
 const box=$("catalogList");if(box)new MutationObserver(decorateRows).observe(box,{childList:true,subtree:true});
 $("workCategory")?.addEventListener("change",()=>setTimeout(decorateRows,0));$("workSearch")?.addEventListener("input",()=>setTimeout(decorateRows,0));
 if(!$("rmRebuildAdd")){const b=document.createElement("button");b.id="rmRebuildAdd";b.type="button";b.textContent="＋ Добавить позицию";b.className="ghostbtn";b.style.cssText="width:100%;margin-top:12px;border-color:#9a7846;color:#f0d49a";b.addEventListener("click",addItem);box?.parentElement?.append(b)}
 decorateRows();decorateServiceRows();
}
async function load(){
 status("Подключение к GitHub…");const ref=await req("git/ref/heads/"+BRANCH),snapshot=ref.object.sha;
 const entries=await Promise.all(FILES.map(async f=>{const x=await req("contents/"+ROOT+f+"?ref="+encodeURIComponent(snapshot));return [f,JSON.parse(decode(x.content))]}));
 data=Object.fromEntries(entries);const verify=await req("git/ref/heads/"+BRANCH);if(verify.object.sha!==snapshot)throw Error("Ветка изменилась во время загрузки.");
 head=snapshot;validate();$("rmAdminGate").hidden=true;syncApp();install();installGenericEditors();installPortfolio();updateSave();status("Подключено. Проверено: 317 позиций, ID уникальны, категории корректны.");
}
async function save(){
 if(busy||!pending.size)return;busy=true;updateSave();let committed=false;
 try{
  const r=await req("git/ref/heads/"+BRANCH);if(r.object?.sha!==head)throw Error("Рабочая ветка действительно изменилась. Изменения в этой вкладке не отправлены; сначала обнови страницу и проверь данные.");
  const parent=await req("git/commits/"+head),baseTree=parent.tree?.sha||parent.commit?.tree?.sha;
  if(!baseTree)throw Error("Сохранение: GitHub не вернул исходный коммит без SHA дерева (tree).");
  const changes=[],expected=new Map();
  for(const [path,value] of pending){const binary=value&&value.__binary===true;const b=await req("git/blobs",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({content:binary?value.content:JSON.stringify(value,null,2),encoding:binary?"base64":"utf-8"})});changes.push({path,mode:"100644",type:"blob",sha:b.sha});expected.set(path,b.sha)}
  const tree=await req("git/trees",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({base_tree:baseTree,tree:changes})});
  const created=await req("git/commits",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({message:"Admin rebuild: save prepared changes",tree:tree.sha,parents:[head]})});
  const c=created.tree?.sha?created:await req("git/commits/"+created.sha);
  if(!c.sha)throw Error("Сохранение: GitHub не вернул SHA созданного коммита.");
  const commitTree=c.tree?.sha||c.commit?.tree?.sha;
  if(!commitTree)throw Error("Сохранение: GitHub создал коммит, но не вернул SHA его дерева.");
  const moved=await req("git/refs/heads/"+BRANCH,{method:"PATCH",headers:{"Content-Type":"application/json"},body:JSON.stringify({sha:c.sha,force:false})});
  if(moved.object?.sha&&moved.object.sha!==c.sha)throw Error("GitHub вернул неожиданный SHA ветки после записи.");
  head=c.sha;committed=true;pending.clear();updateSave();
  const read=await req("git/trees/"+commitTree+"?recursive=1"),map=new Map((read.tree||[]).map(x=>[x.path,x.sha]));
  for(const [p,s] of expected)if(map.get(p)!==s)throw Error("Коммит создан, но контрольное чтение не совпало для файла: "+p);
  status("Сохранено и проверено в "+BRANCH+", commit "+c.sha.slice(0,7)+". GitHub подтвердил перемещение ветки; публикация запускается автоматически.");
 }catch(e){
  status((committed?"Изменения уже записаны в GitHub (commit "+head.slice(0,7)+"), но дополнительная проверка не завершилась: ":"Сохранение не выполнено: ")+e.message,true)
 }
 finally{busy=false;updateSave()}
}
$("rmAdminConnect")?.addEventListener("click",async()=>{const input=$("rmAdminToken");token=input?.value.trim()||"";if(!token){$("rmAdminGateStatus").textContent="Вставь GitHub token.";return}$("rmAdminGateStatus").textContent="Проверяю доступ…";try{await load();if(input)input.value=""}catch(e){$("rmAdminGateStatus").textContent="Ошибка: "+e.message;token=""}});
$("rmAdminSave")?.addEventListener("click",save);$("rmAdminModal")?.remove();updateSave();
})();