package main.code.UI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.code.utils.Player;

import java.util.ArrayList;

public class Rooms {
    private final SimpleStringProperty roomName;
    private final SimpleStringProperty usernumber;
    private final SimpleStringProperty maxUser;
    private ArrayList<Player> players;


    Rooms(String rid, String unumber,String maxuser){
        this.roomName = new SimpleStringProperty(rid);
        this.usernumber = new SimpleStringProperty(unumber);
        this.maxUser = new SimpleStringProperty(maxuser);
    }

    /**
     * get roomName
     * @return string
     */
    public final String getRoomName(){
        return roomName.get();
    }

    /**
     * set the roomname with rid
     * @param rid
     */
    public void setRoomName(String rid){
        roomName.set(rid);

    }

    /**
     * The maximum users allowed in the room
     * @return String
     */
    public String getMaxUsers(){
        return maxUser.get();
    }

    /**
     * return the maximum user
     * @return
     */
    public StringProperty maxUserProperty(){return maxUser;}
    public ArrayList<Player> getPlayers(){
        return players;
    }

    /**
     * set the number of players
     * @param p
     */
    public void setPlayers(ArrayList<Player> p){
        players = p;
    }
    public StringProperty roomNameProperty(){
        return roomName;
    }

    /**
     * get the usernumber
     * @return
     */
    public final String getUsernum(){
        return usernumber.get();
    }

    /**
     * set the usernumber
     * @param unum
     */
    public void setunum(String unum){
        usernumber.set(unum);
    }
    public StringProperty usernumberProperty(){
        return usernumber;
    }
}
