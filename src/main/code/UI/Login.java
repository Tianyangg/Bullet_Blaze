package main.code.UI;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.code.Networking.Client;

import java.awt.*;

//import javax.xml.soap.Text;

public class Login extends Pane {
    private int WIDTH;
    private int HEIGHT;
    private int buttonwidth = 200;
    private int buttonheight = 40;

    private Pane root = new Pane();
    public VBox vb = new VBox(0);
    public GridPane gp = new GridPane();
    public GameName alert = new GameName("Password Incorrect");


    /**
     * login with the client
     * @param window
     * @param client
     * @throws Exception
     */
    public Login(Stage window, Client client) throws Exception{
        root.getStylesheets().add("main/code/UI/tablecss.css");
        window.setFullScreen(true);
        getwh();
        addBackground();
        addTitle();
        double x = WIDTH/3 ;
        double y = HEIGHT/3 + 60;

        gp.setTranslateX(x);
        gp.setTranslateY(y);
        gp.setHgap(20);
        gp.setVgap(20);

        Text userText = new Text("Username");
        userText.setId("fancytext");
        gp.add(userText,0,0);

        Text passwordText = new Text("Password");
        passwordText.setId("fancytext");
        gp.add(passwordText,0,1);

        Text usernameAlert = new Text("wrong password or username");
        usernameAlert.setId("alertText");
        usernameAlert.setVisible(false);
        gp.add(usernameAlert,2,1);

        TextField tf = new TextField();
        tf.setPromptText("Username");
        tf.setPrefHeight(buttonheight);
        tf.setPrefWidth(buttonwidth);
        gp.add(tf,1,0);

        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");
        pw.setPrefHeight(buttonheight);
        pw.setPrefWidth(buttonwidth);
        gp.add(pw,1,1);

        /**
         * add the login button to the gridpane
         */

        Button bt = new Button("Login");
        bt.setOpacity(1);
        bt.setPrefHeight(buttonheight);
        bt.setPrefWidth(buttonwidth);
        bt.setOnAction((ActionEvent arg0) -> {
            hidepasswordAlert();
            String username = tf.getText();
            String password = pw.getText();
            client.setUsername(username);
            client.setPassword(password);

           if(client.handshake()){
                System.out.println("login successful");
                multiPlayer mp = new multiPlayer(window,client);
            } else{
               usernameAlert.setVisible(true);
           }
        });
        gp.add(bt,1,2);

        //gp.add(bt,1,2);
        Button back = new Button("Back");
        bt.setOpacity(1);
        back.setPrefHeight(buttonheight);
        back.setPrefWidth(buttonwidth);
        back.setOnAction((ActionEvent arg0) ->{
            try{
                client.end_client();
                Stage w = (Stage) gp.getScene().getWindow();
                BulletMenumain test = new BulletMenumain();
                test.startwithmulti(w);
            } catch (Exception e) {
                System.out.println(e);
            }

        });
        gp.add(back,0,2);
        //back.setId();
        Scene scene = new Scene(createContent());

        root.getChildren().add(gp);

        window.setScene(scene);
    }


    /**
     * show password alert when wrong password input
     */
    private void passwordAlert(){
        double x = WIDTH/8;
        double y = HEIGHT/5;

        alert.setTranslateX(x);
        alert.setTranslateY(y);
        root.getChildren().add(alert);
        alert.setVisible(false);

    }


    private void hidepasswordAlert(){
        alert.setVisible(false);
    }

    /**
     * add the background and title to the pane
     * @return root
     */
    private Parent createContent() {
        addBackground();
        addTitle();
        passwordAlert();
        //showpasswordAlert();
        return root;
    }

    private void addBackground() {
        ImageView imageView = new ImageView(new Image(getClass().getResource("res/bg.png").toExternalForm()));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);

        root.getChildren().add(imageView);
    }

    private void addTitle() {
        double x = WIDTH/3;
        double y = HEIGHT/3 - 50;

        GameName title = new GameName("BulletBlaze");
        title.setTranslateX(x);
        title.setTranslateY(y);
        //title.setTranslateX(WIDTH / 2 - title.getTitleWidth() / 2);
        //title.setTranslateY(HEIGHT / 3);
        GameName login = new GameName("Login");
        login.setTranslateX(x);
        login.setTranslateY(y + 50);
        root.getChildren().add(title);
        root.getChildren().add(login);
    }

    public void getwh(){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.WIDTH = gd.getDisplayMode().getWidth();
        this.HEIGHT = gd.getDisplayMode().getHeight();
    }


}
