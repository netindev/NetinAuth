package gq.netin.auth.event.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author netindev
 *
 */
public class PlayerCheckEvent extends PlayerEvent {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean premium;

	public PlayerCheckEvent(Player player, boolean isPremium) {
		super(player);
		this.premium = isPremium;
	}

	public boolean isPremium() {
		return this.premium;
	}

	public boolean isCracked() {
		return !this.premium;
	}

	@Override
	public HandlerList getHandlers() {
		return PlayerCheckEvent.HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return PlayerCheckEvent.HANDLERS;
	}

}
