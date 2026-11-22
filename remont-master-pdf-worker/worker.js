const API = "https://api.github.com";
const MAX_PDF_BYTES = 20 * 1024 * 1024;
const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "https://romangold999.github.io",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, X-File-Name",
  "Access-Control-Max-Age": "86400",
  "Vary": "Origin"
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...CORS_HEADERS, "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" }
  });
}
function githubHeaders(env, accept = "application/vnd.github+json") {
  return {
    "Authorization": "Bearer " + env.GITHUB_TOKEN,
    "Accept": accept,
    "X-GitHub-Api-Version": "2022-11-28",
    "User-Agent": "RemontMaster-temporary-pdf"
  };
}
async function gh(env, path, init = {}) {
  return fetch(API + path, {
    ...init,
    headers: { ...githubHeaders(env), ...(init.headers || {}) }
  });
}
async function getOrCreateRelease(env) {
  const base = "/repos/" + env.GH_REPO;
  let r = await gh(env, base + "/releases/tags/" + encodeURIComponent(env.RELEASE_TAG));
  if (r.ok) return r.json();
  if (r.status !== 404) throw new Error("GitHub release lookup failed: " + r.status);
  r = await gh(env, base + "/releases", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      tag_name: env.RELEASE_TAG,
      name: "Temporary PDF delivery",
      body: "Temporary PDF assets. Assets are deleted after download or by scheduled cleanup.",
      draft: false,
      prerelease: true
    })
  });
  if (!r.ok) throw new Error("GitHub release creation failed: " + r.status + " " + (await r.text()).slice(0, 300));
  return r.json();
}
async function sign(env, value) {
  const key = await crypto.subtle.importKey(
    "raw", new TextEncoder().encode(env.GITHUB_TOKEN),
    { name: "HMAC", hash: "SHA-256" }, false, ["sign"]
  );
  const bytes = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(value));
  return btoa(String.fromCharCode(...new Uint8Array(bytes))).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}
