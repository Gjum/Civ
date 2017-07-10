package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.Date;
import java.util.logging.Level;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.Material;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.GameFeaturesConfig;

import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.Hopper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * This is a grab-bag class to hold any _features_ related configurations that impact the
 * game, server-wide. Mostly focused on turning things on or off.
 *
 * It's part of a series of focused hacks.
 *
 * {@link GameFixes} is focused on things that are broken or don't work, and attempts to fix them.
 * {@link GameFeatures} focuses on enabling and disabling features, like elytra, various potion states.
 * {@link GameTuning} neither fixes nor disables, but rather adjusts and reconfigures.
 *
 * Currently you can control the following:
 * - Disable Potato XP
 */
public class GameFeatures extends SimpleHack<GameFeaturesConfig> implements Listener {
	public static final String NAME = "GameFeatures";

	public GameFeatures(SimpleAdminHacks plugin, GameFeaturesConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if (config != null && config.isEnabled()) {
			plugin().log("Registering GameFeatures listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void unregisterListeners() {
		// Bukkit does this for us.
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataBootstrap() {
	}

	@Override
	public void dataCleanup() {
		// NO-OP
	}

	@Override
	public String status() {
		StringBuilder genStatus = new StringBuilder();
		genStatus.append("GameFeatures is ");
		if (config != null && config.isEnabled()) {
			genStatus.append("active\n");
			if (config.isPotatoXPEnabled()) {
				genStatus.append("  Potato XP is enabled\n");
			} else {
				genStatus.append("  Potato XP is disabled\n");
			}

			genStatus.append("  Villager Trading is ");
			if (config.isVillagerTrading()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Wither Spawning is ");
			if (config.isWitherSpawning()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Ender Chest placement is ");
			if (config.isEnderChestPlacement()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Ender Chest use is ");
			if (config.isEnderChestUse()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}
			
			genStatus.append("  Shulker Box use is ");
			if (config.isShulkerBoxUse()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}
			
			genStatus.append("  Totem of Undying effects are ");
			if (config.isTotemPowers()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}
			
			genStatus.append("  Elytra use is ");
			if (config.isElytraUse()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Chorus Fruit teleportation is ");
			if (config.isChorusFruitTeleportation()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  WeepAngel is ");
			if (config.isWeepingAngel()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}
			
			genStatus.append("  Block water in HELL biomes is ");
			if (config.isBlockWaterInHell()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			// more?
		} else {
			genStatus.append("inactive");
		}
		return genStatus.toString();
	}

	public static GameFeaturesConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new GameFeaturesConfig(plugin, config);
	}

	/* From here on, the actual meat of the hack. Above is basically boilerplate for micro-plugins.*/

	/**
	 * Perhaps eventually generalize?
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void potatoXP(FurnaceExtractEvent event) {
		if (!config.isEnabled() || config.isPotatoXPEnabled()) return;
		try {
			Material mat = event.getItemType();
			if (mat == null) return;

			if (Material.BAKED_POTATO.equals(mat)) {
				event.setExpToDrop(0);
			}
		} catch (Exception e) {
			plugin().log(Level.WARNING, "Failed to stop potato XP", e);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void disableVillagerTrading(PlayerInteractEntityEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isVillagerTrading()) {
			Entity npc = event.getRightClicked();

			if (npc != null) {
				if (npc.getType().equals(EntityType.VILLAGER)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void disableWitherSpawning(CreatureSpawnEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isWitherSpawning()) {
			if (event.getEntityType().equals(EntityType.WITHER)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void disableEnderChestPlacement(BlockPlaceEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isEnderChestPlacement()) {
			if (event.getBlock().getType().equals(Material.ENDER_CHEST)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) 
	public void disableEnderChestUse(PlayerInteractEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isEnderChestUse()) {
			Action action = event.getAction();
			Material material = event.getClickedBlock().getType();
			boolean ender_chest = action == Action.RIGHT_CLICK_BLOCK &&
					material.equals(Material.ENDER_CHEST);
			if (ender_chest) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void disableShulkerBoxUse(InventoryOpenEvent event){
		if (!config.isEnabled()) return;
		if (!config.isShulkerBoxUse() && InventoryType.SHULKER_BOX.equals(event.getInventory().getType())) {
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void disabledShulkerBoxHoppering(InventoryMoveItemEvent event) {
		if (!config.isEnabled() || config.isShulkerBoxUse()) return;
		
		if ((event.getDestination() == null) || (event.getSource() == null)) return;
		if (InventoryType.SHULKER_BOX.equals(event.getDestination().getType()) ||
				InventoryType.SHULKER_BOX.equals(event.getSource().getType())) {
			event.setCancelled(true);
		}

	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void disableTotemPowers(EntityResurrectEvent event) {
		if (!config.isEnabled() || config.isTotemPowers()) return;
		
		if (EntityType.PLAYER.equals(event.getEntityType())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void disableChorusFruitTeleportation(PlayerTeleportEvent event) {
		if (!config.isEnabled() || config.isChorusFruitTeleportation()) return;
		
		if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void disableElytraUse(EntityToggleGlideEvent event) {
		if (!config.isEnabled() || config.isElytraUse()) return;
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void weepingAngelListener(PlayerDeathEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isWeepingAngel()) {
			return;
		}

		if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent evt = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause());
			LivingEntity killer = null;

			if (evt.getDamager() instanceof LivingEntity) {
				killer = (LivingEntity) evt.getDamager();
			} else if (evt.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) evt.getDamager();

				if (projectile.getShooter() instanceof LivingEntity) {
					killer = (LivingEntity) projectile.getShooter();
				}
			}

			if (killer != null) {
				if (killer instanceof Player) {
					banPlayer(event.getEntity().getPlayer(), config.getWeepingAngelPlayer());
				} else {
					banPlayer(event.getEntity().getPlayer(), config.getWeepingAngelEnv());
				}
			}
		} else {
			banPlayer(event.getEntity().getPlayer(), config.getWeepingAngelEnv());
		}
	}

	private void banPlayer(Player p, int minutes) {
		if (!config.isWeepingAngel()) {
			return;
		}

		Date exp = DateUtils.addMinutes(new Date(), minutes);
		Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), "You've been banned for " + minutes +
				" minutes due to your death.", exp, "weepingAngel");

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SimpleAdminHacks.instance(), new Runnable() {
			public void run() {
				p.kickPlayer("You've been banned for " + minutes + " minutes due to your death.");
			}
		}, 2L);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e) {
		if (config.isEnabled() && config.isBlockWaterInHell()) {
			if ((e.getBlockClicked().getBiome() == Biome.HELL) && (e.getBucket() == Material.WATER_BUCKET)) {
				e.setCancelled(true);
				e.getItemStack().setType(Material.BUCKET);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDispenseEvent(BlockDispenseEvent event) {
		if (config.isEnabled() && config.isBlockWaterInHell()) {
			if (event.getBlock().getType() == Material.DISPENSER) {
				Dispenser disp = (Dispenser) event.getBlock().getState().getData();
				Biome biome = event.getBlock().getRelative(disp.getFacing()).getBiome();

				if (Biome.HELL.equals(biome) && event.getItem() != null && event.getItem().getType().equals(Material.WATER_BUCKET)) {
					event.setItem(new ItemStack(Material.BUCKET, event.getItem().getAmount()));
					event.setCancelled(true);
				}

			}
		}
	}
}
