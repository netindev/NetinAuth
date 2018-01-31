package gq.netin.auth.event.bungee;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

/**
 *
 * @author netindev
 *
 */
public class PlayerPreCheckEvent extends Event {

	private PendingConnection connection;
	private boolean premium, cancelled = false;

	public PlayerPreCheckEvent(PendingConnection connection, boolean isPremium) {
		this.connection = connection;
		this.premium = isPremium;
	}

	public boolean isPremium() {
		return this.premium;
	}

	public boolean isCracked() {
		return !this.premium;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public PendingConnection getConnection() {
		return this.connection;
	}

}
