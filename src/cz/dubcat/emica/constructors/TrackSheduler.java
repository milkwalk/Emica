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
	    // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
	    // something is playing, it returns false and does nothing. In that case the player was already playing so this
	    // track goes to the queue instead.
	    if (!player.startTrack(track, true)) {
	      queue.offer(track);
	    }
	  }

	  public void nextTrack() {
	    // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
	    // giving null to startTrack, which is a valid argument and will simply stop the player.
	    player.startTrack(queue.poll(), false);
	  }

	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
	    if (endReason.mayStartNext) {
	      nextTrack();
	      if(isLoopEnabled())
	    	  queue(track.makeClone());
	    }
	  }
	  
	  public BlockingQueue<AudioTrack>getQueue(){
		  return queue;
	  }
	}
