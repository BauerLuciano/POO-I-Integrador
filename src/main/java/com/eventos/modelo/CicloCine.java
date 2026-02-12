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
    private Set<Pelicula> peliculas = new LinkedHashSet<>();

    public CicloCine() { super(); }

    public boolean isHayCharlas() { return hayCharlas; }
    public void setHayCharlas(boolean hayCharlas) { this.hayCharlas = hayCharlas; }
    public Set<Pelicula> getPeliculas() { return peliculas; }

    public void agregarPelicula(Pelicula p) {
        if (p != null) {
            peliculas.add(p);
            p.setCiclo(this);
            // Actualizamos la duración del evento padre automáticamente
            this.setDuracionEstimada(getDuracionTotalPeliculas());
        }
    }

    public void eliminarPelicula(Pelicula p) {
        if (p != null) {
            peliculas.remove(p);
            p.setCiclo(null);
            this.setDuracionEstimada(getDuracionTotalPeliculas());
        }
    }

    public int getDuracionTotalPeliculas() {
        return peliculas.stream().mapToInt(Pelicula::getDuracionMinutos).sum();
    }
}