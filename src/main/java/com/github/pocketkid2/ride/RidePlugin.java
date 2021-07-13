package com.github.pocketkid2.ride;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class RidePlugin extends JavaPlugin {

	private ProtocolManager pm;

	@Override
	public void onEnable() {
		// Initialize ProtocolLib
		pm = ProtocolLibrary.getProtocolManager();
		pm.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {

			//
			// Steer Vehicle packet gets called when the player is riding a vehicle and
			// presses WASD or some other keys like spacebar.
			//
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {

					// Grab the necessary objects
					PacketContainer pc = event.getPacket();
					Player player = event.getPlayer();
					Entity vehicle = player.getVehicle();

					// First float in packet is the Left/Right value (A/D)
					float side = pc.getFloat().read(0);

					// Second float in packet is the Forward/Backward value (W/S keys)
					float forw = pc.getFloat().read(1);

					// First byte bitmask (retrieved through boolean) is whether the player is
					// jumping (pressing spacebar)
					boolean jump = pc.getBooleans().read(0);

					if (jump && vehicle.isOnGround()) {
						// Initial jump velocity is 0.5, which allows them to jump over 1 block but not
						// 1.5 or 2 (close to default, can be modified)
						vehicle.setVelocity(vehicle.getVelocity().add(new Vector(0.0, 0.5, 0.0)));
					}

					// Now, calculate the new velocity using the function below, and apply to the
					// vehicle entity
					Vector vel = RidePlugin.getVelocityVector(vehicle.getVelocity(), player, side, forw);
					vehicle.setVelocity(vel);
				}
			}
		});
		getCommand("ride").setExecutor(new RideCommand());
		getLogger().info("Enabled!");
	}

	private static Vector getVelocityVector(Vector vector, Player player, float side, float forw) {
		// First, kill horizontal velocity that the entity might already have
		vector.setX(0.0);
		vector.setZ(0.0);

		//
		// Many tests were run to get the math seen below.
		//

		// Create a new vector representing the direction of WASD
		Vector mot = new Vector(forw * -1.0, 0, side);

		if (mot.length() > 0.0) {
			// Turn to face the direction the player is facing
			mot.rotateAroundY(Math.toRadians(player.getLocation().getYaw() * -1.0F + 90.0F));
			// Now bring it back to a reasonable speed (0.2, reasonable default speed, can
			// be configured)
			mot.normalize().multiply(0.25F);
		}

		// Now, take this new horizontal direction velocity, and add it to what we
		// already have (which will only be vertical velocity at this point.)
		// We need to preserve vertical velocity so we handle gravity properly.
		return mot.add(vector);
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled!");
	}
}
