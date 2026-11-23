(function () {
  const API = String(window.REMONT_MASTER_API || "").replace(/\/$/, "");
  if (!API) return;

  const text = (selector, value) => {
    const node = document.querySelector(selector);
    if (node && typeof value === "string" && value.trim()) node.textContent = value;
  };

  function mediaSource(value) {
    const v=String(value||"").trim();
    if(!v)return "";
    if(/^https?:\/\//i.test(v))return v;
    if(v.startsWith("/media/"))return API+v;
    return API+"/media/"+encodeURIComponent(v);
  }

  function renderContentCollections(content) {
    const services=Array.isArray(content.services)?content.services:[];
    if(services.length) {
      const home=document.getElementById("homeTiles"),list=document.getElementById("serviceList");
      const make=(item,i,homeMode)=>'<button class="'+(homeMode?"tile":"rowcard")+'" data-remote-service="'+i+'">'+(homeMode?'<span class="ico">✧</span>':'<span class="rowico">✧</span>')+'<span><strong>'+esc(item.title||item.name||"Услуга")+'</strong><small>'+esc(item.description||"")+'</small></span>'+(homeMode?"":'<span class="arrow">›</span>')+'</button>';
      if(home){home.innerHTML=services.map((x,i)=>make(x,i,true)).join("");home.querySelectorAll("[data-remote-service]").forEach(b=>b.addEventListener("click",()=>{const x=services[Number(b.dataset.remoteService)];detail(x.title||x.name||"Услуга",x.description||"",x.key||x.category||"other");}));}
      if(list){list.innerHTML=services.map((x,i)=>make(x,i,false)).join("");list.querySelectorAll("[data-remote-service]").forEach(b=>b.addEventListener("click",()=>{const x=services[Number(b.dataset.remoteService)];detail(x.title||x.name||"Услуга",x.description||"",x.key||x.category||"other");}));}
    }
    const materials=Array.isArray(content.materials)?content.materials:[];
    if(materials.length) {
      const section=document.getElementById("materials");
      if(section) {
        section.querySelectorAll(".rowcard").forEach(x=>x.remove());
        let target=document.getElementById("remoteMaterials");
        if(!target){target=document.createElement("div");target.id="remoteMaterials";const notice=section.querySelector(".notice");if(notice)notice.before(target);else section.appendChild(target);}
        target.innerHTML=materials.map((x,i)=>'<button class="rowcard" data-remote-material="'+i+'"><span class="rowico">◈</span><span><strong>'+esc(x.title||x.name||"Материал")+'</strong><small>'+esc(x.description||"")+'</small></span><span class="arrow">›</span></button>').join("");
        target.querySelectorAll("[data-remote-material]").forEach(b=>b.addEventListener("click",()=>{const x=materials[Number(b.dataset.remoteMaterial)];detail(x.title||x.name||"Материал",x.description||"",x.category||x.key||"other");}));
      }
    }
    const portfolio=Array.isArray(content.portfolio)?content.portfolio:[];
    if(portfolio.length) {
      const section=document.getElementById("portfolio");
      if(section) {
        let target=document.getElementById("remotePortfolioContent");
        if(!target){target=document.createElement("div");target.id="remotePortfolioContent";section.appendChild(target);}
        target.innerHTML=portfolio.map(x=>{const src=mediaSource(x.image||x.photo||x.media_key);return '<article class="widecard">'+(src?'<img loading="lazy" src="'+esc(src)+'" alt="'+esc(x.title||"Работа")+'" style="width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:10px">':'')+'<h3>'+esc(x.title||x.name||"Работа")+'</h3><p class="muted">'+esc(x.description||"")+'</p></article>';}).join("");
      }
    }
  }

  async function loadRemoteContent() {
    try {
      const response = await fetch(API + "/api/content", { cache: "no-store" });
      if (!response.ok) throw new Error("content API " + response.status);
      const payload = await response.json();
      const c = payload.content || {};
      const home = c.home || {};
      const profile = c.profile || {};
      const concrete = c.concrete || {};
      renderContentCollections(c);

      text("#home .hero .eyebrow", home.eyebrow);
      text("#home .hero h1", home.title);
      text("#home .hero p", home.description);
      text("#home .hero button", home.button ? home.button + " ↗" : "");
      text("#profile .profilebox h2", profile.name);
      text("#profile .profilebox > p", [profile.specialty, profile.region].filter(Boolean).join(" · "));
      text("#concrete .pagehead h1", concrete.title);
      text("#concrete .concrete-hero p", concrete.description);

      const phone = String(profile.phone || "").trim();
      const phoneLink = document.querySelector("#profile a[href^='tel:']");
      if (phoneLink && phone) {
        phoneLink.href = "tel:" + phone.replace(/[^+\d]/g, "");
        const small = phoneLink.querySelector("small");
        if (small) small.textContent = phone;
      }
      const maxLink = document.querySelector("#profile a[href^='https://max.ru/']");
      if (maxLink && profile.max) maxLink.href = profile.max;
    } catch (error) {
      console.warn("RemontMaster content sync skipped:", error);
    }
  }

  async function loadRemoteCatalog() {
    try {
      const response = await fetch(API + "/api/catalog", { cache: "no-store" });
      if (!response.ok) throw new Error("catalog API " + response.status);
      const payload = await response.json();
      if (!Array.isArray(payload.items) || !payload.items.length) return;
      const remote = payload.items.map(item => ({
        id: String(item.id),
        cat: String(item.category || "other"),
        name: String(item.name || ""),
        unit: String(item.unit || ""),
        price: item.price === null || item.price === undefined ? null : Number(item.price)
      }));
      workCatalog.splice(0, workCatalog.length, ...remote);
      if (typeof renderCatalog === "function") renderCatalog();
      if (typeof renderServiceWorks === "function") renderServiceWorks();
      if (typeof renderEstimate === "function") renderEstimate();
    } catch (error) {
      console.warn("RemontMaster catalog sync skipped:", error);
    }
  }


  function esc(value) {
    return String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
  }


  function videoCard(raw,title) {
    try {
      const u=new URL(String(raw||'').trim());
      if(u.protocol!=='https:'&&u.protocol!=='http:')return '';
      const host=u.hostname.toLowerCase().replace(/^www\./,'');
      let src='';
      if(host==='youtu.be'||host==='youtube.com'||host==='m.youtube.com'||host==='music.youtube.com') {
        const id=host==='youtu.be'?u.pathname.split('/').filter(Boolean)[0]:(u.searchParams.get('v')||u.pathname.split('/').filter(Boolean).pop());
        if(id&&/^[a-zA-Z0-9_-]{6,20}$/.test(id))src='https://www.youtube-nocookie.com/embed/'+id+'?rel=0';
      } else if(host==='rutube.ru'||host.endsWith('.rutube.ru')) {
        const p=u.pathname.split('/').filter(Boolean);const id=p[p.length-1]||'';
        if(p.indexOf('video')>=0||p.indexOf('embed')>=0)src='https://rutube.ru/play/embed/'+id;
      } else if(host==='vk.com'||host.endsWith('.vk.com')||host==='vkvideo.ru'||host.endsWith('.vkvideo.ru')||host==='vk.ru') {
        const p=u.pathname.split('/').filter(Boolean).pop()||'';const pair=p.replace('video','').split('_');
        const oid=u.searchParams.get('oid')||pair[0],id=u.searchParams.get('id')||pair[1],hash=u.searchParams.get('hash');
        if(oid&&id)src='https://vk.com/video_ext.php?oid='+encodeURIComponent(oid)+'&id='+encodeURIComponent(id)+(hash?'&hash='+encodeURIComponent(hash):'');
      }
      if(!src)return `<p><a class="goldbtn" href="${esc(u.href)}" target="_blank" rel="noopener">Открыть видео на площадке ↗</a></p>`;
      return `<div class="video-card"><div class="video-frame"><iframe loading="lazy" src="${esc(src)}" title="${esc(title||'Видео')}" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe></div></div>`;
    } catch(e) { return ''; }
  }
  function installVideoStyles(){if(document.getElementById('rmVideoStyles'))return;const st=document.createElement('style');st.id='rmVideoStyles';st.textContent='.video-card{margin:14px 0;border:1px solid #343a3b;border-radius:14px;overflow:hidden;background:#080909}.video-frame{position:relative;aspect-ratio:16/9}.video-frame iframe{position:absolute;inset:0;width:100%;height:100%;border:0}';document.head.appendChild(st);}
  async function loadRemoteCategories() {
    try {
      const response = await fetch(API + "/api/categories", {cache:"no-store"});
      if (!response.ok) throw new Error("categories API " + response.status);
      const payload = await response.json();
      const select = document.getElementById("workCategory");
      if (!select || !Array.isArray(payload.items)) return;
      const selected = select.value;
      select.innerHTML = '<option value="all">Все работы</option>' + payload.items.map(x =>
        '<option value="' + esc(x.id) + '">' + esc(x.title) + '</option>'
      ).join("");
      select.value = payload.items.some(x => x.id === selected) ? selected : "all";
      if (typeof renderCatalog === "function") renderCatalog();
    } catch (error) { console.warn("Category sync skipped:", error); }
  }

  async function loadDiy() {
    try {
      const response = await fetch(API + "/api/diy", {cache:"no-store"});
      if (!response.ok) throw new Error("DIY API " + response.status);
      const payload = await response.json();
      const section = document.getElementById("diy");
      if (!section) return;
      let target = document.getElementById("remoteDiyContent");
      if (!target) {
        target = document.createElement("div");
        target.id = "remoteDiyContent";
        section.appendChild(target);
      }
      const items = Array.isArray(payload.items) ? payload.items : [];
      target.innerHTML = items.length ? items.map(item => {
        const body = item.body || {};
        const bodyText = typeof body === "string" ? body : (body.text || body.content || body.intro || "");
        const image = item.cover_media_key ? API + "/media/" + encodeURIComponent(item.cover_media_key) : "";
        const video = item.video_url ? videoCard(item.video_url,item.title) : '';
        const external = item.external_url ? '<p><a href="' + esc(item.external_url) + '" target="_blank" rel="noopener">Дополнительный материал ↗</a></p>' : "";
        return '<article class="widecard">' + (image ? '<img src="' + image + '" alt="' + esc(item.title) + '" style="width:100%;max-height:220px;object-fit:cover;border-radius:12px">' : '') +
          '<div class="eyebrow">' + esc(item.category || "Советы") + '</div><h2 style="margin:7px 0">' + esc(item.title) + '</h2>' +
          '<p class="muted">' + esc(item.excerpt || "") + '</p>' +
          (bodyText ? '<div style="white-space:pre-wrap;font-size:13px;line-height:1.55">' + esc(bodyText) + '</div>' : '') + video + external + '</article>';
      }).join("") : '<div class="empty"><b>✎</b>Материалы скоро появятся.</div>';
    } catch (error) { console.warn("DIY sync skipped:", error); }
  }

  async function loadConcrete() {
    try {
      const response = await fetch(API + "/api/concrete", {cache:"no-store"});
      if (!response.ok) throw new Error("Concrete API " + response.status);
      const payload = await response.json();
      const section = document.getElementById("concrete");
      if (!section) return;
      let target = document.getElementById("remoteConcreteProducts");
      if (!target) {
        target = document.createElement("div");
        target.id = "remoteConcreteProducts";
        const grid = section.querySelector(".product-grid");
        if (grid) grid.after(target); else section.appendChild(target);
      }
      const items = Array.isArray(payload.items) ? payload.items : [];
      target.innerHTML = items.length ? '<div class="sectionhead"><h2>Наши изделия</h2></div><div class="grid">' + items.map(item => {
        const gallery = Array.isArray(item.gallery) ? item.gallery : [];
        const image = gallery.length ? API + "/media/" + encodeURIComponent(gallery[0]) : "";
        return '<article class="widecard">' + (image ? '<img src="' + image + '" alt="' + esc(item.title) + '" style="width:100%;max-height:190px;object-fit:cover;border-radius:10px">' : '') +
          '<h3>' + esc(item.title) + '</h3><p class="muted">' + esc(item.description || "") + '</p>' +
          '<p class="price">' + (item.price == null ? "Цена по запросу" : esc(item.price) + " ₽") + '</p>' +
          (item.video_url ? videoCard(item.video_url,item.title) : '') + '</article>';
      }).join("") + '</div>' : "";
    } catch (error) { console.warn("Concrete sync skipped:", error); }
  }

  async function loadPortfolioMedia() {
    try {
      const response = await fetch(API + '/api/media?related_type=portfolio', {cache:'no-store'});
      if (!response.ok) throw new Error('media API ' + response.status);
      const payload = await response.json();
      const section = document.getElementById('portfolio');
      if (!section) return;
      let target = document.getElementById('remotePortfolioMedia');
      if (!target) { target = document.createElement('div'); target.id = 'remotePortfolioMedia'; section.appendChild(target); }
      const items = Array.isArray(payload.items) ? payload.items : [];
      target.innerHTML = items.length ? '<div class="sectionhead"><h2>Мои работы</h2></div><div class="grid">' + items.map(x => '<article class="widecard"><img loading="lazy" src="' + API + '/media/' + encodeURIComponent(x.key) + '" alt="' + esc(x.alt_text || x.title || 'Фото работы') + '" style="width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:10px"><p>' + esc(x.title || '') + '</p></article>').join('') + '</div>' : '';
    } catch (error) { console.warn('Portfolio media sync skipped:', error); }
  }
  async function loadSectionMedia(type, sectionId, targetId, heading) {
    try {
      const response=await fetch(API+"/api/media?related_type="+encodeURIComponent(type),{cache:"no-store"});
      if(!response.ok)throw new Error("media API "+response.status);
      const payload=await response.json(),section=document.getElementById(sectionId);
      if(!section)return;
      let target=document.getElementById(targetId);
      if(!target){target=document.createElement("div");target.id=targetId;section.appendChild(target);}
      const items=Array.isArray(payload.items)?payload.items:[];
      target.innerHTML=items.length?'<div class="sectionhead"><h2>'+esc(heading)+'</h2></div><div class="grid">'+items.map(x=>'<article class="widecard"><img loading="lazy" src="'+API+'/media/'+encodeURIComponent(x.key)+'" alt="'+esc(x.alt_text||x.title||"Фото")+'" style="width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:10px"><p>'+esc(x.title||"")+'</p></article>').join("")+'</div>':"";
    } catch(error){console.warn("Assigned media sync skipped:",error);}
  }

  function trackPageView() {
    const active = document.querySelector(".screen.active");
    const path = active ? "/" + active.id : location.pathname;
    let session = "";
    try {
      session = sessionStorage.getItem("rm_analytics_session") || "";
      if (!session) { session = (crypto.randomUUID ? crypto.randomUUID() : String(Date.now()) + Math.random().toString(36).slice(2)); sessionStorage.setItem("rm_analytics_session", session); }
    } catch (_) {}
    fetch(API + "/api/analytics/pageview", {
      method:"POST", headers:{"content-type":"application/json"},
      body:JSON.stringify({path:path,session:session})
    }).catch(() => {});
  }
  let lastTrackedPath = "";
  function trackIfChanged() {
    const active = document.querySelector(".screen.active");
    const path = active ? "/" + active.id : location.pathname;
    if (path !== lastTrackedPath) { lastTrackedPath = path; trackPageView(); }
  }
  document.addEventListener("click", event => {
    if (event.target.closest("[onclick*=\"go(\"]") || event.target.closest(".nav") || event.target.closest(".back")) {
      setTimeout(trackIfChanged, 80);
    }
  }, true);
  setTimeout(trackIfChanged, 250);

  function addLeadForm() {
    const profile = document.getElementById("profile");
    if (!profile || document.getElementById("remoteLeadForm")) return;
    const card = document.createElement("div");
    card.className = "widecard";
    card.id = "remoteLeadForm";
    card.innerHTML =
      '<div class="sectionhead"><h2>Оставить заявку</h2></div>' +
      '<form id="remoteLeadFormFields">' +
      '<label class="field">Ваше имя<input name="name" autocomplete="name" required minlength="2" placeholder="Как к вам обращаться"></label>' +
      '<label class="field">Телефон<input name="phone" type="tel" autocomplete="tel" required placeholder="+7 ___ ___-__-__"></label>' +
      '<label class="field">Что нужно сделать?<input name="message" placeholder="Кратко опишите задачу"></label>' +
      '<button class="goldbtn" type="submit" style="width:100%">Отправить заявку</button>' +
      '<p class="muted" id="remoteLeadStatus" style="font-size:11px;margin:9px 0 0" aria-live="polite"></p>' +
      '</form>';
    profile.appendChild(card);
    const form = document.getElementById("remoteLeadFormFields");
    form.addEventListener("submit", async function (event) {
      event.preventDefault();
      const button = form.querySelector("button[type='submit']");
      const status = document.getElementById("remoteLeadStatus");
      const data = new FormData(form);
      const body = {
        name: String(data.get("name") || "").trim(),
        phone: String(data.get("phone") || "").trim(),
        message: String(data.get("message") || "").trim(),
        source: "remont-master-client"
      };
      button.disabled = true;
      status.textContent = "Отправляем...";
      try {
        const response = await fetch(API + "/api/leads", {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify(body)
        });
        const result = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(result.error || "Не удалось отправить заявку");
        form.reset();
        status.textContent = "Заявка отправлена. Я свяжусь с вами.";
      } catch (error) {
        status.textContent = error.message || "Ошибка отправки. Попробуйте позже.";
      } finally {
        button.disabled = false;
      }
    });
  }

  installVideoStyles();
  loadRemoteContent();
  loadRemoteCatalog();
  loadRemoteCategories();
  loadDiy();
  loadSectionMedia('diy','diy','remoteDiyMedia','Фотографии и примеры');
  loadConcrete();
  loadSectionMedia('concrete','concrete','remoteConcreteMedia','Фотографии изделий');
  loadPortfolioMedia();
  addLeadForm();
})();
