package main.code.Networking;

import javafx.util.Pair;
import main.code.Networking.utils.MessageType;
import main.code.Networking.utils.PackDigest;
import main.code.Networking.utils.Serialization;
import main.code.utils.Box;
import main.code.utils.GameConstants;
import main.code.utils.Map;
import main.code.utils.Player;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that uses {@link TCPServerSocket} to connect the clients to the main Server, before entering a game.
 * It also handles connection to the game
 *
 * @author Paul Mihaita
 */
public class Server {

    private static Connection db_connection;
    private final Thread serverTimeThread;
    HashMap<Byte,Pair<InetAddress,Short>> clients;
    HashMap< String,GameThread > games;
    HashMap<Byte,String> clientsGames;
    private short gamePort = 4444;
    private TCPServerSocket tcpServerSocket;
    private byte next_id = Byte.MIN_VALUE;
    protected long serverTime;
    protected long startServerTime;
    protected byte[] ids;

    /**
     * Creates a {@link TCPServerSocket} with it's process method.
     * Starts a thread for the server time, updating every 1.5 seconds.
     * More accurate timing is not needed. This time is used to disconnect
     * idle clients, max idle time being 10 seconds.
     *
     * @param tcpPort tcp socket port
     */
    public Server(short tcpPort){
        serverTimeThread = new Thread(()->{
            while(true){
                serverTime = System.nanoTime()/1000000 - startServerTime;
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        startServerTime = System.nanoTime()/1000000;
        serverTimeThread.start();
        ids = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE-1];
        load_db();
        clients = new HashMap<>();
        games = new HashMap<>();
        clientsGames = new HashMap<>();
        tcpServerSocket = new TCPServerSocket(tcpPort,this) {
            
            @Override
            protected void processTCP(byte[] packet, OutputStream outToClient, long time) {
                MessageType type = MessageType.getType(packet);
                System.out.println(type);
                switch (type){
                    case HANDSHAKE:
                        handshake(packet,outToClient,false);
                        break;
                    case HANDSHAKEAFTERGAME:
                        handshake(packet,outToClient,true);
                        break;
                    case ADDUSER:
                        addUser(packet,outToClient);
                        break;
                    case CONNECTTOGAME:
                        connectToGame(packet,outToClient);
                        break;
                    case CREATEGAME:
                        createGame(packet,outToClient);
                        break;
                    case DISCONNECTEDFROMGAME:
                        break;
                    case AVAILABLEGAMES:
                        sendGamesList(packet,outToClient);
                        break;
                    case AVAILABLEGAMESMAX:
                        sendGamesListMax(packet,outToClient);
                        break;
                    case DISCONNECT:
                        disconnect(packet,outToClient);
                        break;
                    case CLOSECONNECTION:
                        close(packet,outToClient);
                        break;
                }
            }
        };
        ;
        handle_idle_clients();
        }

    /**
     * Adds a user to the database, if the user does not exist, and sends the message "USERADDED" to the client.
     * Sends the message "FAILUSERNAME" if the clients already exists.
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void addUser(byte[] packet, OutputStream outToClient) {
        int offset = "ADDUSER".length();
        byte nl = packet[offset++];
        String name = new String(Arrays.copyOfRange(packet,offset,offset+nl));
        offset+=nl;
        byte pl = packet[offset++];
        String pass = new String(Arrays.copyOfRange(packet,offset,offset+pl));
        if(!existsUser(name,pass)) {
            insertUser(name,pass);
            tcpServerSocket.send(Serialization.concat((short) ("USERADDED".length()), "USERADDED".getBytes()), outToClient);
        }else{
            tcpServerSocket.send(Serialization.concat((short) ("FAILUSERNAME".length()), "FAILUSERNAME".getBytes()), outToClient);
        }
    }
    /**
     * Given the client id from the message, tcpServerSocket.send "DISCONNECTED" to the client
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void disconnect(byte[] packet, OutputStream outToClient) {
        int offset = "DISCONNECT".length();
        byte id = packet[offset++];
        if(clients.containsKey(id)) {
            Pair<InetAddress, Short> cData = clients.get(id);
            tcpServerSocket.send("DISCONNECTED".getBytes(),outToClient);
        }
    }

    /**
     * Builds a packet with a list of active game names with the maximum number of users allowed to log in and sends it to the client
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void sendGamesListMax(byte[] packet, OutputStream outToClient) {
        byte offset = (byte) "AVAILABLEGAMESMax".length();
        byte id = packet[offset++];
        if(clients.containsKey(id)){
            short packetLength = offset;
            short namesLength = 0;
            Pair<InetAddress,Short> cData = clients.get(id);
            byte[] type = "AVAILABLEGAMESMAX".getBytes();
            byte nrGames = (byte)games.keySet().size();
            byte[][] sendAux = new byte[nrGames][];
            byte[][] sendAuxNames = new byte[nrGames][];
            int i = 0;
            for(String gameName : games.keySet()){
                GameThread game = games.get(gameName);
                sendAux[i] = new byte[]{game.max_playes};
                sendAuxNames[i] = gameName.getBytes();
                packetLength += 1;
                packetLength+=sendAuxNames[i].length+1;
                namesLength += sendAuxNames[i].length;
                i++;
            }
            byte[] aux = new byte[packetLength - type.length];
            int cont = 0;
            for(int j = 0; j < i;j++){
                aux[cont++] = (byte) sendAuxNames[j].length;
                for(int k = 0;k < sendAuxNames[j].length;k++){
                    aux[cont++] = sendAuxNames[j][k];
                }
                aux[cont++] = sendAux[j][0];

            }
            byte[] send = Serialization.concat((short) (packetLength+1),type,new byte[]{(byte) i},aux);
            tcpServerSocket.send(send,outToClient);
        }
    }

    /**
     * Builds a packet with the list of active games and the players that joined each game(sending the full player packet),
     * and sends it to the client
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void sendGamesList(byte[] packet, OutputStream outToClient) {
        byte offset = (byte) "AVAILABLEGAMES".length();
        byte id = packet[offset++];
        if(clients.containsKey(id)){
            short packetLength = offset;
            short namesLength = 0;
            Pair<InetAddress,Short> cData = clients.get(id);
            byte[] type = "AVAILABLEGAMES".getBytes();
            byte nrGames = (byte)games.keySet().size();
            byte[][] sendAux = new byte[nrGames][];
            byte[][]sendAuxNames = new byte[nrGames][];
            int i = 0;
            for(String gameName : games.keySet()){
                GameThread game = games.get(gameName);
                sendAux[i] = PackDigest.players(game.getPlayers(),"");
                sendAuxNames[i] = gameName.getBytes();
                packetLength += sendAux[i].length ;
                namesLength += sendAuxNames[i].length;
                i++;
            }
            byte[] sendplayers = new byte[packetLength-offset+i];
            byte[] sendnames = new byte[namesLength+i];
            packetLength+=i*2;
            int k =0;
            int j = 0;
            for(byte[] x : sendAux){
                sendplayers[k] = (byte) x.length;
                k++;
                j++;
                for(byte b : x){
                    sendplayers[k] = b;
                    k++;
                }
            }
            k = 0;
            for(byte[] x : sendAuxNames){
                sendnames[k] = (byte) x.length;
                k++;
                for(byte b : x){
                    sendnames[k] = b;
                    k++;
                }
            }
            packetLength += namesLength+2;
            byte[] send = Serialization.concat(packetLength,type,Serialization.shortToByte(packetLength),new byte[]{(byte)i},sendnames,sendplayers);
            tcpServerSocket.send(send,outToClient);
        }
    }

    /**
     * Creates a new game (creates a new {@link GameThread}), and adds it to the
     * list of acive games. Sends a message "GAMECREATED" to the client.
     * If the gamename is taken, the game is not created and the message "GAMECREATIONFAILED" is sent.
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void createGame(byte[] packet, OutputStream outToClient) {
        int offset = 10;
        byte nrP = packet[offset++];
        short size = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset += 2 ;
        byte id = packet[offset++];
        String gameName = new String(Arrays.copyOfRange(packet,offset,size));
        Pair<InetAddress,Short> cData = clients.get(id);
        offset =size;
        short mapPackSize = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset += 2 ;
        Map map = PackDigest.map(Arrays.copyOfRange(packet,offset,offset+mapPackSize),"");
        offset+=mapPackSize;
        ArrayList<Box> boxes = PackDigest.boxes(Arrays.copyOfRange(packet,offset,packet.length),"");
        if(!games.containsKey(gameName)){
            GameThread thr = null;
            try {
                thr = new GameThread(Server.this,gamePort++,gamePort++,map,boxes,nrP,gameName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            games.put(gameName,thr);
            tcpServerSocket.send("GAMECREATED".getBytes(),outToClient);
        }else{
            tcpServerSocket.send("GAMECREATIONFAILED".getBytes(),outToClient);

        }
    }

    /**
     * Connects a client to a game, if the client has joined the main server,the game exists and is not full.Then
     * calls the method {@link GameThread#addPlayer(byte, Pair, OutputStream, Player)}.
     * Else, it sends a message "CONNECTIONTOGAMEFAILED" to the client.
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void connectToGame(byte[] packet, OutputStream outToClient) {
        int offset = 13;
        short size = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset += 2 ;
        byte id = packet[offset++];
        String gameName = new String(Arrays.copyOfRange(packet,offset,size));
        offset =size;
        Player p = PackDigest.player(Arrays.copyOfRange(packet,offset,packet.length),"");

        if(clients.containsKey(id) && !games.get(gameName).isFull()) {
            Pair<InetAddress,Short> cData = clients.get(id);
            if(games.containsKey(gameName)) {
                if(!games.get(gameName).isFull()) {
                    GameThread game = games.get(gameName);
                    clients.remove(id);
                    game.addPlayer(id, cData, outToClient, p);
                    clientsGames.put(id, gameName);
                }
            }
        }else{
            tcpServerSocket.send("CONNECTIONTOGAMEFAILED".getBytes(),outToClient);
        }


    }

    /**
     * Assigns an id for a client, registers the client as connected to the main server, sets the
     * username as logged in in the database, and sends the message "HANDSHAKEOK" to the client if
     * the user exists and it is not already logged in, if the password matches, and if the server is not full.
     * Sends the message "SERVERFULL" or "FAILEDUSERNAME" otherwise.
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     * @param trusty boolean tracking if handshake is after disconnecting from a game, or before that.
     */
    private void handshake(byte[] packet, OutputStream outToClient, boolean trusty) {
        int offset;
        if(!trusty)
            offset = 9;
        else offset = 18;
        short size = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset += 2 ;
        short port = Serialization.byteToShort(Arrays.copyOfRange(packet,offset,offset+2));
        offset += 2;
        byte playerId = packet[offset++];
        InetAddress addr;
        String username;
        String password;
        try {

            addr = InetAddress.getByAddress(Arrays.copyOfRange(packet,offset,size));

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        offset = size;
        byte ul = packet[offset++];
        username = new String(Arrays.copyOfRange(packet,offset,offset+ul));
        offset+=ul;
        byte pl = packet[offset++];
        password = new String(Arrays.copyOfRange(packet,offset,offset+pl));
        if((existsUser(username,password) && !loggedIn(username))||trusty) {
            if (passwordMatches(username, password) || trusty ){
                byte given_id;
                if(!trusty)
                    given_id = getnextId();
                else given_id = playerId;
                if (given_id != Byte.MAX_VALUE) {
                    clients.put(given_id, new Pair<>(addr, port));
                    setLoggedState(username, true);
                    byte[] score = Serialization.intToByteArray(getScore(username));
                    byte[] name = username.getBytes();
                    tcpServerSocket.clientsForLogin.put(outToClient,username);
                    tcpServerSocket.clientsForLoginId.put(outToClient,given_id);
                    tcpServerSocket.send(Serialization.concat((short) ("HANDSHAKEOK".length() + 1 + 4 + name.length), "HANDSHAKEOK".getBytes(), new byte[]{given_id}, score, name), outToClient);
                } else {
                    tcpServerSocket.send(Serialization.concat((short) ("SERVERFULL".length()), "SERVERFULL".getBytes()), outToClient);
                }
            }else{
                tcpServerSocket.send(Serialization.concat((short) ("FAILUSERNAME".length()), "FAILUSERNAME".getBytes()), outToClient);
            }
        }else{
            tcpServerSocket.send(Serialization.concat((short) ("FAILUSERNAME".length()), "FAILUSERNAME".getBytes()), outToClient);
        }
    }

    /**
     * Creates and starts a thread that checks every 2 seconds if there are any idle clients.
     * It uses the actual server time and a table with the time of the last message received from
     * the clients connected to the main server or to a game. If the last message was received more than
     * 10 seconds ago, it disconnects the client from the game/server.
     * See {@link GameThread#closeconnection(byte)}
     */
    private void handle_idle_clients() {
        Thread task  = new Thread(() -> {
            boolean ok = true;
            while (ok) {
                long lastMessageTime;
                ConcurrentHashMap<Byte,Long> lastPacketTime;
                for (GameThread thread : games.values()) {
                    lastPacketTime = thread.lastPacketTime;
                    lastMessageTime = getServerTime();
                    ArrayList<Byte> to_remove = new ArrayList<>();
                    Set<java.util.Map.Entry<Byte, Long>> aux = lastPacketTime.entrySet();
                    for (java.util.Map.Entry<Byte, Long> e : aux) {
                        if (lastMessageTime - e.getValue() > GameConstants.MAX_IDLE_MILISEC) {
                            System.out.println(lastMessageTime - e.getValue());
                            to_remove.add(e.getKey());
                        }
                    }
                    for (Byte b : to_remove) {
                        lastPacketTime.remove(b);
                        thread.closeconnection(b);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            ok = false;
                        }
                    }

                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    ok = false;
                }
            }
        });
        task.start();
    }

    /**
     * @return returns the server time
     */
    public long getServerTime (){
        return serverTime;
    }

    /**
     * @return returns the next available id (range -128,126). 127 is reserved for server purposes.
     */
    private byte getnextId() {
        for(int i = 0; i < ids.length;i++){
            if(ids[i] == 0){
                ids[i] = (byte) (-128 + i);
                return ids[i];
            }
        }
        return Byte.MAX_VALUE;
    }

    /**
     * Closes connection with client. Sets username as logged out, registers the score if it is a highscore,
     * frees the client id, removes client from it's game (if connected to one).
     *
     * See{@link MessageType}
     * @param packet packet to process
     * @param outToClient stream to write after processing
     */
    private void close(byte[] packet, OutputStream outToClient) {
        int offset = "CLOSECONNECTION".length();
        byte idC = packet[offset++];
        int score = Serialization.fromByteArray(Arrays.copyOfRange(packet,offset,offset+4));
        offset+=4;
        String username = new String(Arrays.copyOfRange(packet,offset,packet.length));
        setLoggedState(username, false);
        System.out.println("setting username " + username + " to false");
        if (getScore(username) < score)
            setScore(username, score);
        tcpServerSocket.clientsForLogin.remove(outToClient);
        tcpServerSocket.addBadClient(idC,outToClient);
        if(clientsGames.containsKey(idC))
            clientsGames.remove(idC);
        clients.remove(idC);
        ids[idC+128] = 0;
    }

    /**
     * Starts a thread that prints every 10 seconds the ammount of memory used by the server.
     * Then creates a server object (which starts the server)
     *
     * @param args not used
     */
    public static void main(String[] args){
        int mb = 1024*1024;
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //System.gc();
                System.out.println("Used Memory:"
                        + (runtime.totalMemory() - runtime.freeMemory()) / mb);
                //System.out.println("calling garbage collector");
            }
        },0,10000);
        Server server = new Server(GameConstants.SERVER_TCP_PORT);
    }

    /**
     * Method used only one time, to create the table in the database.
     */
    public static void create_table(){
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost/knightshade";
            Properties props = new Properties();
            props.setProperty("user","paul_mihaita");
            props.setProperty("password","admin");
            c = DriverManager.getConnection(url, props);
            Statement s = c.createStatement();
            String sql = "CREATE TABLE USERS " +
                    "( NAME   TEXT PRIMARY KEY  NOT NULL, " +
                    " PASS    TEXT     NOT NULL, " +
                    " SCORE INT NOT NULL, "+
                    " LOGGED_IN BOOLEAN NOT NULL)";
            String sqldrop = "DROP TABLE USERS";
            String grant = "GRANT ALL PRIVILEGES ON TABLE USERS to paul";
            DatabaseMetaData dbm = c.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "users", null);
            if (tables.next()) {
                s.executeUpdate(grant);
                s.executeUpdate(sqldrop);
            }
            s.executeUpdate(sql);
            s.executeUpdate(grant);
            s.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Mehtod used to load the database into the server, when the server starts.
     */
    private  void load_db() {
        try {
            db_connection = DriverManager.getConnection("jdbc:postgresql://localhost/knightshade","paul_mihaita","admin");
            //deleteAllUsers();
            /*PreparedStatement s = db_connection.prepareStatement("select * from users");
            ResultSet data = s.executeQuery();
            while(data.next()){
                String name = data.getString("NAME");
                String pass = data.getString("PASS");
                registeredUsers.put(name,pass);

            }
            */
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Registers a new user, if it does not exist.
     *
     * @param name username to register
     * @param pass password for that username
     */
    private void insertUser(String name, String pass){
        String insertTableSQL = "INSERT INTO USERS"
                + "(NAME,PASS,SCORE,LOGGED_IN) VALUES"
                + "(?,?,?,?)";
        try {
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setString(1,name);
            s.setString(2,pass);
            s.setBoolean(4,false);
            s.setInt(3,0);
            s.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the password introduced by the user matches the password that user has in the database.
     * @param username the username to check
     * @param password the password to check
     * @return returns true or false if the password matches or not
     */
    private boolean passwordMatches(String username, String password) {
        try {
            String insertTableSQL = "SELECT * FROM USERS WHERE USERS.NAME LIKE ?";
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setString(1,username);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
                String pass = rs.getString(2);
                return pass.equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the user exists or not.
     * @param name username to check
     * @param pass password to check
     * @return returns true or false of the user exists or not
     */
    private boolean existsUser(String name, String pass){
        try {
            String insertTableSQL = "SELECT * FROM USERS WHERE USERS.NAME LIKE ?";
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setString(1,name);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the user is logged in or not
     *
     * @param name to check
     * @return returns true or false if the user is logged in or not.
     */
    public boolean loggedIn(String name){
        try {
            String insertTableSQL = "SELECT * FROM USERS WHERE USERS.NAME LIKE ?";
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setString(1,name);
            ResultSet rs = s.executeQuery();


            if (rs.next()) {
                return rs.getBoolean(4);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Getting the score for a user
     *
     * @param name username to get the score for
     * @return returns the score if the user exists, 0 otherwise.
     */
    private int getScore(String name){
        try {
            String insertTableSQL = "SELECT * FROM USERS WHERE USERS.NAME LIKE ?";
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setString(1,name);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
                return rs.getInt(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Sets a field in the database if the user is logged in or not.
     *
     * @param name username to set the state
     * @param state state to set, false or true
     */
    protected void setLoggedState(String name,boolean state){
        try {
            String insertTableSQL = "UPDATE USERS SET LOGGED_IN = ? WHERE USERS.NAME LIKE ?";
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setBoolean(1,state);
            s.setString(2,name);
            s.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the score for a username.
     *
     * @param name sets the score for this username
     * @param score score to set
     */
    private void setScore(String name,int score){
        try {
            String insertTableSQL = "UPDATE USERS SET SCORE = ? WHERE USERS.NAME LIKE ?";
            PreparedStatement s = db_connection.prepareStatement(insertTableSQL);
            s.setInt(1,score);
            s.setString(2,name);
            s.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to clear the databse, not used now.Used before when the server starts.
     */
    private void deleteAllUsers(){
        String s = "DELETE FROM USERS";
        try {
            PreparedStatement st = db_connection.prepareStatement(s);
            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a game from the list of active games.
     *
     * @param name the name of the game to remove
     */
    public void endGame(String name){
        games.remove(name);
    }

}
