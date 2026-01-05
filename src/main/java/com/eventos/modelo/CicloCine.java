package com.eventos.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ciclos_cine")
@PrimaryKeyJoinColumn(name = "id")
public class CicloCine extends Evento {

    @Column(name = "hay_charlas")
    private boolean hayCharlas;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "ciclo_id") 
    private List<Pelicula> peliculas = new ArrayList<>();

    public CicloCine() { super(); }

    public boolean isHayCharlas() { return hayCharlas; }
    public void setHayCharlas(boolean hayCharlas) { this.hayCharlas = hayCharlas; }

    // Getter para la lista
    public List<Pelicula> getPeliculas() { return peliculas; }
    
    // Método helper para agregar fácil
    public void agregarPelicula(Pelicula p) {
        this.peliculas.add(p);
    }
    
    public void eliminarPelicula(Pelicula p) {
        this.peliculas.remove(p);
    }
}