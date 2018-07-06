package cz.dubcat.emica.constructors;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import cz.dubcat.emica.Emica;
import cz.dubcat.emica.events.OnEnable;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

public class MetaTextTask implements Runnable{
	
	private String text;
	
	public MetaTextTask() {
		text = Emica.getPlugin().getConfig().getString("settings.listening_text");
	}
	
	@Override
	public void run() {
		AudioTrack track = OnEnable.info.player.getPlayingTrack();
		if(track != null){
			String title = track.getInfo().title;
			String timeLeft = OnEnable.convertSecondsToString((int) ((track.getDuration() - track.getPosition()) / 1000L));
			String totalTime = OnEnable.convertSecondsToString((int) (track.getDuration() / 1000L));
			Emica.getBot().changePresence(StatusType.ONLINE, ActivityType.LISTENING, text.replace("%song_name%", title).replace("%time_left%", timeLeft).replace("%total_time%", totalTime));
		}
		
	}
}
