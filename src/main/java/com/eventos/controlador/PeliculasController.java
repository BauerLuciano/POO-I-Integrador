package com.eventos.controlador;

import com.eventos.modelo.CicloCine;
import com.eventos.modelo.Pelicula;
import com.eventos.repo.EventoRepository;
import com.eventos.repo.EventoRepositoryImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PeliculasController {

    @FXML private Label lblCicloNombre;
    @FXML private ListView<Pelicula> listaPeliculas;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtDirector;
    
    // CAMBIO: Variable para la duración
    @FXML private TextField txtDuracion;

    private EventoRepository repo = new EventoRepositoryImpl();
    private CicloCine cicloActual;

    public void initData(CicloCine ciclo) {
        this.cicloActual = ciclo;
        lblCicloNombre.setText("Ciclo: " + ciclo.getNombre());
        actualizarLista();
    }

    private void actualizarLista() {
        listaPeliculas.getItems().clear();
        listaPeliculas.getItems().addAll(cicloActual.getPeliculas());
    }

    @FXML
    public void agregarPelicula() {
        try {
            String titulo = txtTitulo.getText();
            String director = txtDirector.getText();
            
            // Validaciones
            if (titulo.isEmpty()) throw new RuntimeException("Falta el título");
            if (txtDuracion.getText().isEmpty()) throw new RuntimeException("Falta la duración");

            // Parsear duración
            int minutos = Integer.parseInt(txtDuracion.getText());

            // Crear película con duración
            Pelicula p = new Pelicula(titulo, director, minutos);
            
            cicloActual.agregarPelicula(p);
            repo.actualizar(cicloActual); 

            // Limpieza
            txtTitulo.clear(); txtDirector.clear(); txtDuracion.clear();
            actualizarLista();
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La duración debe ser un número (minutos).");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarPelicula() {
        Pelicula seleccionada = listaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        cicloActual.eliminarPelicula(seleccionada);
        repo.actualizar(cicloActual);
        actualizarLista();
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
}