package cz.dubcat.emica.constructors;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class ServerInformation {

    public final AudioPlayer player;
    public final TrackSheduler scheduler;

    public ServerInformation(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackSheduler(player);
        player.addListener(scheduler);

    }

    public MPlayer getAudioProvider() {
        return new MPlayer(player);
    }
}
