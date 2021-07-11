package com.github.pocketkid2.ride;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class RideCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Only players can ride mobs
		if (sender instanceof Player) {
			Player p = (Player) sender;

			// Use the world raytracing algorithm to detect entities on our cursor
			RayTraceResult rtr = p.getWorld().rayTraceEntities(p.getEyeLocation().add(p.getLocation().getDirection()),
					p.getLocation().getDirection(), 10.0D);

			// Make sure that we have a valid result
			if (rtr != null && rtr.getHitEntity() != null) {
				// We have a valid result, ride the entity
				rtr.getHitEntity().addPassenger(p);
			} else {
				p.sendMessage("You're not looking at an entity!");
			}
		} else {
			sender.sendMessage("You must be a player!");
		}
		return true;
	}

}
