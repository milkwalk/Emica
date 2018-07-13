package cz.dubcat.emica.constructors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import cz.dubcat.emica.Emica;
import cz.dubcat.emica.events.OnEnable;

public class TrackSheduler extends AudioEventAdapter {
	
	  private final AudioPlayer player;
	  private final BlockingQueue<AudioTrack> queue;
	  private boolean loopEnabled = false;

	  public TrackSheduler(AudioPlayer player) {
	    this.player = player;
	    this.queue = new LinkedBlockingQueue<>();
	  }
	  
	  public void setLoopEnabled(boolean loopEnabled) {
		this.loopEnabled = loopEnabled;
	}

	  public boolean isLoopEnabled() {
		return loopEnabled;
	}
	  
	  public void queue(AudioTrack track) {
		  if (!player.startTrack(track, true)) {
			  queue.offer(track);
		  }
	  }
	  
	  public void nextTrack(AudioTrack current) {
		  if(current != null && isLoopEnabled()) {	  
			  queue.offer(current.makeClone());
		  }
		  player.startTrack(queue.poll(), false);   
	  }

	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
	    if (endReason.mayStartNext) {
	    	Emica.getLog().info("Starting playing track " + track.getInfo().title);
		      nextTrack(track);
		      if(isLoopEnabled()) {
		    	  queue.offer(track.makeClone());
		      }
	    }
	  }
	  
	  
	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		Emica.getLog().info("[EXCEPTION] Track " + track.getInfo().title + " is broken or somehthing..: " + exception.getMessage());
		OnEnable.info.scheduler.nextTrack(null);
	}
	  
	  public BlockingQueue<AudioTrack>getQueue(){
		  return queue;
	  }
	}
