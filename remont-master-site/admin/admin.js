(() => {
  "use strict";
  const OWNER="Romangold999", REPO="smeta-pro", BRANCH="remont-master-admin-git";
  const ROOT="remont-master-site/", DATA=ROOT+"data/";
  let token="", site={}, catalog=[], categories=[], portfolio={version:1,categories:[],items:[]}, posts={version:1,items:[]}, concreteProducts={version:1,items:[]}, services={version:1,items:[]}, diyMedia={version:1,items:[]}, shas={}, loadedHeadSha="";
  const $=id=>document.getElementById(id);
  const api=(path)=>"https://api.github.com/repos/"+OWNER+"/"+REPO+(path?"/"+path:"");
  const headers=()=>({Authorization:"Bearer "+token,Accept:"application/vnd.github+json","X-GitHub-Api-Version":"2022-11-28"});
  const status=(message,error=false)=>{const el=$("status");el.className=error?"status error":"status";el.textContent=message;};
  const decode=b64=>new TextDecoder().decode(Uint8Array.from(atob(b64.replace(/\s/g,"")),c=>c.charCodeAt(0)));
  const encode=value=>{const bytes=new TextEncoder().encode(value);let binary="";for(let i=0;i<bytes.length;i+=0x8000)binary+=String.fromCharCode(...bytes.subarray(i,i+0x8000));return btoa(binary);};
  async function request(url,options={}) {
    const r=await fetch(url,{...options,headers:{...headers(),...(options.headers||{})}});
    const body=await r.json().catch(()=>({}));
    if(!r.ok)throw new Error(body.message||("GitHub API HTTP "+r.status));
    return body;
  }
  async function readFile(path, snapshotRef=BRANCH) {
    const r=await request(api("contents/"+path+"?ref="+encodeURIComponent(snapshotRef)));
    shas[path]=r.sha;
    return JSON.parse(decode(r.content));
  }
  const pendingWrites=new Map(), pendingDeletes=new Set();
  let saveInProgress=false;
  function pendingCount(){return pendingWrites.size+pendingDeletes.size;}
  function updatePendingUI(){
    const button=$("saveAllChanges"),main=document.querySelector("#app main"),nav=$("nav"),logout=$("logout");
    if(button){button.disabled=!pendingCount()||saveInProgress;button.textContent=saveInProgress?"Сохранение…":"Сохранить изменения"+(pendingCount()?" ("+pendingCount()+")":"");}
    if(main)main.inert=saveInProgress;
    if(nav)nav.inert=saveInProgress;
    if(logout)logout.disabled=saveInProgress;
    const app=$("app");if(app)app.setAttribute("aria-busy",String(saveInProgress));
  }
  async function writeFile(path,value,message) {
    pendingWrites.set(path,{value,message:message||"Update site content",binary:false});
    pendingDeletes.delete(path);updatePendingUI();
    status("Изменения подготовлены. Нажми общую кнопку «Сохранить изменения», чтобы записать их в рабочую ветку GitHub.");
    return {staged:true};
  }
  async function saveAllChanges(){
    if(saveInProgress||!pendingCount())return;
    saveInProgress=true;updatePendingUI();
    const writes=[...pendingWrites.entries()],deletes=[...pendingDeletes];
    try{
      const ref=await request(api("git/ref/heads/"+BRANCH)),parentSha=ref.object.sha;
      if(loadedHeadSha && parentSha!==loadedHeadSha)throw new Error("Ветка изменилась после загрузки данных. Ничего не перезаписано. Обнови админку, проверь изменения и повтори правки.");
      const parent=await request(api("git/commits/"+parentSha)),tree=[],writtenBlobs=new Map();
      for(const [path,item] of writes){
        const content=item.binary?item.base64:JSON.stringify(item.value,null,2)+"\n";
        const blob=await request(api("git/blobs"),{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({content,encoding:item.binary?"base64":"utf-8"})});
        tree.push({path,mode:"100644",type:"blob",sha:blob.sha});writtenBlobs.set(path,blob.sha);
      }
      for(const path of deletes)tree.push({path,mode:"100644",type:"blob",sha:null});
      const nextTree=await request(api("git/trees"),{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({base_tree:parent.commit.tree.sha,tree})});
      const commit=await request(api("git/commits"),{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({message:"Admin: save all prepared changes",tree:nextTree.sha,parents:[parentSha]})});
      await request(api("git/refs/heads/"+BRANCH),{method:"PATCH",headers:{"Content-Type":"application/json"},body:JSON.stringify({sha:commit.sha,force:false})});
      const confirmed=await request(api("git/ref/heads/"+BRANCH));
      if(confirmed.object.sha!==commit.sha)throw new Error("GitHub не подтвердил ожидаемый commit.");
      const verifiedTree=await request(api("git/trees/"+commit.tree.sha+"?recursive=1"));
      const verified=new Map((verifiedTree.tree||[]).filter(x=>x.type==="blob").map(x=>[x.path,x.sha]));
      for(const [path,sha] of writtenBlobs)if(verified.get(path)!==sha)throw new Error("Не прошла проверка файла: "+path);
      for(const path of deletes)if(verified.has(path))throw new Error("Не прошло удаление файла: "+path);
      loadedHeadSha=commit.sha;
      pendingWrites.clear();pendingDeletes.clear();updatePendingUI();
      let refreshWarning="";
      try{await loadAll();}catch(refreshError){refreshWarning=" Данные сохранены и проверены, но экран не удалось перечитать: "+refreshError.message;}
      status("Изменения подтверждены GitHub и записаны одним commit в рабочую ветку. Основной сайт не изменялся."+refreshWarning);
    }catch(error){status("Сохранение не завершено: "+error.message+". Подготовленные изменения оставлены в памяти.",true);}
    finally{saveInProgress=false;updatePendingUI();}
  }
  async function deleteFile(path) {
    pendingWrites.delete(path);pendingDeletes.add(path);updatePendingUI();
  }
  async function uploadFile(path,file) {
    if(file.size>4*1024*1024)throw new Error("Файл больше 4 МБ. Уменьши изображение до загрузки.");
    const data=await new Promise((resolve,reject)=>{const fr=new FileReader();fr.onload=()=>resolve(String(fr.result).split(",")[1]);fr.onerror=reject;fr.readAsDataURL(file);});
    pendingWrites.set(path,{base64:data,binary:true,message:"Upload admin media: "+file.name});
    pendingDeletes.delete(path);updatePendingUI();
    return path.replace(ROOT,"");
  }
  async function loadAll() {
    const snapshot=await request(api("git/ref/heads/"+BRANCH)),snapshotSha=snapshot.object.sha;
    [site,catalog,categories,portfolio,posts]=await Promise.all([
      readFile(DATA+"site-content.json",snapshotSha),
      readFile(DATA+"catalog.json",snapshotSha),
      readFile(DATA+"categories.json",snapshotSha),
      readFile(DATA+"portfolio.json",snapshotSha),
      readFile(DATA+"posts.json",snapshotSha),
      readFile(DATA+"concrete-products.json",snapshotSha),
      readFile(DATA+"services.json",snapshotSha),
      readFile(DATA+"diy-media.json",snapshotSha)
    ]);
    const afterRead=await request(api("git/ref/heads/"+BRANCH));
    if(afterRead.object.sha!==snapshotSha)throw new Error("Ветка изменилась во время загрузки. Перезагрузи админку.");
    loadedHeadSha=snapshotSha;
    site.content=site.content||{};
    site.content.profile=site.content.profile||{};
    catalog=catalog.items||[];
    categories=categories.items||[];
    portfolio.categories=portfolio.categories||[];portfolio.items=portfolio.items||[];
    fillCatalogCategorySelect();renderCategoryManager();renderCatalog();
    const portfolioCategorySelect=$("portfolioCategory");portfolioCategorySelect.innerHTML="";
    portfolio.categories.forEach(x=>{const option=document.createElement("option");option.value=x.id;option.textContent=x.title;portfolioCategorySelect.appendChild(option);});
    posts.items=posts.items||[];
    concreteProducts.items=concreteProducts.items||[];services.items=services.items||[];diyMedia.items=diyMedia.items||[];
    const serviceCategory=$("serviceCategory");serviceCategory.innerHTML=categories.map(x=>'<option value="'+esc(x.id)+'">'+esc(x.title)+'</option>').join("");
    renderProfile();renderCatalog();renderPortfolio();renderPosts();renderOverview();renderPortfolioCategories();renderConcreteProducts();renderServices();renderDiy();
  }
  function show(view) {
    document.querySelectorAll("main>section").forEach(x=>x.classList.toggle("hidden",x.id!=="view-"+view));
    document.querySelectorAll("#nav button").forEach(x=>x.classList.toggle("active",x.dataset.view===view));
    if(view==="storage")refreshStats();
  }
  function renderProfile() {
    const p=site.content.profile||{}, f=$("profileForm");
    ["name","specialty","region","phone","telegram","whatsapp","max"].forEach(k=>{if(f.elements[k])f.elements[k].value=p[k]||"";});
    const home=site.content.home||{};
    f.elements.homeEyebrow.value=home.eyebrow||"";f.elements.homeTitle.value=home.title||"";f.elements.homeDescription.value=home.description||"";f.elements.homeButton.value=home.button||"";
    const concrete=site.content.concrete||{};f.elements.concreteTitle.value=concrete.title||"";f.elements.concreteDescription.value=concrete.description||"";
  }
  async function saveProfile(e) {
    e.preventDefault();
    const f=e.currentTarget,p=site.content.profile||{};
    ["name","specialty","region","phone","telegram","whatsapp","max"].forEach(k=>{if(f.elements[k])p[k]=f.elements[k].value.trim();});
    const home=site.content.home||{};
    home.eyebrow=f.elements.homeEyebrow.value.trim();home.title=f.elements.homeTitle.value.trim();home.description=f.elements.homeDescription.value.trim();home.button=f.elements.homeButton.value.trim();
    site.content.home=home;
    const concrete=site.content.concrete||{};concrete.title=f.elements.concreteTitle.value.trim();concrete.description=f.elements.concreteDescription.value.trim();site.content.concrete=concrete;
    const file=f.elements.avatar.files[0];
    if(file){const ext=(file.name.split(".").pop()||"webp").toLowerCase().replace(/[^a-z0-9]/g,"");const previous=p.avatar;p.avatar=await uploadFile(ROOT+"assets/uploads/profile-"+Date.now()+"."+ext,file);if(previous&&previous.startsWith("assets/uploads/"))await deleteFile(ROOT+previous);}
    site.content.profile=p;
    await writeFile(DATA+"site-content.json",site,"Update RemontMaster profile");
    status("Профиль подготовлен. Нажми общую кнопку «Сохранить изменения».");
  }
  let selectedCatalogCategory=null, editingCatalogId=null;
  function fillCatalogCategorySelect(selected) {
    const select=$("catalogItemForm").elements.category;
    const value=selected||selectedCatalogCategory||categories[0]?.id||"";
    select.innerHTML=categories.slice().sort((a,b)=>(a.sort_order||0)-(b.sort_order||0)).map(x=>'<option value="'+esc(x.id)+'">'+esc(x.title)+'</option>').join("");
    if(value)select.value=value;
  }
  function renderCategoryManager() {
    const box=$("categoryManager");
    box.innerHTML=categories.slice().sort((a,b)=>(a.sort_order||0)-(b.sort_order||0)).map(c=>{
      const count=catalog.filter(x=>x.category===c.id).length,serviceCount=services.items.filter(x=>x.category===c.id).length;
      return '<div class="row" data-category="'+esc(c.id)+'"><span>'+esc(c.title)+' <small>'+count+' работ · '+serviceCount+' услуг · '+esc(c.id)+'</small></span><button class="btn secondary" data-rename-category="'+esc(c.id)+'">Переименовать</button><button class="btn secondary" data-delete-category="'+esc(c.id)+'" '+(count||serviceCount?'disabled title="Сначала перенесите связанные работы и услуги"':'')+'>Удалить</button></div>';
    }).join("");
    box.querySelectorAll("[data-rename-category]").forEach(b=>b.addEventListener("click",async()=>{
      const c=categories.find(x=>x.id===b.dataset.renameCategory);if(!c)return;
      const title=prompt("Новое название категории:",c.title);if(title===null)return;
      if(!title.trim()){status("Название не может быть пустым.",true);return;}
      c.title=title.trim();await writeFile(DATA+"categories.json",{version:1,items:categories},"Rename work category");renderCategoryManager();fillCatalogCategorySelect(c.id);renderCatalog();
    }));
    box.querySelectorAll("[data-delete-category]").forEach(b=>b.addEventListener("click",async()=>{
      const c=categories.find(x=>x.id===b.dataset.deleteCategory);if(!c)return;
      if(catalog.some(x=>x.category===c.id)||services.items.some(x=>x.category===c.id)){status("Категория используется работами или карточками услуг. Сначала перенеси или удали связанные записи.",true);return;}
      if(!confirm("Удалить категорию «"+c.title+"»?"))return;
      categories=categories.filter(x=>x.id!==c.id);await writeFile(DATA+"categories.json",{version:1,items:categories},"Delete work category");renderCategoryManager();renderCatalog();fillCatalogCategorySelect();
    }));
  }
  function renderCatalog(filter="") {
    const cards=$("catalogCategories"),inside=$("catalogInside"),editor=$("catalogEditor");
    if(!selectedCatalogCategory){
      inside.classList.add("hidden");editor.classList.add("hidden");cards.classList.remove("hidden");
      cards.innerHTML=categories.slice().sort((a,b)=>(a.sort_order||0)-(b.sort_order||0)).map(c=>{
        const count=catalog.filter(x=>x.category===c.id).length;
        return '<button type="button" class="card" data-open-category="'+esc(c.id)+'" style="text-align:left;cursor:pointer;padding:16px;border:1px solid var(--line);border-radius:12px;background:var(--panel);color:inherit"><strong>'+esc(c.title)+'</strong><div class="muted">'+count+' работ</div><div class="muted">Открыть категорию →</div></button>';
      }).join("");
      cards.querySelectorAll("[data-open-category]").forEach(b=>b.addEventListener("click",()=>{selectedCatalogCategory=b.dataset.openCategory;$("priceSearch").value="";renderCatalog();}));
      return;
    }
    const category=categories.find(x=>x.id===selectedCatalogCategory);
    if(!category){selectedCatalogCategory=null;renderCatalog();return;}
    cards.classList.add("hidden");inside.classList.remove("hidden");
    $("catalogCategoryTitle").textContent=category.title;
    const q=filter.toLocaleLowerCase("ru-RU");
    const list=catalog.filter(x=>x.category===selectedCatalogCategory&&(x.name||"").toLocaleLowerCase("ru-RU").includes(q));
    $("catalogList").innerHTML=list.map(x=>'<div class="row" data-id="'+esc(x.id)+'"><span><strong>'+esc(x.name)+'</strong><small>'+esc(x.unit||"")+' · '+(x.price==null?"цена не задана":esc(String(x.price))+" ₽")+'</small></span><button type="button" class="btn secondary" data-edit-item="'+esc(x.id)+'">Изменить</button><button type="button" class="btn secondary" data-delete-item="'+esc(x.id)+'">Удалить</button></div>').join("")||'<p class="muted">В этой категории пока нет работ.</p>';
    $("catalogList").querySelectorAll("[data-edit-item]").forEach(b=>b.addEventListener("click",()=>openCatalogEditor(b.dataset.editItem)));
    $("catalogList").querySelectorAll("[data-delete-item]").forEach(b=>b.addEventListener("click",async()=>{
      const item=catalog.find(x=>String(x.id)===b.dataset.deleteItem);if(!item)return;
      if(!confirm("Удалить работу «"+item.name+"»? Это действие попадёт в GitHub после сохранения."))return;
      catalog=catalog.filter(x=>x!==item);await savePrices();renderCatalog($("priceSearch").value);renderCategoryManager();
    }));
  }
  function openCatalogEditor(id) {
    editingCatalogId=id||null;
    const form=$("catalogItemForm");form.reset();
    const item=id?catalog.find(x=>String(x.id)===String(id)):null;
    $("catalogEditorTitle").textContent=item?"Редактировать работу":"Добавить работу";
    if(form.elements.id)form.elements.id.value=item?String(item.id):"Будет создан автоматически";
    form.elements.name.value=item?.name||"";
    form.elements.unit.value=item?.unit||"м²";
    form.elements.price.value=item?.price??"";
    fillCatalogCategorySelect(item?.category||selectedCatalogCategory);
    $("catalogEditor").classList.remove("hidden");
    $("catalogEditor").scrollIntoView({behavior:"smooth",block:"start"});
  }
  async function savePrices() {
    await writeFile(DATA+"catalog.json",{version:1,items:catalog},"Update work catalog");
    status("Каталог подготовлен. Нажми общую кнопку «Сохранить изменения».");
  }
  function editPortfolio(id) {
    const item=portfolio.items.find(x=>x.id===id);if(!item)return;
    show("portfolio");
    const f=$("portfolioForm");f.elements.type.value=item.type;f.elements.title.value=item.title||"";f.elements.description.value=item.description||"";f.elements.category.value=item.category;
    $("imageField").classList.toggle("hidden",item.type!=="image");$("videoField").classList.toggle("hidden",item.type!=="video");
    f.elements.url.value=item.url||"";f.elements.url.dispatchEvent(new Event("input"));f.dataset.editId=item.id;
    $("portfolioSubmit").textContent="Сохранить материал";window.scrollTo({top:0,behavior:"smooth"});
  }
  function openPriceEditor(id) {
    const item=catalog.find(x=>String(x.id)===String(id));if(!item)return;
    show("catalog");$("priceSearch").value=item.name;renderCatalog(item.name);
    const row=Array.from($("catalogList").querySelectorAll(".row")).find(x=>String(x.dataset.id)===String(id));
    if(row){row.scrollIntoView({behavior:"smooth",block:"center"});row.querySelector("input")?.focus();}
  }
  function renderPortfolioCategories(){
    const f=$("portfolioCategoriesForm");if(!f)return;
    (portfolio.categories||[]).forEach(x=>{if(f.elements[x.id])f.elements[x.id].value=x.title||"";});
    const select=$("portfolioCategory");if(select)Array.from(select.options).forEach(option=>{const category=portfolio.categories.find(x=>x.id===option.value);if(category)option.textContent=category.title;});
  }
  async function savePortfolioCategories(e){
    e.preventDefault();const f=e.currentTarget;
    portfolio.categories.forEach(x=>{if(f.elements[x.id])x.title=f.elements[x.id].value.trim()||x.title;});
    await writeFile(DATA+"portfolio.json",portfolio,"Update portfolio category names");
    renderPortfolioCategories();renderPortfolio();status("Названия категорий подготовлены. Нажми общую кнопку «Сохранить изменения».");
  }
  function renderPortfolio() {
    const box=$("portfolioList");
    const ordered=portfolio.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));
    box.innerHTML=ordered.map((x,i)=>'<div class="item"><strong>'+esc(x.title)+'</strong><small>'+esc(categoryName(x.category))+' · '+(x.type==="video"?"Видео":"Фото")+'</small><p class="muted">'+esc(x.description||"")+'</p><div class="actions"><button class="btn secondary" data-edit-media="'+esc(x.id)+'">Изменить</button><button class="btn secondary" data-up-media="'+esc(x.id)+'" '+(i===0?"disabled":"")+'>↑</button><button class="btn secondary" data-down-media="'+esc(x.id)+'" '+(i===ordered.length-1?"disabled":"")+'>↓</button><button class="btn danger" data-delete-media="'+esc(x.id)+'">Удалить</button></div></div>').join("")||'<p class="muted">Портфолио пока пустое. Добавляй материалы только в одну из четырёх категорий.</p>';
    box.querySelectorAll("[data-edit-media]").forEach(b=>b.addEventListener("click",()=>editPortfolio(b.dataset.editMedia)));
    box.querySelectorAll("[data-delete-media]").forEach(b=>b.addEventListener("click",async()=>{if(!confirm("Удалить материал из портфолио?"))return;const item=portfolio.items.find(x=>x.id===b.dataset.deleteMedia);if(item&&item.image)await deleteFile(ROOT+item.image);portfolio.items=portfolio.items.filter(x=>x.id!==b.dataset.deleteMedia);await writeFile(DATA+"portfolio.json",portfolio,"Remove portfolio item");renderPortfolio();status("Удаление подготовлено. Нажми общую кнопку «Сохранить изменения».");}));
    const move=async(id,delta)=>{const list=portfolio.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));const i=list.findIndex(x=>x.id===id),j=i+delta;if(i<0||j<0||j>=list.length)return;[list[i],list[j]]=[list[j],list[i]];list.forEach((x,n)=>x.sortOrder=n);portfolio.items=list;await writeFile(DATA+"portfolio.json",portfolio,"Reorder portfolio");renderPortfolio();status("Порядок портфолио подготовлен. Нажми общую кнопку «Сохранить изменения».");};
    box.querySelectorAll("[data-up-media]").forEach(b=>b.addEventListener("click",()=>move(b.dataset.upMedia,-1).catch(e=>status(e.message,true))));
    box.querySelectorAll("[data-down-media]").forEach(b=>b.addEventListener("click",()=>move(b.dataset.downMedia,1).catch(e=>status(e.message,true))));
  }
  function categoryName(id){return (portfolio.categories||[]).find(x=>x.id===id)?.title||id;}
  async function addPortfolio(e) {
    e.preventDefault();const f=e.currentTarget,type=f.elements.type.value;
    const oldId=f.dataset.editId||"",old=portfolio.items.find(x=>x.id===oldId)||null;
    const item={...(old||{}),id:old?old.id:"media-"+Date.now(),type,title:f.elements.title.value.trim(),description:f.elements.description.value.trim(),category:f.elements.category.value,sortOrder:old?.sortOrder??portfolio.items.length,createdAt:old?.createdAt||new Date().toISOString()};
    if(type==="image"){
      const file=f.elements.file.files[0];
      if(file){const ext=(file.name.split(".").pop()||"jpg").toLowerCase().replace(/[^a-z0-9]/g,"");const previous=item.image;item.image=await uploadFile(ROOT+"assets/uploads/portfolio-"+item.id+"-"+Date.now()+"."+ext,file);if(previous)await deleteFile(ROOT+previous);}
      if(!item.image)throw new Error("Выбери изображение.");
      delete item.url;
    } else {
      const url=f.elements.url.value.trim();
      if(!/^https:\/\/(rutube\.ru|www\.rutube\.ru)\//i.test(url))throw new Error("Пока поддерживается ссылка Rutube.");
      item.url=url;
      if(item.image){await deleteFile(ROOT+item.image);delete item.image;}
    }
    if(old)portfolio.items=portfolio.items.map(x=>x.id===old.id?item:x);else portfolio.items.push(item);
    await writeFile(DATA+"portfolio.json",portfolio,old?"Update portfolio material":"Add portfolio material");
    f.reset();delete f.dataset.editId;$("portfolioSubmit").textContent="Добавить материал";$("imageField").classList.remove("hidden");$("videoField").classList.add("hidden");$("videoPreview").innerHTML="";
    renderPortfolio();status(old?"Изменения материала подготовлены.":"Материал подготовлен к добавлению. Нажми общую кнопку «Сохранить изменения».");
  }
  function renderPosts() {
    $("postsList").innerHTML=posts.items.slice().sort((a,b)=>String(b.date||"").localeCompare(String(a.date||""))).map(x=>'<div class="item"><strong>'+esc(x.title)+'</strong><small>'+esc(x.type)+' · '+esc(x.status)+(x.pinned?" · закреплено":"")+'</small><p class="muted">'+esc(x.excerpt||"")+'</p><div class="actions"><button class="btn secondary" data-edit-post="'+esc(x.id)+'">Изменить</button><button class="btn danger" data-delete-post="'+esc(x.id)+'">Удалить</button></div></div>').join("")||'<p class="muted">Публикаций пока нет.</p>';
    $("postsList").querySelectorAll("[data-delete-post]").forEach(b=>b.addEventListener("click",async()=>{if(!confirm("Удалить публикацию?"))return;const item=posts.items.find(x=>x.id===b.dataset.deletePost);if(item&&item.cover)await deleteFile(ROOT+item.cover);posts.items=posts.items.filter(x=>x.id!==b.dataset.deletePost);await writeFile(DATA+"posts.json",posts,"Delete publication");renderPosts();status("Удаление публикации подготовлено. Нажми общую кнопку «Сохранить изменения».");}));
    $("postsList").querySelectorAll("[data-edit-post]").forEach(b=>b.addEventListener("click",()=>{const x=posts.items.find(p=>p.id===b.dataset.editPost),f=$("postForm");["type","title","excerpt","body","status"].forEach(k=>f.elements[k].value=x[k]||"");f.elements.pinned.value=String(!!x.pinned);f.dataset.editId=x.id;window.scrollTo({top:0,behavior:"smooth"});}));
  }
  async function addPost(e) {
    e.preventDefault();const f=e.currentTarget,id=f.dataset.editId||"post-"+Date.now();
    const old=posts.items.find(x=>x.id===id)||{};
    const item={...old,id,type:f.elements.type.value,title:f.elements.title.value.trim(),excerpt:f.elements.excerpt.value.trim(),body:f.elements.body.value.trim(),status:f.elements.status.value,pinned:f.elements.pinned.value==="true",date:old.date||new Date().toISOString()};
    const file=f.elements.cover.files[0];
    if(file){const ext=(file.name.split(".").pop()||"jpg").toLowerCase().replace(/[^a-z0-9]/g,"");const previous=item.cover;item.cover=await uploadFile(ROOT+"assets/uploads/post-"+id+"-"+Date.now()+"."+ext,file);if(previous)await deleteFile(ROOT+previous);}
    if(old.id)posts.items=posts.items.map(x=>x.id===id?item:x);else posts.items.unshift(item);
    await writeFile(DATA+"posts.json",posts,"Save publication");
    f.reset();delete f.dataset.editId;renderPosts();status("Публикация подготовлена. Нажми общую кнопку «Сохранить изменения».");
  }
  function esc(v){return String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));}
  function renderConcreteProducts(){
    const box=$("concreteProductsList");if(!box)return;
    const ordered=concreteProducts.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));
    box.innerHTML=ordered.map((x,i)=>'<div class="item"><strong>'+esc(x.title)+'</strong><small>'+esc(x.id)+' · '+esc(x.image||"без изображения")+'</small><p class="muted">'+esc(x.description||"")+'</p><div class="actions"><button class="btn secondary" type="button" data-edit-concrete="'+esc(x.id)+'">Изменить</button><button class="btn secondary" type="button" data-move-concrete="'+esc(x.id)+'" data-delta="-1" '+(i===0?"disabled":"")+ '>↑</button><button class="btn secondary" type="button" data-move-concrete="'+esc(x.id)+'" data-delta="1" '+(i===ordered.length-1?"disabled":"")+ '>↓</button><button class="btn danger" type="button" data-delete-concrete="'+esc(x.id)+'">Удалить</button></div></div>').join("")||'<p class="muted">Позиции пока не добавлены.</p>';
    box.querySelectorAll("[data-edit-concrete]").forEach(b=>b.addEventListener("click",()=>editConcreteProduct(b.dataset.editConcrete)));
    box.querySelectorAll("[data-delete-concrete]").forEach(b=>b.addEventListener("click",async()=>{const item=concreteProducts.items.find(x=>x.id===b.dataset.deleteConcrete);if(!item||!confirm("Удалить позицию «"+item.title+"»?"))return;if(item.image&&item.image.startsWith("assets/uploads/"))await deleteFile(ROOT+item.image);concreteProducts.items=concreteProducts.items.filter(x=>x.id!==item.id);await writeFile(DATA+"concrete-products.json",concreteProducts,"Delete concrete product");renderConcreteProducts();}));
    box.querySelectorAll("[data-move-concrete]").forEach(b=>b.addEventListener("click",async()=>{const list=concreteProducts.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));const i=list.findIndex(x=>x.id===b.dataset.moveConcrete),j=i+Number(b.dataset.delta);if(i<0||j<0||j>=list.length)return;[list[i],list[j]]=[list[j],list[i]];list.forEach((x,n)=>x.sortOrder=(n+1)*10);concreteProducts.items=list;await writeFile(DATA+"concrete-products.json",concreteProducts,"Reorder concrete products");renderConcreteProducts();}));
  }
  function editConcreteProduct(id){
    const item=concreteProducts.items.find(x=>x.id===id),f=$("concreteProductForm");f.reset();f.dataset.editId=item?.id||"";
    f.elements.title.value=item?.title||"";f.elements.description.value=item?.description||"";f.elements.imagePath.value=item?.image||"";
    $("concreteProductFormTitle").textContent=item?"Редактировать изделие":"Новое изделие";$("concreteProductCancel").classList.toggle("hidden",!item);f.scrollIntoView({behavior:"smooth",block:"center"});
  }
  async function saveConcreteProduct(e){
    e.preventDefault();const f=e.currentTarget,old=concreteProducts.items.find(x=>x.id===f.dataset.editId)||null,title=f.elements.title.value.trim();
    if(!title){status("Укажи название изделия.",true);return;}
    const item={...(old||{}),id:old?.id||"concrete-"+(crypto.randomUUID?crypto.randomUUID():Date.now()),title,description:f.elements.description.value.trim(),sortOrder:old?.sortOrder??(concreteProducts.items.length+1)*10};
    const file=f.elements.image.files[0];
    if(file){const previous=item.image,ext=(file.name.split(".").pop()||"webp").toLowerCase().replace(/[^a-z0-9]/g,"");item.image=await uploadFile(ROOT+"assets/uploads/concrete-"+item.id+"-"+Date.now()+"."+ext,file);if(previous&&previous.startsWith("assets/uploads/"))await deleteFile(ROOT+previous);}
    else item.image=f.elements.imagePath.value.trim()||item.image||"";
    if(old)concreteProducts.items=concreteProducts.items.map(x=>x.id===old.id?item:x);else concreteProducts.items.push(item);
    await writeFile(DATA+"concrete-products.json",concreteProducts,"Save concrete product");
    f.reset();delete f.dataset.editId;$("concreteProductFormTitle").textContent="Новое изделие";$("concreteProductCancel").classList.add("hidden");renderConcreteProducts();
  }

  function renderServices(){
    const box=$("servicesList");if(!box)return;
    const ordered=services.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));
    box.innerHTML=ordered.map((x,i)=>'<div class="item"><strong>'+esc(x.title)+'</strong><small>'+esc(x.subtitle||"") +' · '+esc(x.category||"раздел бетон") +'</small><p class="muted">'+esc(x.description||"") +'</p><div class="actions"><button class="btn secondary" type="button" data-edit-service="'+esc(x.id)+'">Изменить</button><button class="btn secondary" type="button" data-move-service="'+esc(x.id)+'" data-delta="-1" '+(i===0?"disabled":"")+ '>↑</button><button class="btn secondary" type="button" data-move-service="'+esc(x.id)+'" data-delta="1" '+(i===ordered.length-1?"disabled":"")+ '>↓</button><button class="btn danger" type="button" data-delete-service="'+esc(x.id)+'">Удалить</button></div></div>').join("")||'<p class="muted">Услуги пока не добавлены.</p>';
    box.querySelectorAll("[data-edit-service]").forEach(b=>b.addEventListener("click",()=>editService(b.dataset.editService)));
    box.querySelectorAll("[data-delete-service]").forEach(b=>b.addEventListener("click",async()=>{const item=services.items.find(x=>x.id===b.dataset.deleteService);if(!item||!confirm("Удалить услугу «"+item.title+"»?"))return;if(item.icon&&item.icon.startsWith("assets/uploads/"))await deleteFile(ROOT+item.icon);services.items=services.items.filter(x=>x.id!==item.id);await writeFile(DATA+"services.json",services,"Delete service");renderServices();}));
    box.querySelectorAll("[data-move-service]").forEach(b=>b.addEventListener("click",async()=>{const list=services.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));const i=list.findIndex(x=>x.id===b.dataset.moveService),j=i+Number(b.dataset.delta);if(i<0||j<0||j>=list.length)return;[list[i],list[j]]=[list[j],list[i]];list.forEach((x,n)=>x.sortOrder=(n+1)*10);services.items=list;await writeFile(DATA+"services.json",services,"Reorder services");renderServices();}));
  }
  function editService(id){
    const x=services.items.find(v=>v.id===id),f=$("serviceForm");f.reset();f.dataset.editId=x?.id||"";
    if(x?.category&&!Array.from(f.elements.category.options).some(option=>option.value===x.category)){const option=document.createElement("option");option.value=x.category;option.textContent=x.category+" (текущий ID)";f.elements.category.appendChild(option);}
    ["title","subtitle","description","screen","category"].forEach(k=>{if(f.elements[k])f.elements[k].value=x?.[k]|| (k==="screen"?"detail":"");});
    f.elements.iconPath.value=x?.icon||"";$("serviceFormTitle").textContent=x?"Редактировать услугу":"Новая услуга";$("serviceCancel").classList.toggle("hidden",!x);f.scrollIntoView({behavior:"smooth",block:"center"});
  }
  async function saveService(e){
    e.preventDefault();const f=e.currentTarget,old=services.items.find(x=>x.id===f.dataset.editId)||null,title=f.elements.title.value.trim();if(!title){status("Укажи название услуги.",true);return;}
    const item={...(old||{}),id:old?.id||"service-"+(crypto.randomUUID?crypto.randomUUID():Date.now()),title,subtitle:f.elements.subtitle.value.trim(),description:f.elements.description.value.trim(),screen:f.elements.screen.value,category:f.elements.category.value,sortOrder:old?.sortOrder??(services.items.length+1)*10};
    const file=f.elements.icon.files[0];if(file){const previous=item.icon,ext=(file.name.split(".").pop()||"webp").toLowerCase().replace(/[^a-z0-9]/g,"");item.icon=await uploadFile(ROOT+"assets/uploads/service-"+item.id+"-"+Date.now()+"."+ext,file);if(previous&&previous.startsWith("assets/uploads/"))await deleteFile(ROOT+previous);}else item.icon=f.elements.iconPath.value.trim()||item.icon||"";
    if(old)services.items=services.items.map(x=>x.id===old.id?item:x);else services.items.push(item);
    await writeFile(DATA+"services.json",services,"Save service");f.reset();delete f.dataset.editId;$("serviceFormTitle").textContent="Новая услуга";$("serviceCancel").classList.add("hidden");renderServices();
  }
  function renderDiy(){
    const box=$("diyList");if(!box)return;const ordered=diyMedia.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));
    box.innerHTML=ordered.map((x,i)=>'<div class="item"><strong>'+esc(x.title)+'</strong><small>'+(x.type==="video"?"Видео Rutube":"Фото")+' · '+esc(x.id)+'</small><p class="muted">'+esc(x.description||"") +'</p><div class="actions"><button class="btn secondary" type="button" data-edit-diy="'+esc(x.id)+'">Изменить</button><button class="btn secondary" type="button" data-move-diy="'+esc(x.id)+'" data-delta="-1" '+(i===0?"disabled":"")+ '>↑</button><button class="btn secondary" type="button" data-move-diy="'+esc(x.id)+'" data-delta="1" '+(i===ordered.length-1?"disabled":"")+ '>↓</button><button class="btn danger" type="button" data-delete-diy="'+esc(x.id)+'">Удалить</button></div></div>').join("")||'<p class="muted">Материалов пока нет.</p>';
    box.querySelectorAll("[data-edit-diy]").forEach(b=>b.addEventListener("click",()=>editDiy(b.dataset.editDiy)));
    box.querySelectorAll("[data-delete-diy]").forEach(b=>b.addEventListener("click",async()=>{const item=diyMedia.items.find(x=>x.id===b.dataset.deleteDiy);if(!item||!confirm("Удалить материал?"))return;if(item.image)await deleteFile(ROOT+item.image);diyMedia.items=diyMedia.items.filter(x=>x.id!==item.id);await writeFile(DATA+"diy-media.json",diyMedia,"Delete DIY material");renderDiy();}));
    box.querySelectorAll("[data-move-diy]").forEach(b=>b.addEventListener("click",async()=>{const list=diyMedia.items.slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0));const i=list.findIndex(x=>x.id===b.dataset.moveDiy),j=i+Number(b.dataset.delta);if(i<0||j<0||j>=list.length)return;[list[i],list[j]]=[list[j],list[i]];list.forEach((x,n)=>x.sortOrder=(n+1)*10);diyMedia.items=list;await writeFile(DATA+"diy-media.json",diyMedia,"Reorder DIY materials");renderDiy();}));
  }
  function editDiy(id){
    const item=diyMedia.items.find(x=>x.id===id),f=$("diyForm");f.reset();f.dataset.editId=item?.id||"";f.elements.type.value=item?.type||"image";f.elements.title.value=item?.title||"";f.elements.description.value=item?.description||"";f.elements.url.value=item?.url||"";
    $("diyImageField").classList.toggle("hidden",f.elements.type.value!=="image");$("diyVideoField").classList.toggle("hidden",f.elements.type.value!=="video");$("diyFormTitle").textContent=item?"Редактировать материал":"Добавить материал";$("diyCancel").classList.toggle("hidden",!item);f.scrollIntoView({behavior:"smooth",block:"center"});
  }
  async function saveDiy(e){
    e.preventDefault();const f=e.currentTarget,old=diyMedia.items.find(x=>x.id===f.dataset.editId)||null,type=f.elements.type.value,title=f.elements.title.value.trim();if(!title){status("Укажи название материала.",true);return;}
    const item={...(old||{}),id:old?.id||"diy-"+(crypto.randomUUID?crypto.randomUUID():Date.now()),type,title,description:f.elements.description.value.trim(),sortOrder:old?.sortOrder??(diyMedia.items.length+1)*10};
    if(type==="image"){const file=f.elements.file.files[0];if(file){const previous=item.image,ext=(file.name.split(".").pop()||"webp").toLowerCase().replace(/[^a-z0-9]/g,"");item.image=await uploadFile(ROOT+"assets/uploads/diy-"+item.id+"-"+Date.now()+"."+ext,file);if(previous)await deleteFile(ROOT+previous);}if(!item.image)throw new Error("Выбери изображение.");delete item.url;}
    else{const url=f.elements.url.value.trim();if(!/^https:\/\/(rutube\.ru|www\.rutube\.ru)\\//i.test(url))throw new Error("Укажи ссылку Rutube.");item.url=url;if(item.image){await deleteFile(ROOT+item.image);delete item.image;}}
    if(old)diyMedia.items=diyMedia.items.map(x=>x.id===old.id?item:x);else diyMedia.items.push(item);await writeFile(DATA+"diy-media.json",diyMedia,"Save DIY material");
    f.reset();delete f.dataset.editId;$("diyFormTitle").textContent="Добавить материал";$("diyCancel").classList.add("hidden");$("diyImageField").classList.remove("hidden");$("diyVideoField").classList.add("hidden");renderDiy();
  }
  function renderOverview() {
    $("overviewStats").innerHTML=[
      ["Позиции прайса",catalog.length],
      ["Категории прайса",categories.length],
      ["Карточки услуг",services.items.length],
      ["Изделия из бетона",concreteProducts.items.length],
      ["Материалы портфолио",portfolio.items.length],
      ["Материалы DIY",diyMedia.items.length],
      ["Публикации",posts.items.length]
    ].map(x=>'<div class="stat"><b>'+x[1]+'</b><small>'+x[0]+'</small></div>').join("");
    $("overviewStatus").textContent="Ветка: "+BRANCH+". Все изменения собираются до одной кнопки сохранения и записываются единым commit в рабочую ветку. Основной сайт не изменяется автоматически.";
  }
  async function refreshStats() {
    $("storageStats").innerHTML='<p class="muted">Загружаю статистику…</p>';
    try{
      const branchInfo=await request(api("branches/"+BRANCH));
      const [repo,tree]=await Promise.all([
        request(api("")),
        request(api("git/trees/"+branchInfo.commit.commit.tree.sha+"?recursive=1"))
      ]);
      const files=(tree.tree||[]).filter(x=>x.type==="blob"), siteFiles=files.filter(x=>x.path.startsWith(ROOT));
      const total=siteFiles.reduce((s,x)=>s+(x.size||0),0),assets=siteFiles.filter(x=>x.path.startsWith(ROOT+"assets/")).reduce((s,x)=>s+(x.size||0),0),data=siteFiles.filter(x=>x.path.startsWith(DATA)).reduce((s,x)=>s+(x.size||0),0);
      const cards=[
        ["Размер репозитория",(repo.size/1024).toFixed(2)+" МБ","GitHub API; включает историю Git, не является жёсткой квотой"],
        ["Файлы сайта",(total/1048576).toFixed(2)+" МБ","Текущее дерево файлов, без старых версий Git"],
        ["Фото и ресурсы",(assets/1048576).toFixed(2)+" МБ","Assets в текущем дереве"],
        ["JSON и данные",(data/1024).toFixed(1)+" КБ","Прайс, профиль, портфолио, публикации"]
      ];
      const historicalSize=async(days)=>{
        const until=new Date(Date.now()-days*86400000).toISOString();
        const commits=await request(api("commits?sha="+BRANCH+"&until="+encodeURIComponent(until)+"&per_page=1"));
        if(!commits.length)return null;
        const commit=await request(api("commits/"+commits[0].sha));
        const oldTree=await request(api("git/trees/"+commit.commit.tree.sha+"?recursive=1"));
        return (oldTree.tree||[]).filter(x=>x.type==="blob"&&x.path.startsWith(ROOT)).reduce((sum,x)=>sum+(x.size||0),0);
      };
      for(const days of [7,30]){
        try{
          const previous=await historicalSize(days);
          if(previous!==null){
            const delta=total-previous,sign=delta>0?"+":"";
            cards.push(["Рост за "+days+" дней",sign+(delta/1048576).toFixed(2)+" МБ","Сравнение текущего дерева с последним commit до даты отсечения"]);
          } else cards.push(["Рост за "+days+" дней","Нет данных","В истории пока нет commit до даты отсечения"]);
        }catch(_){cards.push(["Рост за "+days+" дней","Не удалось получить","GitHub API не вернул историческое дерево"]);}
      }
      try{
        const traffic=await request(api("traffic/views?per=month"));
        cards.push(["Просмотры за месяц",String(traffic.count??"—"),"Уникальные посетители: "+String(traffic.uniques??"—")]);
      }catch(_){cards.push(["Трафик Pages","Не предоставлен","GitHub не вернул статистику просмотров для текущего токена"]);}

      try {
        const r=await fetch("../data/build-metrics.json",{cache:"no-store"});
        if(r.ok){
          const m=await r.json(), used=Number(m.pagesPublishedBytes||0), limit=Number(m.pagesLimitBytes||1073741824), remaining=Math.max(0,limit-used), pct=used/limit*100;
          cards.push(["Опубликованный Pages сайт",(used/1048576).toFixed(2)+" МБ","Размер последней сборки"]);
          cards.push(["Остаток лимита Pages",(remaining/1048576).toFixed(1)+" МБ","Из лимита 1 ГБ"]);
          if(pct>=95)cards.push(["Предупреждение","Критично","Использовано более 95% лимита Pages"]);
          else if(pct>=85)cards.push(["Предупреждение","Высокая нагрузка","Использовано более 85% лимита Pages"]);
          else if(pct>=70)cards.push(["Предупреждение","Следить","Использовано более 70% лимита Pages"]);
        } else cards.push(["Pages-сборка","Пока нет данных","Появится после успешной публикации"]);
      } catch (_) { cards.push(["Pages-сборка","Пока нет данных","Метрика появится после публикации"]); }
      try {
        const [artifacts,cache]=await Promise.all([
          request(api("actions/artifacts?per_page=100")),
          request(api("actions/cache/usage"))
        ]);
        const active=(artifacts.artifacts||[]).filter(x=>!x.expired);
        const bytes=active.reduce((sum,x)=>sum+Number(x.size_in_bytes||0),0);
        cards.push(["Actions artifacts",(bytes/1048576).toFixed(2)+" МБ","Неистёкшие артефакты в доступной странице API"]);
        cards.push(["Actions cache",(Number(cache.total_cache_size_in_bytes||0)/1048576).toFixed(2)+" МБ","Кэш репозитория"]);
        const runs=await request(api("actions/workflows/deploy-remont-master-pages.yml/runs?branch=remont-master-pages&per_page=1"));
        const latest=(runs.workflow_runs||[])[0];
        if(latest)cards.push(["Последняя публикация",latest.conclusion||latest.status,"Запуск "+latest.run_number+" · "+new Date(latest.created_at).toLocaleString("ru-RU")]);

      } catch (_) {
        cards.push(["Actions artifacts / cache","Нет доступа","Для этих метрик добавь токену Actions: Read-only"]);
      }
      $("storageStats").innerHTML=cards.map(x=>'<div class="stat"><b>'+esc(x[1])+'</b><small>'+esc(x[0])+'</small><p class="help">'+esc(x[2])+'</p></div>').join("");
      $("largestFiles").innerHTML="<h2>10 крупнейших файлов сайта</h2>"+siteFiles.sort((a,b)=>(b.size||0)-(a.size||0)).slice(0,10).map(x=>'<div class="row"><span>'+esc(x.path)+'</span><small>'+((x.size||0)/1024).toFixed(1)+' КБ</small></div>').join("");
    }catch(e){$("storageStats").innerHTML='<p class="help">Не удалось получить метрики: '+esc(e.message)+'</p>';}
  }


  async function login() {
    token=$("token").value.trim();if(!token)throw new Error("Вставь fine-grained token.");
    await request(api(""));
    $("login").classList.add("hidden");$("app").classList.remove("hidden");
    $("branchLabel").textContent="РАБОЧАЯ ВЕТКА";
    await loadAll();show("overview");
    status("GitHub подключён. Данные загружены из единого снимка рабочей ветки; токен не сохраняется в браузере.");
  }
  $("saveAllChanges").addEventListener("click",()=>saveAllChanges());
  window.addEventListener("beforeunload",event=>{if(pendingCount()){event.preventDefault();event.returnValue="";}});
  $("loginBtn").addEventListener("click",()=>login().catch(e=>{$("loginStatus").textContent=e.message;$("loginStatus").className="status error";}));
  $("logout").addEventListener("click",()=>{if(pendingCount()&&!confirm("Есть неподготовленные к сохранению изменения. Выйти и отбросить их?"))return;pendingWrites.clear();pendingDeletes.clear();updatePendingUI();token="";shas={};loadedHeadSha="";$("token").value="";$("app").classList.add("hidden");$("login").classList.remove("hidden");$("loginStatus").textContent="Сессия завершена.";});
  $("nav").addEventListener("click",e=>{const b=e.target.closest("[data-view]");if(b)show(b.dataset.view);});
  document.querySelectorAll("[data-go]").forEach(b=>b.addEventListener("click",()=>show(b.dataset.go)));
  $("profileForm").addEventListener("submit",e=>saveProfile(e).catch(x=>status(x.message,true)));
  $("concreteProductForm").addEventListener("submit",e=>saveConcreteProduct(e).catch(x=>status(x.message,true)));
  $("serviceForm").addEventListener("submit",e=>saveService(e).catch(x=>status(x.message,true)));
  $("diyForm").addEventListener("submit",e=>saveDiy(e).catch(x=>status(x.message,true)));
  $("diyForm").elements.type.addEventListener("change",e=>{$("diyImageField").classList.toggle("hidden",e.target.value!=="image");$("diyVideoField").classList.toggle("hidden",e.target.value!=="video");});
  $("diyCancel").addEventListener("click",()=>{const f=$("diyForm");f.reset();delete f.dataset.editId;$("diyFormTitle").textContent="Добавить материал";$("diyCancel").classList.add("hidden");$("diyImageField").classList.remove("hidden");$("diyVideoField").classList.add("hidden");});
  $("serviceCancel").addEventListener("click",()=>{const f=$("serviceForm");f.reset();delete f.dataset.editId;$("serviceFormTitle").textContent="Новая услуга";$("serviceCancel").classList.add("hidden");});
  $("concreteProductCancel").addEventListener("click",()=>{const f=$("concreteProductForm");f.reset();delete f.dataset.editId;$("concreteProductFormTitle").textContent="Новое изделие";$("concreteProductCancel").classList.add("hidden");});
  $("catalogBack").addEventListener("click",()=>{selectedCatalogCategory=null;$("catalogEditor").classList.add("hidden");renderCatalog();});
  $("priceSearch").addEventListener("input",e=>renderCatalog(e.target.value));
  $("addCatalogItem").addEventListener("click",()=>openCatalogEditor(null));
  $("cancelCatalogEdit").addEventListener("click",()=>{$("catalogEditor").classList.add("hidden");});
  $("catalogItemForm").addEventListener("submit",async e=>{
    e.preventDefault();const f=e.currentTarget,name=f.elements.name.value.trim(),unit=f.elements.unit.value.trim(),category=f.elements.category.value,raw=f.elements.price.value.trim();
    if(!name||!unit||!category){status("Заполни название, категорию и единицу измерения.",true);return;}
    const price=raw===""?null:Number(raw);if(price!==null&&(!Number.isFinite(price)||price<0)){status("Укажи корректную неотрицательную цену.",true);return;}
    if(editingCatalogId){const item=catalog.find(x=>String(x.id)===String(editingCatalogId));if(!item)return;Object.assign(item,{name,unit,category,price});}
    else catalog.push({id:"custom-"+(crypto.randomUUID?crypto.randomUUID():Date.now()+"-"+Math.random().toString(36).slice(2)),category,name,unit,price});
    selectedCatalogCategory=category;$("catalogEditor").classList.add("hidden");await savePrices();renderCatalog($("priceSearch").value);renderCategoryManager();
  });
  $("newWorkCategory").addEventListener("submit",async e=>{
    e.preventDefault();const title=e.currentTarget.elements.title.value.trim();if(!title)return;
    const id="custom-"+Date.now();categories.push({id,title,sort_order:Math.max(0,...categories.map(x=>Number(x.sort_order)||0))+10});
    await writeFile(DATA+"categories.json",{version:1,items:categories},"Add work category");e.currentTarget.reset();renderCategoryManager();renderCatalog();fillCatalogCategorySelect(id);
  });
  $("savePrices").addEventListener("click",()=>savePrices().catch(e=>status(e.message,true)));
  $("portfolioForm").addEventListener("submit",e=>addPortfolio(e).catch(x=>status(x.message,true)));
  $("portfolioCategoriesForm").addEventListener("submit",e=>savePortfolioCategories(e).catch(x=>status(x.message,true)));
  $("portfolioForm").elements.type.addEventListener("change",e=>{$("imageField").classList.toggle("hidden",e.target.value!=="image");$("videoField").classList.toggle("hidden",e.target.value!=="video");});
  $("portfolioForm").elements.url.addEventListener("input",e=>{
    const m=e.target.value.match(/rutube\.ru\/(?:video|play\/embed)\/([a-zA-Z0-9_-]+)/i);
    $("videoPreview").innerHTML=m?'<div style="position:relative;aspect-ratio:16/9;max-width:520px"><iframe src="https://rutube.ru/play/embed/'+encodeURIComponent(m[1])+'" allowfullscreen style="position:absolute;inset:0;width:100%;height:100%;border:0;border-radius:10px"></iframe></div>':"";
  });

  $("postForm").addEventListener("submit",e=>addPost(e).catch(x=>status(x.message,true)));
  $("refreshStats").addEventListener("click",()=>refreshStats());
})();