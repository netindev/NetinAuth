package gq.netin.auth.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import gq.netin.auth.Bukkit;
import gq.netin.auth.version.Version;

public class Util {

	public static String encode(String paramString) {
		byte[] encode = Base64.getEncoder().encode(paramString.getBytes());
		return new String(encode, Charset.forName("UTF-8"));
	}

	public static String decode(String paramString) {
		byte[] decode = Base64.getDecoder().decode(paramString.getBytes());
		return new String(decode, Charset.forName("UTF-8"));
	}

	public static void info(String string) {
		Bukkit.getPlugin().getLogger().info(string);
	}

	public static void severe(String string) {
		Bukkit.getPlugin().getLogger().severe(string);
	}

	public static List<Player> getOnlinePlayers() {
		final List<Player> list = new ArrayList<>();
		for (World world : Bukkit.getPlugin().getServer().getWorlds()) {
			for (org.bukkit.entity.Player player : world.getPlayers()) {
				list.add(player);
			}
		}
		return list;
	}

	public static boolean hasClass(String string) {
		try {
			Class.forName(string);
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static Object networkList(SocketAddress socketAddress, String connectionMethod, String networkManager) {
		try {
			Class<?> minecraftServer = Class
					.forName("net.minecraft.server." + Version.getPackageVersion() + ".MinecraftServer");
			Object serverInstance = minecraftServer.getMethod("getServer", (Class<?>[]) null).invoke(minecraftServer,
					(Object[]) null);
			Method serverConnection = serverInstance.getClass().getMethod(connectionMethod, (Class<?>[]) null);
			Object invokedConnection = serverConnection.invoke(serverInstance, (Object[]) null);
			Iterable<Object> networkList = (Iterable<Object>) Reflection.getField(networkManager, invokedConnection, 0);
			for (final Object nextNetwork : networkList) {
				Object address = nextNetwork.getClass().getMethod("getSocketAddress", (Class<?>[]) null)
						.invoke(nextNetwork, (Object[]) null);
				if (address.equals(socketAddress)) {
					return nextNetwork;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setOnlineMode(boolean online) {
		try {
			Class<?> minecraftServer = Class
					.forName("net.minecraft.server." + Version.getPackageVersion() + ".MinecraftServer");
			Object getServer = minecraftServer.getMethod("getServer").invoke(null);
			getServer.getClass().getMethod("setOnlineMode", boolean.class).invoke(getServer, online);
			Object server = minecraftServer.getDeclaredField("server").get(getServer);
			Field onlineField = server.getClass().getDeclaredField("online");
			onlineField.setAccessible(true);
			Object getOnline = onlineField.get(server);
			Field setValue = getOnline.getClass().getDeclaredField("value");
			setValue.setAccessible(true);
			setValue.set(getOnline, online);
		} catch (Exception e) {
			Util.info(Messages.IMPOSSIBLE_TO_SET_ONLINE_MODE);
			Bukkit.getPlugin().getServer().getPluginManager().disablePlugin(Bukkit.getPlugin());
		}
	}

}
