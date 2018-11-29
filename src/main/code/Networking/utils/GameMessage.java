package main.code.Networking.utils;


public class GameMessage {
    private MessageType type;
    private byte[] message;

    public GameMessage(byte[] message,MessageType type){
        setMessage(message);
        setType(type);
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }
}
