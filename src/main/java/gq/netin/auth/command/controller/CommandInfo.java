package gq.netin.auth.command.controller;

import org.bukkit.command.CommandSender;

/**
 *
 * @author netindev
 *
 */
public interface CommandInfo {

	public CommandSender getCommandSender();

	public boolean isPlayer();

	public String[] getArgs();

}
