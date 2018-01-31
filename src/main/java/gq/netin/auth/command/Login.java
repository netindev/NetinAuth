package gq.netin.auth.command;

import org.bukkit.entity.Player;

import gq.netin.auth.Bukkit;
import gq.netin.auth.check.Check;
import gq.netin.auth.command.controller.CommandFactory;
import gq.netin.auth.command.controller.CommandInfo;
import gq.netin.auth.event.bukkit.PlayerLoginEvent;
import gq.netin.auth.exception.InvalidCheckException;
import gq.netin.auth.util.Config.ConfigType;
import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Util;

/**
 *
 * @author netindev
 *
 */
public class Login extends CommandFactory {

	public Login() {
		super("login");
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
						.get("AUTH." + info.getCommandSender().getName() + ".PASSWORD") != null) {
					if (info.getArgs().length == 0) {
						info.getCommandSender().sendMessage(Messages.PLEASE_LOGIN);
					} else if (info.getArgs().length == 1) {
						if (info.getArgs()[0].equals(Util.decode(ConfigType.DATA.getConfig()
								.getString("AUTH." + info.getCommandSender().getName() + ".PASSWORD")))) {
							Bukkit.getPlugin().getServer().getPluginManager().callEvent(
									new PlayerLoginEvent((Player) info.getCommandSender(), info.getArgs()[0]));
							Bukkit.getStorage().removeNeedLogin(info.getCommandSender().getName());
							info.getCommandSender().sendMessage(Messages.SUCCESSFUL_LOGIN);
						} else {
							info.getCommandSender().sendMessage(Messages.INCORRECT_PASSWORD);
						}
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
				if (Bukkit.getSQLManager().hasOnDatabase(info.getCommandSender().getName())) {
					if (info.getArgs().length == 0) {
						info.getCommandSender().sendMessage(Messages.PLEASE_LOGIN);
					} else if (info.getArgs().length == 1) {
						if (Util.decode(Bukkit.getSQLManager().getPassword(info.getCommandSender().getName()))
								.equals(info.getArgs()[0])) {
							Bukkit.getPlugin().getServer().getPluginManager().callEvent(
									new PlayerLoginEvent((Player) info.getCommandSender(), info.getArgs()[0]));
							Bukkit.getStorage().removeNeedLogin(info.getCommandSender().getName());
							info.getCommandSender().sendMessage(Messages.SUCCESSFUL_LOGIN);
						} else {
							info.getCommandSender().sendMessage(Messages.INCORRECT_PASSWORD);
						}
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
