package cz.dubcat.emica;

import cz.dubcat.emica.events.OnEnable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

public class Emica extends JavaPlugin {
	private static IDiscordClient bot;
	public static Logger log;
	private static Emica plugin;
	private final ScheduledExecutorService scheduler;
	private static long SERVER_ID;
	private static long VOICE_CHANNEL_ID;
	private static long TEXT_CHANNEL_ID = 0;
	public static String COMMAND = "!emica";

	private static List<String> PLAY_LIST;

	public Emica() {
		this.scheduler = Executors.newScheduledThreadPool(1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		SERVER_ID = getConfig().getLong("server_id");
		VOICE_CHANNEL_ID = getConfig().getLong("voice_channel_id");
		TEXT_CHANNEL_ID = getConfig().getLong("settings.command.text_channel_id");
		
		List<String> tempPlayList = (List<String>) getConfig().getList("playlist");
		
		if (getConfig().getBoolean("settings.shuffle_music_onload")) {
			Collections.shuffle(tempPlayList);
			
			getLogger().info("Playlist was shuffled.");
		}
		
		PLAY_LIST = tempPlayList;
		
		COMMAND = getConfig().getString("settings.command.prefix");

		plugin = this;
		log = getLogger();
		bot = createClient(getConfig().getString("bot_token"), true);
		registerEvents();
		getLogger().info("We are online and rolling.");
	}

	@Override
	public void onDisable() {
		bot.logout();
		getLogger().info("Disabled.");
	}

	public static Emica getPlugin() {
		return plugin;
	}

	private static void registerEvents() {
		EventDispatcher dispatcher = bot.getDispatcher();
		dispatcher.registerListener(new OnEnable());
	}

	public ScheduledExecutorService getScheduler() {
		return this.scheduler;
	}

	private static IDiscordClient createClient(String token, boolean login) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			if (login) {
				return clientBuilder.login();
			}
			return clientBuilder.build();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Logger getLog() {
		return log;
	}

	public static IDiscordClient getBot() {
		return bot;
	}

	public static List<String> getPlayList() {
		return PLAY_LIST;
	}

	public static long getServerID() {
		return SERVER_ID;
	}

	public static long getVoiceChannelID() {
		return VOICE_CHANNEL_ID;
	}

	public static long getTextChannelID() {
		return TEXT_CHANNEL_ID;
	}
}
