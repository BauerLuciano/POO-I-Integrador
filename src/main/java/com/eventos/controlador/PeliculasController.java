package com.eventos.controlador;

import com.eventos.modelo.CicloCine;
import com.eventos.modelo.Pelicula;
import com.eventos.repo.EventoRepository;
import com.eventos.repo.EventoRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;

public class PeliculasController {

    @FXML private Label lblCicloNombre, lblTotalDuracion;
    @FXML private ListView<Pelicula> listaPeliculas;
    @FXML private TextField txtTitulo, txtDirector, txtDuracion;

    private final EventoRepository repo = new EventoRepositoryImpl();
    private CicloCine cicloActual;

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
        
        // Convertimos el Set a un ArrayList para que el ListView de JavaFX pueda mostrarlo
        listaPeliculas.setItems(FXCollections.observableArrayList(new ArrayList<>(cicloActual.getPeliculas())));

        int total = cicloActual.getDuracionTotalPeliculas();
        lblTotalDuracion.setText(total + " minutos");
    }

    @FXML
    public void agregarPelicula() {
        try {
            Pelicula p = new Pelicula(txtTitulo.getText(), txtDirector.getText(), Integer.parseInt(txtDuracion.getText()));
            
            cicloActual.agregarPelicula(p);
            
            // Actualizamos en la base de datos y recuperamos la versión limpia
            cicloActual = (CicloCine) repo.actualizar(cicloActual);

            txtTitulo.clear(); txtDirector.clear(); txtDuracion.clear();
            actualizarInterfaz();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    @FXML
    public void eliminarPelicula() {
        Pelicula seleccionada = listaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            cicloActual.eliminarPelicula(seleccionada);
            cicloActual = (CicloCine) repo.actualizar(cicloActual);
            actualizarInterfaz();
        }
    }
}