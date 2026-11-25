import { maxApi } from "./max-api.mjs";

const url = process.env.MAX_WEBHOOK_URL;
const secret = process.env.MAX_WEBHOOK_SECRET;

if (!url || !url.startsWith("https://")) throw new Error("Set MAX_WEBHOOK_URL to the public HTTPS webhook URL");
if (!secret || secret.length < 5) throw new Error("Set MAX_WEBHOOK_SECRET (minimum 5 characters)");

const bot = await maxApi("/me");
console.log("MAX bot identity:", bot.username || bot.name || "username not returned");

const current = await maxApi("/subscriptions");
const subscriptions = current.subscriptions || [];
const conflict = subscriptions.find((item) => item.url !== url);

if (conflict) {
  throw new Error(`A different webhook is already registered: ${conflict.url}. Nothing was changed. Review it before replacing it.`);
}

const same = subscriptions.find((item) => item.url === url);
if (same) {
  if ((same.update_types || []).includes("bot_started")) {
    console.log("This webhook URL is already registered for bot_started. No changes made.");
    process.exit(0);
  }
  throw new Error("This webhook URL already exists but is not subscribed to bot_started. Nothing was changed; inspect the current subscription before modifying it.");
}

const result = await maxApi("/subscriptions", {
  method: "POST",
  body: {
    url,
    update_types: ["bot_started"],
    secret
  }
});

if (result.success !== true) {
  throw new Error(`MAX did not confirm webhook registration: ${JSON.stringify(result)}`);
}
console.log("Webhook registered for bot_started.");
