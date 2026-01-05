package com.eventos.controlador;

import com.eventos.modelo.Persona;
import com.eventos.repo.PersonaRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter; // <--- NUEVO
import javafx.stage.Stage;
import java.io.IOException;
import java.util.function.UnaryOperator; // <--- NUEVO

public class PersonaControlador {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDni;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private Label lblTitulo;

    private final PersonaRepository personaRepo = new PersonaRepository();
    private Persona personaActual;

    // --- ESTO ES LO QUE BUSCABAS: VALIDACIÓN EN TIEMPO REAL ---
    @FXML
    public void initialize() {
        // Filtro para DNI: Solo números y máximo 8 caracteres
        UnaryOperator<TextFormatter.Change> filtroDni = change -> {
            String nuevoTexto = change.getControlNewText();
            // Regex: \\d* significa "solo dígitos".
            if (nuevoTexto.matches("\\d*") && nuevoTexto.length() <= 8) {
                return change; // Deja pasar la tecla
            }
            return null; // Bloquea la tecla
        };
        txtDni.setTextFormatter(new TextFormatter<>(filtroDni));

        // Filtro para Teléfono: Solo números y máximo 15 caracteres
        UnaryOperator<TextFormatter.Change> filtroTelefono = change -> {
            String nuevoTexto = change.getControlNewText();
            if (nuevoTexto.matches("\\d*") && nuevoTexto.length() <= 15) {
                return change;
            }
            return null;
        };
        txtTelefono.setTextFormatter(new TextFormatter<>(filtroTelefono));
    }
    // -----------------------------------------------------------

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
        String email = txtEmail.getText();
        String telefono = txtTelefono.getText();

        // Validar Campos Vacíos
        if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, completá todos los campos.");
            return;
        }

        // Validar Formato Email (El DNI ya no hace falta validarlo acá porque el filtro no deja poner basura)
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            mostrarAlerta(Alert.AlertType.ERROR, "Email Inválido", "El formato del correo es incorrecto.");
            return;
        }

        try {
            if (personaActual == null) {
                personaActual = new Persona(); 
            }
            
            personaActual.setNombreCompleto(nombre);
            personaActual.setDni(dni);
            personaActual.setEmail(email);
            personaActual.setTelefono(telefono);

            personaRepo.guardar(personaActual);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Guardado correctamente.");
            volverALista(event);

        } catch (Exception e) {
            if (e.getMessage().contains("ConstraintViolation") || e.getMessage().contains("UK_")) {
                mostrarAlerta(Alert.AlertType.ERROR, "DNI Duplicado", "Ya existe una persona registrada con ese DNI.");
            } else {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error Crítico", "No se pudo guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void volverALista(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/lista_personas.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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