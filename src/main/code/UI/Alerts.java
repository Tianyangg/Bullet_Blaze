package main.code.UI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Alerts {
    public static void quital(String title, Stage mwindow){
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);

        Label label = new Label();
        label.setText("Do you want to quit?");
        Button closeButton = new Button("Yes");
        closeButton.setOnAction(arg0 ->{
            window.close();
            mwindow.close();
        });

        Button noButton = new Button("NO");
        noButton.setOnAction(e -> window.close());


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, closeButton, noButton);
        layout.setAlignment(Pos.CENTER);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

    }

    public static void backal(){

    }
}
