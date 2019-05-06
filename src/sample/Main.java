package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;

public class Main extends Application {

    static Connection connection = null;
    static String url = "jdbc:mysql://localhost:3306/my_base?useSSL=false&useTimezone=true&serverTimezone=UTC";
    static String username = "";
    static String password = "";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("schemat.fxml"));
        primaryStage.setTitle("Import Project");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
