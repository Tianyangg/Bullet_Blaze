package main.code.game.JUnit;

import javafx.util.Pair;
import main.code.Networking.Client;
import main.code.Networking.Server;
import main.code.Networking.utils.Serialization;
import main.code.utils.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import sun.awt.windows.ThemeReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import static main.code.utils.GameConstants.*;
import static main.code.utils.GameConstants.NO_WEAPON_ID;
import static main.code.utils.GameConstants.UZI_AMMO;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NetworkingTests {
    static Player player;
    static Server server;
    static Client client;
    static Boolean connectionOk;
    private static Map map;
    private static ArrayList<Box> boxes;
    private static Client clientMainServer;
    private static Player playerMain;

    @BeforeClass
    public static void openserver(){
        ArrayList<Pair<Byte, Short>> chosenWeapons = new ArrayList<>();
        chosenWeapons.add(new Pair<Byte, Short>(MACHINEGUN_ID,MACHINEGUN_AMMO));
        chosenWeapons.add(new Pair<Byte, Short>(SHOTGUN_ID,SHOTGUN_AMMO));
        chosenWeapons.add(new Pair<Byte, Short>(SNIPER_ID, SNIPER_AMM0));
        chosenWeapons.add(new Pair<Byte, Short>(UZI_ID, UZI_AMMO));

        map = (new MapReader().readMap("src/main/resources/Maps"));

        boxes = map.setupBoxes(chosenWeapons);

        Server.create_table();

        server = new Server(GameConstants.SERVER_TCP_PORT);

        player = new Player();
        playerMain = new Player();

        try {

            client = new Client(InetAddress.getLocalHost(), (short) 8888,player,"paul","paul");
            clientMainServer = new Client(InetAddress.getLocalHost(), (short) 8889,playerMain,"admin","admin");
            System.out.println("client ok");
            connectionOk = true;
        } catch (UnknownHostException e) {
            connectionOk = false;
        }
    }
    @Test
    public void AddUser(){
        assert(connectionOk);
        assert(!clientMainServer.addUser());
        assert(!client.addUser());
    }
    @Test
    public void Bhandshake(){
        assert(connectionOk);
        assert (client.handshake());
        assert (clientMainServer.handshake());
        System.out.println(client.getPlayer().getId());
        assert (client.getPlayer().getId() == (byte)(-128));
        assert (clientMainServer.getPlayer().getId() == (byte)(-127));
    }
    @Test
    public void CcreateGame(){
        assert(connectionOk);
        boolean gameCreated = client.createGame("test", (byte) 2,map,boxes);
        boolean containsGame = client.getAvailableGames().containsKey("test");
        assert (gameCreated && containsGame);
    }
    @Test
    public void DconnectToGame(){
        assert(connectionOk);
        Boolean connectedToGame = client.connectToGame("test");
        ArrayList<Player> players = clientMainServer.getAvailableGames().get("test");
        Boolean containsMe = false;
        for(Player p : players){
            if(p.getName().equals("paul"))
                containsMe = true;
        }
        assert (connectedToGame && containsMe);
    }
    @Test
    public void EsendPlayerData(){
        assert(connectionOk);
        client.player.setHp((byte) 50);
        client.player.setCoord((short)2000,(short)3000);
        client.sendPlayer(client.player);
        try {
            Thread.sleep(100);
            ArrayList<Player> players = clientMainServer.getAvailableGames().get("test");
            for(Player p : players){
                if(p.getName().equals("paul"))
                    assert(p.getHp() == 50 && p.getXPos() == 2000 && p.getYPos() == 3000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void FdisconnectFromGame(){
        assert(connectionOk);
        client.disconnectFromGame();
        HashMap<String, ArrayList<Player>> players = client.getAvailableGames();
        assert(!players.containsKey("test"));
    }
    @Test
    public void GcreateConnectDisconnect(){
        assert(connectionOk);
        assert(client.createGame("test",(byte)2,map,boxes));
        assert(client.connectToGame("test"));
        client.disconnect();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert(!server.loggedIn(client.getUsername()));
    }
    @Test
    public void HconnectBackWaitDisconnect(){
        assert(connectionOk);
        try {
            client = new Client(InetAddress.getLocalHost(), (short) 8888,player,"paul","paul");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert(client.handshake());
        assert(client.createGame("test",(byte)2,map,boxes));
        assert(client.connectToGame("test"));
        client.sendPlayer(client.player);
        long t = System.currentTimeMillis();
        client.stopSending();
        while(System.currentTimeMillis() < t + 11000){
        }
        assert(!server.loggedIn(client.getUsername()));
    }

}
