package gq.netin.auth.event.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author netindev
 *
 */
public class PlayerRegisterEvent extends PlayerEvent {

	private static final HandlerList HANDLERS = new HandlerList();
	private String password;

	public PlayerRegisterEvent(Player player, String password) {
		super(player);
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	@Override
	public HandlerList getHandlers() {
		return PlayerRegisterEvent.HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return PlayerRegisterEvent.HANDLERS;
	}

}
