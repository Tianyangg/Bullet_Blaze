package main.code.Networking;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Using the class {@link ServerSocket} for tcp communication, accepting connections and sending/receiving data to/from the clients
 *
 * @author Paul Mihaita
 */
public abstract class TCPServerSocket {
    public Server server;
    protected ServerSocket welcomeSocket;
    protected Thread acceptConnections;
    private boolean openForConnection = true;
    protected short port ;
    protected HashMap<OutputStream,Byte> c = new HashMap<>();
    private DataOutputStream auxout;
    ConcurrentHashMap<OutputStream,String> clientsForLogin = new ConcurrentHashMap<>();
    ConcurrentHashMap<OutputStream,Byte> clientsForLoginId = new ConcurrentHashMap<>();

    /**
     * Creates and starts a thread that waits for connections. Also check {@link TCPServerSocket#acceptConnection()}.
     *
     * @param port the socket port
     * @param server the server object needed for packet time arrival
     */
    public TCPServerSocket(short port, Server server){
        this.server = server;
        try {
            this.welcomeSocket = new ServerSocket(port);
            this.port = port;
            acceptConnections = new Thread(()->acceptConnection());

            acceptConnections.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Keeping track of clients that don't terminate gracefully, the add method is used
     * when getting a IOException or Socket closed exception server side.
     *
     * @param id client id
     * @param outToClient client {@link OutputStream}
     */
    public void addBadClient(byte id,OutputStream outToClient){
        c.remove(outToClient);
        c.put(outToClient,id);

    }

    /**
     * Method describing what the thread listening for connections does. When a connection is established
     * a thread listening for packets is open aswell. Also see {@link TCPServerSocket#listenFromClientTCP(DataInputStream, OutputStream)}
     */
    private void acceptConnection() {

        while(openForConnection){
            Socket conn = accept();
            try {
                DataInputStream in  = new DataInputStream(conn.getInputStream());
                OutputStream out = conn.getOutputStream();
                Thread clientCommunication = new Thread(() -> listenFromClientTCP(in,out));
                c.put(out, (byte) 127);
                clientCommunication.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a message to a client. Sending first the packet size, and then the message.
     *
     * @param pack byte array to send
     * @param clientStream the client stream to send the data to
     * @return this method returns false if an exception in thrown, true if the message is sent
     */
    public boolean send(byte[] pack,OutputStream clientStream){
        try {
                    auxout = new DataOutputStream(clientStream);
                    auxout.writeInt(pack.length);
                    auxout.flush();
                    auxout.write(pack);
                    auxout.flush();
                    return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Describes what the thread listening for packets does; calls the abstract method process, giving it the data received,
     * client output stream and packet time of arrival. Catching an IOException triggers server cleanup commands.
     *
     * @param in stream to read data from
     * @param out stream to write after processing the data
     */
    protected void listenFromClientTCP(DataInputStream in, OutputStream out) {
        byte[] tcppacket;
        int psize;
        long time;
        while(c.get(out) == 127){
                try {
                    psize = in.readInt();
                    tcppacket = new byte[psize];
                    in.readFully(tcppacket);
                    time = server.getServerTime();
                    processTCP(tcppacket, out,time);

                } catch (IOException e) {
                    if(clientsForLogin.get(out)!=null) {
                        server.setLoggedState(clientsForLogin.get(out), false);
                        System.out.println("set logged state in the exception " + clientsForLogin.get(out));
                        server.ids[128 + clientsForLoginId.get(out)] = 0;
                        clientsForLoginId.remove(out);
                        clientsForLogin.remove(out);
                    }
                    break;

                }
        }
    }

    /**
     * Data processing method
     *
     * @param packet the bytes of data
     * @param outToClient client stream to write the answer to
     * @param time packet arrival time relative to the start of the server
     */
    protected abstract void processTCP(byte[] packet, OutputStream outToClient, long time);

    /**
     * Accepts a client connection, and returns the socket for that connection. Also see {@link TCPClientSocket}
     *
     * @return returns the socket after accepting a connection, or null if IOException is caught.
     */
    public Socket accept() {
        try {
            return welcomeSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
