package main.code.UI;

import javafx.beans.binding.Bindings;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class MenuItem extends Pane {
    public Text text;
    public Text textresult;

    private Effect shadow = new DropShadow(5, Color.BLACK);
    private Effect blur = new BoxBlur(0.8, 0.8, 1);
    private Effect buttonblur = new BoxBlur(2,2,3);
    public Polygon button = new Polygon(
            0, 20,
            15, 0,
            200, 0,
            215, 20,
            200, 40,
            15, 40
    );
    public MenuItem(String name){
        /*Polygon button = new Polygon(
                0, 15,
                15, 0,
                200, 0,
                215, 15,
                200, 30,
                15, 30
        );*/

        button.setStroke(Color.color(1, 0.5, 0.5, 0.75));
        button.setEffect(new GaussianBlur());
        //button.setEffect(buttonblur);
        button.setOpacity(0.93);
        button.setStroke(Color.WHITE);
        button.setStrokeWidth(3);
        button.fillProperty().bind(
            Bindings.when(pressedProperty())
            .then(Color.web("#6B3D14"))
            .otherwise(Color.web("#6B481A"))
            );

        text = new Text(name);
        text.setTranslateX(20);
        text.setTranslateY(26);
        text.setFont(Font.loadFont(BulletMenumain.class.getResource("res/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 12));
        text.setFill(Color.web("#EAD5BB"));

        text.effectProperty().bind(
            Bindings.when(hoverProperty())
            .then(shadow)
                    .otherwise(blur)
        );

        textresult = new Text("");
        textresult.setTranslateX(130);
        textresult.setTranslateY(26);
        textresult.setFont(Font.loadFont(BulletMenumain.class.getResource("res/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 12));
        textresult.setFill(Color.web("#EAD5BB"));
        getChildren().addAll(button, text, textresult);
}

    /**
     * change appearance of button
     * add yellow edge
     */
    public void setselected(){
        button.setEffect(buttonblur);
        button.setStrokeWidth(4);
        button.setStroke(Color.GOLD);
    }

    /**
     * change the appearance back to normal button
     */
    public void setback(){
        button.setEffect(new GaussianBlur());
        button.setStroke(Color.WHITE);
        button.setStrokeWidth(3);

    }

    /**
     * change the button appearance to disbled button
     * set transparency to 0.8
     */
    public void setDisableEffect(){
        button.setOpacity(0.8);
        button.fillProperty().bind(
                Bindings.when(pressedProperty())
                        .then(Color.web("#734A11"))
                        .otherwise(Color.web("#734A11"))
        );
        text.setOpacity(0.8);
       // button.fillProperty().setValue(Color.color(0,0,0));
       // button.setFill(Color.color(0,0,0));

    }

    /**
     * set the button appearance to enabled button
     * set transparency 0.93
     */
    public void setEnableEffect(){
        button.setOpacity(0.93);
        text.setOpacity(1);
        button.fillProperty().bind(
                Bindings.when(pressedProperty())
                        .then(Color.web("#6B3D14"))
                        .otherwise(Color.web("#6B481A"))
        );

    }
    public void changefirstText(String s){
        text.setText(s);
    }

    /**
     * change the second text part on the button
     * @param s the text you want to set
     */
    public void changeresult(String s){
        textresult.setText(s);
    }
    public void setOnAction(Runnable action) {
        setOnMouseClicked(e -> action.run());
    }
}
