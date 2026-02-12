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
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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
        // Configuración de columnas
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getParticipante().getNombreCompleto()));
        colDni.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getParticipante().getDni()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getParticipante().getEmail()));
        
        // 1. CORRECCIÓN VISUAL: Columna Asistió (Si/No con colores)
        colAsistio.setCellValueFactory(new PropertyValueFactory<>("asistio"));
        colAsistio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Si" : "No");
                    if (item) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde
                    } else {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: normal;"); // Rojo
                    }
                }
            }
        });

        agregarBotonEliminar();
        
        // Configuración del Converter del Combo
        comboPersonas.setConverter(new StringConverter<>() {
            @Override public String toString(Persona p) { return (p == null) ? "" : p.getNombreCompleto() + " (DNI: " + p.getDni() + ")"; }
            @Override public Persona fromString(String s) { return null; }
        });
    }

    public void initData(Taller taller) {
        this.tallerActual = taller;
        lblEvento.setText("Inscripción al: " + taller.getNombre());
        
        // 2. CORRECCIÓN LÓGICA: Refrescar combo con personas aptas
        actualizarComboPersonas();
        actualizarTabla();
        actualizarCupo();
    }

    /**
     * Filtra la lista de personas para que no aparezcan:
     * - El instructor del taller.
     * - Los organizadores.
     * - Los que ya están inscriptos.
     */
    private void actualizarComboPersonas() {
        if (tallerActual == null) return;
        
        try {
            List<Persona> todas = personaRepo.listarTodos();
            Set<Long> idsOcupados = new HashSet<>();
            
            // Bloqueamos al instructor
            if (tallerActual.getInstructor() != null) {
                idsOcupados.add(tallerActual.getInstructor().getId());
            }
            
            // Bloqueamos a los organizadores
            tallerActual.getOrganizadores().forEach(o -> idsOcupados.add(o.getId()));
            
            // Bloqueamos a los ya inscriptos
            tallerActual.getInscripciones().forEach(i -> idsOcupados.add(i.getParticipante().getId()));
            
            List<Persona> aptos = todas.stream()
                    .filter(p -> !idsOcupados.contains(p.getId()))
                    .collect(Collectors.toList());
            
            comboPersonas.setItems(FXCollections.observableArrayList(aptos));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML 
    public void inscribir() { 
        Persona p = comboPersonas.getValue();
        if (p == null) { mostrarAlerta("Atención", "Seleccioná una persona."); return; }
        
        // Verificación de cupo antes de intentar en BD
        if (tallerActual.getInscripciones().size() >= tallerActual.getCupoMaximo()) {
            mostrarAlerta("Cupo Lleno", "No hay más lugar en este taller.");
            return;
        }

        try {
            tallerActual.inscribir(p);
            eventoRepo.actualizar(tallerActual); 
            
            actualizarComboPersonas(); // Quitamos a la persona del combo
            actualizarTabla();
            actualizarCupo(); 
            mostrarNotificacion("¡Inscripto correctamente!");
        } catch (Exception e) { 
            mostrarAlerta("Error de persistencia", "La persona ya podría estar inscripta."); 
            e.printStackTrace();
        }
    }

    @FXML
    public void marcarAsistencia() {
        Inscripcion sel = tablaInscriptos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Atención", "Seleccioná un inscripto de la tabla.");
            return;
        }

        try {
            // Cambiamos el estado en el objeto
            sel.setAsistio(!sel.isAsistio());
            
            // Sincronizamos con la base de datos
            eventoRepo.actualizar(tallerActual);
            
            // Refrescamos la UI
            tablaInscriptos.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo actualizar la asistencia.");
        }
    }

    private void darDeBaja(Inscripcion ins) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Quitar a " + ins.getParticipante().getNombreCompleto() + "?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                tallerActual.getInscripciones().remove(ins);
                eventoRepo.actualizar(tallerActual);
                
                actualizarComboPersonas(); // La persona vuelve a estar disponible en el combo
                actualizarTabla();
                actualizarCupo();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar la inscripción.");
            }
        }
    }

    private void actualizarTabla() {
        if (tallerActual != null) {
            // Importante: Crear una nueva lista observable para que la tabla refresque bien
            tablaInscriptos.setItems(FXCollections.observableArrayList(new ArrayList<>(tallerActual.getInscripciones())));
        }
    }

    private void actualizarCupo() {
        if (tallerActual != null) {
            int ocupados = tallerActual.getInscripciones().size();
            lblCupo.setText("Cupo: " + ocupados + " / " + tallerActual.getCupoMaximo() + " ocupados");
            lblCupo.setStyle(ocupados >= tallerActual.getCupoMaximo() ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #2c3e50;");
        }
    }

    private void agregarBotonEliminar() {
        ColAcciones.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            { 
                btn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-cursor: hand;"); 
                btn.setOnAction(e -> {
                    Inscripcion ins = getTableView().getItems().get(getIndex());
                    darDeBaja(ins);
                }); 
            }
            @Override protected void updateItem(Void item, boolean empty) { 
                super.updateItem(item, empty); 
                setGraphic(empty ? null : btn); 
            }
        });
    }

    private void mostrarNotificacion(String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void mostrarAlerta(String t, String m) { Alert a = new Alert(Alert.AlertType.WARNING, m); a.setTitle(t); a.show(); }
}