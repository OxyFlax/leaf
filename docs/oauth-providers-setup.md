# Setting up the social sign-in providers

Leaf verifies ID tokens; it never holds a provider secret. Configuring a provider
therefore means declaring **one public client id** on the console of the provider, and
giving that same id to the back-end (as the accepted token audience) and to the
front-end (to obtain the token). The two values must be byte-identical, or every
sign-in fails with `401 Unauthorized - <provider> token verification failed`.

| Where | Setting | Value |
| ----- | ------- | ----- |
| leaf (back-end) | `leaf.oauth.google.clientId` / `OAUTH_GOOGLE_CLIENT_ID` | Google **Web** client id |
| leaf (back-end) | `leaf.oauth.apple.clientId` / `OAUTH_APPLE_CLIENT_ID` | Apple **Services ID** |
| ngleaf (front-end) | `LeafConfig.oauth.google.clientId` | same Google Web client id |
| ngleaf (front-end) | `LeafConfig.oauth.apple.clientId` | same Apple Services ID |
| ngleaf (front-end) | `LeafConfig.oauth.apple.redirectUri` | one of the Apple *Return URLs* (defaults to the page origin) |

Leave a value blank to hide the corresponding button. The back-end accepts several
comma-separated ids per provider — add the iOS/Android client ids there when native
applications share the accounts; the front-end keeps only the web one.

## Google

Console: https://console.cloud.google.com — pick (or create) the project of the
application.

1. **APIs & Services → OAuth consent screen**. User type *External*. Fill the app name,
   support e-mail and developer contact. Scopes: `openid`, `email`, `profile` — all
   non-sensitive, no verification needed. While the app is in *Testing*, only the
   listed test users can sign in; *Publish* it before the client tries.
2. **APIs & Services → Credentials → Create credentials → OAuth client ID**.
   Application type *Web application*. Name it after the environment set it serves.
3. **Authorized JavaScript origins** — one entry per front-end origin, scheme included,
   no path, no trailing slash:
   - `http://localhost:4200` — local `ng serve`
   - `https://<uat-domain>` — UAT
   - `https://<prod-domain>` — production
4. **Authorized redirect URIs** — leave empty. The button uses Google Identity
   Services in popup mode; no redirect is involved.
5. Copy the **Client ID** (`…apps.googleusercontent.com`) into the four settings above.

Changes on the console take a few minutes to propagate; a stale origin shows up as the
button rendering but the popup closing on a `The given origin is not allowed` error in
the browser console.

Native applications: create one *iOS* and/or *Android* client id in the same project,
and append them to the back-end value: `web-id,ios-id,android-id`.

## Apple

Console: https://developer.apple.com/account → **Certificates, Identifiers & Profiles**.
A paid developer account is required.

1. **Identifiers → App IDs** — if the client has no App ID yet: *+ → App IDs → App*,
   bundle id in reverse-DNS form, tick the **Sign in with Apple** capability, register.
   The web sign-in needs an App ID to be its *primary* App ID even without a native
   app.
2. **Identifiers → Services IDs** — *+ → Services IDs*. Description = product name,
   Identifier = reverse-DNS, distinct from the App ID (e.g. `fr.acme.evmap.web`).
   Register, then open it and tick **Sign in with Apple → Configure**:
   - *Primary App ID*: the App ID of step 1.
   - *Domains and Subdomains*: `<uat-domain>`, `<prod-domain>` — bare hosts, no
     scheme, no port.
   - *Return URLs*: `https://<uat-domain>`, `https://<prod-domain>` — must be
     `https`, may include a path, no wildcard, no `localhost`, no IP.
   Save and *Continue*.
3. Copy the **Services ID identifier** into the four settings above. If the front-end
   is served from a path rather than the origin, set `oauth.apple.redirectUri` to the
   exact Return URL declared here.
4. Native iOS application: append its App ID bundle id to the back-end value:
   `services-id,bundle-id`.

Apple specifics that shape the implementation:

- **The user name is disclosed once**, on the very first authorization, outside of the
  ID token. The front-end forwards it in the login action and the back-end stores it
  at account creation. To test that path again, the tester must revoke the app on
  their Apple account (*Settings → Apple ID → Sign in with Apple → the app → Stop using*).
- **Hide My Email** gives the account a `…@privaterelay.appleid.com` address, reported
  as verified. Apple only relays messages to it from senders registered under
  *Sign in with Apple for Email Communication* on the same console page — register the
  SendGrid sending domain there, or transactional e-mails to those users bounce.
- `email_verified` may arrive as a boolean or as a string; both are handled.

## What can be tested where

| | Local (`http://localhost:4200`) | UAT (`https://<uat-domain>`) |
| --- | --- | --- |
| Google | ✅ once the origin is authorized | ✅ |
| Apple | ❌ Apple refuses `http` and `localhost` outright | ✅ |

Apple can only be exercised on a deployed HTTPS environment whose host is declared in
the Services ID. Locally, only the Google button renders — that is the configuration
working as intended, not a bug.

## Troubleshooting

| Symptom | Cause |
| --- | --- |
| `GET /api/account/oauth/providers` returns `[]` | the back-end env var is empty, or was not passed to the JVM |
| Button renders, popup closes, console says origin not allowed | origin missing from the Google console (or not propagated yet) |
| `401 … token verification failed` | front-end and back-end client ids differ, or the token expired (1 h) |
| Apple button never appears | `OAUTH_APPLE_CLIENT_ID` empty on the front-end, or page served over `http` |
| Apple sign-in works but the account has no name | not the first authorization for that Apple ID — revoke the app and retry |
| `400 … already uses this e-mail address` | the provider did not certify the e-mail; sign in with the password, then link the provider from *Connected accounts* |
