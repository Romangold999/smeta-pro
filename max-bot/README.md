# RemontMaster — MAX bot service

This service is deliberately isolated from the RemontMaster Mini App, website and admin.
It only sends a welcome message when MAX delivers a `bot_started` event. The message has one
native `open_app` button that launches the already-connected Mini App.

## Recommended hosting: Netlify Functions (serverless)

No VPS, always-running Node process, database or customer-data storage is needed.
The repository includes a Netlify Function at `/webhook`:

- Netlify receives MAX's HTTPS webhook.
- The function checks `X-Max-Bot-Api-Secret`.
- It ignores every event except `bot_started`.
- It sends one welcome message with the native `open_app` button.
- It returns HTTP 200 after MAX accepts the message.

The function is configured by the root `netlify.toml`. When importing this repository into
Netlify, use branch `remontmaster-max-bot`; do not change the production branch.

### Netlify environment variables

Set these in the Netlify site settings, under environment variables, with Functions runtime scope:

- `MAX_BOT_TOKEN` — secret token from MAX for Business. Never commit or send it in chat.
- `MAX_BOT_USERNAME` — public username of this MAX bot, without the @ sign.
- `MAX_WEBHOOK_SECRET` — random secret, 5–256 characters, letters/numbers/underscore/hyphen.
- `MAX_API_CA_PEM` — full PEM text of the official Russian Trusted Root CA certificate.

The CA certificate is required because MAX's current API endpoint uses the Russian Trusted CA
chain. Keep TLS verification enabled; never bypass certificate validation.

After the Netlify site is deployed, the webhook endpoint will be:

`https://<your-netlify-site>.netlify.app/webhook`

Register this URL with MAX using the existing `setup-webhook.mjs` script. It first checks existing
subscriptions and refuses to replace a different webhook, so an existing integration is not
silently disconnected. Run the script with `MAX_WEBHOOK_URL`, `MAX_BOT_TOKEN`,
`MAX_WEBHOOK_SECRET`, and `MAX_API_CA_PEM` set in the local environment.

Netlify's Free plan currently advertises 300 credits per month. This small event-driven handler
uses no database and should have very low usage for a modest number of bot starts. Monitor usage
in Netlify; free-plan limits and pricing can change.

## Alternative: persistent Node.js server

The original `server.mjs` remains available for a VPS or another persistent Node.js host.
Node.js 20.19+ is required. Set the same secrets plus `PORT`, then run:

```sh
npm start
```

Health check: `GET /health`. Register the webhook only after the public HTTPS endpoint is live.

## MAX requirements

MAX delivers `bot_started` through Webhook. The endpoint must be public HTTPS on port 443,
must validate the optional `X-Max-Bot-Api-Secret` header, and must return HTTP 200 within 30 seconds.
The current API base is `https://platform-api2.max.ru`; authorization uses the raw token in the
`Authorization` header.

## Current scope

- Handles `bot_started` only.
- Does not store customer data.
- Does not change the Mini App, its URL, files, site or admin.
- Does not connect CRM systems.
- Does not reply to arbitrary customer messages or collect contacts.
