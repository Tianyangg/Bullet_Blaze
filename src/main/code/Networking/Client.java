package main.code.Networking;

import javafx.util.Pair;
import main.code.Networking.utils.MessageType;
import main.code.Networking.utils.PackDigest;
import main.code.Networking.utils.Serialization;
import main.code.engine.GameEngine;
import main.code.engine.IGameLogic;
import main.code.game.OnlineMode;
import main.code.game.GameState;
import main.code.utils.*;
import main.code.utils.Map;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

import static main.code.utils.GameConstants.NAME_OF_GAME;

/**
 * This class uses {@link UDPSocket} and {@link TCPClientSocket} to communicate with the servers.
 * It also uses {@link Player} and {@link GameState} to keep the player data and the gamestate data for tge renderer.
 */
public class Client {
    private String userName;
    private String password;
    private final InetAddress serverAddress;
    private short serverTcpPort;
    private UDPSocket udpSocket;
    private short serverUdpPort;
    public Player player;
    private final Object readyLock = new Object();
    private HashMap<String,ArrayList<Player>> availableGames;
    protected InetAddress address;
    protected byte id;
    protected boolean connectedToGame = false;
    private TCPClientSocket serverConnection;
    protected boolean gameCreated ;
    public GameState gameState ;
    private boolean failed;
    private short boxSize;
    private boolean ready;
    private ArrayList<Player> currentGamePlayers = new ArrayList<>();
    private TimerTask pingThread;
    private TimerTask sendThread;
    private boolean not_ready;
    public boolean gameover = false;
    public boolean roundover = false;
    private boolean sendThreadStarted = false;
    private Timer t;
    private boolean pingThreadStarted = false;
    private Timer t2;
    private HashMap<String,Byte> availableGamesMax = new HashMap<>();

