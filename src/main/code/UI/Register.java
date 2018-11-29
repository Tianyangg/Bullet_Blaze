package main.code.UI;


import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javafx.scene.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.code.Networking.Client;

import java.awt.*;

public class Register extends VBox {
    public VBox vb = new VBox(0);

    private int WIDTH;
    private int HEIGHT;
    private int buttonwidth = 200;
    private int buttonheight = 40;

    private Pane root = new Pane();
    public GridPane gp = new GridPane();

    public Register(Stage window, Client client){

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

        Text usernameAlert = new Text("Please input the username");
        usernameAlert.setId("alertText");
        usernameAlert.setVisible(false);
        gp.add(usernameAlert,2,0);

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

        Button bt = new Button("Register");
        bt.setOpacity(1);
        bt.setPrefHeight(buttonheight);
        bt.setPrefWidth(buttonwidth);
        bt.setOnAction((ActionEvent arg0) -> {
            String username = tf.getText();
            String password = pw.getText();
            if (tf.getText() == null || tf.getText().trim().isEmpty()){
                usernameAlert.setVisible(true);
            } else {
                System.out.print(tf.getText());
                client.setUsername(username);
                client.setPassword(password);
                client.addUser();
                try {
                    Login lg = new Login(window, client);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
     * add the background to the pane
     * @return
     */
    private Parent createContent() {
        addBackground();
        addTitle();
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
        GameName login = new GameName("Register");
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
