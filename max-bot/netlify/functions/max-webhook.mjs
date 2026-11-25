import { timingSafeEqual } from "node:crypto";
import { maxApi } from "../../max-api.mjs";

const WELCOME_TEXT = [
  "**Здравствуйте!**",
  "",
  "Вы в RemontMaster — ремонт и отделка в Краснодаре.",
  "Здесь можно рассчитать стоимость работ, изучить каталог услуг и посмотреть наши проекты.",
  "",
  "Откройте приложение — всё необходимое для знакомства с нашими услугами уже внутри."
].join("\n");

function hasValidSecret(request) {
  const expectedValue = process.env.MAX_WEBHOOK_SECRET || "";
  const actualValue = request.headers.get("x-max-bot-api-secret") || "";
  if (expectedValue.length < 5) return false;

  const expected = Buffer.from(expectedValue);
  const actual = Buffer.from(actualValue);
  return actual.length === expected.length && timingSafeEqual(actual, expected);
}

export default async function handler(request) {
  if (request.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  if (!hasValidSecret(request)) {
    return new Response("Unauthorized", { status: 401 });
  }

  let update;
  try {
    update = await request.json();
  } catch {
    return Response.json({ ok: false, error: "Invalid JSON" }, { status: 400 });
  }

  if (update?.update_type !== "bot_started") {
    return Response.json({ ok: true, ignored: true });
  }

  if (!update.chat_id) {
    console.error("bot_started update has no chat_id");
    return Response.json({ ok: false }, { status: 400 });
  }

  const botUsername = process.env.MAX_BOT_USERNAME;
  if (!botUsername) {
    console.error("MAX_BOT_USERNAME is not configured");
    return Response.json({ ok: false }, { status: 500 });
  }

  try {
    await maxApi(`/messages?chat_id=${encodeURIComponent(update.chat_id)}`, {
      method: "POST",
      body: {
        text: WELCOME_TEXT,
        format: "markdown",
        attachments: [{
          type: "inline_keyboard",
          payload: {
            buttons: [[{
              type: "open_app",
              text: "Открыть RemontMaster",
              web_app: botUsername
            }]]
          }
        }]
      }
    });

    console.log("RemontMaster welcome message sent");
    return Response.json({ ok: true });
  } catch (error) {
    console.error("MAX welcome message failed:", error);
    return Response.json({ ok: false }, { status: 500 });
  }
}

export const config = {
  path: "/webhook",
  method: ["POST"]
};
