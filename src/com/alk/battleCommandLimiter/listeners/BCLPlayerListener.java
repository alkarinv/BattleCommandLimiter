package com.alk.battleCommandLimiter.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.alk.battleCommandLimiter.Defaults;
import com.alk.battleCommandLimiter.controllers.CommandController;
import com.alk.battleCommandLimiter.controllers.MC;
import com.alk.battleCommandLimiter.objects.ShouldLimitObject;
import com.alk.battleCommandLimiter.objects.ShouldLimitObject.ShouldLimit;

/**
 * 
 * @author alkarin
 *
 */
public class BCLPlayerListener implements Listener  {
	CommandController cc;
	public HashMap<Player,Long> timeLimit = new HashMap<Player,Long>();

	public BCLPlayerListener(CommandController cc){
		this.cc = cc;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
//		if (event.isCancelled()){
//		System.out.println("o111msg =" + event.getMessage() + " payPrefix="+Defaults.PAY_PREFIX);
		if (event.isCancelled() || event.getPlayer().isOp()){
			return;}
//		System.out.println("omsg =" + event.getMessage() + " payPrefix="+Defaults.PAY_PREFIX);
		final String cmds[] = CommandController.convert(event.getMessage());
		if (!cc.hasCommand(cmds[0]))
			return;
		final Player p = event.getPlayer();
		
		/// First check to see if they are typing commnads too quickly
		Long t = timeLimit.get(p);
		final Long now = System.currentTimeMillis();
		if (t != null &&  now - t < Defaults.TIME_BETWEEN_COMMANDS){
			event.setCancelled(true);
			return;}
		timeLimit.put(p, now);
		
		/// Now find out if we need to limit this command
//		System.out.println("cmd=" + cmds[0] + " payPrefix="+Defaults.PAY_PREFIX);
		final boolean pay = cmds[0].startsWith(Defaults.PAY_PREFIX);
		ShouldLimitObject slo = cc.shouldLimit(p, cmds,pay);
//		System.out.println("cmd=" + cmds[0] + " payPrefix="+Defaults.PAY_PREFIX +"   slo=" + slo);

		if (slo == null || slo.limit == ShouldLimit.NO){
//			System.out.println("not limiting = " + cmds[0] +"   pay=" + slo.isPay +"   converted = " + slo.lc.convert(cmds,pay));
			cc.payRule(slo, p);
			if (slo != null && slo.isPay){ /// remove the "pay" prefix from the command and resend it
				event.setCancelled(true);	
				Bukkit.getServer().dispatchCommand(p, slo.lc.convert(cmds,pay));
				return;
			} else { /// Dont limit.. just return out
				return;
			}
		}
		event.setCancelled(true);	
		if (slo.limit == ShouldLimit.YES){
//			System.out.println("limiting = " + cmd);
			MC.sendMessage(p, slo.getLimitMsg());
		} else if (slo.limit == ShouldLimit.WARN){
//			System.out.println("warn = " + cmd);
			MC.sendMessage(p, slo.getLimitMsg());
		}
		return;
		
		
//		boolean logall = ConfigController.getBoolean("logall");
//		System.out.println("index = " + index + " msg=" + msg);

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player p = event.getPlayer();
		if (p != null)
			timeLimit.remove(p);
	}
}
