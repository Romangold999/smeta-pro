# RemontMaster — MAX bot service

This service is deliberately isolated from the RemontMaster Mini App, website and admin.
It only sends a welcome message when MAX delivers a `bot_started` event. The message has one
native `open_app` button that launches the already-connected Mini App.

## Required environment variables

- `MAX_BOT_TOKEN` — secret token from MAX for Business. Never commit or send it in chat.
- `MAX_BOT_USERNAME` — public username of this MAX bot, without the @ sign.
- `MAX_WEBHOOK_SECRET` — random secret, 5–256 characters, letters/numbers/underscore/hyphen.
- `MAX_WEBHOOK_URL` — public HTTPS URL ending in `/webhook`, used only by the registration script.
- `MAX_API_CA_PEM` — optional PEM certificate text for the Russian Trusted Root CA, if the host's
  Node trust store does not trust MAX API's certificate chain.

## Run

Node.js 20.19+ is required.

```sh
npm start
```

Health check: `GET /health`

Register the webhook only after deploying the service and confirming that `GET /health` returns
HTTP 200 at the public HTTPS URL:

```sh
npm run register-webhook
```

The registration script first reads existing MAX subscriptions. If a different webhook is
already registered, it aborts without replacing it. This prevents accidentally disconnecting an
existing integration.

## Hosting requirements

Use a persistent Node.js service with a public HTTPS domain on port 443. The hosting platform may
terminate TLS and forward requests to the Node process on `PORT`. MAX requires a trusted TLS
certificate, a successful webhook response within 30 seconds, and the secret header
`X-Max-Bot-Api-Secret`.

MAX API calls use `https://platform-api2.max.ru` and the raw token in the `Authorization`
header. If Node reports an unknown issuer, configure `MAX_API_CA_PEM` with the official Russian
Trusted Root CA certificate obtained from Gosuslugi. Never disable TLS verification.

## Current scope

- Handles `bot_started` only.
- Does not store customer data.
- Does not change the Mini App, its URL, files, site or admin.
- Does not connect CRM systems.
- Does not reply to arbitrary customer messages or collect contacts.
