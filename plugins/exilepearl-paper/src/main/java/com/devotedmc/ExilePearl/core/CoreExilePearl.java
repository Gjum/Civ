package com.devotedmc.ExilePearl.core;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PearlPlayerProvider;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.LocationHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.HolderVerifyResult;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.storage.PearlUpdateStorage;
import com.devotedmc.ExilePearl.util.Guard;
import com.devotedmc.ExilePearl.util.PearlLoreUtil;

/**
 * Instance of a player who is imprisoned in an exile pearl
 * @author Gordon
 */
class CoreExilePearl implements ExilePearl {
	private static final int HOLDER_COUNT = 5;
	private static String ITEM_NAME = "Exile Pearl";

	// The player provider instance
	private final PearlPlayerProvider playerProvider;
	
	// The logging instance
	private final PearlLogger logger;
	
	// The storage instance
	private final PearlUpdateStorage storage;
	
	// The ID of the exiled player
	private final UUID playerId;
	
	// The ID of the player who killed the exiled player
	private final UUID killedBy;
	
	private PearlPlayer player;
	private PearlHolder holder;
	private Date pearledOn;
	private LinkedBlockingQueue<PearlHolder> holders;
	private long lastMoved;
	private boolean freedOffline;
	private double health;

	/**
	 * Creates a new prison pearl instance
	 * @param playerId The pearled player id
	 * @param holder The holder instance
	 */
	public CoreExilePearl(final PearlPlayerProvider playerProvider, final PearlLogger logger, final PearlUpdateStorage storage, 
			final UUID playerId, final UUID killedBy, final PearlHolder holder, final double health) {
		Guard.ArgumentNotNull(playerProvider, "playerProvider");
		Guard.ArgumentNotNull(logger, "logger");
		Guard.ArgumentNotNull(storage, "storage");
		Guard.ArgumentNotNull(playerId, "playerId");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		Guard.ArgumentNotNull(holder, "holder");
		
		this.playerProvider = playerProvider;
		this.logger = logger;
		this.storage = storage;
		this.playerId = playerId;
		this.killedBy = killedBy;
		this.pearledOn = new Date();
		this.holders = new LinkedBlockingQueue<PearlHolder>();
		this.lastMoved = pearledOn.getTime();
		this.holder = holder;
		this.holders.add(holder);
		this.health = health;
	}


	/**
	 * Gets the imprisoned player ID
	 * @return The player ID
	 */
	@Override
	public UUID getUniqueId() {
		return playerId;
	}


	/**
	 * Gets the imprisoned player
	 * @return The player instance
	 */
	@Override
	public PearlPlayer getPlayer() {
		if (player == null) {
			player = playerProvider.getPearlPlayer(playerId);
		}
		return player;
	}


	/**
	 * Gets when the player was pearled
	 * @return The time the player was pearled
	 */
	@Override
	public Date getPearledOn() {
		return this.pearledOn;
	}


	/**
	 * Sets when the player was pearled
	 * @param pearledOn The time the player was pearled
	 */
	@Override
	public void setPearledOn(Date pearledOn) {
		this.pearledOn = pearledOn;
	}


	/**
	 * Gets the imprisoned name
	 * @return The player name
	 */
	@Override
	public String getPlayerName() {
		return this.getPlayer().getName();
	}


	/**
	 * Gets the pearl holder
	 * @return The pearl holder
	 */
	@Override
	public PearlHolder getHolder() {
		return this.holder;
	}


	/**
	 * Sets the pearl holder to a player
	 * @param player The new pearl holder
	 */
	@Override
	public void setHolder(PearlPlayer player) {
		Guard.ArgumentNotNull(player, "player");
		setHolderInternal(new PlayerHolder(player.getBukkitPlayer()));
	}


	/**
	 * Sets the pearl holder to a block
	 * @param block The new pearl block
	 */
	@Override
	public void setHolder(Block block) {
		Guard.ArgumentNotNull(block, "block");
		setHolderInternal(new BlockHolder(block));
	}


