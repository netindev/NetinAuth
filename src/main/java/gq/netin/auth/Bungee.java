package gq.netin.auth;

import gq.netin.auth.listener.bungee.Commons;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/**
 *
 * @author netindev
 *
 */
public class Bungee extends Plugin implements Listener {

	private static Bungee bungee;

	@Override
	public void onEnable() {
		/* Instance. */
		Bungee.bungee = this;

		this.getProxy().getPluginManager().registerListener(this, new Commons());
	}

	public static Bungee getInstance() {
		return Bungee.bungee;
	}

}