async function upload(request, env) {
  if (request.headers.get("Origin") !== "https://romangold999.github.io") return json({ error: "origin_not_allowed" }, 403);
  const length = Number(request.headers.get("Content-Length") || 0);
  if (length > MAX_PDF_BYTES) return json({ error: "file_too_large_max_20mb" }, 413);
  if (!(request.headers.get("Content-Type") || "").toLowerCase().startsWith("application/pdf")) return json({ error: "pdf_only" }, 415);
  const bytes = await request.arrayBuffer();
  if (!bytes.byteLength || bytes.byteLength > MAX_PDF_BYTES) return json({ error: "invalid_file_size" }, 413);
  const magic = new TextDecoder().decode(bytes.slice(0, 5));
  if (magic !== "%PDF-") return json({ error: "invalid_pdf" }, 415);

  const release = await getOrCreateRelease(env);
  const rawName = request.headers.get("X-File-Name") || "RemontMaster-smeta.pdf";
  const safeName = rawName.replace(/[^a-zA-Z0-9._-]/g, "_").slice(0, 90) || "estimate.pdf";
  const assetName = crypto.randomUUID() + "-" + safeName;
  // GitHub Releases are public in a public repository: encrypt each PDF before it is stored there.
  const key = await crypto.subtle.importKey("raw", await crypto.subtle.digest("SHA-256", new TextEncoder().encode(env.GITHUB_TOKEN)), { name: "AES-GCM" }, false, ["encrypt", "decrypt"]);
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encrypted = new Uint8Array(await crypto.subtle.encrypt({ name: "AES-GCM", iv }, key, bytes));
  const stored = new Uint8Array(iv.length + encrypted.length);
  stored.set(iv, 0); stored.set(encrypted, iv.length);
  const uploadUrl = release.upload_url.replace(/\{\?name,label\}$/, "") + "?name=" + encodeURIComponent(assetName + ".enc");
  const result = await fetch(uploadUrl, {
    method: "POST",
    headers: { ...githubHeaders(env), "Content-Type": "application/octet-stream" },
    body: stored
  });
  if (!result.ok) throw new Error("GitHub asset upload failed: " + result.status + " " + (await result.text()).slice(0, 300));
  const asset = await result.json();
  const expires = Math.floor(Date.now() / 1000) + 3600;
  const payload = asset.id + "." + expires;
  const signature = await sign(env, payload);
  const url = new URL(request.url);
  return json({ url: url.origin + "/download/" + asset.id + "/" + expires + "." + signature, fileName: safeName });
}
async function removeAsset(env, assetId) {
  const r = await gh(env, "/repos/" + env.GH_REPO + "/releases/assets/" + assetId, { method: "DELETE" });
  if (!r.ok && r.status !== 404) throw new Error("GitHub asset deletion failed: " + r.status);
}
async function download(request, env, ctx, assetId, token) {
  const [expiresText, signature] = token.split(".");
  const expires = Number(expiresText);
  if (!Number.isSafeInteger(expires) || expires < Math.floor(Date.now() / 1000) || expires > Math.floor(Date.now() / 1000) + 3700) return new Response("Link expired", { status: 410 });
  const expected = await sign(env, assetId + "." + expires);
  if (signature !== expected) return new Response("Invalid link", { status: 403 });

  const r = await gh(env, "/repos/" + env.GH_REPO + "/releases/assets/" + assetId, {
    headers: githubHeaders(env, "application/octet-stream")
  });
  if (!r.ok || !r.body) return new Response("PDF not found", { status: r.status === 404 ? 404 : 502 });
  const encrypted = new Uint8Array(await r.arrayBuffer());
  if (encrypted.length < 29) { await removeAsset(env, assetId); return new Response("Invalid encrypted PDF", { status: 502 }); }
  const key = await crypto.subtle.importKey("raw", await crypto.subtle.digest("SHA-256", new TextEncoder().encode(env.GITHUB_TOKEN)), { name: "AES-GCM" }, false, ["decrypt"]);
  let pdf;
  try {
    pdf = await crypto.subtle.decrypt({ name: "AES-GCM", iv: encrypted.slice(0, 12) }, key, encrypted.slice(12));
  } catch (error) {
    await removeAsset(env, assetId);
    throw new Error("Temporary PDF could not be decrypted");
  }
  // Remove the GitHub asset immediately after the encrypted bytes have been retrieved and decrypted.
  await removeAsset(env, assetId);
  return new Response(pdf, {
    headers: {
      "Content-Type": "application/pdf",
      "Content-Disposition": 'attachment; filename="RemontMaster-smeta.pdf"',
      "Cache-Control": "no-store, no-cache, must-revalidate",
      "X-Content-Type-Options": "nosniff",
      "Access-Control-Allow-Origin": "*"
    }
  });
}
async function cleanup(env) {
  const release = await getOrCreateRelease(env);
  const r = await gh(env, "/repos/" + env.GH_REPO + "/releases/" + release.id + "/assets?per_page=100");
  if (!r.ok) throw new Error("Could not list temporary PDF assets: " + r.status);
  const assets = await r.json();
  const cutoff = Date.now() - 60 * 60 * 1000;
  for (const asset of assets) {
    if (new Date(asset.created_at).getTime() < cutoff) {
      try { await removeAsset(env, asset.id); } catch (e) { console.error("Cleanup failed for asset", asset.id, e); }
    }
  }
}
export default {
  async fetch(request, env, ctx) {
    try {
      if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS_HEADERS });
      const url = new URL(request.url);
      if (url.pathname === "/upload" && request.method === "POST") return await upload(request, env);
      const match = url.pathname.match(/^\/download\/(\d+)\/(\d+\.[A-Za-z0-9_-]+)$/);
      if (match && request.method === "GET") return await download(request, env, ctx, match[1], match[2]);
      return json({ error: "not_found" }, 404);
    } catch (error) {
      console.error(error);
      return json({ error: error.message || "internal_error" }, 500);
    }
  },
  async scheduled(event, env, ctx) {
    ctx.waitUntil(cleanup(env));
  }
};