package gq.netin.auth.util;

import java.lang.reflect.Field;

import gq.netin.auth.util.Config.ConfigType;

/**
 *
 * @author netindev
 *
 */
public class Messages {

	public static String PLUGIN_PREFIX;

	public static String PLEASE_REGISTER;
	public static String PLEASE_LOGIN;
	public static String SUCCESSFUL_LOGIN;
	public static String INCORRECT_PASSWORD;
	public static String INCORRECT_ARGS;
	public static String ALREADY_REGISTERED;
	public static String ALREADY_LOGGED_IN;
	public static String SUCCESSFUL_REGISTER;
	public static String NEED_TO_LOGIN_FIRST;
	public static String INVALID_CHANGEPASS_ARGS;
	public static String INCORRECT_ACTUAL_PASSWORD;
	public static String SAME_PASSWORD;
	public static String NEW_PASSWORD_CANNOT_HAVE_SPACES;
	public static String PASSWORD_CHANGED;
	public static String PASSWORD_SO_BIG;
	public static String NAME_IS_SO_BIG;
	public static String YOU_ARE_A_PREMIUM_PLAYER;
	public static String CANT_CHANGE_PASSWORD;

	public static String COMPATIBLE_SERVER;
	public static String NOT_COMPATIBLE_SERVER;
	public static String ONLY_USING_THE_PLUGIN;
	public static String REFUSED_PLAYER_CONNECTION;
	public static String ONLY_USABLE_AS_A_PLAYER;
	public static String IMPOSSIBLE_TO_SET_ONLINE_MODE;
	public static String MYSQL_CONNECTION_ERROR;
	public static String IMPOSSIBLE_TO_SAVE_CONFIG;
	public static String CONNECTED_WITH_MYSQL;
	public static String CONNECTING_WITH_MYSQL;

	enum Languages {
		EN_US, PT_BR;
	}

	static {
		ConfigType configType = null;
		Languages loadLang = null;
		for (int i = 0; i < Languages.values().length; i++) {
			String lang = ConfigType.CONFIG.getConfig().getString("LANG");
			if (lang.equals("DETECT")) {
				if (System.getProperty("user.language").equals("pt")) {
					Util.info("/ Linguagem automaticamente detectada, utilizando a lingua 'PT_BR'.");
					loadLang = Languages.PT_BR;
				} else if (System.getProperty("user.language").equals("en")) {
					Util.info("/ Language automatically detected, using the 'EN_US' language.");
					loadLang = Languages.EN_US;
				} else {
					Util.info("/ This system language does not yet have a translation, using the 'EN_US' language.");
					loadLang = Languages.EN_US;
				}
				break;
			} else if (lang.toLowerCase().equalsIgnoreCase(Languages.values()[i].toString().toLowerCase())) {
				loadLang = Languages.values()[i];
			}
		}
		for (int i = 0; i < ConfigType.values().length; i++) {
			if (ConfigType.values()[i].toString().equalsIgnoreCase(loadLang.toString().toLowerCase())) {
				configType = ConfigType.values()[i];
			}
		}
		if (configType == null) {
			throw new NullPointerException("Null configType variable.");
		}
		Field[] fields = Messages.class.getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().isAssignableFrom(String.class)) {
				continue;
			}
			field.setAccessible(true);
			try {
				if (field.getName().equalsIgnoreCase("PLUGIN_PREFIX")) {
					field.set(Messages.class, configType.getConfig().getString("PLUGIN_PREFIX").replace("&", "�"));
					continue;
				} else {
					field.set(Messages.class, configType.getConfig().getString(field.getName())
							.replace("<prefix>", Messages.PLUGIN_PREFIX).replace("&", "�"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
