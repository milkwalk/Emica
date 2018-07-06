package cz.dubcat.emica.constructors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

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
		  if(isLoopEnabled()) {	  
			  queue.offer(current.makeClone());
		  }
		  player.startTrack(queue.poll(), false);   
	  }

	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
	    if (endReason.mayStartNext) {
	      nextTrack(track);
	      if(isLoopEnabled()) {
	    	  queue.offer(track.makeClone());
	      }
	    }
	  }
	  
	  public BlockingQueue<AudioTrack>getQueue(){
		  return queue;
	  }
	}
