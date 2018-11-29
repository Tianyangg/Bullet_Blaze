package main.code.UI;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Text class for game UI in credits
 */

public class  GameCreditsText extends Pane {
    private Text text;

    public GameCreditsText(String name) {
        String spread = "";
        for (char c : name.toCharArray()) {
            spread += c + " ";
        }

        text = new Text(spread);
        text.setFont(Font.loadFont(BulletMenumain.class.getResource("res/Penumbra-HalfSerif-Std_35114.ttf").toExternalForm(), 16));
        text.setFill(Color.BROWN);
        text.setEffect(new DropShadow(2, Color.BLACK));

        getChildren().addAll(text);
    }

    public double getTitleWidth() {
        return text.getLayoutBounds().getWidth();
    }

    public double getTitleHeight() {
        return text.getLayoutBounds().getHeight();
    }
}

