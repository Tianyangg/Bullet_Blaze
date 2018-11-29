package main.code.Networking;

import com.sun.org.apache.xpath.internal.operations.String;
import javafx.util.Pair;
import main.code.Networking.utils.MessageType;
import main.code.Networking.utils.PackDigest;
import main.code.Networking.utils.Serialization;
import main.code.utils.GameConstants;
import main.code.utils.Map;
import main.code.utils.Player;
import org.joml.internal.Runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static main.code.utils.GameConstants.MAX_PACK_SIZE;

/**
 * @author Paul Mihaita
 *
 * Using the class {@link Socket} for tcp communication, ending/receiving data to/from the server
 */
public abstract class TCPClientSocket {
    private  DataInputStream inputStream;
    private  DataOutputStream outputStream;
    protected  InetAddress serverAddress;
    protected Socket socket;
    protected Thread listenFromServer;
    private boolean listening ;
    protected short serverPort ;

    /**
     * Creates a new {@link TCPClientSocket}, makes the connection to the server and
     * opens a thread that listens for data. Also see {@link TCPClientSocket#listen(DataInputStream, DataOutputStream)}
     *
     * @param addr socket address
     * @param serverPort socket port
     */
    public TCPClientSocket(InetAddress addr, short serverPort) {
        try {
            this.socket = new Socket(addr,serverPort);
            this.listening = true;
            this.serverAddress = addr;
            this.serverPort = serverPort;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            listenFromServer = new Thread(()->listen(inputStream,outputStream));
            listenFromServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the streams and socket.
     */
    public void close_streams(){

        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abstract method close, left open to implementation for any client.
     */
    protected abstract void close();


    /**
     * Describes what happens when a packet is received. The method {@link TCPClientSocket#processTCP(byte[], OutputStream)} is called.
     *
     * @param inputStream stream to read data from
     * @param outputStream stream to write the result after processing the input
     */
    public void listen(DataInputStream inputStream, DataOutputStream outputStream) {
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
        byte[] tcppacket ;
        int psize;
        while(listening){
            try {
                psize = inputStream.readInt();
                tcppacket = new byte[psize];
                inputStream.readFully(tcppacket);
                processTCP(tcppacket,outputStream);

            }
            catch (IOException e) {
                if(!socket.isClosed())
                    e.printStackTrace();
            }
        }
    }

    /**
     * Sending data to the server.
     *
     * @param pack the bytes to send
     */
    public void send(byte[] pack){
        try {
            outputStream.writeInt(pack.length);
            outputStream.flush();
            outputStream.write(pack);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when data is received and needs processing.
     *
     * @param packet bytes to process
     * @param outToClient stream to write the results to
     */
    protected abstract void processTCP(byte[] packet, OutputStream outToClient);

    /**
     * closing the listening thread, and this triggers a shutdown hook that calls
     * the abstract method {@link TCPClientSocket#close()}
     */
    public void end() {
        listening = false;
    }
}
