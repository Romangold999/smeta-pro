import https from "node:https";

const API_BASE = "https://platform-api2.max.ru";

export function maxApi(path, { method = "GET", body } = {}) {
  const token = process.env.MAX_BOT_TOKEN;
  if (!token) throw new Error("MAX_BOT_TOKEN is not configured");

  return new Promise((resolve, reject) => {
    const url = new URL(path, API_BASE);
    const payload = body === undefined ? null : Buffer.from(JSON.stringify(body));
    const headers = { Authorization: token, Accept: "application/json" };
    if (payload) {
      headers["Content-Type"] = "application/json";
      headers["Content-Length"] = String(payload.length);
    }

    const options = {
      protocol: url.protocol,
      hostname: url.hostname,
      port: 443,
      path: url.pathname + url.search,
      method,
      headers,
      timeout: 20000
    };
    if (process.env.MAX_API_CA_PEM) options.ca = process.env.MAX_API_CA_PEM;

    const req = https.request(options, (res) => {
      const chunks = [];
      res.on("data", (chunk) => chunks.push(chunk));
      res.on("end", () => {
        const raw = Buffer.concat(chunks).toString("utf8");
        let data = null;
        try { data = raw ? JSON.parse(raw) : {}; }
        catch { return reject(new Error("MAX API returned non-JSON response")); }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          return reject(new Error(`MAX API HTTP ${res.statusCode}: ${data?.message || data?.code || raw}`));
        }
        resolve(data);
      });
    });
    req.on("timeout", () => req.destroy(new Error("MAX API request timed out")));
    req.on("error", reject);
    if (payload) req.write(payload);
    req.end();
  });
}
