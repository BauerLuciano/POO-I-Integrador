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

public class EventoController {

    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<EstadoEvento> comboEstado;
    @FXML private TextField txtNombre, txtHora, txtDuracion, txtCupo, txtStands, txtTipoArte;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Persona> comboOrganizador, comboInstructor, comboArtista, comboCurador;
    @FXML private VBox panelTaller, panelConcierto, panelFeria, panelExposicion, panelCicloCine;
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
        
        dpFecha.setDayCellFactory(p -> new DateCell() {
            @Override public void updateItem(LocalDate d, boolean e) {
                super.updateItem(d, e);
                if (d != null && d.isBefore(LocalDate.now())) setDisable(true);
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
        if (!evento.getOrganizadores().isEmpty()) comboOrganizador.setValue(evento.getOrganizadores().iterator().next());

        comboTipo.setDisable(true);
        ocultarPaneles();

        if (evento instanceof Taller t) {
            txtCupo.setText(String.valueOf(t.getCupoMaximo()));
            comboModalidad.setValue(t.getModalidad());
            comboInstructor.setValue(t.getInstructor());
            mostrarPanel(panelTaller);
        } else if (evento instanceof CicloCine cc) {
            chkHayCharlas.setSelected(cc.isHayCharlas());
            mostrarPanel(panelCicloCine);
        } else if (evento instanceof Concierto c) {
            comboTipoEntrada.setValue(c.getTipoEntrada());
            if (!c.getArtistas().isEmpty()) comboArtista.setValue(c.getArtistas().iterator().next());
            mostrarPanel(panelConcierto);
        } else if (evento instanceof Feria f) {
            txtStands.setText(String.valueOf(f.getCantidadStands()));
            comboUbicacionFeria.setValue(f.isAlAireLibre() ? "Es al aire libre" : "Techado");
            mostrarPanel(panelFeria);
        } else if (evento instanceof Exposicion ex) {
            txtTipoArte.setText(ex.getTipoArte());
            comboCurador.setValue(ex.getCurador());
            mostrarPanel(panelExposicion);
        }
    }

    @FXML
    public void guardarEvento() {
        try {
            // RE-SINCRONIZACIÓN: Clave para evitar clones
            Evento ev = (eventoEnEdicion != null) ? eventoRepo.buscarPorId(eventoEnEdicion.getId()) : crearInstancia(comboTipo.getValue());
            
            ev.setNombre(txtNombre.getText());
            ev.setEstado(comboEstado.getValue());
            ev.setFechaInicio(dpFecha.getValue().atTime(LocalTime.parse(txtHora.getText())));

            if (ev instanceof CicloCine cc) {
                cc.setHayCharlas(chkHayCharlas.isSelected());
                ev.setDuracionEstimada(cc.getDuracionTotalPeliculas());
            } else {
                ev.setDuracionEstimada(Integer.parseInt(txtDuracion.getText()));
            }

            if (ev instanceof Taller t) {
                t.setCupoMaximo(Integer.parseInt(txtCupo.getText()));
                t.setModalidad(comboModalidad.getValue());
                t.setInstructor(comboInstructor.getValue());
            } else if (ev instanceof Concierto c) {
                c.setTipoEntrada(comboTipoEntrada.getValue());
                if (comboArtista.getValue() != null) { c.getArtistas().clear(); c.getArtistas().add(comboArtista.getValue()); }
            } else if (ev instanceof Feria f) {
                f.setCantidadStands(Integer.parseInt(txtStands.getText()));
                f.setAlAireLibre("Es al aire libre".equals(comboUbicacionFeria.getValue()));
            } else if (ev instanceof Exposicion ex) {
                ex.setTipoArte(txtTipoArte.getText());
                ex.setCurador(comboCurador.getValue());
            }

            if (comboOrganizador.getValue() != null) {
                ev.getOrganizadores().clear();
                ev.getOrganizadores().add(comboOrganizador.getValue());
            }

            eventoRepo.actualizar(ev);
            cerrarVentana();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    private Evento crearInstancia(String t) {
        return switch (t) {
            case "Taller" -> new Taller();
            case "CicloCine" -> new CicloCine();
            case "Feria" -> new Feria();
            case "Exposicion" -> new Exposicion();
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

    @FXML public void onTipoChange() {
        ocultarPaneles();
        String t = comboTipo.getValue();
        if ("Taller".equals(t)) mostrarPanel(panelTaller);
        else if ("CicloCine".equals(t)) mostrarPanel(panelCicloCine);
        else if ("Concierto".equals(t)) mostrarPanel(panelConcierto);
        else if ("Feria".equals(t)) mostrarPanel(panelFeria);
        else if ("Exposicion".equals(t)) mostrarPanel(panelExposicion);
    }

    private void ocultarPaneles() { VBox[] p = {panelTaller, panelConcierto, panelFeria, panelExposicion, panelCicloCine}; for (VBox v : p) if (v != null) { v.setVisible(false); v.setManaged(false); } }
    private void mostrarPanel(VBox p) { if (p != null) { p.setVisible(true); p.setManaged(true); } }
    @FXML public void cerrarVentana() { ((Stage)txtNombre.getScene().getWindow()).close(); }
}