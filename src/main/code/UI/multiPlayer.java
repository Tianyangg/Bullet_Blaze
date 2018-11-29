package main.code.UI;

import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import main.code.Networking.Client;
import main.code.utils.Box;
import main.code.utils.Map;
import main.code.utils.MapReader;
import main.code.utils.Player;

import java.awt.*;
import java.util.*;
import java.util.List;

import static main.code.utils.GameConstants.*;

import main.code.UI.MenuItem;


public class multiPlayer extends Pane {
    private static int WIDTH = 1280;
    private static int HEIGHT = 720;
    public boolean active = true;
    private final TableView<Rooms> table = new TableView<>();
    private Client client;


    private Pane root = new Pane();
    private VBox playerBox = new VBox(0);
    private Line line;

    private VBox settingBox = new VBox(0); // VBox for Host a Game


    private int map = 4 ; // set default as map1
    private VBox mapBox = new VBox(0);// Vbox for map selection

    private VBox playernumberBox = new VBox(0);

    private VBox weaponBox = new VBox(0);

    private byte numofplayers = 2;

    private VBox NameBox = new VBox(10);
    public String gname = "";

    /**
     * boolean for the weapons
     */

    public boolean weapon1 = false;
    public boolean weapon2 = false;
    public boolean weapon3 = false;
    public boolean weapon4 = false;

    private Stage currentStage;
    private TextField gameNameTextBox;

    /**
     * change the scene of the stage
     * @param window
     * @param client the client of thes
     */
    public multiPlayer(Stage window,Client client){
        root.getStylesheets().add("main/code/UI/tablecss.css");
        getwh();
        currentStage = window;
        currentStage.setFullScreen(true);
        Scene scene = new Scene(createContent());
        currentStage.setTitle("BulletBlaze Menu");
        currentStage.setScene(scene);
        this.client = client;

    }

    /**
     * add the table the pane
     * get the list from server and add it to the table
     * @param x
     * @param y
     */
    public void configureTable(double x, double y){
        if(root.getChildren().contains(table))root.getChildren().remove(table);
        table.setTranslateX(x);
        table.setTranslateY(y);

        table.setEditable(false);
        table.setPrefSize(450,400);

        Callback<TableColumn, TableCell> stringCellFactory;
        stringCellFactory = p -> {
            StringTabelCell cell = new StringTabelCell();
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new MyEventHandler());
            return cell;
        };

        //TableColumn<Lobby, String> roomsCol = new TableColumn<>();
        TableColumn roomsCol = new TableColumn<>();
        roomsCol.setText("room name");
        roomsCol.setEditable(false);
        roomsCol.setPrefWidth(150);
        roomsCol.setCellValueFactory(
                new PropertyValueFactory<Rooms, String>("roomName"));
        roomsCol.setCellFactory(stringCellFactory);
        //roomsCol.setCellFactory(TextFieldTableCell.<Lobby>forTableColumn());

        TableColumn playerCol = new TableColumn();
        playerCol.setText("Usernumber");
        playerCol.setPrefWidth(150);
        playerCol.setCellValueFactory(
                new PropertyValueFactory<>("usernumber"));
        playerCol.setCellFactory(stringCellFactory);
        // get the mouse action to

        TableColumn maxCol = new TableColumn();
        maxCol.setText("UserCap");
        maxCol.setMinWidth(150);
        maxCol.setCellValueFactory(
                new PropertyValueFactory<>("maxUser"));
        maxCol.setCellFactory(stringCellFactory);

