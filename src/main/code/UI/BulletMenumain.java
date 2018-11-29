/**
 * The First Scene for Game UI
 * (1) Single Palyer
 * (1) Difficulty
 * (2) Maps
 * (3) Weapons
 * (4) Next [Call the game Logic]
 * <p>
 * (2) Multi-player
 * (1) Login  -> Login Page
 * (2) Register -> Register Page
 */

package main.code.UI;

import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import main.code.Networking.Client;
import main.code.engine.GameEngine;
import main.code.engine.IGameLogic;
import main.code.game.SinglePlayerMode;
import main.code.game.audio.AudioHandler;
import main.code.utils.GameConstants;
import main.code.utils.Map;
import main.code.utils.MapReader;
import main.code.utils.Player;
import org.junit.internal.runners.statements.RunAfters;

import javax.management.relation.RelationNotFoundException;
import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static main.code.utils.GameConstants.*;




public class BulletMenumain extends Application {
    public Scene scene;
    private static int WIDTH = 1280;
    private static int HEIGHT = 720;
    boolean debug = false;

    private Pane root = new Pane();
    private VBox menuBox = new VBox(0);
    private VBox diffBox = new VBox(0);
    private VBox singleBox = new VBox(0);
    private VBox mapBox = new VBox(0);
    private VBox weaponBox = new VBox(0);
    private VBox multiActionBox = new VBox(0); // for Login Register

    private VBox multiBox = new VBox(0);

    private Line line;
    private Boolean flagsingleBox = false;
    private String credits ="\nJamie Moloney\n\n" +
            "Maria Antonia Badarau\n\n"+
            "Matei Vicovan Hantascu\n\n" +
            "Paul Mihaita\n\n"+
            "Thomas Murphy\n\n" +
            "Tianyang Sun\n\n";

    private GameCreditsText cred = new GameCreditsText(credits);

    public int difficulty = 2;// set default: medium
    public int map = 4; // set map1 as default
    public boolean weapon1 = false;
    public boolean weapon2 = false;
    public boolean weapon3 = false;
    public boolean weapon4 = false;
    // public final int[] text1_value = {50};
    public final int[] shoot_value = {50};

    private GridPane optiongp = new GridPane();

    private Pane inspane = new Pane();
    private Pane gunPane = new Pane();
    private Pane textPane = new Pane();
    public Map chosenMap = null;

    public AudioHandler audioHandler = new AudioHandler();

    private static double vlm = 0.0;
    private static double sfx = 0.0;

    public static double volumeMusic;

    public String path = "src//main//resources//audio//";

    private static boolean isPlaying = true;

    private static boolean forMenu = false;
    // background music for UI
    private Media introSong = new Media(new File(path + "b2.mp3").toURI().toString());
    private Media introTheme = new Media(new File(path + "Intro Theme.mp3").toURI().toString());
    private Media grassLandsTheme = new Media(new File(path + "Grasslands Theme.mp3").toURI().toString());
    private Media dungeonTheme = new Media(new File(path + "Dungeon Theme.mp3").toURI().toString());

    public Media back = new Media(new File(path + "background.mp3").toURI().toString());

    private AudioClip clickSound = new AudioClip(new File(path + "clickSound.mp3").toURI().toString());
    private Media backgroundMusicUI[] = {introTheme, introSong, grassLandsTheme, dungeonTheme};

    public MediaPlayer mediaPlayer = new MediaPlayer(back);

    public Media pickBackgroundSound() {
        Random random = new Random();
        int i = random.nextInt(backgroundMusicUI.length);
        return backgroundMusicUI[i];
    }

    private MediaPlayer mediaPlayer1 = new MediaPlayer(introTheme);

    ArrayList<Pair<Byte, Short>> chosenWeapons = new ArrayList<>();


