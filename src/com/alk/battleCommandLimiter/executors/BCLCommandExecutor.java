package com.alk.battleCommandLimiter.executors;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alk.battleCommandLimiter.BattleCommandLimiter;
import com.alk.battleCommandLimiter.controllers.MC;


/**
 * 
 * @author alkarin
 *
 */
public class BCLCommandExecutor implements CommandExecutor  {


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player p = null;
		if (sender instanceof Player) {p = (Player) sender;}
		String commandStr = cmd.getName().toLowerCase();
		for (String arg: args){
			if (!arg.matches("[a-zA-Z0-9_\\-/:,]*")) {
				sendMessage(sender, "arguments can be only alphanumeric with underscores");
				return true;
			}
		}
		if (commandStr.equalsIgnoreCase("commandlimiter")){
			if (args.length > 0 && args[0].equalsIgnoreCase("reload")){
				return reloadConfig(sender, p,args);
			}
		}
		return true;
	}	

	private boolean reloadConfig(CommandSender sender, Player p, String[] args) {
		if (p != null && !p.isOp())
			return true;
		BattleCommandLimiter.getSelf().loadAllConfig();
		sendMessage(sender,"&eCommandLimiter config reloaded");
		return true;
	}

	public static boolean sendMessage(CommandSender sender, String msg){
		return MC.sendMessage(sender,msg);
	}

	public static Player findPlayer(String name) {
		Server server =Bukkit.getServer();
		Player lastPlayer = server.getPlayer(name);
		if (lastPlayer != null) 
			return lastPlayer;

        Player[] online = server.getOnlinePlayers();
        for (Player player : online) {
            final String playerName = player.getName();
            if (playerName.equalsIgnoreCase(name)) {
                lastPlayer = player;
                break;
            }
            if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
                if (lastPlayer != null) {
                    return null;}
                lastPlayer = player;
            }
        }

        return lastPlayer;
	}


}
