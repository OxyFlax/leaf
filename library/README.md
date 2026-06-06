# LEAF

Leaf is a **Spring based** library that offers conveniant turnkey  **Services** for Web App backends.
The main features are:
 - [x] Administration management
 - [x] Account/User management
 	- [x] Whitelisting
 - [x] Email sending services
 - [ ] Content Management Service
 - [ ] Account/User notifications

## Quickstart

Import the releases repository:

 	<repositories>
		<repository>
		  <id>io-labs-snapshots</id>
		  <url>https://nexus.io-labs.fr/repository/maven-snapshots/</url>
		</repository>
	</repositories>

Import the dependency:

	<dependency>
		<groupId>fr.io-labs</groupId>
		<artifactId>leaf</artifactId>
		<version>0.0.3-SNAPSHOT</version>
	</dependency>

Annotate your main application class:

    @SpringBootApplication
	@ComponentScan(basePackages = { "fr.iolabs.leaf", <your own main package here> })
	@EnableMongoRepositories(basePackages = { "fr.iolabs.leaf", <your own main package here> })

## Main features

### Account/User management
#### Extending the Leaf default Account

	public class MyAccount extends LeafAccount {

    private String name;

    public Account() {
        super();
        this.name = "default name";
    }

Your class will be discovered and used by Leaf automatically.
Make sure to have a default constructor which is calling the super() constructor.

#### Exposing the default Leaf API

    @RestController
	@RequestMapping("/api")
	public class AccountController extends LeafAccountController<MyAccount> {
	}

You can of course add your own methods to this controller.

#### Injecting the Service or the Repository in your components

    @Autowired
    private LeafAccountService<MyAccount> leafAccountService;

    @Autowired
    private LeafAccountRepository<MyAccount> leafAccountRepository;

### Administration management
#### Exposing the default Leaf API

    @RestController
	@RequestMapping("/api")
	public class AdminController extends LeafAdminController<MyAccount> {
	}

You can of course add your own methods to this controller.

#### Injecting the Service in your components

    @Autowired
    private LeafAminService<MyAccount> leafAccountService;

#### Enabling whitelisting

Whitelisting allows administrators to set a list of accepted email.
If an user try to register but his email is not in the whitelist, then the account creation is blocked.

To enable whitelisting add the following in application.properties:

	leaf.whitelisting.enabled=true

### OAuth2 authentication

Leaf provides turnkey OAuth2 login/registration. The frontend obtains a token
from the provider and posts it to:

	POST /api/account/oauth/{provider}
	{ "idToken": "<provider token>", "name": "<optional display name>" }

Leaf verifies the token, then finds, links or creates the matching account and
returns a session JWT.

#### Built-in providers

 - `google` — verifies a Google ID token (`leaf.oauth.google.clientId`)
 - `apple` — verifies an Apple ID token (`leaf.oauth.apple.clientId`)
 - `ampeco` — verifies an AMPECO access token through its userinfo endpoint
   (`leaf.oauth.ampeco.userInfoUri`)

#### Adding any OAuth2 provider

Any provider exposing an OpenID Connect style `userinfo` endpoint can be added
**without writing code**, by declaring it in your configuration:

	leaf:
	  oauth:
	    providers:
	      myprovider:
	        userInfoUri: "https://example.com/oauth/userinfo"
	        # Optional claim overrides (defaults shown):
	        idClaim: "sub"
	        emailClaim: "email"
	        firstnameClaim: "given_name"
	        lastnameClaim: "family_name"
	        nameClaim: "name"
	        avatarClaim: "picture"

The declared provider becomes immediately available at
`/api/account/oauth/myprovider`.

For providers needing custom token verification (such as locally verified ID
tokens), implement the `OAuthTokenVerifier` interface and expose it as a Spring
`@Component`; userinfo-endpoint based providers can simply extend
`AbstractUserInfoOAuthTokenVerifier`.

### Email sending service
Emailling his using a third party SAAS API named MailGun.
Documentation can be found here: https://www.mailgun.com/

#### Injecting the Service in your components

    @Autowired
    private LeafEmailService leafEmailService;

#### Configuring emailling
Add the following in application.properties:

    mailgun.api.url=<your mailgun api>/messages
	mailgun.api.from=<your sender email>
	mailgun.api.key=<your mailgun api key>
