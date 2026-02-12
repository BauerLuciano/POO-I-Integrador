package com.eventos.modelo;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "peliculas")
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String director;

    @Column(name = "duracion_minutos")
    private int duracionMinutos;

    @ManyToOne
    @JoinColumn(name = "ciclo_id")
    private CicloCine ciclo;

    public Pelicula() {}

    public Pelicula(String titulo, String director, int duracionMinutos) {
        this.titulo = titulo;
        this.director = director;
        this.duracionMinutos = duracionMinutos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pelicula pelicula = (Pelicula) o;
        return duracionMinutos == pelicula.duracionMinutos && 
               Objects.equals(titulo.toLowerCase().trim(), pelicula.titulo.toLowerCase().trim()) && 
               Objects.equals(director.toLowerCase().trim(), pelicula.director.toLowerCase().trim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo.toLowerCase().trim(), director.toLowerCase().trim(), duracionMinutos);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public CicloCine getCiclo() { return ciclo; }
    public void setCiclo(CicloCine ciclo) { this.ciclo = ciclo; }
}