    public Stage mainStage;
    private List<Pair<String, Runnable>> menuData = Arrays.asList(
            new Pair<String, Runnable>("  Single Player", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                addSingleButtons();
            }),
            new Pair<String, Runnable>("  Multiplayer", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                multiPlayerAction();
            }),
            new Pair<String, Runnable>("  Options", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                options();
            }),
            new Pair<String, Runnable>("  Instruction", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                instruction();
            }),
            new Pair<String, Runnable>("  Credits", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                credits();
            }),
            new Pair<String, Runnable>("  Exit", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                if (this.client != null) {
                    this.client.disconnect();
                    this.client.end_client();
                }
                ConcurrentLinkedQueue<Thread> allThreads = new ConcurrentLinkedQueue<Thread>();

                // spawn threads, make sure they're added to allThreads before they're run
                Platform.exit();
                System.exit(0);
            })
    );

    /**
     * credits method is used to display credits information and back button on the root
     * add the credit text to the root at coordinate (WIDTH/2, HEIGHT/3)
     * add the button the root, and the action is to remove all credits information from the root
     *
     */
    private void credits(){
        cred.setTranslateX(WIDTH/2);
        cred.setTranslateY(HEIGHT/3);


        Button back = new Button("back");
        back.setOnAction((ActionEvent arg0) -> {
            deletecred();
        });

        back.setTranslateX(WIDTH*2/3 - 100);
        back.setTranslateY(HEIGHT*2/3);
        back.setId("optionButton");

        root.getChildren().addAll(cred, back);

        movePane(menuBox, -400);
        disablebuttons(menuBox);
    }

    /**
     * deletecred method is used to remove credits information and the back button on the root
     * find the button with ID "option button" and remove the button from root
     * remove the credit pane from the root
     */
    private void deletecred(){
        root.getChildren().remove(cred);
        Node n = root.lookup("#optionButton");
        root.getChildren().remove(n);
        movePane(menuBox, 0);
        enableButtons(menuBox);

    }
    /**
     * instruction method is used to add the keyboard instruction method to the root,
     * call the instructonText method and disable the buttons of the main menu.
     */
    private void instruction() {
        //root.getChildren().remove(inspane);
        inspane = new Pane();
        ImageView img = new ImageView(new Image(getClass().getResource("res/instruction.png").toExternalForm()));
        img.setTranslateX(0);
        img.setTranslateY(0);
        //inspane.getChildren().add(img);
        inspane.setTranslateX(WIDTH/3);
        inspane.setTranslateY(HEIGHT*2/3 + 70);
        inspane.getChildren().addAll(img);
        instructionText();
        movePane(menuBox, -400);
        disablebuttons(menuBox);
        root.getChildren().add(inspane);
    }

    /**
     * instructionText method is used to add the text for the keyboard to the pane
     * call addGunPane and addTextPane
     */
    private void instructionText(){
        GameText esc = new GameText("exit the game");
        esc.setTranslateX(50);
        esc.setTranslateY(55);

        GameText weapon = new GameText("change weapon");
        weapon.setTranslateX(50);
        weapon.setTranslateY(70);

        GameText left = new GameText("change left");
        left.setTranslateX(50);
        left.setTranslateY(90);

        GameText right = new GameText("change right");
        right.setTranslateX(200);
        right.setTranslateY(30);

        GameText jump = new GameText("jump and move");
        jump.setTranslateX(450);
        jump.setTranslateY(30);

        GameText shift = new GameText("sprint");
        shift.setTranslateX(110);
        shift.setTranslateY(120);

        GameText space = new GameText("shoot");
        space.setTranslateX(110);
        space.setTranslateY(150);

        GameText drop = new GameText("drop weapon");
        drop.setTranslateX(320);
        drop.setTranslateY(10);

        Button back = new Button("back");
        back.setOnAction((ActionEvent arg0) -> {
            deleteinstruction();
        });

        back.setTranslateX(590);
        back.setTranslateY(100);
        back.setId("optionButton");
        addGunPane();
        addtextPane();

        inspane.getChildren().addAll(esc, weapon, left, right, jump, shift, space, drop, back);

    }

    /**
     * add the instruction of the gun to the root
     */
    private void addGunPane(){
        gunPane.setTranslateX(WIDTH/3);
        gunPane.setTranslateY(HEIGHT/4);


        ImageView pistol = new ImageView(new Image(getClass().getResource("res/Pistol.png").toExternalForm()));
        ImageView mgun = new ImageView(new Image(getClass().getResource("res/Machine-gun.png").toExternalForm()));
        ImageView shgun = new ImageView(new Image(getClass().getResource("res/Short-gun.png").toExternalForm()));
        ImageView snipper = new ImageView(new Image(getClass().getResource("res/Snipper.png").toExternalForm()));
        ImageView uzi = new ImageView(new Image(getClass().getResource("res/UZI.png").toExternalForm()));

        pistol.setTranslateX(10);
        pistol.setTranslateY(0);

        GameText ptext = new GameText("Pistol :          low damage, \n                    low range,\n                    low firing rate");
        ptext.setTranslateX(200);
        ptext.setTranslateY(70);

        mgun.setTranslateX(10);
        mgun.setTranslateY(50);

        GameText mtext = new GameText("machine gun: high damage, \n                    Medium range, \n                    medium firing rate");
        mtext.setTranslateX(200);
        mtext.setTranslateY(120);

        shgun.setTranslateX(10);
        shgun.setTranslateY(100);

        GameText sgun = new GameText("short gun:    high damage,\n                   low range, \n                   low firing rate");
        sgun.setTranslateX(200);
        sgun.setTranslateY(170);

        snipper.setTranslateX(50);
        snipper.setTranslateY(120);

        GameText snip = new GameText("snipper:        high damage,\n                   high range,\n                   low firing rate");
        snip.setTranslateX(200);
        snip.setTranslateY(220);

        uzi.setTranslateX(-10);
        uzi.setTranslateY(200);

        GameText uz = new GameText("uzi:              medium damage,\n                   medium range,\n                   high firing rate");
        uz.setTranslateX(200);
        uz.setTranslateY(270);


        gunPane.getChildren().addAll(pistol, mgun, shgun, snipper, uzi, ptext, mtext, sgun, snip, uz);
        root.getChildren().addAll(gunPane);


    }

    /**
     * add the instruction to the root about single player and multi-player game
     */
    private void addtextPane(){
        GameText single = new GameText("\nsingle player: \nShoot the AI, \nsurvive as many rounds as you can");
        single.setTranslateX(10);
        single.setTranslateY(10);
        GameText multi = new GameText("\nMulti player:\n" +
                "Join a game: \nplay against up to 3 other players\n" +
                "Create a game: \nCustomize your own game \nenjoy.");
        multi.setTranslateX(10);
        multi.setTranslateY(100);
        textPane.setTranslateX(WIDTH*2/3 + 100);
        textPane.setTranslateY(HEIGHT/3 + 30);
        textPane.getChildren().addAll(single, multi);
        root.getChildren().addAll(textPane);
    }

    /**
     * remove all the panes for instruction from root
     */
    private void deleteinstruction(){
        if (root.getChildren().contains(inspane)){
            root.getChildren().remove(inspane);
            inspane = new Pane();
        }
        if(root.getChildren().contains(gunPane)){
            root.getChildren().remove(gunPane);
            gunPane = new Pane();
        }
        if(root.getChildren().contains(textPane)){
            root.getChildren().remove(textPane);
            textPane = new Pane();
        }
        movePane(menuBox,0);
        enableButtons(menuBox);
    }
    private List<Pair<String, Runnable>> singlePlayer = Arrays.asList(
            new Pair<String, Runnable>("  difficulty", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                difficulty();
            }),
            new Pair<String, Runnable>("  Maps", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                maps();
            }),
            new Pair<String, Runnable>("  Weapons", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                weapons();
            }),
            new Pair<String, Runnable>("  Start game", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                chosenWeapons = new ArrayList<>();
                if (map == 1) {
                    chosenMap = (new MapReader().readMap("src/main/resources/map/map1"));
                }
                if (map == 2) {
                    chosenMap = (new MapReader().readMap("src/main/resources/map/map2"));
                }
                if (map == 3) {
                    chosenMap = (new MapReader().readMap("src/main/resources/map/map3"));
                }
                if (map == 4) {
                    Random randomGenerator = new Random();
                    int i = randomGenerator.nextInt(3);
                    i++;
                    chosenMap = (new MapReader().readMap("src/main/resources/map/map" + Integer.toString(i)));
                }
                if (weapon1) {
                    chosenWeapons.add(new Pair<Byte, Short>(MACHINEGUN_ID, MACHINEGUN_AMMO));
                }
                if (weapon2) {
                    chosenWeapons.add(new Pair<Byte, Short>(SHOTGUN_ID, SHOTGUN_AMMO));
                }
                if (weapon3) {
                    chosenWeapons.add(new Pair<Byte, Short>(SNIPER_ID, SNIPER_AMM0));
                }
                if (weapon4) {
                    chosenWeapons.add(new Pair<Byte, Short>(UZI_ID, UZI_AMMO));
                }
                try {
                    if(chosenWeapons.isEmpty()){
                        chosenWeapons.add(new Pair<Byte,Short>(NO_WEAPON_ID, (short) 0));
                    }
                    mainStage.hide();
                    boolean vSync = true;
                    IGameLogic gameLogic = new SinglePlayerMode((byte) difficulty, chosenMap, chosenWeapons);
                    audioHandler.fightSound.setVolume(audioHandler.getSoundEffectVolume());
                    audioHandler.playSoundEffect(audioHandler.fightSound);
                    System.out.println("Game log created");
                    GameEngine gameEng = new GameEngine(NAME_OF_GAME, 850, 850, vSync, gameLogic);
                    System.out.println("Game engine created");
                    String res = gameEng.begin();
                    while (gameEng.isGameRunning()) {
                        mainStage.hide();
                    }
                    switch (res){
                        case "back":
                            movePane(menuBox,0);
                            enableButtons(menuBox);
                            isPlaying = false;
                            deleteVBox();
                            mainStage.show();
                            break;
                        case "exit":
                            Platform.exit();
                            System.exit(0);
                            break;
                        default:
                            mainStage.show();
                            break;
                    }

                } catch (Exception excp) {
                    excp.printStackTrace();
                    System.exit(-1);
                }
            }),
            new Pair<String, Runnable>("  Previous", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                movePane(menuBox, 0);
                enableButtons(menuBox);
                deleteVBox();
            })
    );

    private List<Pair<String, Runnable>> diffSelect = Arrays.asList(
            new Pair<String, Runnable>("  easy", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                difficulty = 1;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 1, "easy");
                enableButtons(singleBox);
                hidedifficulty();

            }),
            new Pair<String, Runnable>("  medium", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                difficulty = 2;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 1, "medium");
                enableButtons(singleBox);
                hidedifficulty();

            }),
            new Pair<String, Runnable>("  hard", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                difficulty = 3;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 1, "hard");
                enableButtons(singleBox);
                hidedifficulty();

            })

    );
    private List<Pair<String, Runnable>> mapSelect = Arrays.asList(
            new Pair<String, Runnable>("  Map1", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                map = 1;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 2, "Map1");
                enableButtons(singleBox);
                hideMaps(map);
            }),
            new Pair<String, Runnable>("  map2", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                map = 2;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 2, "Map2");
                enableButtons(singleBox);
                hideMaps(map);
            }),
            new Pair<String, Runnable>("  map3", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                map = 3;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 2, "Map3");
                enableButtons(singleBox);
                hideMaps(map);
            }),
            new Pair<String, Runnable>("  Random", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                map = 4;
                movePane(singleBox, 0);
                setbuttonText(singleBox, 2, "Random");
                enableButtons(singleBox);
                hideMaps(map);
            })
    );

    private List<Pair<String, Runnable>> weaponSelect = Arrays.asList(
            new Pair<String, Runnable>("  Machine Gun", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                weapon1 = !weapon1;
                moveWeapon(weaponBox, 0, weapon1);
            }),
            new Pair<String, Runnable>("  Shotgun", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                weapon2 = !weapon2;
                moveWeapon(weaponBox, 1, weapon2);
            }),
            new Pair<String, Runnable>("  Sniper", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                weapon3 = !weapon3;
                moveWeapon(weaponBox, 2, weapon3);
            }),
            new Pair<String, Runnable>("  UZI", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                weapon4 = !weapon4;
                moveWeapon(weaponBox, 3, weapon4);
            }),
            new Pair<String, Runnable>("  Confirm", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                /*
                String s = "gun0";
                if (weapon1) s = s + " 1";
                if (weapon2) s = s + " 2";
                if (weapon3) s = s + " 3";
                if (weapon4) s = s + " 4";*/
                movePane(singleBox, 0);
                //setbuttonText(singleBox,3,s);
                enableButtons(singleBox);
                hideweapon(weaponBox);
            })
    );

    private List<Pair<String, Runnable>> multiPlayer = Arrays.asList(
            new Pair<String, Runnable>("  Create a Game", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();

            }),
            new Pair<String, Runnable>("  Join a Game", () ->
            {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
            })
    );

    private List<Pair<String, Runnable>> loginregister = Arrays.asList(
            new Pair<String, Runnable>("  Login", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                loginPage();

            }),
            new Pair<String, Runnable>("  Register", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();

                registerPage();
            }),
            new Pair<String, Runnable>("  Previous", () -> {
                clickSound.setVolume(audioHandler.getSoundEffectVolume());
                clickSound.play();
                this.client.disconnect();
                this.client.end_client();
                movePane(menuBox, 0);
                enableButtons(menuBox);
                deleteVBox();
            })
    );
    public Client client;

    /**
     * create the content on the root
     * add the background image and the title for the game
     * @return root
     */
    private Parent createContent() {
        addBackground();
        addTitle();

        //double lineX = WIDTH / 2 - 100;
        // double lineY = HEIGHT / 3 + 50;

        double lineX = WIDTH / 2 - 150;
        double lineY = HEIGHT / 3;
        addMenu(lineX, lineY, menuData, menuBox);
        startAnimation(menuBox);

        return root;
    }


    /**
     * load the background image from the source and add it to the root
     */
    private void addBackground() {
        ImageView imageView = new ImageView(new Image(getClass().getResource("res/bg.png").toExternalForm()));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);


        root.getChildren().add(imageView);
    }


    private void addTitle() {
        GameName title = new GameName("BulletBlaze");
        double x = WIDTH / 4;
        double y = HEIGHT / 5;
        title.setTranslateX(x);
        title.setTranslateY(y);

        root.getChildren().add(title);
    }

    /**
     * start animation for the buttons on vbox
     * @param vbox
     */
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
     * addMenu is used to add the buttons to a Vbox at (x, y)
     * @param x coordinate x
     * @param y coordinate y
     * @param list the list which define the text and action of each button
     * @param vbox aff those button to the VBox
     */
    private void addMenu(double x, double y, List<Pair<String, Runnable>> list, VBox vbox) {
        if (root.getChildren().contains(vbox)) {
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
        } else {
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

    /**
     * move Pane x from original coordinate
     * @param node the Vbox you want to move
     * @param x the distance and direction
     */
    private void movePane(VBox node, double x) {
        PathTransition pathTransition = new PathTransition();
        pathTransition.setOnFinished(e -> {
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
     * disablebuttons on VBox vb
     * increase the transparency
     * set clickable to false
     * @param vb
     */
    private void disablebuttons(VBox vb) {
        //int i = vb.getChildren().size();
        for (int i = 0; i < vb.getChildren().size(); ++i) {
            MenuItem n = (MenuItem) vb.getChildren().get(i);
            n.setDisable(true);
            n.setDisableEffect();
        }
    }

    /**
     * enable the buttons on the VBox vb
     * set the transparency back
     * set clickable to true
     * @param vb
     */
    private void enableButtons(VBox vb) {

        for (int i = 0; i < vb.getChildren().size(); ++i) {
            MenuItem n = (MenuItem) vb.getChildren().get(i);
            n.setDisable(false);
            n.setEnableEffect();
        }
    }



    private void addSingleButtons() {
        rmvmultiBox();
        rmvsingleBox();

        double lineX = WIDTH / 2 - 150;
        double lineY = HEIGHT / 3;

        addMenu(lineX, lineY, singlePlayer, singleBox);
        if (difficulty == 1){
            setbuttonText(singleBox, 1, "Easy");
        }
        if (difficulty == 2){
            setbuttonText(singleBox, 1, "Medium");
        }
        if (difficulty == 3){
            setbuttonText(singleBox, 1, "Hard");
        }

        if (map == 1){
            setbuttonText(singleBox, 2, "Map1");
        }
        if (map == 2){
            setbuttonText(singleBox, 2, "Map2");
        }
        if (map == 3){
            setbuttonText(singleBox, 2, "Map3");
        }
        if (map == 4){
            setbuttonText(singleBox, 2, "Random");
        }

        // setbuttonText(singleBox,3,"GUN 0");
        movePane(menuBox, -350);
        disablebuttons(menuBox);
        startAnimation(singleBox);

    }

    /**
     * set The second text on the buttons
     * @param vb name for the VBox
     * @param index index for the buttons
     * @param s String to be put on the button
     */
    private void setbuttonText(VBox vb, int index, String s) {
        MenuItem n = (MenuItem) vb.getChildren().get(index - 1);
        n.changeresult(s);
    }


    private void adddifficulty(double x, double y) {
        addMenu(x, y, diffSelect, diffBox); // add the difficulty to the root VBox diffBox
        startAnimation(diffBox);
    }


    private void hidedifficulty() {
        int i;
        for (int ctr = 0; ctr < diffBox.getChildren().size(); ctr++) {
            MenuItem n = (MenuItem) diffBox.getChildren().get(ctr);
            i = ctr;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.05), n);
            n.setVisible(false);

            tt.play();
        }
    }

    /**
     * add the difficulty buttons for the single-player setting
     */
    private void difficulty() {
        rmvsingleBox();

        double lineX = WIDTH / 2;
        double lineY = HEIGHT / 3;

        movePane(singleBox, -200);
        disablebuttons(singleBox);
        adddifficulty(lineX, lineY);
        if (difficulty == 1) moveselected(diffBox, 0);
        if (difficulty == 2) moveselected(diffBox, 1);
        if (difficulty == 3) moveselected(diffBox, 2);

    }


    private void addMaps(double x, double y) {
        addMenu(x, y, mapSelect, mapBox); // add the difficulty to the root VBox diffBox
        startAnimation(mapBox);
    }

    private void hideMaps(int index) {
        int i;
        for (int ctr = 0; ctr < mapBox.getChildren().size(); ctr++) {
            MenuItem n = (MenuItem) mapBox.getChildren().get(ctr);
            i = ctr;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.05), n);

            System.out.println(i);
            n.setVisible(false);
            tt.play();
        }

    }

    /**
     * add the maps and set the text that show which map has been chosen
     */
    public void maps() {
        rmvsingleBox();
        double lineX = WIDTH / 2;
        double lineY = HEIGHT / 3;

        movePane(singleBox, -200);
        disablebuttons(singleBox);
        addMaps(lineX, lineY);
        if (map == 1) moveselected(mapBox, 0);
        if (map == 2) moveselected(mapBox, 1);
        if (map == 3) moveselected(mapBox, 2);
        if (map == 4) moveselected(mapBox, 3);
    }

    /**
     * add the weapon for the setting
     */
    public void weapons() {
        rmvsingleBox();
        double lineX = WIDTH / 2;
        double lineY = HEIGHT / 3;
        movePane(singleBox, -200);
        disablebuttons(singleBox);

        addMenu(lineX, lineY, weaponSelect, weaponBox);
        if (weapon1) moveWeapon(weaponBox, 0, true);
        if (weapon2) moveWeapon(weaponBox, 1, true);
        if (weapon3) moveWeapon(weaponBox, 2, true);
        if (weapon4) moveWeapon(weaponBox, 3, true);
        startAnimation(weaponBox);
    }

    private void hideweapon(VBox vbox) {
        int i;
        for (int ctr = 0; ctr < vbox.getChildren().size(); ctr++) {
            Node n = vbox.getChildren().get(ctr);
            i = ctr;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.5 + i * 0.05), n);
            tt.setToX(-50);

            tt.setOnFinished(e2 -> {
                n.setClip(null);
                n.setVisible(false);
            });
            tt.play();
            Line line = (Line) root.lookup("#weaponline");
            if (line != null) {
                line.setVisible(false);
            } else {
                System.out.println("not exist");
            }
        }


    }

    public void moveWeapon(VBox vbox, int index, boolean flag) {

        MenuItem n = (MenuItem) vbox.getChildren().get(index);

        if (flag) n.setselected();

        else n.setback();

    }

    public void moveselected(VBox vb, int index) {
        for (int i = 0; i < vb.getChildren().size(); ++i) {
            MenuItem item = (MenuItem) vb.getChildren().get(i);
            item.setback();
            if (i == index) {
                item.setselected();
            }
        }


    }

    /**
     * delete buttons for single-player settings
     * initialize the VBox
     */
    public void deleteVBox() {


        if (root.getChildren().contains(singleBox)) root.getChildren().remove(singleBox);
        if (root.getChildren().contains(weaponBox)) root.getChildren().remove(weaponBox);
        if (root.getChildren().contains(mapBox)) root.getChildren().remove(mapBox);
        if (root.getChildren().contains(diffBox)) root.getChildren().remove(diffBox);
        if (root.getChildren().contains(multiBox)) root.getChildren().remove(multiBox);
        singleBox = new VBox(0);
        weaponBox = new VBox(0);
        mapBox = new VBox(0);
        diffBox = new VBox(0);
        multiBox = new VBox(0);
    }


    /**
     * change the scence to the login page
     */
    private void loginPage() {
        try {
            //loginAction(); //--fxml version
            Stage st = (Stage) multiBox.getScene().getWindow();
            Login lg = new Login(st, client);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * change the scene to
     * @throws Exception
     */
    private void registerAction() throws Exception {

        Stage st = (Stage) multiBox.getScene().getWindow();
        Register rg = new Register(st, client);
    }

    private void registerPage() {
        try {
            double lineX = WIDTH / 2 - 55;
            double lineY = HEIGHT / 3;

            registerAction();
            movePane(menuBox, -215);
            addMenu(lineX, lineY, multiPlayer, multiBox);
            startAnimation(multiBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if single player boxes is on the root,
     * remove them from the root.
     */
    private void rmvsingleBox() {
        if (root.getChildren().contains(diffBox)) {
            root.getChildren().remove(diffBox);
            diffBox = new VBox(0);
        }
        if (root.getChildren().contains(mapBox)) {
            root.getChildren().remove(mapBox);
            mapBox = new VBox(0);
        }
        if (root.getChildren().contains(weaponBox)) {
            root.getChildren().remove(weaponBox);
            weaponBox = new VBox(0);
        }
    }

    private void rmvmultiBox() {
        if (root.getChildren().contains(multiActionBox)) root.getChildren().remove(multiActionBox);

    }

    /**
     * Add the Vbox and the buttons to the root panel
     * before this, make sure the other Vboxes has been deleted.
     */
    private void addMultiAction() {
        rmvsingleBox();
        if (root.getChildren().contains(singleBox)) root.getChildren().remove(singleBox);
        double lineX = WIDTH / 2 - 50;
        double lineY = HEIGHT / 3 + 30;
        //addLine(lineX, lineY, "singleLine");
        addMenu(lineX, lineY, loginregister, multiBox);
        movePane(menuBox, -300);
        disablebuttons(menuBox);
        startAnimation(multiBox);

    }


    private void multiPlayerAction() {
        if (debug) System.out.println("multiBox size: " + multiActionBox.getChildren().size());
        if (root.getChildren().contains(multiBox)) System.out.println("exists");
        if (multiBox.getChildren().size() == 0) {
            addMultiAction();
            Player player = new Player();
            try {
                client = new Client(InetAddress.getByName(GameConstants.serverIP), (short) 8849, player, "", "");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            movePane(menuBox, 0);
            deleteVBox();
        }
    }

    /**
     * add the buttons and slider to the root
     */
    private void options() {
        root.getStylesheets().add("main/code/UI/tablecss.css");
        movePane(menuBox, -200);
        disablebuttons(menuBox);
        if (root.getChildren().contains(optiongp)) {
            root.getChildren().remove(optiongp);
            optiongp = new GridPane();
        }
        double lineX = WIDTH / 2;
        double lineY = HEIGHT / 3 + 50;

        optiongp.setTranslateX(lineX);
        optiongp.setTranslateY(lineY);

        Text text1 = new Text("Background Sound");
        text1.setId("fancytext2");
        optiongp.add(text1, 0, 0);
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(audioHandler.getBackgroundVolume() * 100);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);


        /*slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                System.out.println("test UI:"+new_val.intValue());
                audioHandler.updateBackgroundVolume(new_val.intValue()/100);
                //text1_value[0] = new_val.intValue();
            }
        });*/


        /**
         * Slider that is used to set the volume of the background music
         */

        slider.valueProperty().addListener(new InvalidationListener() {
                                               @Override
                                               public void invalidated(Observable observable) {
                                                   vlm = slider.getValue() / 100.0f;
                                                   updateBackgroundVolumeUI(vlm);
                                                   audioHandler.updateBackgroundVolume(vlm);
                                               }
                                           }
        );



        optiongp.add(slider, 1, 0);

        Text shoot_voice = new Text("Sound Effect");
        shoot_voice.setId("fancytext2");

        optiongp.add(shoot_voice, 0, 1);
        Slider shoot = new Slider();
        shoot.setMin(0);
        shoot.setMax(100);
        shoot.setValue(audioHandler.getSoundEffectVolume() * 100.0f);
        shoot.setShowTickLabels(true);
        shoot.setShowTickMarks(true);
        shoot.setMajorTickUnit(50);
        shoot.setMinorTickCount(5);
        shoot.setBlockIncrement(10);

        /**
         * Slider that is used to set the volume of the sound effects
         */

        shoot.valueProperty().addListener(new InvalidationListener() {
                                              @Override
                                              public void invalidated(Observable observable) {
                                                  sfx = shoot.getValue() / 100.0f;
                                                  audioHandler.updateSFXVolume(sfx);
                                                  System.out.println("BulletMainSFX: " + slider.getValue() / 100.0f);
                                              }
                                          }
        );
        shoot.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    int i = newvalue.intValue();
                    shoot_value[0] = i;
                });
        optiongp.setVgap(10);
        optiongp.setHgap(10);
        optiongp.add(shoot, 1, 1);

        Button confirm = new Button("Confirm");
        confirm.setId("optionButton");
        confirm.setOnAction((ActionEvent arg0) -> {
            clickSound.setVolume(audioHandler.getSoundEffectVolume());
            clickSound.play();
            //System.out.println(text1_value[0]);
            removeoptions();

        });
        optiongp.add(confirm, 1, 2);
        root.getChildren().addAll(optiongp);

    }

    private void removeoptions() {
        for (int i = 0; i < optiongp.getChildren().size(); i++) {
            optiongp.getChildren().remove(i);
        }
        movePane(menuBox, 0);
        root.getChildren().remove(optiongp);
        optiongp = new GridPane();
        enableButtons(menuBox);
    }


    public static void main(String[] args) {
        launch(args);
    }

    /**
     * getwh methos get the width and height of the current screen
     */
    public void getwh() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.WIDTH = gd.getDisplayMode().getWidth();
        this.HEIGHT = gd.getDisplayMode().getHeight();
    }

    /**
     * Intializes the UI
     * Starts the music in the UI
     * Sets the volume of the UI
     * Shows the UI
     * @param primaryStage
     * @throws Exception
     */

    @Override
    public void start(Stage primaryStage) throws Exception {
        getwh();
        root.getStylesheets().add("main/code/UI/tablecss.css");
        mainStage = primaryStage;
        mainStage.setFullScreen(true);
        scene = new Scene(createContent());
        mainStage.setTitle(NAME_OF_GAME);
        audioHandler.updateBackgroundVolume(audioHandler.getBackgroundVolume());
        startMusicUI();
        mainStage.setScene(scene);
        mainStage.show();

    }

    /**
     * Initializes the mutiplayer UI
     * Starts the music of the multiplayer UI
     * Sets the volume of the music
     * Shows the UI
     * @param stage
     * @throws Exception
     */


    public void startwithmulti(Stage stage) throws Exception {
        getwh();
        root.getStylesheets().add("main/code/UI/tablecss.css");
        mainStage = stage;
        mainStage.setFullScreen(true);
        scene = new Scene(createContent());
        mainStage.setTitle("BulletBlaze Menu");
        audioHandler.updateBackgroundVolume(audioHandler.getBackgroundVolume());
        startMusicUI();
        multiPlayerAction();
        mainStage.setScene(scene);
    }

    /**
     * Starts the UI music by starting the mediaplayer
     */

    public void startMusicUI() {
        mediaPlayer.play();
    }

    /**
     * Updates the volume of the music in the UI
     * Sets the volume of the UI music
     * @param volume
     */

    public void updateBackgroundVolumeUI(double volume) {
        mediaPlayer.setVolume(volume);
        volumeMusic = volume;
    }

    /**
     * Checks if the UI music is playing
     * @return
     */
    public static boolean getIsPlaying() {
        return isPlaying;
    }

}