	/**
	 * Sets the pearl holder to a location
	 * @param location The new pearl location
	 */
	@Override
	public void setHolder(Location location) {
		Guard.ArgumentNotNull(location, "location");
		setHolderInternal(new LocationHolder(location));
	}
	
	
	/**
	 * Internal method for updating the holder
	 * @param holder The new holder instance
	 */
	private void setHolderInternal(PearlHolder holder) {
		this.holder = holder;
		this.holders.add(holder);

		if (holders.size() > HOLDER_COUNT) {
			holders.poll();
		}

		storage.pearlUpdateLocation(this);
	}

    
    /**
     * Gets the pearl seal strength
     * @return The strength value
     */
	@Override
    public double getHealth() {
    	return this.health;
    }
    
    
    /**
     * Sets the pearl seal strength
     * @param The strength value
     */
	@Override
    public void setHealth(double health) {
    	if (health < 0) {
    		health = 0;
    	}
    	
    	this.health = health;
    	storage.pearlUpdateHealth(this);
    }

	/**
	 * Gets the pearl location
	 */
	@Override
	public Location getLocation() {
		return this.holders.peek().getLocation();
	}


	@Override
	public String getItemName() {
		return ITEM_NAME;
	}


	@Override
	public String getKilledByName() {
		return playerProvider.getPearlPlayer(killedBy).getName();
	}


	/**
	 * Gets the name of the current location
	 * @return The string of the current location
	 */
	@Override
	public String getLocationDescription() {
		final Location loc = holder.getLocation();
		final Vector vec = loc.toVector();
		final String str = loc.getWorld().getName() + " " + vec.getBlockX() + " " + vec.getBlockY() + " " + vec.getBlockZ();
		return "held by " + holder.getName() + " at " + str;
	}


	/**
	 * Marks when the pearl was moved last
	 */
	@Override
	public void updateLastMoved() {
		this.lastMoved = System.currentTimeMillis();
	}


	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	@Override
	public boolean getFreedOffline() {
		return this.freedOffline;
	}

	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	@Override
	public void setFreedOffline(boolean freedOffline) {
		this.freedOffline = freedOffline;
	}


	/**
	 * Creates an item stack for the pearl
	 * @return The new item stack
	 */
	@Override
	public ItemStack createItemStack() {
		List<String> lore = PearlLoreUtil.generateLore(this);
		ItemStack is = new ItemStack(Material.ENDER_PEARL, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(this.getPlayerName());
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}


	/**
	 * Validates that an item stack is the prison pearl
	 * @param is The item stack
	 * @return true if it checks out
	 */
	public boolean validateItemStack(ItemStack is) {

		UUID id = PearlLoreUtil.getIDFromItemStack(is);
		if (id != null && id.equals(this.playerId)) {

			// re-create the item stack to update the values
			ItemMeta im = is.getItemMeta();
			im.setLore(PearlLoreUtil.generateLore(this));
			is.setItemMeta(im);
			return true;
		}

		return false;
	}


	/**
	 * Verifies the pearl location
	 * @return
	 */
	public boolean verifyLocation() {
		StringBuilder sb = new StringBuilder();

		StringBuilder verifier_log = new StringBuilder();
		StringBuilder failure_reason_log = new StringBuilder();

		for (final PearlHolder holder : this.holders) {
			HolderVerifyResult reason = this.verifyHolder(holder, verifier_log);
			if (reason.isValid()) {
				sb.append(String.format("PP (%s, %s) passed verification for reason '%s': %s",
						playerId.toString(), this.getPlayerName(), reason.toString(), verifier_log.toString()));
				logger.log(sb.toString());

				return true;
			} else {
				failure_reason_log.append(reason.toString()).append(", ");
			}
			verifier_log.append(", ");
		}
		sb.append(String.format("PP (%s, %s) failed verification for reason %s: %s",
				playerId.toString(), this.getPlayerName(), failure_reason_log.toString(), verifier_log.toString()));

		logger.log(sb.toString());
		return false;
	}


	/**
	 * Verifies the holder of a pearl
	 * @param holder The holder to check
	 * @param feedback The feedback string
	 * @return true if the pearl was found in a valid location
	 */
	private HolderVerifyResult verifyHolder(PearlHolder holder, StringBuilder feedback) {

		if (System.currentTimeMillis() - this.lastMoved < 2000) {
			// The pearl was recently moved. Due to a race condition, this exists to
			//  prevent players from spamming /ppl to get free when a pearl is moved.
			return HolderVerifyResult.TIME;
		}

		return holder.validate(this, feedback);
	}


	/**
	 * Gets the item stack from an inventory if it exists
	 * @param inv The inventory to search
	 * @return The pearl item
	 */
	public ItemStack getItemFromInventory(Inventory inv) {

		for (ItemStack item : inv.all(Material.ENDER_PEARL).values()) {
			if (this.validateItemStack(item)) {
				return item;
			}
		}

		return null;
	}
}
