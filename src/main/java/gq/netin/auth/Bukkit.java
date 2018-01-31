package gq.netin.auth;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.huskehhh.mysql.MySQL;

import gq.netin.auth.command.controller.CommandFactory;
import gq.netin.auth.database.SQLManager;
import gq.netin.auth.listener.bukkit.Auth;
import gq.netin.auth.listener.bukkit.Auth.LoginType;
import gq.netin.auth.listener.bukkit.Commons;
import gq.netin.auth.listener.bukkit.plugins.AuthMe;
import gq.netin.auth.storage.Storage;
import gq.netin.auth.util.Config;
import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Util;
import gq.netin.auth.version.Version;

/**
 *
 * @author netindev
 *
 */
public class Bukkit extends JavaPlugin {

	private static final Storage STORAGE;

	private static MySQL mySQL;
	private static Connection connection;
	private static SQLManager sqlManager;

	@Override
	public void onEnable() {
		Config.loadConfig();

		if (!this.getServer().getOnlineMode()) {
			Util.setOnlineMode(true);
		}

		Util.info("/ Loading...");

		if (Version.getPackageVersion() != null) {
			Util.info(Messages.COMPATIBLE_SERVER.replace("<version>", Version.getPackageVersion().toString()));
		} else {
			Util.info(Messages.NOT_COMPATIBLE_SERVER);
			this.getServer().getPluginManager().disablePlugin(this);
		}

		if (Version.is1_7()) {
			new gq.netin.auth.protocol.v1_7.LoginReceiver();
		} else {
			new gq.netin.auth.protocol.v1_8.LoginReceiver();
		}

		if (this.getConfig().getBoolean("USE_MYSQL")) {
			try {
				Util.info(Messages.CONNECTING_WITH_MYSQL);

				Bukkit.mySQL = new MySQL(this.getConfig().getString("MYSQL.HOSTNAME"),
						this.getConfig().getString("MYSQL.PORT"), this.getConfig().getString("MYSQL.DATABASE"),
						this.getConfig().getString("MYSQL.USERNAME"), this.getConfig().getString("MYSQL.PASSWORD"));
				Bukkit.connection = Bukkit.getMySQL().openConnection();
				Bukkit.sqlManager = new SQLManager();
			} catch (ClassNotFoundException | SQLException e) {
				Util.info(Messages.MYSQL_CONNECTION_ERROR);
				e.printStackTrace();
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}

		this.registerEvents();

		if (!this.getConfig().getBoolean("ONLY_USE_PREMIUM_CHECKER")) {
			CommandFactory.loadCommands();
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Player player : Util.getOnlinePlayers()) {
						if (Bukkit.getStorage().needLogin(player.getName())) {
							if (Auth.getLoginMap().get(player) == LoginType.LOGIN) {
								player.sendMessage(Messages.PLEASE_LOGIN);
							} else {
								player.sendMessage(Messages.PLEASE_REGISTER);
							}
						}
					}
				}
			}.runTaskTimer(this, 0L, 20 * this.getConfig().getInt("TIME_TO_REPEAT_LOGIN_MESSAGE"));
		} else {
			if (this.getConfig().getBoolean("PLUGIN_AUTO_LOGIN.AUTHME")) {
				if (this.getServer().getPluginManager().getPlugin("AuthMe") != null) {
					this.getServer().getPluginManager().registerEvents(new AuthMe(), this);
					Util.info(Messages.ONLY_USING_THE_PLUGIN.replace("<plugin_name>", "AuthMe"));
					return;
				}
			}
		}
	}

	@Override
	public void onDisable() {
		if (this.getServer().getOnlineMode() == false) {
			return;
		}

		Util.info("/ Disabling...");

		if (Bukkit.useMySQL()) {
			try {
				Bukkit.getConnection().close();
				Bukkit.getMySQL().closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		HandlerList.unregisterAll();
	}

	private void registerEvents() {
		final PluginManager manager = this.getServer().getPluginManager();

		manager.registerEvents(new Commons(), this);

		if (!this.getConfig().getBoolean("ONLY_USE_PREMIUM_CHECKER")) {
			manager.registerEvents(new Auth(), this);
		}
	}

	public static Plugin getPlugin() {
		return JavaPlugin.getPlugin(Bukkit.class);
	}

	public static MySQL getMySQL() {
		return Bukkit.mySQL;
	}

	public static Connection getConnection() {
		return Bukkit.connection;
	}

	public static SQLManager getSQLManager() {
		return Bukkit.sqlManager;
	}

	public static boolean useMySQL() {
		return Bukkit.getPlugin().getConfig().getBoolean("USE_MYSQL");
	}

	public static Storage getStorage() {
		return Bukkit.STORAGE;
	}

	static {
		STORAGE = new Storage();
	}

}
