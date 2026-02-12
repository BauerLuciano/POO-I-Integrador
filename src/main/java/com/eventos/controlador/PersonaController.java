package com.eventos.controlador;

import com.eventos.modelo.Persona;
import com.eventos.repo.PersonaRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.function.UnaryOperator;

public class PersonaController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDni;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private Label lblTitulo;

    private final PersonaRepository personaRepo = new PersonaRepository();
    private Persona personaActual;

    @FXML
    public void initialize() {
        configurarInput(txtNombre, 30, "[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]*");
        configurarInput(txtDni, 8, "[0-9]*");
        configurarInput(txtTelefono, 20, "[0-9]*");
    }

    public void setPersona(Persona p) {
        this.personaActual = p;
        if (p != null) {
            if(lblTitulo != null) lblTitulo.setText("Editar: " + p.getNombreCompleto());
            txtNombre.setText(p.getNombreCompleto());
            txtDni.setText(p.getDni());
            txtEmail.setText(p.getEmail());
            txtTelefono.setText(p.getTelefono());
        }
    }

    private void configurarInput(TextField textField, int maxLength, String regex) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches(regex) && text.length() <= maxLength) return change;
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    @FXML
    public void guardarPersona(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String dni = txtDni.getText().trim();

        if (nombre.isEmpty() || dni.isEmpty()) {
            mostrarAlerta("Datos Incompletos", "Por favor, ingresá Nombre y DNI.", true);
            return;
        }

        // Validación de duplicado
        Persona existente = personaRepo.buscarPorDni(dni);
        if (existente != null) {
            // Si es NUEVA (personaActual null) o si es EDICIÓN pero el ID es distinto
            if (personaActual == null || !existente.getId().equals(personaActual.getId())) {
                mostrarAlerta("¡DNI Duplicado!", "Ya existe la persona: " + existente.getNombreCompleto(), true);
                return;
            }
        }

        try {
            boolean esEdicion = (personaActual != null && personaActual.getId() != null);

            if (!esEdicion) {
                personaActual = new Persona();
            }
            
            // Actualizamos los datos del objeto
            personaActual.setNombreCompleto(nombre);
            personaActual.setDni(dni);
            personaActual.setEmail(txtEmail.getText().trim());
            personaActual.setTelefono(txtTelefono.getText().trim());

           
            if (esEdicion) {
                personaRepo.actualizar(personaActual);
            } else {
                personaRepo.guardar(personaActual); 
            }

            mostrarAlerta("Éxito", "Persona guardada correctamente.", false);
            cerrar(event);

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar: " + e.getMessage(), true);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, boolean esError) {
        Alert alert = new Alert(esError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle("-fx-border-color: " + (esError ? "#e74c3c" : "#2ecc71") + "; -fx-border-width: 2px;");
        alert.showAndWait();
    }
    
    @FXML public void volverALista(ActionEvent event) { cerrar(event); }

    private void cerrar(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
}