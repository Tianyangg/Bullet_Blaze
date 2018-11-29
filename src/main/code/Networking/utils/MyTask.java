package main.code.Networking.utils;

import main.code.Networking.GameThread;
import main.code.utils.Player;

import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

public class MyTask extends TimerTask {
    public List<Player> players;
    public byte[] pack;
    protected GameThread game;
    public MyTask(GameThread g){
        game = g;
    }
    public void run() {
        players = Collections.synchronizedList(game.getPlayers());
        pack = PackDigest.players(players, "PLAYERS");
    }
    public byte[] getPack(){
        return pack;
    }
}
