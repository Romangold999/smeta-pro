/* GitHub Pages data layer. All reads are same-origin static files; no external backend. */
(async function () {
  const scriptUrl = document.currentScript && document.currentScript.src
    ? document.currentScript.src
    : new URL("site-data.js", location.href).href;
  const dataUrl = name => new URL("data/" + name, scriptUrl).href;
  async function read(name) {
    const response = await fetch(dataUrl(name), { cache: "no-cache" });
    if (!response.ok) throw new Error(name + ": HTTP " + response.status);
    return response.json();
  }
  const safeText = (selector, value) => {
    const node = document.querySelector(selector);
    if (node && typeof value === "string" && value.trim()) node.textContent = value;
  };
  try {
    const [site, catalog, categories, portfolio, posts, concreteProducts, services, diyMedia] = await Promise.all([
      read("site-content.json"), read("catalog.json"), read("categories.json"),
      read("portfolio.json"), read("posts.json"), read("concrete-products.json"), read("services.json"), read("diy-media.json")
    ]);
    const content = site.content || {};
    const home = content.home || {};
    const profile = content.profile || {};
    const concrete = content.concrete || {};

    safeText("#home .hero .eyebrow", home.eyebrow);
    safeText("#home .hero h1", home.title);
    safeText("#home .hero p", home.description);
    const heroButton = document.querySelector("#home .hero button");
    if (heroButton && home.button) heroButton.textContent = home.button + " ↗";
    safeText("#profile .profilebox h2", profile.name);
    safeText("#profile .profilebox > p",
      [profile.specialty, profile.region].filter(Boolean).join(" · "));
    safeText("#profile .profile-region strong", profile.region);
    const regionMap = document.querySelector("#profile .profile-region-map");
    if (regionMap && profile.region) regionMap.hidden = !/краснодар/i.test(profile.region);
    safeText("header .brand small", [profile.name, profile.region].filter(Boolean).join(" · "));
    if (profile.avatar) document.querySelectorAll("#profile .profilepic img, header .avatar img").forEach(img => img.src = new URL(profile.avatar, scriptUrl).href);
    safeText("#concrete .pagehead h1", concrete.title);
    safeText("#concrete .concrete-hero p", concrete.description);

    const phoneLink = document.querySelector("#profile a[href^='tel:']");
    if (phoneLink && profile.phone) {
      phoneLink.href = "tel:" + String(profile.phone).replace(/[^+\d]/g, "");
      const small = phoneLink.querySelector("small");
      if (small) small.textContent = profile.phone;
      phoneLink.setAttribute("aria-label", "Позвонить по телефону " + profile.phone);
    }
    const extraContacts=document.getElementById("extraProfileContacts");
    if(extraContacts){
      extraContacts.replaceChildren();
      const contacts=[["telegram",profile.telegram,"Telegram","✈"],["whatsapp",profile.whatsapp,"WhatsApp","◉"]];
      contacts.forEach(([key,url,label,icon])=>{
        if(!url)return;let parsed;try{parsed=new URL(url);}catch(_){return;}
        const allowed=key==="telegram"?["t.me","www.t.me"].includes(parsed.hostname):["wa.me","www.wa.me","api.whatsapp.com"].includes(parsed.hostname);
        if(parsed.protocol!=="https:"||!allowed)return;
        const link=document.createElement("a");link.className="rowcard";link.href=parsed.href;link.target="_blank";link.rel="noopener noreferrer";link.style.textDecoration="none";
        const badge=document.createElement("span");badge.className="rowico";badge.textContent=icon;
        const copy=document.createElement("span"),strong=document.createElement("strong"),small=document.createElement("small");strong.textContent="Написать в "+label;small.textContent="Открыть "+label;copy.append(strong,small);
        const arrow=document.createElement("span");arrow.className="arrow";arrow.textContent="›";link.append(badge,copy,arrow);extraContacts.appendChild(link);
      });
    }
    const maxLink = document.querySelector("#profile a[href^='https://max.ru/']");
    if (maxLink && profile.max) maxLink.href = profile.max;

    if (Array.isArray(catalog.items) && catalog.items.length &&
        typeof workCatalog !== "undefined" && Array.isArray(workCatalog)) {
      workCatalog.splice(0, workCatalog.length, ...catalog.items.map(item => ({
        id: String(item.id),
        cat: String(item.category || "other"),
        name: String(item.name || ""),
        unit: String(item.unit || ""),
        price: item.price === null || item.price === undefined ? null : Number(item.price)
      })));
    }
    const select = document.getElementById("workCategory");
    if (select && Array.isArray(categories.items) && categories.items.length) {
      const selected = select.value;
      select.innerHTML = '<option value="all">Все работы</option>' +
        categories.items.map(item => '<option value="' +
          String(item.id).replace(/[&<>"']/g, "") + '">' +
          String(item.title || "").replace(/[&<>"']/g, "") + '</option>').join("");
      select.value = categories.items.some(item => item.id === selected) ? selected : "all";
    }

    const portfolioGroups = [
      { key: "decorative-walls", id: "portfolio-decorative-walls" },
      { key: "tile", id: "portfolio-tile" },
      { key: "concrete", id: "portfolio-concrete" },
      { key: "complex-structures", id: "portfolio-complex-structures" }
    ];
    const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, ch =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch]);
    const videoEmbed = url => {
      try {
        const u = new URL(url);
        const host = u.hostname.toLowerCase().replace(/^www\./, "");
        if (host === "rutube.ru") {
          const m = u.pathname.match(/\/(?:shorts|video|play\/embed)\/([a-zA-Z0-9_-]+)/);
          return m ? { src: "https://rutube.ru/play/embed/" + encodeURIComponent(m[1]), platform: "Rutube" } : null;
        }
        if (host === "youtu.be") {
          const id = u.pathname.split("/").filter(Boolean)[0] || "";
          return /^[a-zA-Z0-9_-]{6,}$/.test(id) ? { src: "https://www.youtube.com/embed/" + encodeURIComponent(id), platform: "YouTube" } : null;
        }
        if (host === "youtube.com" || host === "m.youtube.com" || host === "music.youtube.com" || host === "youtube-nocookie.com") {
          const m = u.pathname.match(/^\/(?:shorts|live|embed|v)\/([a-zA-Z0-9_-]+)/);
          const id = u.pathname === "/watch" ? (u.searchParams.get("v") || "") : (m ? m[1] : "");
          return /^[a-zA-Z0-9_-]{6,}$/.test(id) ? { src: "https://www.youtube.com/embed/" + encodeURIComponent(id), platform: "YouTube" } : null;
        }
      } catch (_) {}
      return null;
    };
    const diyTarget=document.getElementById("diyMaterials");
    if(diyTarget){
      diyTarget.replaceChildren();
      (Array.isArray(diyMedia.items)?diyMedia.items:[]).slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0)).forEach(item=>{
        const card=document.createElement("article");card.className="widecard";card.dataset.diyId=item.id||"";
        const title=document.createElement("h3");title.textContent=item.title||"Материал";card.appendChild(title);
        if(item.description){const desc=document.createElement("p");desc.className="muted";desc.textContent=item.description;card.appendChild(desc);}
        if(item.type==="video"&&item.url){
          const video=videoEmbed(item.url);
          if(video){const frame=document.createElement("iframe");frame.src=video.src;frame.title=item.title||"Видео";frame.allow="clipboard-write; autoplay; encrypted-media; fullscreen";frame.allowFullscreen=true;frame.loading="lazy";frame.style.cssText="width:100%;aspect-ratio:16/9;border:0;border-radius:10px";card.appendChild(frame);}
          else{const link=document.createElement("a");link.href=item.url;link.target="_blank";link.rel="noopener";link.textContent="Открыть видео";card.appendChild(link);}
        }else if(item.image){const img=document.createElement("img");img.src=new URL(item.image,scriptUrl).href;img.alt=item.title||"Материал";img.loading="lazy";img.style.cssText="width:100%;max-height:320px;object-fit:cover;border-radius:10px";card.appendChild(img);}
        diyTarget.appendChild(card);
      });
      if(!diyTarget.children.length){const empty=document.createElement("div");empty.className="empty";empty.textContent="Материалы появятся здесь.";diyTarget.appendChild(empty);}
    }
    const serviceList=document.getElementById("serviceList");
    if(serviceList){
      serviceList.replaceChildren();
      (Array.isArray(services.items)?services.items:[]).slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0)).forEach(item=>{
        const button=document.createElement("button");button.type="button";button.className="rowcard";button.dataset.serviceId=item.id||"";
        const icon=document.createElement("span");icon.className="rowico";
        if(item.icon){const img=document.createElement("img");img.src=new URL(item.icon,scriptUrl).href;img.alt="";img.loading="lazy";icon.appendChild(img);}
        const copy=document.createElement("span"),title=document.createElement("strong"),subtitle=document.createElement("small");
        title.textContent=item.title||"";subtitle.textContent=item.subtitle||"";copy.append(title,subtitle);
        const arrow=document.createElement("span");arrow.className="arrow";arrow.textContent="›";button.append(icon,copy,arrow);
        button.addEventListener("click",()=>{if(item.screen==="concrete"&&typeof window.go==="function")window.go("concrete");else if(typeof window.detail==="function")window.detail(item.title||"",item.description||"",item.category||"other");});
        serviceList.appendChild(button);
      });
    }
    const concreteGrid = document.getElementById("concreteProducts");
    if (concreteGrid) {
      concreteGrid.replaceChildren();
      (Array.isArray(concreteProducts.items) ? concreteProducts.items : []).slice().sort((a,b)=>(a.sortOrder??0)-(b.sortOrder??0)).forEach(item => {
        const card=document.createElement("div");card.className="product-card";card.dataset.concreteId=item.id||"";
        if(item.image)card.style.backgroundImage='url("'+new URL(item.image,scriptUrl).href.replace(/["\\\\]/g,"")+'")';
        const title=document.createElement("strong");title.textContent=item.title||"";
        const description=document.createElement("small");description.textContent=item.description||"";
        card.append(title,description);concreteGrid.appendChild(card);
      });
    }
    const portfolioItems = Array.isArray(portfolio.items) ? portfolio.items : [];
    const categoryTitles = portfolio.categories || [];
    categoryTitles.forEach(category => {
      const tile = document.querySelector('#portfolio .portfolio-category .tile[data-category="' + category.id + '"] strong');
      if (tile) tile.textContent = category.title;
    });
    const galleryScreens = {
      "decorative-walls":"portfolioDecorativeWalls",
      "tile":"portfolioTile",
      "concrete":"portfolioConcrete",
      "complex-structures":"portfolioComplexStructures"
    };
    categoryTitles.forEach(category => {
      const screen = document.getElementById(galleryScreens[category.id]);
      const heading = screen && screen.querySelector(".pagehead h1");
      if (heading) heading.textContent = category.title;
    });

    portfolioGroups.forEach(group => {
      const target = document.getElementById(group.id);
      if (!target) return;
      const items = portfolioItems.filter(item => item.category === group.key).sort((a,b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
      target.innerHTML = items.length
        ? '<div class="portfolio-gallery">' + items.map(item => {
            const title = item.title || "Работа";
            if (item.type === "video") {
              const video = videoEmbed(item.url || "");
              return video
                ? '<article class="widecard" data-portfolio-id="' + escapeHtml(item.id) + '"><div style="position:relative;width:100%;aspect-ratio:16/9"><iframe src="' + escapeHtml(video.src) + '" title="' + escapeHtml(title) + '" allow="clipboard-write; autoplay; encrypted-media; fullscreen" allowfullscreen loading="lazy" style="position:absolute;inset:0;width:100%;height:100%;border:0;border-radius:9px"></iframe></div><p>' + escapeHtml(title) + '</p><small>' + escapeHtml(item.description || "") + '</small></article>'
                : '<article class="widecard"><p>' + escapeHtml(title) + '</p><a href="' + escapeHtml(item.url || "") + '" target="_blank" rel="noopener">Открыть видео</a></article>';
            }
            const src = item.image ? new URL(item.image, scriptUrl).href : "";
            return src
              ? '<article class="widecard" data-portfolio-id="' + escapeHtml(item.id) + '"><img loading="lazy" src="' + escapeHtml(src) + '" alt="' + escapeHtml(title) + '" style="width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:10px"><p>' + escapeHtml(title) + '</p><small>' + escapeHtml(item.description || "") + '</small></article>'
              : "";
          }).join("") + '</div>'
        : '<div class="empty"><b>▧</b>Фотографии пока не добавлены.</div>';
    });

    const publishedPosts = (posts.items || []).filter(item => item.status === "published");
    const newsPosts = publishedPosts.filter(item => item.type === "news" || item.type === "announcement");
    const readIds = new Set(JSON.parse(localStorage.getItem("rm_read_posts") || "[]"));
    const unread = newsPosts.filter(item => !readIds.has(item.id));
    const header = document.querySelector("header.top");
    if (header && !document.getElementById("publicationBell")) {
      const bell = document.createElement("button");
      bell.id = "publicationBell";
      bell.type = "button";
      bell.setAttribute("aria-label", "Новости и объявления");
      bell.innerHTML = '♢<i id="publicationBellDot" aria-hidden="true"></i>';
      bell.style.cssText = "position:relative;width:39px;height:39px;margin-right:9px;border:1px solid #9b7848;border-radius:12px;background:#211b12;color:#f0d49a;font-size:21px";
      bell.onclick = () => { if (typeof go === "function") go("publications"); };
      header.insertBefore(bell, header.lastElementChild);
      const dot = document.createElement("style");
      dot.textContent = "#publicationBellDot{display:none;position:absolute;right:5px;top:5px;width:9px;height:9px;border-radius:50%;background:#e34b45;box-shadow:0 0 0 2px #211b12}#publicationBellDot.active{display:block}";
      document.head.appendChild(dot);
    }
    const bellDot = document.getElementById("publicationBellDot");
    if (bellDot) bellDot.classList.toggle("active", unread.length > 0);
    let publicationScreen = document.getElementById("publications");
    if (!publicationScreen) {
      publicationScreen = document.createElement("section");
      publicationScreen.className = "screen";
      publicationScreen.id = "publications";
      publicationScreen.innerHTML = '<div class="pagehead"><button class="back" onclick="back()">‹</button><div><h1>Новости и публикации</h1><p>Статьи, новости и объявления</p></div></div><div id="publicationList"></div>';
      document.querySelector("main").appendChild(publicationScreen);
    }
    const publicationList = document.getElementById("publicationList");
    let publicationFilter = "all";
    const renderPublications = () => {
      if (!publicationList) return;
      const filtered = publishedPosts.filter(item => publicationFilter === "all" || item.type === publicationFilter);
      const sorted = filtered.slice().sort((a,b) => Number(!!b.pinned)-Number(!!a.pinned) || String(b.date||"").localeCompare(String(a.date||"")));
      publicationList.innerHTML = '<div class="chips" id="publicationFilters">' +
        '<button class="chip selected" data-filter="all">Все</button>' +
        '<button class="chip" data-filter="article">Статьи</button>' +
        '<button class="chip" data-filter="news">Новости</button>' +
        '<button class="chip" data-filter="announcement">Объявления</button></div>' +
        (sorted.length ? sorted.map(item => {
          const cover = item.cover ? '<img src="' + escapeHtml(new URL(item.cover, scriptUrl).href) + '" alt="" loading="lazy" style="width:100%;max-height:190px;object-fit:cover;border-radius:10px">' : "";
          const kind = item.type === "announcement" ? "Объявление" : item.type === "news" ? "Новости" : "Статья";
          return '<article class="widecard">' + (item.pinned ? '<span class="tag">Закреплено</span>' : '') + cover + '<small class="muted">' + kind + ' · ' + escapeHtml(item.date ? new Date(item.date).toLocaleDateString("ru-RU") : "") + '</small><h3>' + escapeHtml(item.title || "") + '</h3><p>' + escapeHtml(item.excerpt || "") + '</p><details><summary>Читать полностью</summary><p style="white-space:pre-wrap">' + escapeHtml(item.body || "") + '</p></details></article>';
        }).join("") : '<div class="empty"><b>♢</b>В этой ленте пока нет публикаций.</div>');
      publicationList.querySelectorAll("[data-filter]").forEach(button => {
        button.classList.toggle("selected", button.dataset.filter === publicationFilter);
        button.addEventListener("click", () => { publicationFilter = button.dataset.filter; renderPublications(); });
      });
    };
    renderPublications();
    window.__rmPublicationOpen = () => {
      const ids = publishedPosts.filter(item => item.type === "news" || item.type === "announcement").map(item => item.id);
      const all = new Set(JSON.parse(localStorage.getItem("rm_read_posts") || "[]"));
      ids.forEach(id => all.add(id));
      localStorage.setItem("rm_read_posts", JSON.stringify([...all]));
      const dot = document.getElementById("publicationBellDot");
      if (dot) dot.classList.remove("active");
    };
    if (typeof window.go === "function" && !window.__rmGoWrapped) {
      const originalGo = window.go;
      window.go = function(id) { if (id === "publications") window.__rmPublicationOpen(); return originalGo(id); };
      window.__rmGoWrapped = true;
    }
    if (typeof renderCatalog === "function") renderCatalog();
    const renderedCatalogCount=document.querySelectorAll("#catalogList .catalogitem").length;
    document.documentElement.dataset.catalogCount=String(workCatalog.length);
    document.documentElement.dataset.catalogRenderedCount=String(renderedCatalogCount);
    const catalogCountNode=document.getElementById("catalogCount");
    if(catalogCountNode)catalogCountNode.textContent=workCatalog.length+" всего · показано "+renderedCatalogCount;
    if(renderedCatalogCount!==workCatalog.length)console.warn("Rendered catalog count differs from source",renderedCatalogCount,workCatalog.length);
    if (typeof renderServiceWorks === "function") renderServiceWorks();
    if (typeof renderEstimate === "function") renderEstimate();
    document.documentElement.dataset.siteData = "github";
  } catch (error) {
    console.warn("GitHub static data load skipped; built-in site data remains active.", error);
    document.documentElement.dataset.siteData = "fallback";
  }
})();
