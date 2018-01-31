package gq.netin.auth.command;

import java.io.IOException;

import org.bukkit.entity.Player;

import gq.netin.auth.Bukkit;
import gq.netin.auth.check.Check;
import gq.netin.auth.command.controller.CommandFactory;
import gq.netin.auth.command.controller.CommandInfo;
import gq.netin.auth.database.SQLManager.Status;
import gq.netin.auth.event.bukkit.PlayerRegisterEvent;
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
public class Register extends CommandFactory {

	public Register() {
		super("register");
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
			if (Bukkit.getStorage().needLogin(info.getCommandSender().getName())) {
				if (ConfigType.DATA.getConfig()
						.get("AUTH." + info.getCommandSender().getName() + ".PASSWORD") == null) {
					if (info.getArgs().length == 0) {
						info.getCommandSender().sendMessage(Messages.PLEASE_REGISTER);
					} else if (info.getArgs().length == 1) {
						if (info.getArgs().length > 16) {
							info.getCommandSender().sendMessage(Messages.PASSWORD_SO_BIG);
							return;
						}
						ConfigType.DATA.getConfig().set("AUTH." + info.getCommandSender().getName() + ".PASSWORD",
								new String(Util.encode(info.getArgs()[0])));
						try {
							ConfigType.DATA.getConfig().save(Config.getPaths()[2]);
						} catch (IOException e) {
							Util.severe(Messages.IMPOSSIBLE_TO_SAVE_CONFIG);
							e.printStackTrace();
						}
						Bukkit.getPlugin().getServer().getPluginManager().callEvent(
								new PlayerRegisterEvent((Player) info.getCommandSender(), info.getArgs()[0]));
						Bukkit.getStorage().removeNeedLogin(info.getCommandSender().getName());
						info.getCommandSender().sendMessage(Messages.SUCCESSFUL_REGISTER);
					} else if (info.getArgs().length > 1) {
						info.getCommandSender().sendMessage(Messages.INCORRECT_ARGS);
					}
				} else {
					info.getCommandSender().sendMessage(Messages.ALREADY_REGISTERED);
				}
			} else {
				info.getCommandSender().sendMessage(Messages.ALREADY_LOGGED_IN);
			}
		} else {
			if (Bukkit.getStorage().needLogin(info.getCommandSender().getName())) {
				if (!Bukkit.getSQLManager().hasOnDatabase(info.getCommandSender().getName())) {
					if (info.getArgs().length == 0) {
						info.getCommandSender().sendMessage(Messages.PLEASE_REGISTER);
					} else if (info.getArgs().length == 1) {
						if (info.getArgs().length > 16) {
							info.getCommandSender().sendMessage(Messages.PASSWORD_SO_BIG);
							return;
						}
						Bukkit.getSQLManager().setPasswordAndStatus(info.getCommandSender().getName(), Status.CRACKED,
								new String(Util.encode(info.getArgs()[0])));
						Bukkit.getPlugin().getServer().getPluginManager().callEvent(
								new PlayerRegisterEvent((Player) info.getCommandSender(), info.getArgs()[0]));
						Bukkit.getStorage().removeNeedLogin(info.getCommandSender().getName());
						info.getCommandSender().sendMessage(Messages.SUCCESSFUL_REGISTER);
					} else if (info.getArgs().length > 1) {
						info.getCommandSender().sendMessage(Messages.INCORRECT_ARGS);
					}
				} else {
					info.getCommandSender().sendMessage(Messages.ALREADY_REGISTERED);
				}
			} else {
				info.getCommandSender().sendMessage(Messages.ALREADY_LOGGED_IN);
			}
		}
	}

}
