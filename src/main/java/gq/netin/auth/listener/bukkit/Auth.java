package gq.netin.auth.listener.bukkit;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import gq.netin.auth.Bukkit;
import gq.netin.auth.database.SQLManager.Status;
import gq.netin.auth.event.bukkit.PlayerCheckEvent;
import gq.netin.auth.event.bukkit.PlayerRegisterEvent;
import gq.netin.auth.util.Config.ConfigType;
import gq.netin.auth.util.Messages;

/**
 *
 * @author netindev
 *
 */
public class Auth implements Listener {

	private static final HashMap<Player, LoginType> LOGIN_MAP = new HashMap<>();

	public static HashMap<Player, LoginType> getLoginMap() {
		return Auth.LOGIN_MAP;
	}

	public enum LoginType {
		LOGIN, REGISTER
	}

	@EventHandler
	private void onCheck(PlayerCheckEvent event) {
		if (event.isCracked()) {
			if (!Bukkit.useMySQL()) {
				if (ConfigType.DATA.getConfig().get("AUTH." + event.getPlayer().getName() + ".PASSWORD") != null) {
					Auth.getLoginMap().put(event.getPlayer(), LoginType.LOGIN);
				} else {
					Auth.getLoginMap().put(event.getPlayer(), LoginType.REGISTER);
				}
			} else {
				if (Bukkit.getSQLManager().hasOnDatabase(event.getPlayer().getName())) {
					Auth.getLoginMap().put(event.getPlayer(), LoginType.LOGIN);
				} else {
					Auth.getLoginMap().put(event.getPlayer(), LoginType.REGISTER);
				}
			}
			Bukkit.getStorage().addNeedLogin(event.getPlayer().getName());
		} else if (event.isPremium()) {
			if (!Bukkit.useMySQL()) {
				return;
			} else {
				if (!Bukkit.getSQLManager().hasOnDatabase(event.getPlayer().getName())) {
					Bukkit.getSQLManager().setStatus(event.getPlayer().getName(), Status.PREMIUM);
				}
			}
		}
	}

	@EventHandler
	private void onRegister(PlayerRegisterEvent event) {
		if (Auth.getLoginMap().containsKey(event.getPlayer())) {
			Auth.getLoginMap().remove(event.getPlayer(), Auth.getLoginMap().get(event.getPlayer()));
		}
	}

	@EventHandler
	private void onLog(PlayerLoginEvent event) {
		if (Auth.getLoginMap().containsKey(event.getPlayer())) {
			Auth.getLoginMap().remove(event.getPlayer(), Auth.getLoginMap().get(event.getPlayer()));
		}
	}

	@EventHandler
	private void onLogin(PlayerLoginEvent event) {
		if (event.getPlayer().getName().length() > 20) {
			event.disallow(Result.KICK_OTHER, Messages.NAME_IS_SO_BIG);
		}
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		if (ConfigType.CONFIG.getConfig().getBoolean("REMOVE_JOIN_MESSAGE")) {
			event.setJoinMessage(null);
		}
	}

	@EventHandler
	private void onLeave(PlayerQuitEvent event) {
		if (Bukkit.getStorage().needLogin(event.getPlayer().getName())) {
			Bukkit.getStorage().removeNeedLogin(event.getPlayer().getName());
		}
		if (Auth.getLoginMap().containsKey(event.getPlayer())) {
			Auth.getLoginMap().remove(event.getPlayer(), Auth.getLoginMap().get(event.getPlayer()));
		}
		if (ConfigType.CONFIG.getConfig().getBoolean("REMOVE_QUIT_MESSAGE")) {
			event.setQuitMessage(null);
		}
	}

	@EventHandler
	private void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			if (Bukkit.getStorage().needLogin(((Player) event.getEntity()).getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	private void onMove(PlayerMoveEvent event) {
		if (Bukkit.getStorage().needLogin(event.getPlayer().getName())) {
			if (ConfigType.CONFIG.getConfig().getBoolean("BLOCK_ON_MOVE")) {
				event.getPlayer().teleport(event.getFrom());
			}
		}
	}

	@EventHandler
	private void onFood(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			if (Bukkit.getStorage().needLogin(event.getEntity().getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	private void onDrop(PlayerDropItemEvent event) {
		if (Bukkit.getStorage().needLogin(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void onInteract(PlayerInteractEvent event) {
		if (Bukkit.getStorage().needLogin(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void onChat(AsyncPlayerChatEvent event) {
		if (Bukkit.getStorage().needLogin(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void onCommand(PlayerCommandPreprocessEvent event) {
		if (Bukkit.getStorage().needLogin(event.getPlayer().getName())) {
			if (event.getMessage().startsWith("/register") || event.getMessage().startsWith("/login")) {
				event.setCancelled(false);
			} else {
				event.setCancelled(true);
			}
		}
	}

}
