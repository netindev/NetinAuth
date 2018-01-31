package gq.netin.auth.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import gq.netin.auth.Bukkit;
import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Util;

/**
 *
 * @author netindev
 *
 */
public class SQLManager {

	public SQLManager() {
		try {
			Bukkit.getMySQL().updateSQL(
					"CREATE TABLE IF NOT EXISTS `player_data` (`player` varchar(20), `status` varchar(7), `password` varchar(65))");
		} catch (ClassNotFoundException | SQLException e) {
			Util.info(Messages.MYSQL_CONNECTION_ERROR);
			e.printStackTrace();
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
	}

	public enum Status {
		CRACKED, PREMIUM
	}

	public String getPassword(String playerName) {
		try {
			if (this.hasOnDatabase(playerName)) {
				PreparedStatement statement = Bukkit.getConnection()
						.prepareStatement("SELECT * FROM `player_data` WHERE `player`='" + playerName + "';");
				ResultSet set = statement.executeQuery();
				if (!set.next()) {
					return null;
				}
				return set.getString("password");
			}
		} catch (SQLException e) {
			Util.info(Messages.MYSQL_CONNECTION_ERROR);
			e.printStackTrace();
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
		return null;
	}

	public void setStatus(String playerName, Status status) {
		try {
			PreparedStatement statement = Bukkit.getConnection()
					.prepareStatement("INSERT INTO `player_data` (`player`, `status`) VALUES ('" + playerName + "', '"
							+ status.toString().toLowerCase() + "');");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			Util.info(Messages.MYSQL_CONNECTION_ERROR);
			e.printStackTrace();
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
	}

	public void updatePassword(String playerName, String password) {
		try {
			PreparedStatement statement = Bukkit.getConnection().prepareStatement(
					"UPDATE `player_data` SET password = '" + password + "' WHERE player = '" + playerName + "';");
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			Util.info(Messages.MYSQL_CONNECTION_ERROR);
			e.printStackTrace();
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
	}

	public void setPasswordAndStatus(String playerName, Status status, String password) {
		try {
			PreparedStatement statement = Bukkit.getConnection()
					.prepareStatement("INSERT INTO `player_data` (`player`, `status`, `password`) VALUES ('"
							+ playerName + "', '" + status.toString().toLowerCase() + "', '" + password + "');");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			Util.info(Messages.MYSQL_CONNECTION_ERROR);
			e.printStackTrace();
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
	}

	public boolean hasOnDatabase(String playerName) {
		try {
			PreparedStatement statement = Bukkit.getConnection()
					.prepareStatement("SELECT * FROM `player_data` WHERE player = '" + playerName + "';");
			ResultSet set = statement.executeQuery();
			return set.next();
		} catch (SQLException e) {
			Util.info(Messages.MYSQL_CONNECTION_ERROR);
			e.printStackTrace();
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
		return false;
	}

}
