package com.eventos.controlador;

import com.eventos.modelo.Inscripcion;
import com.eventos.modelo.Persona;
import com.eventos.modelo.Taller;
import com.eventos.repo.EventoRepository;
import com.eventos.repo.EventoRepositoryImpl;
import com.eventos.repo.PersonaRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InscripcionController {

    @FXML private Label lblEvento, lblCupo;
    @FXML private ComboBox<Persona> comboPersonas;
    @FXML private TableView<Inscripcion> tablaInscriptos;
    @FXML private TableColumn<Inscripcion, String> colNombre, colDni, colEmail;
    @FXML private TableColumn<Inscripcion, Boolean> colAsistio;
    @FXML private TableColumn<Inscripcion, Void> ColAcciones; 

    private final EventoRepository eventoRepo = new EventoRepositoryImpl();
    private final PersonaRepository personaRepo = new PersonaRepository(); 
    private Taller tallerActual; 

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getParticipante().getNombreCompleto()));
        colDni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getParticipante().getDni()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getParticipante().getEmail()));
        colAsistio.setCellValueFactory(new PropertyValueFactory<>("asistio"));
        agregarBotonEliminar();
        cargarComboPersonas();
    }

    public void initData(Taller taller) {
        this.tallerActual = taller;
        lblEvento.setText("Inscripción al: " + taller.getNombre());
        actualizarTabla();
        actualizarCupo();
    }

    private void cargarComboPersonas() {
        comboPersonas.setItems(FXCollections.observableArrayList(personaRepo.listarTodos()));
        comboPersonas.setConverter(new StringConverter<>() {
            @Override public String toString(Persona p) { return (p == null) ? "" : p.getNombreCompleto() + " (DNI: " + p.getDni() + ")"; }
            @Override public Persona fromString(String s) { return null; }
        });
    }

    @FXML 
    public void inscribir() { 
        Persona p = comboPersonas.getValue();
        if (p == null) { mostrarAlerta("Atención", "Seleccioná una persona."); return; }
        try {
            tallerActual.inscribir(p);
            eventoRepo.actualizar(tallerActual); 
            actualizarTabla();
            actualizarCupo(); 
            mostrarNotificacion("¡Inscripto correctamente!");
        } catch (Exception e) { mostrarAlerta("Error", e.getMessage()); }
    }

    @FXML
    public void marcarAsistencia() {
        Inscripcion sel = tablaInscriptos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setAsistio(!sel.isAsistio());
            eventoRepo.actualizar(tallerActual);
            tablaInscriptos.refresh();
        }
    }

    private void darDeBaja(Inscripcion ins) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Quitar a " + ins.getParticipante().getNombreCompleto() + "?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            tallerActual.getInscripciones().remove(ins);
            eventoRepo.actualizar(tallerActual);
            actualizarTabla();
            actualizarCupo();
        }
    }

    private void actualizarTabla() {
        if (tallerActual != null) {
            // Convertimos el Set a una Lista para la TableView
            tablaInscriptos.setItems(FXCollections.observableArrayList(new ArrayList<>(tallerActual.getInscripciones())));
        }
    }

    private void actualizarCupo() {
        if (tallerActual != null) {
            int ocupados = tallerActual.getInscripciones().size();
            lblCupo.setText("Cupo: " + ocupados + " / " + tallerActual.getCupoMaximo() + " ocupados");
            lblCupo.setStyle(ocupados >= tallerActual.getCupoMaximo() ? "-fx-text-fill: red;" : "-fx-text-fill: #666666;");
        }
    }

    private void agregarBotonEliminar() {
        ColAcciones.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            { btn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;"); btn.setOnAction(e -> darDeBaja(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : btn); }
        });
    }

    private void mostrarNotificacion(String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void mostrarAlerta(String t, String m) { Alert a = new Alert(Alert.AlertType.WARNING, m); a.setTitle(t); a.show(); }
}