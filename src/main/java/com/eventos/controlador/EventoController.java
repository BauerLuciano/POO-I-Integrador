package com.eventos.controlador;

import com.eventos.enums.*;
import com.eventos.modelo.*;
import com.eventos.repo.EventoRepository;
import com.eventos.repo.EventoRepositoryImpl;
import com.eventos.repo.PersonaRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventoController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private TextField txtNombre, txtHora, txtDuracion;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Persona> comboOrganizador, comboInstructor, comboArtista, comboCurador;
    @FXML private VBox panelTaller, panelConcierto, panelFeria, panelExposicion, panelCicloCine;
    @FXML private TextField txtCupo, txtStands, txtTipoArte;
    @FXML private ComboBox<Modalidad> comboModalidad;
    @FXML private ComboBox<TipoEntrada> comboTipoEntrada;
    @FXML private ComboBox<String> comboUbicacionFeria;
    @FXML private CheckBox chkHayCharlas;

    private final EventoRepository eventoRepo = new EventoRepositoryImpl();
    private final PersonaRepository personaRepo = new PersonaRepository();
    private Evento eventoEnEdicion;

    @FXML
    public void initialize() {
        comboTipo.getItems().addAll("Taller", "Concierto", "Feria", "Exposicion", "CicloCine");
        comboUbicacionFeria.getItems().addAll("Es al aire libre", "Techado");
        comboModalidad.getItems().setAll(Modalidad.values());
        comboTipoEntrada.getItems().setAll(TipoEntrada.values());
        comboEstado.getItems().setAll(EstadoEvento.values());
        
        actualizarCombosPersonas();

        // --- FIX DATEPICKER: Bloquear fechas pasadas ---
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        });
    }

    public void initData(Evento evento) {
        this.eventoEnEdicion = evento;
        txtNombre.setText(evento.getNombre());
        if (evento.getFechaInicio() != null) {
            dpFecha.setValue(evento.getFechaInicio().toLocalDate());
            txtHora.setText(evento.getFechaInicio().toLocalTime().toString());
        }
        txtDuracion.setText(String.valueOf(evento.getDuracionEstimada()));
        comboEstado.setValue(evento.getEstado());
        if (!evento.getOrganizadores().isEmpty()) comboOrganizador.setValue(evento.getOrganizadores().get(0));

        comboTipo.setDisable(true);
        
        // --- FIX CARGA CICLO CINE ---
        ocultarPaneles();
        if (evento instanceof Taller t) {
            comboTipo.setValue("Taller");
            txtCupo.setText(String.valueOf(t.getCupoMaximo()));
            comboModalidad.setValue(t.getModalidad());
            comboInstructor.setValue(t.getInstructor());
            mostrarPanel(panelTaller);
        } else if (evento instanceof CicloCine cc) {
            comboTipo.setValue("CicloCine");
            chkHayCharlas.setSelected(cc.isHayCharlas()); // Aquí cargamos el dato
            mostrarPanel(panelCicloCine);
        } // ... resto de los else if
    }

    @FXML
    public void guardarEvento() {
        try {
            Evento ev = (eventoEnEdicion != null) ? eventoEnEdicion : crearInstancia(comboTipo.getValue());
            ev.setNombre(txtNombre.getText());
            ev.setEstado(comboEstado.getValue());
            ev.setFechaInicio(dpFecha.getValue().atTime(LocalTime.parse(txtHora.getText())));
            ev.setDuracionEstimada(Integer.parseInt(txtDuracion.getText()));

            // --- FIX PERSISTENCIA CICLO CINE ---
            if (ev instanceof CicloCine cc) {
                cc.setHayCharlas(chkHayCharlas.isSelected()); // Aquí guardamos el dato
            } else if (ev instanceof Taller t) {
                t.setCupoMaximo(Integer.parseInt(txtCupo.getText()));
                t.setModalidad(comboModalidad.getValue());
                t.setInstructor(comboInstructor.getValue());
            }

            if (eventoEnEdicion != null) eventoRepo.actualizar(ev);
            else eventoRepo.guardar(ev);

            cerrarVentana();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage()).show();
        }
    }

    private Evento crearInstancia(String tipo) {
        return switch (tipo) {
            case "Taller" -> new Taller();
            case "CicloCine" -> new CicloCine();
            case "Feria" -> new Feria();
            default -> new Concierto();
        };
    }

    private void actualizarCombosPersonas() {
        var todas = personaRepo.listarTodos();
        comboOrganizador.getItems().setAll(todas);
        comboInstructor.getItems().setAll(todas);
        comboArtista.getItems().setAll(todas);
        comboCurador.getItems().setAll(todas);
    }

    @FXML
    public void onTipoChange() {
        if (eventoEnEdicion != null) return;
        ocultarPaneles();
        String tipo = comboTipo.getValue();
        if (tipo == null) return;
        switch (tipo) {
            case "Taller" -> mostrarPanel(panelTaller);
            case "CicloCine" -> mostrarPanel(panelCicloCine);
            case "Concierto" -> mostrarPanel(panelConcierto);
            case "Feria" -> mostrarPanel(panelFeria);
        }
    }

    private void ocultarPaneles() {
        VBox[] p = {panelTaller, panelConcierto, panelFeria, panelExposicion, panelCicloCine};
        for (VBox v : p) if (v != null) { v.setVisible(false); v.setManaged(false); }
    }

    private void mostrarPanel(VBox p) { if (p != null) { p.setVisible(true); p.setManaged(true); } }

    @FXML public void cerrarVentana() { ((Stage)txtNombre.getScene().getWindow()).close(); }
}