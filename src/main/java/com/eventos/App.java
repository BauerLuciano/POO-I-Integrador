package com.eventos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // CAMBIO TEMPORAL: Apuntamos directo a tu formulario para probarlo
        // Antes decía: scene = new Scene(loadFXML("main"), 900, 600);
        scene = new Scene(loadFXML("main.fxml"), 900, 700); 
        
        stage.setTitle("Trabajo Práctico Integrador - POO1 2026");
        stage.setScene(scene);
        stage.show();
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/vista/main.fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}