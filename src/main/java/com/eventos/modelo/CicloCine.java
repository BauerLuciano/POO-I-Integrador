package com.eventos.modelo;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "ciclos_cine")
@PrimaryKeyJoinColumn(name = "id")
public class CicloCine extends Evento {

    @Column(name = "hay_charlas")
    private boolean hayCharlas;

    @OneToMany(mappedBy = "ciclo", cascade = CascadeType.ALL,
               fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Pelicula> peliculas = new ArrayList<>();

    public CicloCine() {
        super();
    }

    public boolean isHayCharlas() { return hayCharlas; }
    public void setHayCharlas(boolean hayCharlas) { this.hayCharlas = hayCharlas; }

    public List<Pelicula> getPeliculas() {
        return peliculas;  // devolvemos la lista directamente
    }

    public void agregarPelicula(Pelicula p) {
        if (p != null && !peliculas.contains(p)) {
            peliculas.add(p);
            p.setCiclo(this);
        }
    }

    public void eliminarPelicula(Pelicula p) {
        if (p != null) {
            peliculas.remove(p);
            p.setCiclo(null);
        }
    }
}