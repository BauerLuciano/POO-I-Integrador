package com.eventos.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "taller_inscripciones", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"taller_id", "persona_id"})
})
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inscripcion")
    private LocalDateTime fechaInscripcion;

    private boolean asistio;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona participante;

    @ManyToOne
    @JoinColumn(name = "taller_id")
    private Taller taller;

    public Inscripcion() {
        this.fechaInscripcion = LocalDateTime.now();
        this.asistio = false;
    }

    public Inscripcion(Taller taller, Persona participante) {
        this();
        this.taller = taller;
        this.participante = participante;
    }

    // --- CRÍTICO: Para evitar duplicados en el Set ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inscripcion that = (Inscripcion) o;
        return Objects.equals(participante, that.participante) && 
               Objects.equals(taller, that.taller);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participante, taller);
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
    public boolean isAsistio() { return asistio; }
    public void setAsistio(boolean asistio) { this.asistio = asistio; }
    public Persona getParticipante() { return participante; }
    public void setParticipante(Persona participante) { this.participante = participante; }
    public Taller getTaller() { return taller; }
    public void setTaller(Taller taller) { this.taller = taller; }
}