    /**
     * Creates and starts the sockets and listening threads
     *
     * @param serverAddr the server address , should be taken from {@link GameConstants}
     * @param udpPort the udp port of the client
     * @param p the player object of this client
     * @param userName the client's username
     * @param password the client's password
     */
    public Client(InetAddress serverAddr,short udpPort,Player p,String userName,String password){
        gameCreated = false;
        gameState = new GameState();
        this.userName = userName;
        this.password = password;
        this.serverUdpPort = GameConstants.SERVER_UDP_PORT;
        this.serverTcpPort = GameConstants.SERVER_TCP_PORT;
        this.serverAddress = serverAddr;
        try {
            this.address = InetAddress.getByName(getIpAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.player = p;
        availableGames = new HashMap<>();
        udpSocket = new UDPSocket(udpPort) {
            @Override
            protected void process(DatagramPacket packetData, long time) {
                packet = packetData.getData();
                MessageType type = MessageType.getType(packet);
                switch (type){
                    case PLAYERS:
                        update(PackDigest.players(packet,"PLAYERS"));
                        break;
                    case UPDATEBOXES:
                        gameState.setBoxes(PackDigest.boxes(packet,"UPDATEBOXES"));
                    default:
                        break;
                }
            }
        };
        serverConnection = getMainServerTcpSocket(serverAddr);

    }

    /**
     * Creates a new tcp client socket, with it's close and process methods implemented
     * @param serverAddr the server address , should be taken from {@link GameConstants}
     * @return returns the tcp client socket.
     */
    private TCPClientSocket getMainServerTcpSocket(InetAddress serverAddr) {
        TCPClientSocket serverConnection = new TCPClientSocket(serverAddr, GameConstants.SERVER_TCP_PORT) {
            @Override
            protected void close() {
                byte[] typ = "CLOSECONNECTION".getBytes();
                byte[] idB = new byte[]{id};
                byte[] score = Serialization.intToByteArray(player.getScore());
                byte[] name = player.name.getBytes();
                if(!socket.isClosed()) {
                    send(Serialization.concat((short) (typ.length+1+4+name.length),typ,idB,score,name));
                    close_streams();
                }
            }

            @Override
            protected void processTCP(byte[] packet, OutputStream outToClient) {
                MessageType type = MessageType.getType(packet);
                short mapSize;
                int offset;
                int score;
                String name;
                System.out.println(type);
                switch (type) {
                    case HANDSHAKEOK:
                        offset = "HANDSHAKEOK".length();
                        id = packet[offset++];
                        score = Serialization.fromByteArray(Arrays.copyOfRange(packet,offset,offset+4));
                        offset+=4;
                        name = new String(Arrays.copyOfRange(packet,offset,packet.length));
                        //player.setScore(score);
                        player.setId(id);
                        player.name = name;
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        failed = false;
                        break;
                    case FAILUSERNAME:
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        failed = true;
                        break;
                    case SERVERFULL:
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        failed = true;
                        break;
                    case USERADDED:
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        failed = false;
                        break;
                    case GAMECREATIONFAILED:
                        gameCreated = false;
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case GAMECREATED:
                        gameCreated = true;
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case CONNECTEDTOGAME:
                        serverUdpPort = Serialization.byteToShort(Arrays.copyOfRange(packet, 15, 17));
                        serverTcpPort = Serialization.byteToShort(Arrays.copyOfRange(packet, 17, 19));
                        offset = 19;
                        boxSize = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
                        offset+=2;
                        gameState.setBoxes(PackDigest.boxes(Arrays.copyOfRange(packet,offset,offset+boxSize),""));
                        offset+=boxSize;
                        mapSize = Serialization.byteToShort(Arrays.copyOfRange(packet, offset, offset+2));
                        offset+=2;
                        connectedToGame = true;
                        gameState.setMap(PackDigest.map(Arrays.copyOfRange(packet,offset,offset+mapSize),""));
                        handshake(serverTcpPort);
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case CONNECTIONTOGAMEFAILED:
                        connectedToGame = false;
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case DISCONNECTEDFROMGAME:
                        connectedToGame = false;
                        serverUdpPort = Serialization.byteToShort(Arrays.copyOfRange(packet, 20, 22));
                        serverTcpPort = GameConstants.SERVER_TCP_PORT;
                        handshakeInternal(true);
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case AVAILABLEGAMES:
                        updateGames(packet);
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case AVAILABLEGAMESMAX:
                        updateGamesMax(packet);
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        break;
                    case DISCONNECTED:
                        synchronized (Client.this){
                            Client.this.notify();
                        }
                        end_client();
                        break;
                        default:
                        break;

                }
            }
        };
        return serverConnection;
    }

    /**
     * Sending a ping message to the server trough udp, not used now
     */
    public void sendUdpPing() {
        if(connectedToGame) {
            udpSocket.send(Serialization.concat((byte) 5, "MISC".getBytes(), new byte[]{id}), serverAddress, serverUdpPort);
        }
    }

    /**
     * sets the client username
     * @param s the username
     */
    public void setUsername(String s){
        this.userName = s;
    }

    /**
     * sets the client password
     * @param s the password
     */
    public void setPassword(String s){
        this.password = s;
    }

    /**
     * Sends a request to e websites to get the external ip address of the machine the client runs on
     * @return returns the string of that address
     */
    public static String getIpAddress()
    {
        while(true) {
            URL myIP;
            try {
                myIP = new URL("http://api.externalip.net/ip/");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(myIP.openStream())
                );
                return in.readLine();
            } catch (Exception e) {
                try {
                    myIP = new URL("http://myip.dnsomatic.com/");

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(myIP.openStream())
                    );
                    return in.readLine();
                } catch (Exception e1) {
                    try {
                        myIP = new URL("http://icanhazip.com/");

                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(myIP.openStream())
                        );
                        return in.readLine();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Updates the arraylist of players in the gamestate, for the renderer to draw the new player states.
     * @param players the arraylist of updated players
     */
    private void update(ArrayList<Player> players) {
        ArrayList<Player> aux = new ArrayList<>();
        for(Player p : players)
            if ( p.getId() != player.getId())
                aux.add(p);
        aux.add(player);
        gameState.setPlayers(aux);
    }

    /**
     * sends a request to the server to add a user. Blocks and waits untill the server sends the confirmation.
     * @return true or false if the user was set or not
     */
    public boolean addUser(){
        failed = true;
        byte[] typ = "ADDUSER".getBytes();
        byte[] name = userName.getBytes();
        byte[] pass = password.getBytes();
        byte[] nl = new byte[]{(byte) name.length};
        byte[] pl = new byte[]{(byte) pass.length};
        short size = (short) (typ.length+name.length+pass.length+2);
        serverConnection.send(Serialization.concat(size,typ,nl,name,pl,pass));
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return failed;
    }

    /**
     * handshake with the main server before joining any game
     * @return true or false, if it connected to the main server or not
     */
    public boolean handshake(){
        return handshakeInternal(false);
    }

    /**
     * handhsake with the main server before or after joining a game, based on trusty parameter
     * Sends a message "HANDSHAKE" or "HANDSHAKEAFTERGAME" to the server.Blocks and waits for server answer.
     *
     * @param trusty true or false, if the client connected to a game before or not.
     * @return true or false, if it connected to the main server or not
     */
    public boolean handshakeInternal(boolean trusty){
        serverConnection.end();
        serverConnection = getMainServerTcpSocket(serverAddress);
        byte[] type;
        if(trusty)
            type = "HANDSHAKEAFTERGAME".getBytes();
        else type = "HANDSHAKE".getBytes();
        byte[] p = Serialization.shortToByte(udpSocket.port);
        byte[] addr = address.getAddress();

        byte[] userN = userName.getBytes();
        byte[] pass = password.getBytes();
        byte userNL = (byte) userN.length;
        byte userPL = (byte) pass.length;
        short s = (byte)(type.length+addr.length+1+2+2+2+userN.length+pass.length);
        byte[] size = Serialization.shortToByte((short) (s-2-userN.length-pass.length));

        byte[] toSend = Serialization.concat(s,type,size,p,new byte[]{id},addr,new byte[]{userNL},userN,new byte[]{userPL},pass);
        serverConnection.send(toSend);
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return !failed;
    }

    /**
     * Sends a message to the server saying that the client is ready to start the game.
     * Blocks and waits for server answer ( either start game or not ready, if the client presses not ready )
     * @return true or false if the client is ready to start the game or not
     */
    public boolean ready() {
        if (connectedToGame) {
            byte[] typ = "CLIENT_READY".getBytes();
            byte[] toSend = Serialization.concat((short) (typ.length + 1), typ, new byte[]{id});
            serverConnection.send(toSend);
            ready = true;
            synchronized (readyLock){
                try {
                    readyLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return ready;
    }

    /**
     * Sends a message "CLIENT_NOT_READY" to the server and waits for server confirmation.
     * This will end method {@link Client#ready()} aswell, with return value false.
     */
    public void notReady(){
        if(connectedToGame){
            byte[] typ = "CLIENT_NOT_READY".getBytes();
            byte[] toSend = Serialization.concat((short) (typ.length + 1), typ, new byte[]{id});
            serverConnection.send(toSend);
            ready = false;
            synchronized (readyLock){
                try {
                    readyLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ready = false;
        }
    }

    /**
     * Sends a request to the server "GETPLAYERS" and waits for server answer.When the method terminates,
     * the arraylist of players in the game is updated.
     * @return the arraylist of updated players
     */
    public ArrayList<Player> getGamePlayers(){
        currentGamePlayers.clear();
        if(connectedToGame){
            byte[]toSend = "GETPLAYERS".getBytes();
            serverConnection.send(toSend);
            try {
                synchronized (this){
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return currentGamePlayers;
    }

    /**
     * Connects the client to a game.
     * Creates a {@link TCPClientSocket} that is connected to the game, with the abstract methods {@link TCPClientSocket#processTCP(byte[], OutputStream)} and {@link TCPClientSocket#close()}
     * implemented.
     * Also sending a message "ADDOUTPUTSTREAM" to the game server, to add this tcp client outputstream as valid and open for messages.
     * @param gameTcpPort the port of the game on the server.
     */
    private void handshake(short gameTcpPort) {

        serverConnection.end();
        serverConnection = new TCPClientSocket(serverAddress, gameTcpPort) {
            @Override
            protected void close() {
                byte[] typ = "CLOSECONNECTION".getBytes();
                byte[] idb = new byte[] { id };
                byte[] toSend = Serialization.concat((short)(typ.length+1),typ,idb);
                send(toSend);
                close_streams();
            }

            @Override
                protected void processTCP(byte[] packet, OutputStream outToClient) {
                    MessageType type = MessageType.getType(packet);
                    int offset;
                    switch (type) {
                        case DISCONNECTEDFROMGAME:
                            pingThread.cancel();

                            connectedToGame = false;
                            serverUdpPort = Serialization.byteToShort(Arrays.copyOfRange(packet, 20, 22));
                            serverTcpPort = GameConstants.SERVER_TCP_PORT;
                            handshakeInternal(true);
                            synchronized (Client.this){
                                Client.this.notify();
                            }
                            break;
                        case GETPLAYERS:
                            currentGamePlayers = PackDigest.players(packet,"GETPLAYERS");
                            synchronized (Client.this){
                                Client.this.notify();
                            }
                            break;
                        case AVAILABLEGAMES:
                            updateGames(packet);
                            synchronized (Client.this){
                                Client.this.notify();
                            }
                            break;
                        case SETSPAWN:
                            offset = "SETSPAWN".length();
                            double xD = Serialization.bytesToDouble(Arrays.copyOfRange(packet,offset,offset+8));
                            offset+=8;
                            double yD = Serialization.bytesToDouble(Arrays.copyOfRange(packet,offset,offset+8));
                            player.setCoord((short)xD,(short)yD);
                            send(Serialization.concat((short) 9,"SETSPAWN".getBytes(),new byte[]{id}));
                            break;
                        case GAMEOVER:
                            Client.this.gameover = true;
                            break;
                        case ROUNDOVER:
                            offset = "ROUNDOVER".length();
                            gameState.setNewRoundBoxes(PackDigest.boxes(Arrays.copyOfRange(packet,offset,packet.length),""));
                            gameState.setMapBoxes();
                            Client.this.roundover = true;
                            break;
                        case CLIENT_READY:
                                offset = "CLIENT_READY".length();
                                short x = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
                                offset+=2;
                                short y = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
                                offset+=2;
                                player.setCoord(x,y);
                                gameState.setPlayers(PackDigest.players(Arrays.copyOfRange(packet,offset,packet.length),""));
                            System.out.println("players size " + gameState.getPlayers().size());

                            synchronized (readyLock){
                                    readyLock.notify();
                                }                            break;
                        case CLIENT_NOT_READY:
                            player.ready = false;
                            synchronized (readyLock){
                                readyLock.notifyAll();
                            }
                            break;
                        case UPDATEBOXES:
                            gameState.setNewRoundBoxes(PackDigest.boxes(packet,"UPDATEBOXES"));
                        default:
                            break;
                    }
                }
            };

        System.out.println("sending addoutput stream");
        byte[] typ = "ADDOUTPUTSTREAM".getBytes();
        byte[] tos = Serialization.concat((byte)(typ.length+1),typ,new byte[]{id});
        serverConnection.send(tos);
    }

    /**
     * Sends a message to the main server to create a game. Blocks and waits untill server confirmation is received.
     * @param name the name of the game to create
     * @param nrPlayers the max number of players allowed in the game
     * @param map the game map
     * @param boxes the map boxes and their content
     * @return true or false if the game was created or not.
     */
    public boolean createGame(String name,byte nrPlayers,Map map,ArrayList<Box> boxes) {
        byte[] type = "CREATEGAME".getBytes();
        byte[] gameName = name.getBytes();
        byte[] size = Serialization.shortToByte((short)(type.length + gameName.length + 1+2+1));
        byte[] mapBytes = PackDigest.map(map,"");
        this.gameState.setMap(map);
        byte[] boxesBytes = PackDigest.boxes(boxes,"");
        short packSize = (short)(type.length + gameName.length + 1+2+1 + mapBytes.length + 2 + boxesBytes.length);
        byte [] toSend = Serialization.concat(packSize,type,new byte[]{nrPlayers},size,new byte[]{id},gameName,Serialization.shortToByte((short) mapBytes.length),mapBytes,boxesBytes);
        serverConnection.send(toSend);
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gameCreated;
    }

    /**
     * Sends a message to the main server to connect to a game.Blocks and waits untill
     * server confirmation is received.
     * If connected to the game, starts the ping thread to send messages every second untill the game starts, so the client is not idle.
     * @param name the game name to connect to
     * @return true or false if the client connected to the game or not
     */
    public boolean connectToGame(String name){
        byte[] type = "CONNECTTOGAME".getBytes();
        byte[] gameName = name.getBytes();
        short packSize = (short)(type.length + gameName.length + 1+2);
        byte[] size = Serialization.shortToByte(packSize);
        System.out.println("id " + id);
        byte[] toSend = Serialization.concat(packSize,type,size,new byte[]{id},gameName);
        byte [] p = PackDigest.player(player,"");
        toSend = Serialization.concat((short)(toSend.length+p.length),toSend,p);
        serverConnection.send(toSend);
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(connectedToGame) {
            t2 = new Timer();
            pingThread = new TimerTask() {
                @Override
                public void run() {
                    serverConnection.send(Serialization.concat((byte) 5, "MISC".getBytes(), new byte[]{id}));
                }
            };
            t2.scheduleAtFixedRate(pingThread,0,1000);
            pingThreadStarted = true;
            System.out.println(connectedToGame);
        }
        return connectedToGame;
    }

    /**
     * Disconnects a client from a game (if conencted to one). Sends a message "DISCONNECTFROMGAME" to the
     * game server. Blocks and waits for server answer.After the answer is received, method {@link Client#handshakeInternal(boolean)}
     * with boolean true is called, to connect the client back to the main server.
     */
    public void disconnectFromGame() {
        if(connectedToGame) {
            byte[] type = "DISCONNECTFROMGAME".getBytes();
            byte[] id = new byte[]{this.id};
            //byte[] port = Serialization.shortToByte(this.port);
            byte[] addr = this.address.getAddress();
            short psize = (short) (type.length + id.length + 2 + addr.length);
            byte[] packSize = Serialization.shortToByte(psize);
            byte[] toSend = Serialization.concat(psize, type, packSize, id, addr);
            serverConnection.send(toSend);
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Digests a packet with the available games and the lists of players connected to them to display in the UI join game table.
     * @param packet the packet containing games data
     */
    private void updateGames(byte[] packet) {
        int offset = "AVAILABLEGAMES".length();
        short packSize = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset +=2;
        byte nrGames = packet[offset++];
        String[] gameNames = new String[nrGames];
        int i = 0;
        for(;i<nrGames;i++){
            byte l = packet[offset++];
            gameNames[i] = new String(Arrays.copyOfRange(packet,offset,offset+l));
            offset += l;
        }
        ArrayList<ArrayList<Player>> players = new ArrayList<>();
        for(i=0;i<nrGames;i++){
            byte nrPlayers = packet[offset++];
            players.add(PackDigest.players(Arrays.copyOfRange(packet,offset,offset+nrPlayers),""));
            offset+=nrPlayers;
        }
        i = 0;
        for(;i<nrGames;i++){
            availableGames.remove(gameNames[i]);
            availableGames.put(gameNames[i], players.get(i));
        }
    }

    /**
     * Digests a packet with the available games to join from the server and their max number of players allowed, to display in the UI join game table.
     * @param packet the packet containing games data
     */
    private void updateGamesMax(byte[] packet) {
        int offset = "AVAILABLEGAMESMAX".length();
        byte nrGames = packet[offset++];
        String[] gameNames = new String[nrGames];
        int i = 0;
        ArrayList<Byte> players = new ArrayList<>();
        for(;i<nrGames;i++){
            byte l = packet[offset++];
            gameNames[i] = new String(Arrays.copyOfRange(packet,offset,offset+l));
            offset += l;
            players.add(packet[offset++]);
        }
        i = 0;
        for(;i<nrGames;i++){
            availableGamesMax.remove(gameNames[i]);
            availableGamesMax.put(gameNames[i], players.get(i));
        }
    }

    /**
     * Sends player data to the game server trough udp.
     * @param p the player object to send
     */
    public  void sendPlayer(Player p){
        if(connectedToGame) {
            byte[] toSend = PackDigest.player(p,"PLAYER");
            udpSocket.send(toSend,serverAddress,serverUdpPort);
        }
    }

    /**
     * Sends the gamestate boxes to the game server trough udp.
     */
    public void sendBoxes(){
        if(connectedToGame){
            byte[] toSend = PackDigest.boxes(gameState.getBoxes(),"UPDATEBOXES");
            udpSocket.send(toSend,serverAddress,serverUdpPort);
        }
    }
    public void sendTcpBoxes(){
        if(connectedToGame){
            byte[] toSend = PackDigest.boxes(gameState.getBoxes(),"UPDATEBOXES");
            serverConnection.send(toSend);
        }
    }


    /**
     * @return returns the client's player object
     */
    public Player getPlayer() { return player; }

    /**
     * Sends a message to the server to disconnect totally. From game, if conencted to one, and from the main server after that.
     */
    public void disconnect() {
        if (connectedToGame)
            disconnectFromGame();
        byte[] type = "DISCONNECT".getBytes();
        byte[] id = new byte[]{this.id};
        byte[] toSend = Serialization.concat((short) (type.length+1),type,id);
        serverConnection.send(toSend);
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a request to the server to get the available games to join. Blocks and waits for server answer.
     * @return returns a hashmap with the game name and the list of players connected to the game.
     */
    public HashMap<String, ArrayList<Player>> getAvailableGames() {
        if(!connectedToGame) {
            availableGames.clear();
            byte[] toSend = "AVAILABLEGAMES".getBytes();
            toSend = Serialization.concat((short) (toSend.length + 1), toSend, new byte[]{id});
            serverConnection.send(toSend);
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return availableGames;
        }
        return null;
    }
    /**
     * Sends a request to the server to get the available games and their maximum number of players allowed to join.
     * Blocks and waits for server confirmation.
     * @return a hashmap with the game name and the game's max player number
     */
    public HashMap<String, Byte> getGamesMaxPlayers() {
        if(!connectedToGame) {
            availableGamesMax.clear();
            byte[] toSend = "AVAILABLEGAMESMAX".getBytes();
            toSend = Serialization.concat((short) (toSend.length + 1), toSend, new byte[]{id});
            serverConnection.send(toSend);
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return availableGamesMax;
        }
        return null;
    }

    /**
     * Sends a message to the server to end the connection with the client, and then closes the serversocket and does cleanup tasks.
     */
    public void end_client() {
        udpSocket.close();
        byte[] typ = "CLOSECONNECTION".getBytes();
        byte[] idB = new byte[]{id};
        byte[] score = Serialization.intToByteArray(player.getScore());
        byte[] name = userName.getBytes();
        serverConnection.send(Serialization.concat((short) (typ.length+1+4+name.length),typ,idB,score,name));
        System.out.println("sending closeconnection message");
        serverConnection.end();
        serverConnection.close_streams();
    }

    /**
     * @return if connected to a game or not
     */
    public boolean isConnectedToGame() {
        return connectedToGame;
    }

    /**
     * Creates and stars a thread that sends player data to the game server 60 times per second.
     */
    public void startSending() {
        t = new Timer();
        sendThread = new TimerTask() {
            @Override
            public void run() {
                Client.this.sendPlayer(Client.this.player);
                //if(Client.this.player.getBulletsFired().size() >0)
                  //  Client.this.sendTcpBullet(Client.this.player.getBulletsFired());
            }
        };
        sendThreadStarted = true;
        t.scheduleAtFixedRate(sendThread,0,1000/60);
        pingThreadStarted = false;
        t2.cancel();
       pingThread.cancel();
    }

    private void sendTcpBullet(ArrayList<Bullet> bulletsFired) {
        byte[] bullets = PackDigest.bullets(bulletsFired,"");
        byte[] typ = "BULLETSFIRED".getBytes();
        byte[] toSend = Serialization.concat((short) (bullets.length+typ.length),typ,bullets);
        serverConnection.send(toSend);
    }

    /**
     * Stops the thread that sends player and/or ping data to the game server
     */
    public void stopSending() {
        if(sendThreadStarted) {
            sendThread.cancel();
            t.cancel();
        }
        if(pingThreadStarted) {
            pingThread.cancel();
            t2.cancel();
        }
    }

    public void sendTcpPlayer(Player p) {
        if(connectedToGame) {
            byte[] toSend = PackDigest.player(p,"PLAYER");
            serverConnection.send(toSend);
        }
    }

    public String getUsername() {
        return userName;
    }
}
