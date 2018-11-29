
/**
 * Game Lobby for the Multi Player Mode
 * */
package main.code.UI;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import main.code.Networking.Client;
import main.code.engine.GameEngine;
import main.code.engine.IGameLogic;
import main.code.game.OnlineMode;
import main.code.game.audio.AudioHandler;
import main.code.utils.Player;

import java.applet.AudioClip;
import java.awt.*;
import java.util.*;
import java.util.List;

import static main.code.utils.GameConstants.NAME_OF_GAME;

public class Lobby extends Application {
    private  Thread updateTable;
    private  String gameName;
    private Rooms lobby;

    //private static final int WIDTH = 1280;
    //private static final int HEIGHT = 720;

    private int WIDTH = 1280;
    private int HEIGHT = 720;

    private static final Integer STARTTIME = 15;
    private Timeline timeline;
    private Label timerLabel = new Label();
    private IntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIME);
    private Pane root = new Pane();
    private Scene scene ;
    private Stage window;
    private AudioHandler audioHandler = new AudioHandler();
    private javafx.scene.media.AudioClip clickSound = audioHandler.clickSound;

    private VBox buttonbox = new VBox(10);
   // private ImageView texture = new ImageView(new javafx.scene.image.Image(getClass().getResource("slice02_02.png").toExternalForm()));


    private Client client;

    public byte character = 1;

    private VBox charBox = new VBox(10);
    // for the Table in the lobby
    private TableView<Users> table = new TableView<>();
    private  ObservableList<Users> data ;
    private multiPlayer mp;

    private ArrayList<Users> get_user_data(Rooms lobby){
        ArrayList<Users> users = new ArrayList<>();
        ArrayList<Player> players = lobby.getPlayers();
        for(Player p : players){
            users.add(new Users(String.valueOf(p.getId()),p.getStatus(), p.get_charId(), String.valueOf(p.getScore())));
        }
        return users;
    }

    private Thread game;
    private GameEngine gameEng;
    private boolean ready = false;
    private Thread  notReadyThread = getNotReadyThread();
    private Thread readyThread = getReadyThread();
    private Thread gameThread = getGameThread();
    private boolean readyflag= false;

    public BulletMenumain bulletMenumain = new BulletMenumain();

    /**
     * disable button which select the character
     */
    public void disablechar(){
        MenuItem n = (MenuItem) buttonbox.getChildren().get(0);
        n.setDisableEffect();
        n.setDisable(true);
    }

    /**
     * enable the button which allow the user to select character
     */
    public void enablechar(){
        MenuItem n = (MenuItem) buttonbox.getChildren().get(0);
        n.setEnableEffect();
        n.setDisable(false);
    }

    private List<Pair<String, Runnable>> buttons = Arrays.asList(
            new Pair<String, Runnable>("  Character", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                character();
            }),
            new Pair<String, Runnable>("  Ready", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                if (!readyflag){
                    if(!notReadyThread.isAlive() && ! gameThread.isAlive() && !readyThread.isAlive()) {
                        readyThread = getReadyThread();
                        gameThread = getGameThread();
                        readyThread.start();
                        gameThread.start();
                        readyflag = ! readyflag;
                        //set the text

                        MenuItem n = (MenuItem) buttonbox.getChildren().get(1);
                        n.changefirstText("   Not Ready");
                        diablechar();
                    }
                    disablechar();

                } else {
                    if(readyThread.isAlive() && !notReadyThread.isAlive()) {
                        notReadyThread = getNotReadyThread();
                        notReadyThread.start();
                        readyflag = ! readyflag;

                        MenuItem n = (MenuItem) buttonbox.getChildren().get(1);
                        n.changefirstText("   Ready");
                        enablechar();
                    }
                    enablechar();
                }

            }),

            new Pair<String, Runnable>("  Exit", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                if(gameEng!=null)
                    gameEng.stop();
                updateTable.interrupt();
                try {
                    updateTable.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(readyThread.isAlive()){
                    notReadyThread = getNotReadyThread();
                    notReadyThread.start();
                    try {
                        notReadyThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                client.disconnectFromGame();
                System.out.println("disconnected");
                multiPlayer mp = new multiPlayer(window,client);

            })
    );

    private List<Pair<String, Runnable>> Charselect = Arrays.asList(
            new Pair<String, Runnable>("  normal", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                character = 1;
                client.player.set_charId(character);
                client.sendPlayer(client.player);
                removecharacter();
                MenuItem item = (MenuItem) buttonbox.getChildren().get(0);
                item.changeresult("Normal");

            }),
            new Pair<String, Runnable>("  soldier", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                character = 2;
                client.player.set_charId(character);
                client.sendPlayer(client.player);

                removecharacter();
                MenuItem item = (MenuItem) buttonbox.getChildren().get(0);
                item.changeresult("soldier");

            }),
            new Pair<String, Runnable>("  unnamed", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                character = 3;
                removecharacter();
                client.player.set_charId(character);
                client.sendPlayer(client.player);

                MenuItem item = (MenuItem) buttonbox.getChildren().get(0);
                item.changeresult("unnamed");

            }),
            new Pair<String, Runnable>("  woman soldier", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                character = 4;
                removecharacter();
                client.player.set_charId(character);
                client.sendPlayer(client.player);

                MenuItem item = (MenuItem) buttonbox.getChildren().get(0);
                item.changeresult("Female");

            })

    );

    /**
     * add the buttons to the vbox
     * the buttons are defined by the list
     * set Vbox to (x, y)
     * @param x
     * @param y
     * @param list
     * @param vbox
     */
    private void addButtons(double x, double y, List<Pair<String, Runnable>> list, VBox vbox) {
            if(root.getChildren().contains(vbox)) root.getChildren().remove(vbox);

            vbox.setTranslateX(x);
            vbox.setTranslateY(y);
            list.forEach(data -> {
                MenuItem item = new MenuItem(data.getKey());
                item.setOnAction(data.getValue());
                Rectangle clip = new Rectangle(300, 50);
                item.setClip(clip);

                vbox.getChildren().addAll(item);
                vbox.setBackground(new Background(new BackgroundFill(null, null,null)));
            });

            root.getChildren().add(vbox);

    }

    /**
     * load the background image and add it to the roor
     */
    private void addBackground() {
        ImageView imageView = new ImageView(new javafx.scene.image.Image(getClass().getResource("res/bg.png").toExternalForm()));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);

       // texture.setTranslateX(WIDTH/3);
       // texture.setTranslateY(HEIGHT*2/3);

        root.getChildren().addAll(imageView);
    }

    /**
     * set up the background and content
     * @param x coordinate x
     * @param y coordinate y
     */
    public void setup(double x, double y){
        /**
         * add the background image
         * */
        addBackground();

        double timerx = 6*x/10;
        double timery = y/2;

        addButtons(timerx, timery - 100, buttons, buttonbox);
        /**
         * set the text for character
         */
        MenuItem item = (MenuItem) buttonbox.getChildren().get(0);
        if (character == 1){
            client.player.set_charId((byte) 1);
            item.changeresult("Normal");
        }
        if (character == 2){
            client.player.set_charId((byte) 2);
            item.changeresult("soldier");
        }
        if (character == 3){
            client.player.set_charId((byte) 3);
            item.changeresult("require a name");
        }
        if (character == 4){
            client.player.set_charId((byte) 4);
            item.changeresult("Woman soldier");
        }


        Button button = new Button();
        button.setTranslateX(timerx);
        button.setTranslateY(timery - 200);
        button.setId("optionButton");
        button.setText("Assume all the players are ready");
        button.setOnAction(new EventHandler<ActionEvent>() {


            public void handle(ActionEvent event) {
                addTimer(timerx,timery);

                if (timeline != null) {
                    timeline.stop();
                }
                timeSeconds.set(STARTTIME);
                timeline = new Timeline();
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(STARTTIME+1),
                                new KeyValue(timeSeconds, 0)));
                timeline.playFromStart();
            }
        });

       //root.getChildren().add(button);


    }

    /***
     * Add the timer to the panel when all the players are ready
     * Set the <NOT Ready> false when the timer start
     */
    private void addTimer(double x, double y){
        timerLabel.setTranslateX(x);
        timerLabel.setTranslateY(y);
        // Bind the timerLabel text property to the timeSeconds property
        timerLabel.textProperty().bind(timeSeconds.asString());
        timerLabel.setTextFill(Color.GREEN);
        timerLabel.setStyle("-fx-font-size: 10em;");
        root.getChildren().add(timerLabel);

        buttonbox.setVisible(false);

    }
    /**
     * Add the table to the Scene
     */
    public void configureTable(double x,double y){
        table.setTranslateX(x);
        table.setTranslateY(y);

        table.setEditable(false);
        table.setPrefSize(400,300);

        Callback<TableColumn, TableCell> stringCellFactory;

        //TableColumn<Lobby, String> roomsCol = new TableColumn<>();
        TableColumn UserCol= new TableColumn<>();
        UserCol.setText("Username");
        UserCol.setEditable(false);
        UserCol.setPrefWidth(100);
        UserCol.setMinWidth(100);
        UserCol.setCellValueFactory(
                new PropertyValueFactory<Users, String>("Username"));

        TableColumn statusCol = new TableColumn<>();
        statusCol.setText("Status");
        statusCol.setEditable(false);
        statusCol.setPrefWidth(100);
        statusCol.setMinWidth(100);
        statusCol.setCellValueFactory(
                new PropertyValueFactory<Users, String>("Status"));

        TableColumn character = new TableColumn();
        character.setText("Character");
        character.setPrefWidth(100);
        character.setMinWidth(100);
        character.setCellValueFactory(
                new PropertyValueFactory<>("CharId"));

        TableColumn creditsCol = new TableColumn();
        creditsCol.setText("Credits");
        creditsCol.setPrefWidth(100);
        creditsCol.setMinWidth(100);
        creditsCol.setCellValueFactory(
                new PropertyValueFactory<>("Credits"));
        // get the mouse action to

        table.getColumns().clear();
        table.getColumns().addAll(UserCol, statusCol, character, creditsCol);
    }
    public void addTable(double x, double y){
        if(root.getChildren().contains(table))root.getChildren().remove(table);

        Scene scene = root.getScene();
        // scene.getStylesheets().add("main/code/UI/tablecss.css");

        table.setOpacity(0.9);

        root.getStylesheets().add("main/code/UI/tablecss.css");

        ArrayList<Users> users = new ArrayList<>();
        for(Player p : client.getGamePlayers()){
            users.add(new Users(String.valueOf(p.name),p.getStatus(),p.get_charId(),String.valueOf(p.getScore())));
        }
        table.getItems().clear();
        table.setItems(FXCollections.observableArrayList(users));
        root.getChildren().add(table);


    }

    public void updateTable(){
        ArrayList<Users> users = new ArrayList<>();
        for(Player p : client.getGamePlayers()){
            users.add(new Users(String.valueOf(p.name),p.getStatus(),p.get_charId(),String.valueOf(p.getScore())));
        }
        table.setItems(FXCollections.observableArrayList(users));

    }



    private Parent creatcontent(){
        getwh();
        double btx = 6*WIDTH/10;
        double bty = 5*HEIGHT/10;

        double tbx = WIDTH/4;
        double tby = HEIGHT/3;
        setup(WIDTH, HEIGHT);
        configureTable(tbx,tby);
        addTable(tbx, tby);
        root.getChildren().remove(table);
        root.getChildren().add(table);
       // root.getChildren().remove(buttons);
        //addButtons(btx, bty, buttons, buttonbox);
        return root;
    }

    public void getwh(){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.WIDTH = gd.getDisplayMode().getWidth();
        this.HEIGHT = gd.getDisplayMode().getHeight();
    }

    /**
     * set the button as selected
     * add yellow edge
     * @param vb
     * @param index
     */
    public void moveselected(VBox vb, int index){
        for (int i = 0; i< vb.getChildren().size(); ++ i){
            MenuItem item = (MenuItem) vb.getChildren().get(i);
            item.setback();
            if (i == index){
                item.setselected();
            }
        }


    }

    /**
     * set the character box
     * set the selected button and add yellow edge to it
     */
    private void character(){
        movePane(buttonbox, -700);
        disablebuttons(buttonbox);

        if (root.getChildren().contains(charBox)){
            root.getChildren().remove(charBox);
            charBox = new VBox(10);
        }

        double x = 6*WIDTH/10;
        double y = HEIGHT/2 - 50;
        addButtons(x, y, Charselect, charBox);
        if (character == 1)  moveselected(charBox,0);
        if (character == 2)  moveselected(charBox,1);
        if (character == 3)  moveselected(charBox,2);
        if (character == 4)  moveselected(charBox,3);



    }

    /**
     * remove the charactert selection
     * move the vbox with ready/ not ready back
     * enable those buttons
     */
    private void removecharacter(){
        for (int i = 0; i < charBox.getChildren().size(); ++i){
            charBox.getChildren().remove(i);
        }

        root.getChildren().remove(charBox);
        charBox = new VBox(10);
        movePane(buttonbox, 0);
        enableButtons(buttonbox);
    }

    /**
     * move the botton on the node
     * distance is x
     * @param node the vbox you want to move
     * @param x distance
     */

    private void movePane(VBox node, double x) {
        PathTransition pathTransition = new PathTransition();
        pathTransition.setOnFinished( e -> {
                    for (int i = 0; i < node.getChildren().size(); i++) {
                        Node n = node.getChildren().get(i);

                        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.15), n);
                        tt.setToX(x);
                        tt.setOnFinished(e2 -> n.setClip(null));
                        tt.play();
                    }
                }

        );
        pathTransition.setDuration(Duration.millis(2000));

        pathTransition.setNode(node);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(Timeline.INDEFINITE);
        pathTransition.setAutoReverse(true);
        pathTransition.play();

    }

    /**
     * disable the buttons on vb
     * @param vb
     */
    private void disablebuttons(VBox vb){
        //int i = vb.getChildren().size();
        for (int i = 0; i < vb.getChildren().size(); ++ i){
            MenuItem n = (MenuItem) vb.getChildren().get(i);
            n.setDisable(true);
            n.setDisableEffect();
        }
    }

    /**
     * enable the buttons on vb
     * @param vb
     */
    private void enableButtons(VBox vb){

        for (int i = 0; i < vb.getChildren().size(); ++ i){
            MenuItem n = (MenuItem) vb.getChildren().get(i);
            n.setDisable(false);
            n.setEnableEffect();
        }
    }

    public Lobby(Stage w, Client client, String gameName){
        window = w;
        this.client = client;
        getwh();
        window.setTitle(NAME_OF_GAME);
        window.setHeight(HEIGHT);
        window.setWidth(WIDTH);
        //window.setFullScreen(true);
        this.gameName = gameName;
        configureTable(200,100);
        updateTable = new Thread(()-> {
            boolean ok = true;
            while (ok){

                Platform.runLater(() -> {   // Ensure data is updated on JavaFX thread
                    updateTable();
                   // addTable(WIDTH/4,HEIGHT/3);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    ok = false;
                }
            }

        });
        updateTable.setDaemon(true);
        updateTable.start();
        scene = new Scene(creatcontent());
        window.setScene(scene);


        //w.show();

    }

    /**
     * disable button
     */
    public void diablechar(){
        MenuItem n = (MenuItem) buttonbox.getChildren().get(0);
        n.setDisableEffect();
        n.setDisable(true);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //getwh();
        window = primaryStage;
        window.setTitle("FX Timer with Binding");
        window.setHeight(HEIGHT);
        window.setWidth(WIDTH);
        //window.setFullScreen(true);
        scene = new Scene(creatcontent());
        window.setScene(scene);
        //window.show();
    }


    /**
     * Thread for user press ready
     * @return new Thread
     */
    public Thread getReadyThread() {
        return new Thread(()->{
            ready = client.ready();
            System.out.println("Ready Ended " + ready);
        });
    }

    /**
     * Thread for user press not ready
     * @return new Thread
     */
    public Thread getNotReadyThread(){
        return new Thread(()->{
            client.notReady();
            System.out.println("not ready ended");
        });
    }

    /**
     * Thread that get the game from the server
     * @return new Thread
     */
    public Thread getGameThread(){
        return new Thread(()-> {
            try {
                readyThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ready) {
                game = new Thread(() -> {
                    ArrayList<Player> players = client.getGamePlayers();
                    client.gameState.setOnlineGame(true);
                    client.gameState.setPlayers(players);
                    boolean vSync = true;
                    audioHandler.fightSound.setVolume(audioHandler.getSoundEffectVolume());
                    audioHandler.playSoundEffect(audioHandler.fightSound);
                    IGameLogic gameLogic = new OnlineMode(client, client.gameState.getMap());
                    System.out.println("Game log created");
                    try {
                        audioHandler = new AudioHandler();
                        //audioHandler.playSoundEffect(audioHandler.audioInput.backgroundSound2);
                        gameEng = new GameEngine(NAME_OF_GAME, 700, 700, vSync, gameLogic);
                        updateTable.interrupt();
                        try {
                            updateTable.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mp = null;
                        Thread t = new Thread(()->{
                            Platform.runLater(()->{
                                mp = new multiPlayer(window,client);
                                mp.clear();
                                mp.active = false;
                            });
                        });
                        t.start();
                        t.join();
                        String res = gameEng.begin();
                        switch (res){
                            case "back":
                                table.getItems().clear();
                                table = new TableView<>();
                                if(gameEng!=null)
                                    gameEng.stop();
                                client.disconnectFromGame();
                                mp.setClient(client);
                                mp.active = true;
                                break;
                            default:
                                if(gameEng!=null)
                                    gameEng.stop();

                                client.disconnect();

                                t = new Thread(()->{
                                    Platform.runLater(()->{
                                        window.close();
                                    });
                                });
                                t.start();
                                t.join();
                                System.exit(0);
                                break;
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                game.start();
            }
        });
    }
}