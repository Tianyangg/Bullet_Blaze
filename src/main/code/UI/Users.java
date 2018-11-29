package main.code.UI;
/**
 * The Class for User info in the Lobby page
 * Username
 * Credits
 *
 */

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
public class Users {
    private final SimpleStringProperty Username;
    private final SimpleStringProperty Credits;
    private final SimpleStringProperty Status;
    private final SimpleStringProperty CharId;

    Users(String uid, String status, byte charId, String credits){
        this.Username = new SimpleStringProperty(uid);
        this.Status = new SimpleStringProperty(status);
        this.Credits = new SimpleStringProperty(credits);
        this.CharId = new SimpleStringProperty(String.valueOf(charId));
    }

    /**
     * UserName
     * @return string
     */
    public final String getUsername(){
        return Status.get();
    }

    public void setUsername(String status){
        Status.set(status);

    }
    public void setCharId(String username){
        CharId.set(username);

    }
    public final String getCharId(){return CharId.get();};
    public StringProperty UsernameProperty(){
        return Username;
    }
    public  StringProperty CharIdProperty(){
        return CharId;
    }


    /**
     * Status Ready/not Ready
     */
    public final String getStatus(){
        return Username.get();
    }

    public void setStatus(String username){
        Username.set(username);

    }
    public StringProperty StatusProperty(){
        return Status;
    }

    /***
     * Credits default is 0
     * @return string
     */
    public final String getCredits(){
        return Credits.get();
    }

    public void setcredits(String credits){
        Credits.set(credits);
    }
    public StringProperty creditsProperty(){
        return Credits;
    }



}
