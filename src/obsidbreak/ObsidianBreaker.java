package obsidbreak;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import obsidbreak.nms.NMS;

/**
 * The main class of ObsidianBreaker
 * 
 * @author oggehej
 */
public class ObsidianBreaker extends JavaPlugin {
	BlockListener blockListener;
	private PlayerListener playerListener;
	private StorageHandler storage;
	private NMS nmsHandler;
	private BukkitTask crackRunner;
	BukkitTask regenRunner;

	/**
	 * To be run on enable
	 */
	public void onEnable() {
		blockListener = new BlockListener(this);
		playerListener = new PlayerListener(this);
		storage = new StorageHandler(this);

		// Initialise NMS class
		setupNMS();

		// Register listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(blockListener, this);
		pm.registerEvents(playerListener, this);
		if(pm.isPluginEnabled("Cannons"))
			pm.registerEvents(new CannonsListener(this), this);

		// Load configuration file
		getConfig().options().copyDefaults(true);
		saveConfig();
		Locale.setupLocale(this);

		// Initialise command
		getCommand("obsidianbreaker").setExecutor(new CommandHandler(this));

		// Schedule runners
		scheduleRegenRunner();
		scheduleCrackCheck();
	}

	/**
	 * To be run on disable
	 */
	public void onDisable() {
		storage = null;
		blockListener = null;
		playerListener = null;
	}

	/**
	 * Get the storage handler of ObsidianBreaker
	 * 
	 * @return Storage handler
	 */
	public StorageHandler getStorage() {
		return storage;
	}

	/**
	 * Return the {@code NMS} handler for the current version
	 * 
	 * @return {@code NMS} handler
	 */
	public NMS getNMS() {
		return this.nmsHandler;
	}

	/**
	 * Set up the NMS functions
	 */
	private void setupNMS() {
		String packageName = this.getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);

		try {
			final Class<?> clazz = Class.forName(getClass().getPackage().getName() + ".nms." + version);
			if (NMS.class.isAssignableFrom(clazz)) {
				getLogger().info("Using NMS version " + version);
				this.nmsHandler = (NMS) clazz.getConstructor().newInstance();
			}
		} catch (final Exception e) {
			getLogger().info("Couldn't find support for " + version +". Block cracks not activated.");
			class Dummy implements NMS {
				@Override
				public void sendCrackEffect(Location location, int damage) {}
				@Override
				public boolean isDummy() {return true;}
			}
			this.nmsHandler = new Dummy();
		}
	}

	/**
	 * Schedule the crack broadcaster
	 */
	void scheduleCrackCheck() {
		if(crackRunner != null) {
			crackRunner.cancel();
			crackRunner = null;
		}

		if(getConfig().getBoolean("BlockCracks.Enabled"))
			crackRunner = new CrackRunnable().runTaskTimerAsynchronously(this, 0, getConfig().getLong("BlockCracks.Interval") * 20);
	}

	/**
	 * Schedule the regen runner
	 */
	void scheduleRegenRunner() {
		if(regenRunner != null) {
			regenRunner.cancel();
			regenRunner = null;
		}

		// Configuration can be set to a negative frequency in order to disable
		long freq = getConfig().getLong("Regen.Frequency") * 20 * 60;
		if(freq > 0) {
			regenRunner = new RegenRunnable().runTaskTimerAsynchronously(this, freq, freq);
		}
	}

	/**
	 * Print a formatted error message
	 * 
	 * @param message Error message
	 * @param e The {@code Exception} object or null if none
	 */
	public void printError(String message, Exception e) {
		String s = "<-- Start -->\n"
				+ "[" + getName() + " v" + getDescription().getVersion() + "] " + message + "\n"
				+ "If you've decided to post this error message, "
				+ "please include everything between the \"Start\" and \"End\" tag PLUS your config.yml and lang.yml\n"
				+ "<-- Stack trace -->\n";
		if(e != null)
			s += ExceptionUtils.getStackTrace(e) + "\n";
		else
			s += "None provided\n";
		s += "<-- End -->";
		Bukkit.getLogger().severe(s);
	}

	@SuppressWarnings("deprecation")
	public static boolean isMatch(Block block, String string) {
		try {
			String[] s = string.split(":");
			if(block.getTypeId() == Integer.parseInt(s[0]) && (s.length == 1 || block.getData() == Byte.parseByte(s[1])))
				return true;
		} catch(Exception e) {}

		return false;
	}

	/**
	 * Will broadcast cracks to all players
	 * 
	 * @author oggehej
	 */
	class CrackRunnable extends BukkitRunnable {
		@Override
		public void run() {
			try {
				for(ConcurrentHashMap<String, BlockStatus> map : getStorage().damage.values()) {
					for(BlockStatus status : map.values()) {
						try {
							Location loc = getStorage().generateLocation(status.getBlockHash());
							if(loc.getChunk().isLoaded())
								getStorage().renderCracks(loc.getBlock());
						} catch(Exception e) {}
					}
				}
			} catch(Exception e) {
				printError("Error occured when broadcasting block cracks. TaskID: " + this.getTaskId(), e);
			}
		}
	}

	/**
	 * Will regenerate blocks when run
	 * 
	 * @author oggehej
	 */
	class RegenRunnable extends BukkitRunnable {
		@Override
		public void run() {
			try {
				for(ConcurrentHashMap<String, BlockStatus> map : storage.damage.values()) {
					for(BlockStatus status : map.values()) {
						if(status.isModified())
							status.setModified(false);
						else {
							status.setDamage(status.getDamage() - (float) getConfig().getDouble("Regen.Amount"));
							if(status.getDamage() < 0.001f)
								getStorage().removeBlockStatus(status);							
						}
					}
				}
			} catch(Exception e) {
				printError("Error occured while trying to regen block (task "+getTaskId()+")", e);
			}
		}
	}
}
