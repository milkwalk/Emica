package cz.dubcat.emica.events;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import cz.dubcat.emica.Emica;
import cz.dubcat.emica.constructors.EmblemTask;
import cz.dubcat.emica.constructors.ServerInformation;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.PermissionUtils;

public class OnEnable {
	static AudioPlayerManager playerManager;
	public static ServerInformation info;
	
	@EventSubscriber
	public void onReady(ReadyEvent e) {
		IGuild guild = Emica.getBot().getGuildByID(Emica.getServerID());
		playerManager = new DefaultAudioPlayerManager();

		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);

		info = new ServerInformation(playerManager);
		info.player.setVolume(Emica.getPlugin().getConfig().getInt("settings.default_player_volume"));
		info.scheduler.setLoopEnabled(true);
		guild.getAudioManager().setAudioProvider(info.getAudioProvider());

		joinVoiceChannel();
		loadSongs();	
		Emica.getScheduler().scheduleAtFixedRate(new EmblemTask(), 1000, 5000, TimeUnit.MILLISECONDS);
		Emica.log.info("Connected and loaded songs.");
	}

	@EventSubscriber
	public void onReady(MessageReceivedEvent e) {
		IGuild guild = e.getGuild();
		IChannel channel = e.getChannel();
		
		String[] args = e.getMessage().getContent().split(" ");
		if (e.getMessage().getContent().startsWith(Emica.COMMAND)) {
			
			if ((Emica.getTextChannelID() != 0) && (channel.getLongID() != Emica.getTextChannelID())) {
				return;
			}
			
			if (args.length == 1 && info.player.getPlayingTrack() != null) {
				AudioTrack track = info.player.getPlayingTrack();
				EmbedBuilder embed = new EmbedBuilder();

				embed.withTitle(track.getInfo().title);
				embed.withDescription("**Track link** " + track.getInfo().uri);
				embed.appendField("Length", convertSecondsToString((int) (track.getDuration() / 1000L)), true);
				embed.appendField("Remaining",
						convertSecondsToString((int) ((track.getDuration() - track.getPosition()) / 1000L)), true);
				embed.appendField("State", track.getState().name(), true);

				channel.sendMessage(embed.build());
			} else if(args.length == 2 && args[1].equalsIgnoreCase("skip")) {
				IVoiceChannel vc = guild.getVoiceChannelByID(Emica.getVoiceChannelID());
				int usersNumber = vc.getConnectedUsers().size()-1;
				if(e.getAuthor().getVoiceStateForGuild(guild).getChannel().getLongID() == Emica.getVoiceChannelID() && usersNumber == 1) {
					
					AudioTrack track = info.player.getPlayingTrack();
					EmbedBuilder embed = new EmbedBuilder();
					
					embed.withColor(Color.CYAN);
					embed.withTitle("Skipped "+track.getInfo().title);
					embed.withDescription("Track has been skipped");
					
					channel.sendMessage(embed.build());
					
					info.scheduler.nextTrack(info.player.getPlayingTrack());
				}
			} else if(args.length == 2 && args[1].equalsIgnoreCase("playlist") && PermissionUtils.hasPermissions(guild, e.getAuthor(), Permissions.ADMINISTRATOR)) {
				EmbedBuilder embed = new EmbedBuilder();
				
				embed.withColor(Color.GREEN);
				int i = 1;
				long totalTime = 0;
				embed.withDescription("**Playlist:** \n");
				long delay = 0;
				
				embed.appendDesc("\n:arrow_forward: **PLAYING** [" + info.player.getPlayingTrack().getInfo().title + "]("+info.player.getPlayingTrack().getInfo().uri+")\n");
				for(AudioTrack track : info.scheduler.getQueue()) {
					
					if(embed.getTotalVisibleCharacters() >= EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - 100) {
						EmbedObject embedToSend = embed.build();
						embed.clearFields();
						embed.withDesc("");
						Emica.getScheduler().schedule(new Runnable() {
							@Override
							public void run() {
								channel.sendMessage(embedToSend);
							}
							
						}, delay, TimeUnit.MILLISECONDS);
						delay += 1000;
					}
						
					embed.appendDesc("\n**"+i + ".** " + track.getInfo().title);
					i++;
					totalTime += track.getDuration();
				}
				
				long finalTime = totalTime;
				Emica.getScheduler().schedule(new Runnable() {
					@Override
					public void run() {
						embed.appendDesc("\n\n**Total playtime:** " + convertSecondsToString((int)(finalTime/1000)));
						channel.sendMessage(embed.build());
					}
					
				}, delay, TimeUnit.MILLISECONDS);
			}
		}
	}

	public static String convertSecondsToString(int seconds) {
		int day = (int) TimeUnit.SECONDS.toDays(seconds);
		long hours = TimeUnit.SECONDS.toHours(seconds) - day * 24;
		long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60L;
		long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60L;

		StringBuilder sb = new StringBuilder();
		if (day > 0) {
			sb.append(day + "d ");
		}
		if (hours > 0L) {
			sb.append(hours + "h ");
		}
		if (minute > 0L) {
			sb.append(minute + "m ");
		}
		if (second > 0L) {
			sb.append(second + "s");
		}
		return sb.toString();
	}

	private void joinVoiceChannel() {
		IVoiceChannel chillstepChannel = Emica.getBot().getGuildByID(Emica.getServerID())
				.getVoiceChannelByID(Emica.getVoiceChannelID());
		chillstepChannel.join();
	}

	private void loadSongs(){
    for (final String url : Emica.getPlayList()) {
      playerManager.loadItemOrdered(info, url, new AudioLoadResultHandler(){
        public void trackLoaded(AudioTrack track){
          OnEnable.info.scheduler.queue(track);
          Emica.getLog().info("Adding song to queue " + track.getInfo().title);
        }
        
        public void playlistLoaded(AudioPlaylist playlist){
          Emica.getLog().info("Adding playlist to queue " + playlist.getName());
          List<AudioTrack> tracks = playlist.getTracks();
          
          if(Emica.getPlugin().getConfig().getBoolean("settings.shuffle_music_onload")) {
            Collections.shuffle(tracks);
          }
          
          for (AudioTrack track : tracks) {
            OnEnable.info.scheduler.queue(track);
          }
          
        }
        
        public void noMatches(){
          Emica.getLog().info("Nothing found by " + url);
        }
        
        public void loadFailed(FriendlyException e){
          Emica.getLog().info("Could not play " + e.getMessage());
        }
      });
    }
  }
}
