package gq.netin.auth.command;

import java.io.IOException;

import org.bukkit.entity.Player;

import gq.netin.auth.Bukkit;
import gq.netin.auth.check.Check;
import gq.netin.auth.command.controller.CommandFactory;
import gq.netin.auth.command.controller.CommandInfo;
import gq.netin.auth.event.bukkit.PlayerChangePasswordEvent;
import gq.netin.auth.exception.InvalidCheckException;
import gq.netin.auth.util.Config;
import gq.netin.auth.util.Config.ConfigType;
import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Util;

/**
 *
 * @author netindev
 *
 */
public class ChangePassword extends CommandFactory {

	public ChangePassword() {
		super("changepassword");
	}

	@Override
	protected void onCommand(CommandInfo info) {
		if (!info.isPlayer()) {
			Util.info(Messages.ONLY_USABLE_AS_A_PLAYER);
			return;
		}

		try {
			if (Check.fastCheck(info.getCommandSender().getName())) {
				info.getCommandSender().sendMessage(Messages.YOU_ARE_A_PREMIUM_PLAYER);
				return;
			}
		} catch (InvalidCheckException e) {
			e.printStackTrace();
		}

		if (!Bukkit.useMySQL()) {
			if (!Bukkit.getStorage().needLogin(info.getCommandSender().getName())) {
				if (info.getArgs().length == 1 || info.getArgs().length == 0) {
					info.getCommandSender().sendMessage(Messages.INVALID_CHANGEPASS_ARGS);
					return;
				} else if (info.getArgs().length == 2) {
					if (info.getArgs()[1].length() > 16) {
						info.getCommandSender().sendMessage(Messages.PASSWORD_SO_BIG);
						return;
					}
					if (Util.decode(ConfigType.DATA.getConfig()
							.getString("AUTH." + info.getCommandSender().getName() + ".PASSWORD")) == null) {
						info.getCommandSender().sendMessage(Messages.CANT_CHANGE_PASSWORD);
						return;
					}
					if (info.getArgs()[0].equals(Util.decode(ConfigType.DATA.getConfig()
							.getString("AUTH." + info.getCommandSender().getName() + ".PASSWORD")))) {
						if (info.getArgs()[0].equals(info.getArgs()[1])) {
							info.getCommandSender().sendMessage(Messages.SAME_PASSWORD);
						} else {
							ConfigType.DATA.getConfig().set("AUTH." + info.getCommandSender().getName() + ".PASSWORD",
									new String(Util.encode(info.getArgs()[1])));
							try {
								ConfigType.DATA.getConfig().save(Config.getPaths()[2]);
							} catch (IOException e) {
								Util.severe(Messages.IMPOSSIBLE_TO_SAVE_CONFIG);
								e.printStackTrace();
							}
							Bukkit.getPlugin().getServer().getPluginManager().callEvent(new PlayerChangePasswordEvent(
									(Player) info.getCommandSender(), info.getArgs()[0], info.getArgs()[1]));
							info.getCommandSender().sendMessage(Messages.PASSWORD_CHANGED);
						}
					} else {
						info.getCommandSender().sendMessage(Messages.INCORRECT_ACTUAL_PASSWORD);
					}
				} else if (info.getArgs().length > 2) {
					info.getCommandSender().sendMessage(Messages.NEW_PASSWORD_CANNOT_HAVE_SPACES);
				}
			} else {
				info.getCommandSender().sendMessage(Messages.NEED_TO_LOGIN_FIRST);
			}
		} else {
			if (!Bukkit.getStorage().needLogin(info.getCommandSender().getName())) {
				if (info.getArgs().length == 1 || info.getArgs().length == 0) {
					info.getCommandSender().sendMessage(Messages.INVALID_CHANGEPASS_ARGS);
					return;
				} else if (info.getArgs().length == 2) {
					if (info.getArgs()[1].length() > 16) {
						info.getCommandSender().sendMessage(Messages.PASSWORD_SO_BIG);
						return;
					}
					if (Util.decode(ConfigType.DATA.getConfig()
							.getString("AUTH." + info.getCommandSender().getName() + ".PASSWORD")) == null) {
						info.getCommandSender().sendMessage(Messages.CANT_CHANGE_PASSWORD);
						return;
					}
					if (info.getArgs()[0].equals(
							Util.decode(Bukkit.getSQLManager().getPassword(info.getCommandSender().getName())))) {
						if (info.getArgs()[0].equals(info.getArgs()[1])) {
							info.getCommandSender().sendMessage(Messages.SAME_PASSWORD);
						} else {
							Bukkit.getSQLManager().updatePassword(info.getCommandSender().getName(),
									Util.encode(info.getArgs()[1]));
							Bukkit.getPlugin().getServer().getPluginManager().callEvent(new PlayerChangePasswordEvent(
									(Player) info.getCommandSender(), info.getArgs()[0], info.getArgs()[1]));
							info.getCommandSender().sendMessage(Messages.PASSWORD_CHANGED);
						}
					} else {
						info.getCommandSender().sendMessage(Messages.INCORRECT_ACTUAL_PASSWORD);
					}
				} else if (info.getArgs().length > 2) {
					info.getCommandSender().sendMessage(Messages.NEW_PASSWORD_CANNOT_HAVE_SPACES);
				}
			} else {
				info.getCommandSender().sendMessage(Messages.NEED_TO_LOGIN_FIRST);
			}
		}
	}

}
