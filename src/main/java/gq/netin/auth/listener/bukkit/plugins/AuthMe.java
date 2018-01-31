package gq.netin.auth.listener.bukkit.plugins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.xephi.authme.api.API;
import fr.xephi.authme.api.NewAPI;
import fr.xephi.authme.api.v3.AuthMeApi;
import gq.netin.auth.event.bukkit.PlayerCheckEvent;
import gq.netin.auth.util.Util;

/**
 *
 * @author netindev
 *
 */
@SuppressWarnings("deprecation")
public class AuthMe implements Listener {

	@EventHandler
	private void onJoin(PlayerCheckEvent event) {
		if (event.isPremium()) {
			if (Util.hasClass("fr.xephi.authme.api.v3.AuthMeApi")) {
				if (!AuthMeApi.getInstance().isRegistered(event.getPlayer().getName())) {
					AuthMeApi.getInstance().registerPlayer(event.getPlayer().getName(), "netinauth-bypass");
				}
				AuthMeApi.getInstance().forceLogin(event.getPlayer());
			} else if (Util.hasClass("fr.xephi.authme.api.NewAPI")) {
				if (!NewAPI.getInstance().isRegistered(event.getPlayer().getName())) {
					NewAPI.getInstance().registerPlayer(event.getPlayer().getName(), "netinauth-bypass");
				}
				NewAPI.getInstance().forceLogin(event.getPlayer());
			} else {
				if (!API.isRegistered(event.getPlayer().getName())) {
					API.registerPlayer(event.getPlayer().getName(), "netinauth-bypass");
				}
				API.forceLogin(event.getPlayer());
			}
		}
	}

}
