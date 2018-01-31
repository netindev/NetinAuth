package gq.netin.auth.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author netindev
 *
 */
public class Storage {

	private final Map<String, Boolean> premiumMap = new HashMap<>();
	private final List<String> needToLogin = new ArrayList<>();

	public Map<String, Boolean> getPremiumMap() {
		return this.premiumMap;
	}

	public List<String> getLoginList() {
		return this.needToLogin;
	}

	public void setPremium(String playerName, boolean premium) {
		this.getPremiumMap().put(playerName, premium);
	}

	public void removeVerified(String playerName, boolean premiumState) {
		this.getPremiumMap().remove(playerName, premiumState);
	}

	public void addNeedLogin(String playerName) {
		this.getLoginList().add(playerName);
	}

	public void removeNeedLogin(String playerName) {
		this.getLoginList().remove(playerName);
	}

	public boolean needLogin(String playerName) {
		return this.getLoginList().contains(playerName);
	}

	public boolean getState(String playerName) {
		return this.getPremiumMap().get(playerName);
	}

}
