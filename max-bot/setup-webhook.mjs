import { maxApi } from "./max-api.mjs";

const url = process.env.MAX_WEBHOOK_URL;
const secret = process.env.MAX_WEBHOOK_SECRET;

if (!url || !url.startsWith("https://")) throw new Error("Set MAX_WEBHOOK_URL to the public HTTPS webhook URL");
if (!secret || secret.length < 5) throw new Error("Set MAX_WEBHOOK_SECRET (minimum 5 characters)");

const current = await maxApi("/subscriptions");
const subscriptions = current.subscriptions || [];
const conflict = subscriptions.find((item) => item.url !== url);

if (conflict) {
  throw new Error(`A different webhook is already registered: ${conflict.url}. Nothing was changed. Review it before replacing it.`);
}

if (subscriptions.some((item) => item.url === url)) {
  console.log("This webhook URL is already registered. No changes made.");
  process.exit(0);
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