        table.setItems(data);
        table.getColumns().clear();
        table.getColumns().addAll(roomsCol, playerCol,maxCol);
        root.getChildren().add(table);

    }
    private ObservableList<Rooms> data;
    private Thread joinThread;
    private Thread updateJoinTable;
    private List<Pair<String, Runnable>> multiPlayer = Arrays.asList(
            new Pair<String, Runnable>("  Host a Game", () -> {
                if(active) {
                    deleteJoinGame();
                    if (updateJoinTable != null)
                        if (updateJoinTable.isAlive()) {
                            updateJoinTable.interrupt();
                            try {
                                updateJoinTable.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    double lineX = WIDTH / 2 - 60;
                    double lineY = HEIGHT / 3;
                    addMultiSelect(lineX, lineY);
                    movePane(playerBox, -400);
                    disablebuttons(playerBox);
                }


            }),
            new Pair<String, Runnable>("  Join a Game", () -> {
                if(active) {
                    deleteHostGame();
                    movePane(playerBox, -400);
                    configureTable(WIDTH / 2, HEIGHT / 3);
                    updateJoinTable = getUpdateJoinThread();
                    updateJoinTable.setDaemon(true);
                    updateJoinTable.start();
                }
            }),
            new Pair<String, Runnable>("  Log out", () -> {
                try{
                    if(active) {
                        if (updateJoinTable != null)
                            if (updateJoinTable.isAlive()) {
                                updateJoinTable.interrupt();
                                try {
                                    updateJoinTable.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        backtoMain();
                    }
                } catch (Exception e) {
                    System.out.println("test"+e);
                }


            })

    );

    private List<Pair<String, Runnable>> settings = Arrays.asList(
            new Pair<String, Runnable>("  Game name", () -> {
                Gamename();
                movePane(settingBox, -300);
                disablebuttons(settingBox);
            }),
            new Pair<String, Runnable>("  Players", () -> {
                addnumofPlyers(WIDTH/2 - 60, HEIGHT/3);
            }),
            new Pair<String, Runnable>("  Maps", () -> {
                addmapSelect(WIDTH/2 - 60, HEIGHT/3);
            }),
            new Pair<String, Runnable>("  Weapons", () -> {
                addweaponSelect(WIDTH/2 - 60, HEIGHT/3);
            }),
            new Pair<String, Runnable>("  Create the Game", () -> {
                create_game();
                enableButtons(playerBox);
                deleteHostGame();
            }),
            new Pair<String, Runnable>("  Previous", () -> {
                movePane(playerBox,0);
                enableButtons(playerBox);
                deleteHostGame();

            })

    );

    private List<Pair<String, Runnable>> numofPlayers = Arrays.asList(
            new Pair<String, Runnable>("  2 players", () -> {
                numofplayers = 2;
                setbuttonText(settingBox,2, "2");
                hidenumplayers(playernumberBox);
            }),
            new Pair<String, Runnable>("  3 Players", () -> {
                numofplayers = 3;
                setbuttonText(settingBox,2, "3");
                hidenumplayers(playernumberBox);
            }),
            new Pair<String, Runnable>("  4 Players", () -> {
                numofplayers = 4;
                setbuttonText(settingBox,2, "4");
                hidenumplayers(playernumberBox);
            })

    );

    private List<Pair<String, Runnable>> mapSelect = Arrays.asList(
            new Pair<String, Runnable>("  Map1", () -> {
                map = 1;
                setbuttonText(settingBox,3,"Map1");
                hideMap(mapBox);
            }),
            new Pair<String, Runnable>("  map2", () -> {
                map = 2;
                setbuttonText(settingBox,3,"Map2");
                hideMap(mapBox);
            }),
            new Pair<String, Runnable>("  map3", () -> {
                map = 3;
                setbuttonText(settingBox,3,"Map3");
                hideMap(mapBox);
            }),
            new Pair<String, Runnable>("  random", () -> {
                map = 4;
                setbuttonText(settingBox,3,"random");
                hideMap(mapBox);
            })


    );

    private ArrayList<Pair<Byte,Short>> chosenWeapons = new ArrayList<>();

    private List<Pair<String, Runnable>> weaponSelect = Arrays.asList(
            new Pair<String, Runnable>("  Machine Gun", () -> {
                weapon1=!weapon1;
                moveWeapon(weaponBox, 0, weapon1);
            }),
            new Pair<String, Runnable>("  Short Gun", () -> {
                weapon2 = !weapon2;
                moveWeapon(weaponBox, 1, weapon2);
            }),
            new Pair<String, Runnable>("  Sniper", () -> {
                weapon3 = !weapon3;
                moveWeapon(weaponBox, 2, weapon3);
            }),
            new Pair<String, Runnable>("  UZI", () -> {
                weapon4 = !weapon4;
                moveWeapon(weaponBox, 3, weapon4);
            }),
            new Pair<String, Runnable>("  Confirm", () -> {
                hideweapon(weaponBox);
            })
    );


    /**
     * add the Textfield and the button to the pane
     */
    private void Gamename(){
        if (root.getChildren().contains(NameBox)) {
            root.getChildren().remove(NameBox);
            NameBox = new VBox(10);
        }
        double lineX = WIDTH / 2 - 60;
        double lineY = HEIGHT / 3;
        NameBox.setTranslateX(lineX);
        NameBox.setTranslateY(lineY);
        gameNameTextBox = new TextField();
        gameNameTextBox.setPromptText("game name");
        gameNameTextBox.setPrefHeight(40);
        gameNameTextBox.setMinHeight(40);

        Button confirm = new Button("CONFIRM");
        confirm.setOnAction((ActionEvent arg0) -> {
            String s = gameNameTextBox.getText();
            if (s != null){
                gname = s;
                setbuttonText(settingBox, 1, s);
                movePane(settingBox,0);
                deletenameBox();
                enableButtons(settingBox);

            }
            else {
                movePane(settingBox,0);
                deletenameBox();
                enableButtons(settingBox);
            }
        });
        NameBox.getChildren().addAll(gameNameTextBox, confirm);
        root.getChildren().add(NameBox);
    }

    private void deletenameBox(){
        System.out.println(getChildren().size());
        for(int i = 0; i < NameBox.getChildren().size(); i++){
            Node n = NameBox.getChildren().get(i);
            NameBox.getChildren().remove(n);
        }

        root.getChildren().remove(NameBox);
        NameBox = new VBox(10);
    }

    private void deleteHostGame(){
        if (root.getChildren().contains(settingBox)) root.getChildren().remove(settingBox);
        if (root.getChildren().contains(playernumberBox)) root.getChildren().remove(settingBox);
        if (root.getChildren().contains(weaponBox)) root.getChildren().remove(settingBox);
        if (root.getChildren().contains(mapBox)) root.getChildren().remove(settingBox);

        settingBox = new VBox(0);
        playernumberBox = new VBox(0);
        weaponBox = new VBox(0);
        mapBox = new VBox(0);
       // updateJoinTable.interrupt();
    }

    private void deleteJoinGame(){
        if (root.getChildren().contains(table)) root.getChildren().remove(table);
    }
    /**
     * move the pane
     * @param node
     * @param x
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

    private void disTextButtons(VBox vb){
        // get the textfield
        Node n1 = vb.getChildren().get(0);
        n1.setDisable(true);
        //n.setDisableEffect();
        for (int i = 1; i < vb.getChildren().size(); ++ i){
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

    /**
     * set the text of intdex^th button on vb to Sting s
     * @param vb
     * @param index
     * @param s String you want to set
     */
    public void setbuttonText(VBox vb, int index, String s){
        MenuItem n = (MenuItem) vb.getChildren().get(index - 1);
        n.changeresult(s);
    }

    private ArrayList<Rooms> getGames() {
        HashMap<String, ArrayList<Player>> games = client.getAvailableGames();
        HashMap<String,Byte> gamesMax = client.getGamesMaxPlayers();
        Set<String> gameNames = games.keySet();
        ArrayList<Rooms> rooms = new ArrayList<>();
        Rooms auxRoom ;
        ArrayList<Player> players;
        for(String gN : gameNames){
            players = games.get(gN);
            auxRoom = new Rooms(gN,Integer.toString(players.size()),Integer.toString(gamesMax.get(gN)));
            auxRoom.setPlayers(players);
            rooms.add(auxRoom);
            System.out.println(gN);
        }
        return rooms;
    }


    private boolean create_game() {
        Map m ;
        ArrayList<Box> boxes;
        chosenWeapons = new ArrayList<>();
        switch (map){
            case 1:
                m = (new MapReader().readMap("src/main/resources/map/map1"));
                break;
            case 2:
                m = (new MapReader().readMap("src/main/resources/map/map2"));
                break;
            case 3:
                m = (new MapReader().readMap("src/main/resources/map/map3"));
                break;
            default:
                Random randomGenerator = new Random();
                int i = randomGenerator.nextInt(3);
                i++;
                m = (new MapReader().readMap("src/main/resources/map/map" + Integer.toString(i)));
                break;
        }
        if (weapon1){
            chosenWeapons.add(new Pair<Byte, Short>(MACHINEGUN_ID,MACHINEGUN_AMMO));
        }
        if (weapon2){
            chosenWeapons.add(new Pair<Byte, Short>(SHOTGUN_ID,SHOTGUN_AMMO));
        }
        if (weapon3){
            chosenWeapons.add(new Pair<Byte, Short>(SNIPER_ID, SNIPER_AMM0));
        }
        if (weapon4){
            chosenWeapons.add(new Pair<Byte, Short>(UZI_ID, UZI_AMMO));
        }
        if(chosenWeapons.isEmpty()){
            chosenWeapons.add(new Pair<Byte,Short>(NO_WEAPON_ID, (short) 0));
        }
        boxes = m.setupBoxes(chosenWeapons);
        String gameName = gameNameTextBox.getText();
        if(gameName!=null && gameName!= "")
            return client.createGame(gameName,numofplayers,m,boxes);
        else return false;
    }

    public void moveWeapon(VBox vbox, int index, boolean flag){

        MenuItem n = (MenuItem) vbox.getChildren().get(index);

        if(flag) n.setselected();

        else n.setback();

    }

    /**
     * hide the vbox which set the number of players
     * @param vbox
     */
    private void hidenumplayers(VBox vbox){
        int i ;
        for (int ctr = 0; ctr < vbox.getChildren().size(); ctr ++){
            Node n = vbox.getChildren().get(ctr);
            i = ctr;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.05 + i * 0.05), n);
           // tt.setToX(-10);
            tt.setOnFinished(e2 -> {
                n.setClip(null);
                n.setVisible(false);
                vbox.getChildren().remove(n);
            });
            tt.play();
        }
        playernumberBox = new VBox(0);
        movePane(settingBox, 0);
        enableButtons(settingBox);
    }

    /**
     * hide the vbox which set the allowed weapons
     * @param vbox
     */
    private void hideweapon(VBox vbox){
        int i;
        for (int ctr = 0; ctr < vbox.getChildren().size(); ctr ++){
            Node n = vbox.getChildren().get(ctr);
            i = ctr;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.05), n);
            tt.setToX(-10);
            tt.setOnFinished(e2 -> {
                vbox.getChildren().remove(n);
            });
            tt.play();
        }
        weaponBox = new VBox(0);
        movePane(settingBox,0);
        enableButtons(settingBox);

    }

    /**
     * hide the vbox which set the maps
     * @param vbox
     */
    private void hideMap(VBox vbox){
        int i;
        for (int ctr = 0; ctr < vbox.getChildren().size(); ctr ++){
            Node n = vbox.getChildren().get(ctr);
            i = ctr;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.05), n);
            tt.setToX(-10);
            tt.setOnFinished(e2 -> {
                vbox.getChildren().remove(n);
            });
            tt.play();
        }
        mapBox = new VBox(0);
        movePane(settingBox,0);
        enableButtons(settingBox);

    }

    /**
     * create the content on the root
     * add background
     * add title
     * @return root
     */
    private Parent createContent() {
        addBackground();
        addTitle();
        double lineX = WIDTH / 2 - 60;
        double lineY = HEIGHT / 3;

        addMenu(lineX, lineY, multiPlayer, playerBox);
        startAnimation(playerBox);
        return root;
    }

    private void addBackground() {
        ImageView imageView = new ImageView(new Image(getClass().getResource("res/bg.png").toExternalForm()));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);

        root.getChildren().add(imageView);
    }

    private void addTitle() {
        GameName title = new GameName("BulletBlaze");
        title.setTranslateX(WIDTH/3);
        title.setTranslateY(HEIGHT/4);
        //title.setTranslateX(WIDTH / 2 - title.getTitleWidth() / 2);
        //title.setTranslateY(HEIGHT / 3);

        root.getChildren().add(title);
    }


    private void startAnimation(VBox vbox) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), line);
        st.setToY(1);
        st.setOnFinished(e -> {

            for (int i = 0; i < vbox.getChildren().size(); i++) {
                Node n = vbox.getChildren().get(i);

                TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.05), n);
                tt.setToX(0);
                tt.setOnFinished(e2 -> n.setClip(null));
                tt.play();
            }
        });
        st.play();
    }

    /**
     * add the buttons defined by list to (x,y)
     * @param x
     * @param y
     * @param list
     * @param vbox the vbox to add the buttons
     */
    private void addMenu(double x, double y, List<Pair<String, Runnable>> list, VBox vbox) {
        if (root.getChildren().contains(vbox)){
            System.out.println("exist");
            for (int i = 0; i < vbox.getChildren().size(); i++) {
                vbox.setTranslateX(x);
                vbox.setTranslateY(y);
                //vbox.setBackground(new Background(new BackgroundFill(Color.WHITE, null,null)));
                MenuItem n = (MenuItem) vbox.getChildren().get(i);
                n.setTranslateX(-215);

                Rectangle clip = new Rectangle(300, 30);
                clip.translateXProperty().bind(n.translateXProperty().negate());
                n.setClip(clip);
                n.setVisible(true);
                n.setDisable(false);
            }
        }else{
            //addLine(x, y, "rightline");
            vbox.setTranslateX(x);
            vbox.setTranslateY(y);
            list.forEach(data -> {
                MenuItem item = new MenuItem(data.getKey());
                item.setOnAction(data.getValue());
                item.setTranslateX(-215);
                // item.setTranslateY(-300);
                Rectangle clip = new Rectangle(300, 30);
                clip.translateXProperty().bind(item.translateXProperty().negate());

                item.setClip(clip);

                vbox.getChildren().addAll(item);
            });
            root.getChildren().add(vbox);
        }
    }


    private void addMultiSelect(double x, double y){
        if (root.getChildren().contains(settingBox)) root.getChildren().remove(settingBox);

        settingBox = new VBox(0);
        addMenu(x, y, settings, settingBox);
        String nop = new String(String.valueOf(numofplayers));
        String maps = "";
        if(map == 1) maps = "Map 1";
        if(map == 2) maps = "Map 2";
        if(map == 3) maps = "Map 3";
        if(map == 4) maps = "Random";
        setbuttonText(settingBox,1,gname);
        setbuttonText(settingBox,2, nop);
        setbuttonText(settingBox,3, maps);

        startAnimation(settingBox);
        disablebuttons(playerBox);
    }

    private void addTableView(double x, double y){
        if (root.getChildren().contains(table)) root.getChildren().remove(table);

        Scene scene = root.getScene();

        table.setOpacity(0.9);

        root.getStylesheets().add("main/code/UI/tablecss.css");
        table.setItems(FXCollections.observableArrayList(getGames()));
        root.getChildren().add(table);


    }

    /**
     * add the vbox contains number of players,
     * move the setting box to the left
     * @param x
     * @param y
     */
    private void addnumofPlyers(double x, double y){
        //addLine(x, y, 100,"numofplayerline");
        addMenu(x, y, numofPlayers, playernumberBox);
        movePane(settingBox,-300);
        disTextButtons(settingBox);
        startAnimation(playernumberBox);
        if (numofplayers == 2) moveselected(playernumberBox,0);
        if (numofplayers == 3) moveselected(playernumberBox,1);
        if (numofplayers == 4) moveselected(playernumberBox,2);

    }


    private void addweaponSelect(double x, double y){
        movePane(settingBox,-300);
        disablebuttons(settingBox);
        addMenu(x, y, weaponSelect, weaponBox);

        if (weapon1) moveWeapon(weaponBox,0,true);
        if (weapon2) moveWeapon(weaponBox,1,true);
        if (weapon3) moveWeapon(weaponBox,2,true);
        if (weapon4) moveWeapon(weaponBox,3,true);
        startAnimation(weaponBox);
    }

    private void addmapSelect(double x, double y){
        disablebuttons(settingBox);
        movePane(settingBox,-300);

        addMenu(x, y, mapSelect, mapBox);
        startAnimation(mapBox);

        if (map == 1) moveselected(mapBox,0);
        if (map == 2) moveselected(mapBox,1);
        if (map == 3) moveselected(mapBox,2);
        if (map == 4) moveselected(mapBox,3);

    }

    public Thread getUpdateJoinThread() {
        return new Thread(()-> {
            boolean ok = true;
            while (ok){

                Platform.runLater(() -> {   // Ensure data is updated on JavaFX thread
                    //addTableView(WIDTH/2, HEIGHT/3);
                    updateTable();
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    ok = false;
                }
            }

        });
    }

    /**
     * update the content in the table
     */
    public void updateTable(){
        table.setItems(FXCollections.observableArrayList(getGames()));
    }

    public void clear() {
        table.getItems().clear();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * mouse event for the table rows
     * goes to Lobby after select a game
     */
    class StringTabelCell extends TableCell<Rooms, String>{

        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : getString());
            setGraphic(null);
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }

    }

    class MyEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent t) {

            Rooms lobby = table.getSelectionModel().getSelectedItem();
            String gN = lobby.getRoomName();
            System.out.println(gN);
            System.out.println(gN + "clicked game");

            if(updateJoinTable!=null)
                if(updateJoinTable.isAlive()){
                    updateJoinTable.interrupt();
                    try {
                        updateJoinTable.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            boolean aux = client.connectToGame(gN);
            System.out.println("Connected to game " + aux);
            if(aux) {
                System.out.println("connected to game");
                Lobby lb = new Lobby(currentStage, client,gN);
            }else{
                updateJoinTable = getUpdateJoinThread();
                updateJoinTable.setDaemon(true);
                updateJoinTable.start();
            }

        }
    }


    /**
     * go back to main menu scene when press "log out"
     * @throws Exception
     */
    public void backtoMain() throws  Exception{
        //currentStage = window;
        client.disconnect();
        client.end_client();
        Stage window = (Stage) playerBox.getScene().getWindow();
        BulletMenumain test = new BulletMenumain();
        test.start(window);
    }

    public void getwh(){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.WIDTH = gd.getDisplayMode().getWidth();
        this.HEIGHT = gd.getDisplayMode().getHeight();
    }

    public void moveselected(VBox vb, int index){
        for (int i = 0; i< vb.getChildren().size(); ++ i){
            MenuItem item = (MenuItem) vb.getChildren().get(i);
            item.setback();
            if (i == index){
                item.setselected();
            }
        }


    }

}
