package main.code.Networking;

import javafx.util.Pair;
import main.code.Networking.utils.MessageType;
import main.code.Networking.utils.MyTask;
import main.code.Networking.utils.PackDigest;
import main.code.Networking.utils.Serialization;
import main.code.game.GameState;
import main.code.utils.*;
import main.code.utils.Map;

import java.awt.*;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.BatchUpdateException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameThread  {
    /**
     * @author Paul Mihaita
     * Class that deals with the game thread, receiving processing and sending messages from/to the clients connected to the game.
     */
    private final Server server;
    private final String name;
    private final Map map;
    private final ArrayList<Box> newBoxes;
    public byte max_playes;
    private byte nr_players;
    private  OutputStream toServer;
    private DataInputStream fromServer;
    private ConcurrentHashMap<Byte,Pair<InetAddress,Short>> clientsUdp;
    public ConcurrentHashMap<Byte,Long> lastPacketTime;
    private HashMap<Byte,Boolean> readyClients;
    private GameState state;
    private ConcurrentHashMap<Byte, OutputStream> clientsTcp;
    private TCPServerSocket tcpSocket;
    private UDPSocket udpSocket;
    private TCPClientSocket socketToServer;
    private TimerTask sendPlayerThread;
    private Thread checkGameState;
    private MyTask digestPlayers;
    private Timer t;
    private Timer t2;

    /**
     * Creates the {@link TCPServerSocket} and {@link UDPSocket} of the server with their process methods.
     *
     * @param server the server that created this object, for cleanup purposes. See {@link Server}
     * @param tcpPort the tcp port
     * @param udpPort the udp port
     * @param map the map of this game
     * @param boxes the map boxes with their content
     * @param nrp the number of players allowed in this game
     * @param n the name of the game
     * @throws UnknownHostException
     */
    public GameThread(Server server , short tcpPort, short udpPort, Map map,ArrayList<Box> boxes,byte nrp,String n) throws UnknownHostException {
        this.nr_players = 0;
        this.name = n;
        this.server = server;
        readyClients = new HashMap<>();
        this.max_playes = nrp;
        lastPacketTime = new ConcurrentHashMap<>();
        this.newBoxes = boxes;
        this.map = map;

        socketToServer = new TCPClientSocket(InetAddress.getLocalHost(),GameConstants.SERVER_TCP_PORT) {
            @Override
            protected void close() {

            }

            @Override
            protected void processTCP(byte[] packet, OutputStream outToClient) {

            }
        };

        tcpSocket = new TCPServerSocket(tcpPort, server) {
            @Override
            protected void processTCP(byte[] packet, OutputStream outToClient, long time) {
                MessageType type = MessageType.getType(packet);
                byte id_;
                Player p;
                switch (type){
                    case PLAYER:
                        p = PackDigest.player(packet,"PLAYER");
                        if (lastPacketTime.containsKey(p.getId()))
                            lastPacketTime.remove(p.getId());
                        lastPacketTime.put(p.getId(), time);
                        update(p);
                        break;
                    case DISCONNECTFROMGAME:
                        disconnectFromGame(packet,outToClient);
                        break;
                    case CLOSECONNECTION:
                        closeconnection(packet,outToClient);
                        break;
                    case GETPLAYERS:
                        byte[] playersBytes = PackDigest.players(state.getPlayers(),"GETPLAYERS");
                        send(playersBytes,outToClient);
                        break;
                    case ADDOUTPUTSTREAM:
                        int offset = "ADDOUTPUTSTREAM".length();
                        byte id = packet[offset];
                        clientsTcp.put(id,outToClient);
                        break;
                    case CLIENT_READY:
                        client_ready(packet,outToClient);
                        break;
                    case CLIENT_NOT_READY:
                        client_not_ready(packet,outToClient);
                        break;
                    case MISC:
                        id_ = packet["misc".length()];
                        System.out.println("MISC " + id_);
                        if (lastPacketTime.containsKey(id_))
                            lastPacketTime.remove(id_);
                        lastPacketTime.put(id_, time);
                        break;
                    case UPDATEBOXES:
                        ArrayList<Box> updated = PackDigest.boxes(packet,"UPDATEBOXES");
                        state.setBoxes(updated);
                        sendBackTcpBoxes(state);
                        break;
                    case BULLETSFIRED:
                        ArrayList<Bullet> bullets = PackDigest.bullets(packet,"BULLETSFIRED");
                        byte playerID = bullets.get(0).getSourcePlayerId();
                        state.setPlayerBullets(playerID,bullets);
                        break;
                    case SETSPAWN:
                        this.notify();
                        break;
                    default:
                        break;
                }
            }

        };

        udpSocket = new UDPSocket(udpPort,server) {

            @Override
            protected void process(DatagramPacket packetData, long time) {
                Player p;
                packet = packetData.getData();
                MessageType type = MessageType.getType(packet);
                switch (type){
                    case PLAYER:
                        p = PackDigest.player(packet,"PLAYER");
                        if (lastPacketTime.containsKey(p.getId()))
                            lastPacketTime.remove(p.getId());
                        lastPacketTime.put(p.getId(), time);
                        update(p);
                        break;
                    default:
                        break;
                }
            }
        };

        state = new GameState(new ArrayList<>(),map,boxes,true);
        clientsUdp = new ConcurrentHashMap<>();
        clientsTcp = new ConcurrentHashMap<>();
        state.setOnlineGame(true);
    }

    /**
     * Sends a reply for the message "CLIENT_NOT_READY", letting the client know that
     * the server aknowledged the fact that he is no longer ready to start.
     *
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void client_not_ready(byte[] packet, OutputStream outToClient) {
        int offset ="CLIENT_NOT_READY".length();
        byte id = packet[offset];
        readyClients.remove(id);
        state.setReady(id,false);
        byte[] typ = "CLIENT_NOT_READY".getBytes();
        byte[] toSend = Serialization.concat((short) typ.length,typ);
        tcpSocket.send(toSend,clientsTcp.get(id));
    }

    /**
     * Sends a reply for the message "CLIENT_READY", letting the client know that
     * the server aknowledged the fact that he is ready to start. If the client is not
     * already "ready", if the game is not full, it will register the client as ready.
     * When the number of ready clients is equal to the number of max players allowed in the game,
     * a message "CLIENT_READY" is send to all the ready clients. Threads for sending player data to the client
     * and checking round over and game over start.
     *
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void client_ready(byte[] packet, OutputStream outToClient) {
        int offset ="CLIENT_READY".length();
        byte id = packet[offset++];
        if(readyClients.size() < max_playes) {

            if (!readyClients.containsKey(id)) {
                readyClients.put(id, true);
                state.setReady(id, true);
                if (readyClients.size() == max_playes){
                    Point[] spawns = map.getSpawns();
                    int i = 0;
                    for (Byte b : readyClients.keySet()) {
                        Point position = spawns[i++];
                        short x = (short) position.getX();
                        short y = (short) position.getY();
                        byte[] typ = "CLIENT_READY".getBytes();
                        byte[] xBytes = Serialization.shortToByte(x);
                        byte[] yBytes = Serialization.shortToByte(y);
                        byte[] playersBytes = PackDigest.players(state.getPlayers(),"");
                        short packSize = (short) (typ.length + 4+playersBytes.length);
                        byte[] toSend = Serialization.concat(packSize, typ, xBytes, yBytes,playersBytes);
                        tcpSocket.send(toSend, clientsTcp.get(b));
                    }
                    if(t!=null)
                        t.cancel();
                    if(t2!=null)
                        t.cancel();
                    if(digestPlayers!=null)
                        digestPlayers.cancel();
                    if(sendPlayerThread!=null)
                        sendPlayerThread.cancel();
                    t = new Timer();
                    t2= new Timer();
                    digestPlayers = new MyTask(GameThread.this);
                    t.scheduleAtFixedRate(digestPlayers,0,1000/60);
                    sendPlayerThread = new TimerTask() {
                        @Override
                        public void run() {
                            if(state.getPlayers().size()>0) {
                                int c = 0;
                                for (Pair<InetAddress, Short> cData : clientsUdp.values()) {
                                    udpSocket.send(digestPlayers.getPack(), cData.getKey(), cData.getValue());
                                }
                            }
                        }
                    };
                    t2.scheduleAtFixedRate(sendPlayerThread,0,1000/60);
                    if(checkGameState!=null) {
                        if (!checkGameState.isAlive()) {
                            checkGameState = getCheckerThread();
                            checkGameState.start();
                        }
                    }else{
                        checkGameState = getCheckerThread();
                        checkGameState.start();
                    }
                }
            }
        }
    }

    /**
     * Disconnects a client from the game. See {@link GameThread#disconnectFromGame(byte[], OutputStream)}
     *
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void disconnectFromGame(byte[] packet, OutputStream outToClient) {

        int offset = "DISCONNECTFROMGAME".length();
        short pszie = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset += 2;
        byte id = packet[offset++];
        disconnectClientFromGame(packet, outToClient, offset, pszie, id);

    }

    /**
     * If the client is connected to the game, it disconnects it.
     * Removes the client from the tables used, sends "DISCONNECTEDFROMGAME" to the client and main server, for cleanup purposes
     * If the game becomes empty, it gets removed from the active games available to join in the main server.
     *
     * @param packet data to process
     * @param outToClient stream to write the result
     * @param offset length of the header
     * @param pszie packet size
     * @param id client id to disconnect
     */
    private void disconnectClientFromGame(byte[] packet, OutputStream outToClient, int offset, short pszie, byte id) {
        try {
            if(clientsUdp.containsKey(id)) {
                InetAddress serverAddr = InetAddress.getByAddress(Arrays.copyOfRange(packet, offset, pszie));
                Pair<InetAddress, Short> cData = clientsUdp.get(id);
                for (Player p : state.getPlayers()) {
                    if (p.getId() == id) {
                        state.removePlayer(p);
                        break;
                    }
                }
                clientsUdp.remove(id);
                clientsTcp.remove(id);
                byte[] type = "DISCONNECTEDFROMGAME".getBytes();
                byte[] portBytes = Serialization.shortToByte(GameConstants.SERVER_UDP_PORT);
                byte[] toSend = Serialization.concat((short) (type.length + portBytes.length), type, portBytes);
                tcpSocket.send(toSend,outToClient);
                byte[] clientAddr = cData.getKey().getAddress();
                byte[] clientPort = Serialization.shortToByte(cData.getValue());
                short plength = (short) (type.length + 1 + 2 + 2 + clientAddr.length);
                toSend = Serialization.concat(plength, type, new byte[]{id}, Serialization.shortToByte(plength), clientPort, clientAddr);
                socketToServer.send(toSend);
                nr_players = (byte) (nr_players - 1);
                readyClients.remove(id);
                if(nr_players==0){
                    GameThread.this.server.endGame(GameThread.this.name);
                    System.out.println("game should have ended " + GameThread.this.name);
                    if(t!=null)
                        t.cancel();
                    if(t2!=null)
                        t.cancel();
                    if(digestPlayers!=null)
                        digestPlayers.cancel();
                    if(sendPlayerThread!=null)
                        sendPlayerThread.cancel();
                    if(checkGameState!=null) {
                        checkGameState.interrupt();
                        checkGameState.join();
                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return returns the thread that checks if a game or round is over.It checks every 2 seconds. See {@link GameThread#checkGameState(ArrayList)}
     */
    private Thread getCheckerThread(){
        return new Thread(()->{
            ArrayList<OutputStream> outClients = new ArrayList<>();
            for(Byte id : readyClients.keySet()){
                outClients.add(clientsTcp.get(id));
            }
            while(true){
                if(!checkGameState(outClients)) break;
                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("closing checking state thread");
                    break;
                }
            }
        });
    }

    /**
     * It uses the {@link GameState} methods to check if a game or round is over. See {@link GameState#isOnlineGameOver()} and {@link GameState#isOnlineRoundOver()}
     * @param outClients clients outputstreams to send the messsages to, if a game or round is over
     * @return returns true or false if a game or round is over
     */
    private boolean checkGameState(ArrayList<OutputStream> outClients) {
        byte[] toSend;
        System.out.println("checking");
        boolean k = true;
        boolean kk = true;
        if(state.isOnlineGameOver()){
            toSend = "GAMEOVER".getBytes();
            System.out.println("Sending game over");
            for(OutputStream out:outClients){
                k = tcpSocket.send(toSend,out);
            }
        }
        else if(state.isOnlineRoundOver() && ! state.isOnlineGameOver()){
            state.setNewRoundBoxes(newBoxes);
            byte[] typ = "ROUNDOVER".getBytes();
            byte[] auxx = PackDigest.boxes(newBoxes,"");
            toSend = Serialization.concat((short)(typ.length+auxx.length),typ,auxx);
            System.out.println("Sending round over");
            for(OutputStream out:outClients){
                kk =tcpSocket.send(toSend,out);
            }
        }
        if(!k || !kk) return false;
        return true;
    }

    /**
     * Sends the boxes to the cliets trough udp socket, not used yet.
     * @param updated the arraylist of boxes to send.
     */
    private void sendBackUdpBoxes(ArrayList<Box> updated) {
        byte[] pack = PackDigest.boxes(updated,"UPDATEBOXES");

        for (Pair<InetAddress, Short> cData : clientsUdp.values()) {
            udpSocket.send(pack, cData.getKey(), cData.getValue());
        }
    }

    /**
     * Sends a message to the client trough tcp to set it's spawn point.
     * @param id client id
     * @param spawn spwan point for that client
     */
    private void setSpawn(byte id,Point spawn){
        OutputStream out  = clientsTcp.get(id);
        byte[] typ = "SETSPAWN".getBytes();
        byte[] x = Serialization.doubleToByte(spawn.getX());
        byte[] y = Serialization.doubleToByte(spawn.getY());
        byte[] toSend = Serialization.concat((short) (typ.length+16),typ,x,y);
        tcpSocket.send(toSend,out);
        try{
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends to the clients the boxes from the gamestate, trough tcp.
     * @param state the gamestate to take the boxes from
     */
    private void sendBackTcpBoxes(GameState state) {
        ArrayList<Box> boxes = state.getBoxes();
        byte[] pack = PackDigest.boxes(boxes,"UPDATEBOXES");
        for (OutputStream cData : clientsTcp.values()) {
            tcpSocket.send(pack,cData);
        }


    }

    /**
     * Closes the connection with the client, sends a message "CLOSECONNECTION" back to the server,
     * for cleanup purposes.See {@link Client} and {@link Server#close(byte[], OutputStream)}.
     * This method is called for a proper cleanup, with a message received from the client.
     * There are other methods if the client doesn't terminate gracefully, or it it's shutdownhooks
     *  are not triggered.
     *
     * @param packet data to process
     * @param outToClient stream to write the answer to
     */
    private void closeconnection(byte[] packet, OutputStream outToClient) {
        int offset = "CLOSECONNECTION".length();
        byte id = packet[offset++];
        Player p = null;
        for (int i = 0; i < state.getPlayers().size(); i++) {
            p = state.getPlayer(i);
            if (p.getId() == id) {
                state.removePlayer(i);
                nr_players = (byte) (nr_players - 1);
                readyClients.remove(id);
                break;
            }
        }
        clientsUdp.remove(id);
        clientsTcp.remove(id);
        tcpSocket.addBadClient(id, outToClient);
        if(clientsUdp.size() == 0){
            if(digestPlayers!=null)
                digestPlayers.cancel();
            if(sendPlayerThread != null)
                sendPlayerThread.cancel();
            if(t2!=null)
                t2.cancel();
            if(t!=null)
                t.cancel();
            if(checkGameState!=null) {
                checkGameState.interrupt();
                try {
                    checkGameState.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            GameThread.this.server.endGame(GameThread.this.name);
        }
        if (p != null) {
            byte[] typ = "CLOSECONNECTION".getBytes();
            byte[] idB = new byte[]{id};
            byte[] score = Serialization.intToByteArray(p.getScore());
            byte[] name = p.name.getBytes();
            socketToServer.send(Serialization.concat((short) (typ.length + 1 + 4 + name.length), typ, idB, score, name));
        }
    }

    /**
     * Closes the connection given a client id (called for idle clients).
     * Sends a message "CLOSECONNECTION" to the server for cleanup code.See {@link Server#close(byte[], OutputStream)}.
     * @param id the client id to disconnect
     */
    public void closeconnection(byte id) {
            Player p = null;
            for(int i = 0 ; i < state.getPlayers().size();i++){
                p = state.getPlayer(i);
                if (p.getId() == id) {
                    state.removePlayer(i);
                    break;
                }
            }
        if(p!=null) {
            readyClients.remove(id);
            nr_players--;
            clientsUdp.remove(id);
            clientsTcp.remove(id);
            if(clientsUdp.size() == 0){
                if(digestPlayers!=null)
                    digestPlayers.cancel();
                if(sendPlayerThread != null)
                    sendPlayerThread.cancel();
                if(t2!=null)
                    t2.cancel();
                if(t!=null)
                    t.cancel();
                if(checkGameState!=null) {
                    checkGameState.interrupt();
                    try {
                        checkGameState.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                GameThread.this.server.endGame(GameThread.this.name);
            }
            byte[] typ = "CLOSECONNECTION".getBytes();
            byte[] idB = new byte[]{id};
            byte[] score = Serialization.intToByteArray(p.getScore());
            byte[] name = p.name.getBytes();
            System.out.println("Disconnecting name " + p.name);
            socketToServer.send(Serialization.concat((short) (typ.length + 1 + 4 + name.length), typ, idB, score, name));
        }
    }

    /**
     * Sends the list of updated boxes to the client via udp, not used now.
     * @param updated the list of boxes.
     */
    private void sendBack(ArrayList<Box> updated) {
        byte[] pack = PackDigest.boxes(updated,"UPDATEBOXES");

                for (Pair<InetAddress, Short> cData : clientsUdp.values()) {
                    udpSocket.send(pack, cData.getKey(), cData.getValue());
                }



    }

    /**
     * Sends the list of players in the gamestate to all the clients via udp, not used now.
     * @param state the state to take the players from.
     */
    private void sendBack(GameState state) {
        ArrayList<Player> players = state.getPlayers();
        byte[] pack = PackDigest.players(players,"PLAYERS");

        for (Pair<InetAddress, Short> cData : clientsUdp.values()) {
            udpSocket.send(pack, cData.getKey(), cData.getValue());
        }


    }

    /**
     * Updates a player in the game's gamestate.
     * @param player the player object to update
     */
    private void update(Player player) {
        //Finds given player object in gamestate and updates it before
        //sending updated gamestate back to client
        Player aux ;
        for(int i = 0; i < this.state.players.size(); i++){
            if(this.state.players.get(i).getId() == player.getId()){
                this.state.players.remove(i);
                this.state.players.add(i,player);
                break;
            }
        }

        //System.out.println(player.toString());
        //System.out.println(this.state.getPlayers().size());
    }

    /**
     * Adds a player to the game.This method is called by the main server.
     * Sends a message "CONNECTEDTOGAME" back to the client.
     * @param id client id to add
     * @param cData address and udp port of the client
     * @param cTcp client's tcp stream, for sending data to the client
     * @param p the player object of the client
     */
    public void addPlayer(byte id,Pair<InetAddress,Short>cData ,OutputStream cTcp, Player p) {
        clientsUdp.put(id,cData);
        state.addPlayer(p);
        byte[] type = "CONNECTEDTOGAME".getBytes();
        byte[] udpPort = Serialization.shortToByte(udpSocket.port);
        byte[] tcpPort = Serialization.shortToByte(tcpSocket.port);
        byte[] map = PackDigest.map(state.getMap(),"");
        byte[] boxes = PackDigest.boxes(state.getBoxes(),"");
        byte[] bLength = Serialization.shortToByte((short) boxes.length);
        byte[] mSize = Serialization.shortToByte((short) map.length);
        byte[] toSend =  Serialization.concat((short) (type.length+4+2+boxes.length+2+map.length),type,udpPort,tcpPort,bLength,boxes,mSize,map);
        tcpSocket.send(toSend,cTcp);
        nr_players++;
    }

    /**
     * Removes a player from the gamestate
     * @param id id of the client to remove
     */
    public void removePlayer(byte id) {
        clientsUdp.remove(id);
        for(Player p : state.getPlayers()){
            if (p.getId() == id){
                state.removePlayer(p);
                break;
            }
        }
    }

    /**
     * @return returns the arraylist of players in the gamestate.
     */
    public ArrayList<Player> getPlayers() {
        return state.getPlayers();
    }

    /**
     * @return returns true or false if the game is full or not.
     */
    public boolean isFull() {
        return nr_players>=max_playes;
    }

    /**
     * @return returns an empty arraylist of boxes.
     */
    public ArrayList<Box> getResetedBoxes() {
        ArrayList<Box> resetedBoxes = new ArrayList<>();
        return resetedBoxes;
    }
}
