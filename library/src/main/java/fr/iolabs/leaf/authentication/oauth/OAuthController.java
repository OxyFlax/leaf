package fr.iolabs.leaf.authentication.oauth;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.actions.OAuthLoginAction;
import fr.iolabs.leaf.authentication.model.LeafAccount;

@RestController
@RequestMapping("/api/account/oauth")
public class OAuthController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private OAuthAccountService oauthAccountService;

	/**
	 * @return the providers the back-end is actually configured for, so the
	 *         front-end only offers buttons that can work.
	 */
	@CrossOrigin
	@PermitAll
	@GetMapping("/providers")
	public List<String> listEnabledProviders() {
		return this.oauthAccountService.listEnabledProviders();
	}

	@CrossOrigin
	@PermitAll
	@PostMapping("/{provider}")
	public OAuthLoginResponse oauthLogin(@PathVariable String provider, @RequestBody OAuthLoginAction action) {
		return this.oauthAccountService.authenticateWithOAuth(provider, action);
	}

	@CrossOrigin
	@GetMapping("/me")
	public List<OAuthIdentitySummary> listMyIdentities() {
		return OAuthIdentitySummary.of(this.oauthAccountService.listIdentities(this.coreContext.getAccount()));
	}

	@CrossOrigin
	@PostMapping("/me/{provider}")
	public List<OAuthIdentitySummary> linkProvider(@PathVariable String provider,
			@RequestBody OAuthLoginAction action) {
		LeafAccount account = this.oauthAccountService.linkProvider(this.coreContext.getAccount(), provider, action);
		return OAuthIdentitySummary.of(this.oauthAccountService.listIdentities(account));
	}

	@CrossOrigin
	@DeleteMapping("/me/{provider}")
	public List<OAuthIdentitySummary> unlinkProvider(@PathVariable String provider) {
		LeafAccount account = this.oauthAccountService.unlinkProvider(this.coreContext.getAccount(), provider);
		return OAuthIdentitySummary.of(this.oauthAccountService.listIdentities(account));
	}
}
