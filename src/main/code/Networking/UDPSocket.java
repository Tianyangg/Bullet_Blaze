package main.code.Networking;

import java.io.IOException;
import java.net.*;

import static main.code.utils.GameConstants.MAX_PACK_SIZE;

/**
 * Using the class {@link DatagramSocket} to do tasks needed by both server and client ( sending and listening for data )
 *
 *@author Paul Mihaita
 */
public abstract class UDPSocket {
    private Server server;
    protected short port;
    protected DatagramSocket socket;
    protected boolean listening  ;
    protected byte[] packet = new byte[MAX_PACK_SIZE];
    protected Thread receiveThread;

    /**
     * Creating a {@link DatagramSocket} and opening a thread that listens for packets. Also giving it a {@link Server} object for
     * getting the server time when a message is received.
     *
     * @param port the port of the socket
     * @param server the server object for getting the time
     */
    public UDPSocket(short port,Server server){
        this.server = server;
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            listening = true;
            receiveThread = new Thread(()->listenForPackets());
            receiveThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creating a {@link DatagramSocket} and opening a thread that listens for packets.This constructor is used for the client.
     * The server is null, because the client doesn't need the time of arrival of the packet.
     *
     * @param udpPort the port of the socket
     */
    public UDPSocket(short udpPort) {
        this.server = null;
        this.port = udpPort;
        try {
            socket = new DatagramSocket((int)port);
            listening = true;
            receiveThread = new Thread(()->listenForPackets());
            receiveThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method describing what the listening thread does. Listens for Datagram Packets and
     * calls abstract method process when one is received.
     */
    private void listenForPackets() {
        DatagramPacket packetData = new DatagramPacket(new byte[MAX_PACK_SIZE],MAX_PACK_SIZE);
        long time= 0;
        while(listening){
            try {
                socket.receive(packetData);
                if(server != null)
                    process(packetData,server.getServerTime());
                else process(packetData,0);
            } catch (IOException e) {
                if(!socket.isClosed())
                    e.printStackTrace();
            }
        }
    }

    /**
     * The method that processes the received messages, it is abstract because the server/client
     * do different things, but they both need basic functionality (sending and receiving).
     *
     * @param packetData the data to process
     * @param time packet time arrival (relative to the start of the server)
     */
    protected abstract void process(DatagramPacket packetData, long time) ;

    /**
     * Sends a byte array at the specified address and port
     *
     * @param data the byte array
     * @param address the address to send at
     * @param port the port at that address
     */
    public void send(byte[] data, InetAddress address, short port){
        DatagramPacket toSend = new DatagramPacket(data,data.length,address,port);
        try {
            socket.send(toSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closing the listening thread and then closing the @see DatagramSocket.
     */
    public void close() {
        listening = false;
        socket.close();
    }
}
