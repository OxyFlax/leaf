# Leaf

## Installation & run

1) `mvn install`
2) `cd demo`
3) `mvn spring-boot:run`


While developping, you can go directly to the demo folder and run each time:
`cd .. && mvn install && cd ./demo && mvn spring-boot:run`


## Bump version script

To bump the leaf packages to the same version, you can use the script bump_version.sh with the version you want to bump:

```
sh ./bump_version.sh 1.30
```

## OAuth (social sign-in)

Leaf supports "Sign in with Google" and "Sign in with Apple". The front-end obtains an
ID token from the provider SDK and posts it to Leaf, which verifies it against the
provider public keys and issues a regular Leaf session token. No provider secret ever
reaches the browser, and Leaf never stores a provider access token.

### Configuration

```yaml
leaf:
  oauth:
    google:
      clientId: "${OAUTH_GOOGLE_CLIENT_ID:}"
    apple:
      clientId: "${OAUTH_APPLE_CLIENT_ID:}"
```

A provider whose `clientId` is left blank is disabled: it is not advertised by
`GET /api/account/oauth/providers` and any login attempt is rejected.

`clientId` accepts several **comma separated** values, for applications declared once
per platform. All of them are accepted as valid audiences:

```yaml
      clientId: "123-web.apps.googleusercontent.com,123-ios.apps.googleusercontent.com"
```

- **Google**: the OAuth 2.0 *Web application* client ID from the Google Cloud console
  (APIs & Services > Credentials). Declare the front-end origin in "Authorized
  JavaScript origins".
- **Apple**: the *Services ID* from the Apple developer console (Certificates,
  Identifiers & Profiles > Identifiers > Services IDs), plus the bundle ids of the
  native applications when there are any. Declare the front-end origin and the return
  URL in the Services ID configuration.

### Endpoints

| Method   | Path                                | Auth | Description                                                        |
| -------- | ----------------------------------- | ---- | ------------------------------------------------------------------ |
| `GET`    | `/api/account/oauth/providers`      | no   | Providers the back-end is configured for                            |
| `POST`   | `/api/account/oauth/{provider}`     | no   | Sign in, registering the account when the identity is unknown       |
| `GET`    | `/api/account/oauth/me`             | yes  | Social identities linked to the current account                     |
| `POST`   | `/api/account/oauth/me/{provider}`  | yes  | Link a provider to the current account                              |
| `DELETE` | `/api/account/oauth/me/{provider}`  | yes  | Unlink a provider from the current account                          |

`POST /api/account/oauth/{provider}` takes `{ idToken, name?, firstname?, lastname? }`
and answers `{ token, created, provider }`. `created` tells a registration from a plain
login, so the client can run its post-registration flow. The name fields exist because
Apple only exposes the user identity once, on the very first sign-in, outside of the
ID token.

### How an identity is matched

1. An account already owning the `(provider, subject)` identity signs in directly.
2. Otherwise, if the provider **certifies** the e-mail address and an account already
   uses it, the identity is attached to that account. An address the provider has not
   verified is never trusted for this, as declaring somebody else's e-mail would
   otherwise be enough to take their account over.
3. Otherwise a new account is created. Its profile (first name, last name, avatar,
   username) is pre-filled from the provider **at that moment only** — later sign-ins
   never touch the profile, so what the user edits afterwards sticks. This also matches
   Apple, which discloses the name once, on the very first authorization. The account
   holds a generated password and is flagged `authentication.passwordless`, which
   disables password login and prevents unlinking its last identity until the user
   defines a password through the "forgotten password" flow.

### How a token is verified — adding a provider

Verification goes through Spring events, like the other extension points of Leaf
(`LeafCustomAuthorizationEvent`, `LeafEligibilitiesEvent`...):

- `OAuthTokenValidationEvent` carries the provider name and the raw ID token. The
  listener in charge of that provider calls `event.validate(userInfo)` or
  `event.reject(reason)`; the others ignore it. `validated` is `false` by default.
- `OAuthProviderDiscoveryEvent` collects the providers that are actually configured,
  which is what `GET /api/account/oauth/providers` returns.

`GoogleTokenVerifier` and `AppleTokenVerifier` are such listeners. They extend
`OAuthTokenVerifier`, which answers both events and only asks for three things:

```java
@Component
public class AcmeTokenVerifier extends OAuthTokenVerifier {
    @Override public String getProvider()      { return "acme"; }
    @Override public boolean isConfigured()    { return !clientId.isBlank(); }
    @Override protected OAuthUserInfo verify(String idToken) {
        // check signature / issuer / audience / expiry, then map the claims
        // throw UnauthorizedException when the token is not acceptable
    }
}
```

Declaring that bean in **the application** is enough: no change to the library, the
controller, or the front-end besides a config entry and a button. An application
that needs a different shape can also listen to the two events directly.

Step-by-step console setup for Google and Apple (origins, return URLs, what works on
localhost): [docs/oauth-providers-setup.md](docs/oauth-providers-setup.md).

## Stripe

### Checkout

Useful links: 
- API checkout: https://stripe.com/docs/api/checkout/sessions
- Webhook test with stripe command line (for local testing): https://stripe.com/docs/webhooks/test

Command line:
After installing and loggin in with stripe cli:
-- Listen to the webhook: `stripe listen --forward-to http://127.0.0.1:8080/api/payment/stripe/checkout-sessions/webhook`
-- Dispatch an event manually for testing (cf trigger event doc below): `stripe trigger checkout.session.completed`


- Event API (returned by Stripe's webhook): https://stripe.com/docs/api/events/object && https://stripe.com/docs/cli/trigger#trigger-event
- Postman collection: https://www.postman.com/stripedev/workspace/stripe-developers/overview


