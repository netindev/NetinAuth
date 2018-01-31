package gq.netin.auth.listener.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import gq.netin.auth.Bukkit;
import gq.netin.auth.check.Check;
import gq.netin.auth.event.bukkit.PlayerCheckEvent;
import gq.netin.auth.exception.InvalidCheckException;

/**
 *
 * @author netindev
 *
 */
public class Commons implements Listener {

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		if (Bukkit.getStorage().getPremiumMap().get(event.getPlayer().getName()) == null) {
			try {
				Bukkit.getStorage().setPremium(event.getPlayer().getName(),
						Check.fastCheck(event.getPlayer().getName()));
			} catch (InvalidCheckException e) {
				e.printStackTrace();
			}
		}

		PlayerCheckEvent checkEvent = new PlayerCheckEvent(event.getPlayer(),
				Bukkit.getStorage().getState(event.getPlayer().getName()));
		Bukkit.getPlugin().getServer().getPluginManager().callEvent(checkEvent);
	}

	@EventHandler
	private void onLeave(PlayerQuitEvent event) {
		if (!Bukkit.getStorage().getPremiumMap().containsKey(event.getPlayer().getName())) {
			return;
		}

		Bukkit.getStorage().removeVerified(event.getPlayer().getName(),
				Bukkit.getStorage().getState(event.getPlayer().getName()));
	}
}
