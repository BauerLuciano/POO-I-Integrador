package com.eventos.controlador;

import com.eventos.modelo.Persona;
import com.eventos.modelo.Taller;
import com.eventos.repo.EventoRepository;
import com.eventos.repo.EventoRepositoryImpl;
import com.eventos.repo.PersonaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class InscripcionController {

    @FXML private Label lblNombreTaller;
    @FXML private Label lblCupo;
    @FXML private ComboBox<Persona> comboPersonas;

    private EventoRepository eventoRepo = new EventoRepositoryImpl();
    private PersonaRepository personaRepo = new PersonaRepository();
    
    // Variable para guardar el taller que estamos editando
    private Taller tallerActual;

    @FXML
    public void initialize() {
        cargarPersonas();
    }

    private void cargarPersonas() {
        try {
            // Reusamos el método listarTodos que agregamos antes
            comboPersonas.getItems().addAll(personaRepo.listarTodos());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- ESTE ES EL MÉTODO CLAVE ---
    // La ventana principal llama a esto para pasarnos el Taller seleccionado
    public void initData(Taller taller) {
        this.tallerActual = taller;
        lblNombreTaller.setText(taller.getNombre());
        actualizarCupoVisual();
    }

    private void actualizarCupoVisual() {
        // Calculamos cupo restante (Maximo - Inscriptos actuales)
        // Nota: tallerActual.getInscripciones() podría ser null si es nuevo, manejar con cuidado
        int inscriptos = tallerActual.getInscripciones().size();
        int disponibles = tallerActual.getCupoMaximo() - inscriptos;
        lblCupo.setText("Cupo disponible: " + disponibles + " / " + tallerActual.getCupoMaximo());
    }

    @FXML
    public void confirmarInscripcion() {
        try {
            Persona participante = comboPersonas.getValue();
            if (participante == null) {
                mostrarAlerta("Error", "Debe seleccionar una persona.");
                return;
            }

            // 1. Lógica de negocio (Polimorfismo en acción)
            // El método inscribir ya valida si hay cupo dentro de la clase Taller
            tallerActual.inscribir(participante);

            // 2. Guardar cambios en la BD
            eventoRepo.actualizar(tallerActual);

            // 3. Feedback
            mostrarAlerta("Éxito", "Inscripción realizada correctamente.");
            
            // 4. Cerrar ventana
            Stage stage = (Stage) lblNombreTaller.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo inscribir: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}