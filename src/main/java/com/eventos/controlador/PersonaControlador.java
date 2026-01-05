package com.eventos.controlador;

import com.eventos.modelo.Persona;
import com.eventos.repo.PersonaRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PersonaControlador {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDni;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private Label lblTitulo;

    private final PersonaRepository personaRepo = new PersonaRepository();
    private Persona personaActual;

    public void setPersona(Persona p) {
        this.personaActual = p;
        lblTitulo.setText("Editar Persona: " + p.getNombreCompleto());
        txtNombre.setText(p.getNombreCompleto());
        txtDni.setText(p.getDni());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
    }

    @FXML
    public void guardarPersona(ActionEvent event) {
        String nombre = txtNombre.getText();
        String dni = txtDni.getText();

        if (nombre == null || nombre.trim().isEmpty() || dni == null || dni.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan datos", "Nombre y DNI son obligatorios.");
            return;
        }

        try {
            if (personaActual == null) {
                personaActual = new Persona();
            }
            
            personaActual.setNombreCompleto(nombre);
            personaActual.setDni(dni);
            personaActual.setEmail(txtEmail.getText());
            personaActual.setTelefono(txtTelefono.getText());

            personaRepo.guardar(personaActual);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Guardado correctamente.");
            
            // Llamamos al método corregido
            volverALista(event);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al guardar: " + e.getMessage());
        }
    }

    // --- ACÁ ESTÁ EL CAMBIO IMPORTANTE ---
    @FXML
    public void volverALista(ActionEvent event) {
        // En lugar de cargar un FXML nuevo, solo cerramos la ventana actual.
        // Al cerrarse, se ve la ventana de atrás (la de pestañas) que sigue abierta.
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void limpiarFormulario() {
        txtNombre.clear();
        txtDni.clear();
        txtEmail.clear();
        txtTelefono.clear();
        personaActual = null;
        lblTitulo.setText("Alta de Nueva Persona");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}