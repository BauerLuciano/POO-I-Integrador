package com.eventos.controlador;

import com.eventos.modelo.CicloCine;
import com.eventos.modelo.Pelicula;
import com.eventos.repo.EventoRepository;
import com.eventos.repo.EventoRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class PeliculasController {

    @FXML private Label lblCicloNombre, lblTotalDuracion;
    @FXML private ListView<Pelicula> listaPeliculas;
    @FXML private TextField txtTitulo, txtDirector, txtDuracion;

    private final EventoRepository repo = new EventoRepositoryImpl();
    private CicloCine cicloActual;   // ← siempre actualizada con la última versión gestionada

    @FXML
    public void initialize() {
        listaPeliculas.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Pelicula p, boolean empty) {
                super.updateItem(p, empty);
                setText((empty || p == null) ? null : p.getTitulo() + " [" + p.getDuracionMinutos() + " min]");
            }
        });
    }

    public void initData(CicloCine ciclo) {
        this.cicloActual = ciclo;
        lblCicloNombre.setText("Ciclo: " + ciclo.getNombre());
        actualizarInterfaz();
    }

    private void actualizarInterfaz() {
        if (cicloActual == null) return;
        List<Pelicula> pelis = cicloActual.getPeliculas();
        listaPeliculas.setItems(FXCollections.observableArrayList(pelis));

        int total = pelis.stream().mapToInt(Pelicula::getDuracionMinutos).sum();
        lblTotalDuracion.setText(total + " minutos");
    }

    @FXML
    public void agregarPelicula() {
        try {
            Pelicula p = new Pelicula(
                txtTitulo.getText(),
                txtDirector.getText(),
                Integer.parseInt(txtDuracion.getText())
            );

            cicloActual.agregarPelicula(p);

            // 🔥 GUARDAR Y REASIGNAR LA ENTIDAD GESTIONADA
            cicloActual = (CicloCine) repo.actualizar(cicloActual);

            limpiarCampos();
            actualizarInterfaz();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    @FXML
    public void eliminarPelicula() {
        Pelicula seleccionada = listaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        cicloActual.eliminarPelicula(seleccionada);

        cicloActual = (CicloCine) repo.actualizar(cicloActual);

        actualizarInterfaz();
    }

    private void limpiarCampos() {
        txtTitulo.clear();
        txtDirector.clear();
        txtDuracion.clear();
    }
}