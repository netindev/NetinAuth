package gq.netin.auth.command.controller;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import gq.netin.auth.Bukkit;
import gq.netin.auth.command.ChangePassword;
import gq.netin.auth.command.Login;
import gq.netin.auth.command.Register;

/**
 *
 * @author netindev
 *
 */
public abstract class CommandFactory implements CommandExecutor {

	private static final HashMap<String, CommandExecutor> COMMANDS_TO_REGISTER;

	public CommandFactory() {
	}

	public CommandFactory(String commandName) {
		CommandFactory.COMMANDS_TO_REGISTER.put(commandName, this);
	}

	public static void loadCommands() {
		Class<?>[] classesToLoad = { ChangePassword.class, Login.class, Register.class };
		for (Class<?> classesIteration : classesToLoad) {
			try {
				classesIteration.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		for (String loadCommands : CommandFactory.COMMANDS_TO_REGISTER.keySet()) {
			JavaPlugin.getPlugin(Bukkit.class).getCommand(loadCommands)
					.setExecutor(CommandFactory.COMMANDS_TO_REGISTER.get(loadCommands));
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.onCommand(new CommandInfo() {

			@Override
			public boolean isPlayer() {
				return sender instanceof Player;
			}

			@Override
			public CommandSender getCommandSender() {
				return sender;
			}

			@Override
			public String[] getArgs() {
				return args;
			}

		});
		return false;
	}

	abstract protected void onCommand(CommandInfo info);

	static {
		COMMANDS_TO_REGISTER = new HashMap<>();
	}

}
