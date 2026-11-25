import http from "node:http";
import { timingSafeEqual } from "node:crypto";
import { maxApi } from "./max-api.mjs";

const PORT = Number(process.env.PORT || 3000);
const WEBHOOK_SECRET = process.env.MAX_WEBHOOK_SECRET;
const BOT_USERNAME = process.env.MAX_BOT_USERNAME;

if (!WEBHOOK_SECRET || WEBHOOK_SECRET.length < 5) throw new Error("MAX_WEBHOOK_SECRET is required (minimum 5 characters)");
if (!BOT_USERNAME) throw new Error("MAX_BOT_USERNAME is required (public MAX bot username)");

const seen = new Map();
const WELCOME_TEXT = [
  "**Здравствуйте!**",
  "",
  "Вы в RemontMaster — ремонт и отделка в Краснодаре.",
  "Здесь можно рассчитать стоимость работ, изучить каталог услуг и посмотреть примеры наших проектов.",
  "",
  "Откройте приложение — всё необходимое для знакомства с нашими услугами уже внутри."
].join("\n");

function authorized(req) {
  const actual = Buffer.from(req.headers["x-max-bot-api-secret"] || "");
  const expected = Buffer.from(WEBHOOK_SECRET);
  return actual.length === expected.length && timingSafeEqual(actual, expected);
}

function readJson(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let size = 0;
    req.on("data", (chunk) => {
      size += chunk.length;
      if (size > 1024 * 1024) {
        reject(new Error("Webhook payload too large"));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on("end", () => {
      try { resolve(JSON.parse(Buffer.concat(chunks).toString("utf8"))); }
      catch { reject(new Error("Invalid JSON")); }
    });
    req.on("error", reject);
  });
}

async function sendWelcome(update) {
  const chatId = update.chat_id;
  if (!chatId) throw new Error("bot_started update has no chat_id");

  const dedupeKey = `${chatId}:${update.timestamp || "no-time"}`;
  if (seen.has(dedupeKey)) return;
  if (seen.size > 2000) {
    const cutoff = Date.now() - 6 * 60 * 60 * 1000;
    for (const [key, time] of seen) if (time < cutoff) seen.delete(key);
  }

  await maxApi(`/messages?chat_id=${encodeURIComponent(chatId)}`, {
    method: "POST",
    body: {
      text: WELCOME_TEXT,
      format: "markdown",
      attachments: [{
        type: "inline_keyboard",
        payload: {
          buttons: [[{
            type: "open_app",
            text: "Открыть приложение",
            web_app: BOT_USERNAME
          }]]
        }
      }]
    }
  });
  seen.set(dedupeKey, Date.now());
  console.log("Welcome message sent", { chatId });
}

const server = http.createServer(async (req, res) => {
  if (req.method === "GET" && req.url === "/health") {
    res.writeHead(200, { "content-type": "application/json" });
    res.end(JSON.stringify({ ok: true, service: "remontmaster-max-bot" }));
    return;
  }

  if (req.method !== "POST" || req.url !== "/webhook") {
    res.writeHead(404);
    res.end();
    return;
  }
  if (!authorized(req)) {
    res.writeHead(401);
    res.end();
    return;
  }

  try {
    const update = await readJson(req);
    if (update.update_type === "bot_started") {
      await sendWelcome(update);
    }
    res.writeHead(200, { "content-type": "application/json" });
    res.end(JSON.stringify({ ok: true }));
  } catch (error) {
    console.error("Webhook processing failed:", error);
    res.writeHead(500, { "content-type": "application/json" });
    res.end(JSON.stringify({ ok: false }));
  }
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`RemontMaster MAX bot listening on ${PORT}`);
});
