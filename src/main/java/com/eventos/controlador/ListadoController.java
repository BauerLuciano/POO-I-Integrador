package com.eventos.controlador;

import com.eventos.modelo.Persona;
import com.eventos.repo.PersonaRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality; // IMPORTANTE
import javafx.stage.Stage;    // IMPORTANTE
import java.io.IOException;
import java.util.Optional;

public class ListadoController {

    @FXML private TableView<Persona> tabla;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colDni;
    @FXML private TableColumn<Persona, String> colEmail;

    private final PersonaRepository repo = new PersonaRepository();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        cargarDatos();
    }

    public void cargarDatos() {
        try {
            tabla.setItems(FXCollections.observableArrayList(repo.listarTodos()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAFormulario(ActionEvent event) {
        // Pasamos null porque es una persona NUEVA
        abrirVentanaModal(null); 
    }

    @FXML
    public void editarPersona(ActionEvent event) {
        Persona seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccioná a alguien de la lista para editar.");
            return;
        }
        // Pasamos la persona seleccionada para EDITAR
        abrirVentanaModal(seleccionada); 
    }

    // --- MÉTODO CORREGIDO: ABRE UNA VENTANA FLOTANTE ---
    private void abrirVentanaModal(Persona p) {
        try {
            // IMPORTANTE: Chequeá que el nombre del archivo sea el correcto. 
            // En tu código dice "formulario_persona.fxml", pero antes usábamos "persona.fxml"
            // Si te tira error, cambiá el nombre acá abajo:
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/formulario_persona.fxml")); 
            Parent root = loader.load();

            if (p != null) {
                // Si estamos editando, pasamos los datos
                PersonaControlador controller = loader.getController();
                controller.setPersona(p);
            }

            // CREAMOS UNA VENTANA NUEVA (NO reemplazamos la actual)
            Stage stage = new Stage();
            stage.setTitle(p == null ? "Nueva Persona" : "Editar Persona");
            stage.setScene(new Scene(root));
            
            // MODALIDAD: Bloquea la ventana de atrás hasta que cierres esta
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Esperamos a que se cierre para seguir
            stage.showAndWait();

            // AL VOLVER: Refrescamos la tabla automáticamente
            cargarDatos();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarPersona() {
        Persona seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Seleccioná a alguien para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Borrar a " + seleccionada.getNombreCompleto() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            repo.eliminar(seleccionada.getId());
            cargarDatos();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msj) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msj);
        a.showAndWait();
    }
}