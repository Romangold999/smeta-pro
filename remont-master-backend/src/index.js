const enc = new TextEncoder();
const json = (data, status = 200, headers = {}) => new Response(JSON.stringify(data), { status, headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store", ...headers } });
const clean = (v, n = 3000) => String(v ?? "").trim().slice(0, n);
const uid = () => crypto.randomUUID();
const b64 = b => btoa(String.fromCharCode(...new Uint8Array(b))).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
const un64 = s => Uint8Array.from(atob(s.replace(/-/g, "+").replace(/_/g, "/") + "=".repeat((4-s.length%4)%4)), c => c.charCodeAt(0));
async function sign(secret, value) {
  const key = await crypto.subtle.importKey("raw", enc.encode(secret), {name:"HMAC",hash:"SHA-256"}, false, ["sign"]);
  return b64(await crypto.subtle.sign("HMAC", key, enc.encode(value)));
}
async function tokenValid(req, env) {
  const [p,s,...rest] = (req.headers.get("authorization") || "").replace(/^Bearer\s+/i,"").split(".");
  if (!p || !s || rest.length || !env.SESSION_SECRET) return false;
  try {
    const key = await crypto.subtle.importKey("raw", enc.encode(env.SESSION_SECRET), {name:"HMAC",hash:"SHA-256"}, false, ["verify"]);
    if (!await crypto.subtle.verify("HMAC", key, un64(s), enc.encode(p))) return false;
    const data = JSON.parse(new TextDecoder().decode(un64(p)));
    return data.role === "admin" && data.exp > Date.now();
  } catch { return false; }
}
const defaults = {
  home:{eyebrow:"Ремонт · отделка · интерьер",title:"Качество, которое видно в деталях",description:"Профессиональный подход к ремонту и отделке. От идеи до готового результата.",button:"Рассчитать стоимость"},
  profile:{name:"Роман Геннадьевич",specialty:"Мастер по ремонту и отделке",region:"Краснодар и Краснодарский край",phone:"",telegram:"",whatsapp:"",max:""},
  services:[],materials:[],portfolio:[],concrete:{title:"Архитектурный бетон",description:""},settings:{}
};
function cors(req, env) {
  const origin=req.headers.get("Origin"), allowed=env.ALLOWED_ORIGIN||"";
  if(origin && allowed && origin!==allowed) return null;
  return origin ? {"access-control-allow-origin":origin,"vary":"Origin","access-control-allow-headers":"content-type,authorization","access-control-allow-methods":"GET,POST,PUT,PATCH,DELETE,OPTIONS"} : {};
}
async function limit(req, env) {
  const ip=req.headers.get("CF-Connecting-IP")||"unknown";
  const hash=b64(await crypto.subtle.digest("SHA-256",enc.encode(ip+":"+(env.SESSION_SECRET||"rate"))));
  const now=Math.floor(Date.now()/1000), start=Math.floor(now/3600)*3600;
  await env.DB.prepare("INSERT INTO lead_rate_limits(ip_hash,window_start,request_count) VALUES(?,?,1) ON CONFLICT(ip_hash) DO UPDATE SET window_start=excluded.window_start,request_count=CASE WHEN lead_rate_limits.window_start=excluded.window_start THEN lead_rate_limits.request_count+1 ELSE 1 END").bind(hash,start).run();
  const row=await env.DB.prepare("SELECT request_count FROM lead_rate_limits WHERE ip_hash=?").bind(hash).first();
  return (row?.request_count||0)<=5;
}
export default {
 async fetch(req,env) {
  const corsHeaders=cors(req,env);
  if(corsHeaders===null)return json({error:"Origin not allowed"},403);
  if(req.method==="OPTIONS")return new Response(null,{status:204,headers:corsHeaders||{}});
  const url=new URL(req.url),path=url.pathname;
  try {
   if(path==="/api/health"&&req.method==="GET"){const row=await env.DB.prepare("SELECT 1 AS ok").first();return json({ok:row?.ok===1,service:"remont-master-backend"},200,corsHeaders||{});}
   if(path==="/api/content"&&req.method==="GET"){const row=await env.DB.prepare("SELECT content_json,updated_at FROM site_content WHERE id=1").first();return json({content:row?JSON.parse(row.content_json):defaults,updatedAt:row?.updated_at||null},200,corsHeaders||{});}
   if(path==="/api/catalog"&&req.method==="GET"){const r=await env.DB.prepare("SELECT id,category,name,unit,price FROM work_catalog WHERE is_active=1 ORDER BY sort_order,name").all();return json({items:r.results||[]},200,corsHeaders||{});}
   if(path==="/api/leads"&&req.method==="POST"){
    if(!await limit(req,env))return json({error:"Слишком много обращений. Попробуйте позже."},429,corsHeaders||{});
    const b=await req.json(),name=clean(b.name,100),phone=clean(b.phone,40),message=clean(b.message,3000);
    if(name.length<2||phone.replace(/\D/g,"").length<7)return json({error:"Укажите имя и корректный телефон."},400,corsHeaders||{});
    const id=uid();await env.DB.prepare("INSERT INTO leads(id,name,phone,message,source) VALUES(?,?,?,?,?)").bind(id,name,phone,message,clean(b.source||"site",40)).run();
    return json({ok:true,id},201,corsHeaders||{});
   }
   if(path.startsWith("/media/")&&req.method==="GET"){
    const key=decodeURIComponent(path.slice(7));if(!key||key.includes(".."))return json({error:"Not found"},404,corsHeaders||{});
    const obj=await env.MEDIA.get(key);if(!obj)return json({error:"Not found"},404,corsHeaders||{});
    const h=new Headers(corsHeaders||{});obj.writeHttpMetadata(h);h.set("etag",obj.httpEtag);h.set("cache-control","public,max-age=31536000,immutable");return new Response(obj.body,{headers:h});
   }
   if(path==="/api/admin/login"&&req.method==="POST"){
    const b=await req.json();if(!env.ADMIN_PASSWORD||!env.SESSION_SECRET)return json({error:"Admin secrets are not configured"},503,corsHeaders||{});
    const a=enc.encode(clean(b.password,300)),z=enc.encode(env.ADMIN_PASSWORD);let diff=a.length^z.length;
    for(let i=0;i<Math.max(a.length,z.length);i++)diff|=(a[i]||0)^(z[i]||0);
    if(diff!==0)return json({error:"Неверный пароль"},401,corsHeaders||{});
    const p=b64(enc.encode(JSON.stringify({role:"admin",exp:Date.now()+8*60*60*1000})));
    return json({token:p+"."+await sign(env.SESSION_SECRET,p),expiresIn:28800},200,corsHeaders||{});
   }
   if(path.startsWith("/api/admin/")){
    if(!await tokenValid(req,env))return json({error:"Требуется вход администратора"},401,corsHeaders||{});
    if(path==="/api/admin/content"&&req.method==="GET"){const row=await env.DB.prepare("SELECT content_json,updated_at FROM site_content WHERE id=1").first();return json({content:row?JSON.parse(row.content_json):defaults,updatedAt:row?.updated_at||null},200,corsHeaders||{});}
    if(path==="/api/admin/content"&&req.method==="PUT"){
     const b=await req.json();if(!b.content||typeof b.content!=="object"||Array.isArray(b.content))return json({error:"Некорректное содержимое"},400,corsHeaders||{});
     const data=JSON.stringify(b.content);if(data.length>1000000)return json({error:"Слишком большой объём данных"},413,corsHeaders||{});
     await env.DB.prepare("INSERT INTO site_content(id,content_json,updated_at) VALUES(1,?,strftime('%Y-%m-%dT%H:%M:%fZ','now')) ON CONFLICT(id) DO UPDATE SET content_json=excluded.content_json,updated_at=excluded.updated_at").bind(data).run();return json({ok:true},200,corsHeaders||{});
    }
    if(path==="/api/admin/catalog"&&req.method==="GET"){const r=await env.DB.prepare("SELECT id,category,name,unit,price,is_active FROM work_catalog ORDER BY sort_order,name").all();return json({items:r.results||[]},200,corsHeaders||{});}
    if(path==="/api/admin/catalog"&&req.method==="PUT"){
     const b=await req.json();if(!Array.isArray(b.items)||b.items.length>3000)return json({error:"Некорректный каталог"},400,corsHeaders||{});
     const batch=[];for(let i=0;i<b.items.length;i++){const x=b.items[i];if(!x||!clean(x.id,80)||!clean(x.name,250)||!clean(x.unit,40))continue;
      batch.push(env.DB.prepare("INSERT INTO work_catalog(id,category,name,unit,price,sort_order,is_active,updated_at) VALUES(?,?,?,?,?,?,?,strftime('%Y-%m-%dT%H:%M:%fZ','now')) ON CONFLICT(id) DO UPDATE SET category=excluded.category,name=excluded.name,unit=excluded.unit,price=excluded.price,sort_order=excluded.sort_order,is_active=excluded.is_active,updated_at=excluded.updated_at").bind(clean(x.id,80),clean(x.category,80)||"other",clean(x.name,250),clean(x.unit,40),x.price===null||x.price===""?null:Math.max(0,Number(x.price)||0),i,x.is_active===false?0:1));
     }if(batch.length)await env.DB.batch(batch);return json({ok:true,saved:batch.length},200,corsHeaders||{});
    }
    if(path==="/api/admin/leads"&&req.method==="GET"){const r=await env.DB.prepare("SELECT id,name,phone,message,source,status,created_at FROM leads ORDER BY created_at DESC LIMIT 500").all();return json({items:r.results||[]},200,corsHeaders||{});}
    if(path.startsWith("/api/admin/leads/")&&req.method==="PATCH"){const id=clean(path.split("/").pop(),80),b=await req.json(),status=["new","in_progress","done","spam"].includes(b.status)?b.status:null;if(!status)return json({error:"Некорректный статус"},400,corsHeaders||{});await env.DB.prepare("UPDATE leads SET status=? WHERE id=?").bind(status,id).run();return json({ok:true},200,corsHeaders||{});}
    if(path==="/api/admin/media"&&req.method==="POST"){
     const form=await req.formData(),file=form.get("file");if(!file||typeof file.arrayBuffer!=="function")return json({error:"Файл не выбран"},400,corsHeaders||{});
     if(!file.type.startsWith("image/")||file.size>10*1024*1024)return json({error:"Допустимы изображения до 10 МБ"},400,corsHeaders||{});
     const ext={"image/jpeg":"jpg","image/png":"png","image/webp":"webp","image/avif":"avif","image/gif":"gif"}[file.type];if(!ext)return json({error:"Формат не поддерживается"},400,corsHeaders||{});
     const key="uploads/"+uid()+"."+ext;await env.MEDIA.put(key,await file.arrayBuffer(),{httpMetadata:{contentType:file.type}});
     await env.DB.prepare("INSERT INTO media(key,filename,content_type,size) VALUES(?,?,?,?)").bind(key,clean(file.name,200),file.type,file.size).run();return json({ok:true,key,url:"/media/"+key},201,corsHeaders||{});
    }
    if(path==="/api/admin/media"&&req.method==="GET"){const r=await env.DB.prepare("SELECT key,filename,content_type,size,created_at FROM media ORDER BY created_at DESC LIMIT 300").all();return json({items:r.results||[]},200,corsHeaders||{});}
    if(path.startsWith("/api/admin/media/")&&req.method==="DELETE"){const key=decodeURIComponent(path.slice("/api/admin/media/".length));if(!key.startsWith("uploads/")||key.includes(".."))return json({error:"Некорректный ключ"},400,corsHeaders||{});await env.MEDIA.delete(key);await env.DB.prepare("DELETE FROM media WHERE key=?").bind(key).run();return json({ok:true},200,corsHeaders||{});}
    return json({error:"Admin route not found"},404,corsHeaders||{});
   }
   if(path==="/"||path==="/admin"||path==="/admin/")return env.ASSETS.fetch(new Request(new URL("/admin/index.html",url),req));
   return env.ASSETS.fetch(req);
  }catch(e){return json({error:"Внутренняя ошибка сервера",detail:env.ENVIRONMENT==="development"?String(e):undefined},500,corsHeaders||{});}
 }
};
