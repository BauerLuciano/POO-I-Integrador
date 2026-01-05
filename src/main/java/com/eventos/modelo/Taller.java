package com.eventos.modelo;

import com.eventos.enums.Modalidad;
import com.eventos.interfaz.Inscribible;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "talleres")
@PrimaryKeyJoinColumn(name = "id")
public class Taller extends Evento implements Inscribible {

    @Column(name = "cupo_maximo", nullable = false)
    private int cupoMaximo;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Persona instructor;

    // Relación Muchos a Muchos: Un taller tiene muchas personas inscriptas
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "taller_inscripciones",
        joinColumns = @JoinColumn(name = "taller_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> inscripciones = new ArrayList<>();

    public Taller() {
        super();
    }

    public Taller(String nombre, java.time.LocalDateTime fecha, int duracion, int cupo, Modalidad mod) {
        super();
        this.setNombre(nombre);
        this.setFechaInicio(fecha);
        this.setDuracionEstimada(duracion);
        this.cupoMaximo = cupo;
        this.modalidad = mod;
    }

    // --- LÓGICA DE INSCRIPCIÓN (Cumpliendo contrato con Inscribible) ---
    
    @Override
    public boolean hayCupo() {
        // Devuelve TRUE si la cantidad de inscriptos es menor al tope
        return inscripciones.size() < cupoMaximo;
    }

    @Override
    public void inscribir(Persona persona) {
        if (!hayCupo()) { // Reutilizamos el método de arriba
            throw new RuntimeException("No hay cupo disponible en este taller.");
        }
        if (inscripciones.contains(persona)) {
            throw new RuntimeException("Esta persona ya está inscripta.");
        }
        inscripciones.add(persona);
    }

    // --- GETTERS Y SETTERS ---
    public int getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(int cupoMaximo) { this.cupoMaximo = cupoMaximo; }

    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }

    public Persona getInstructor() { return instructor; }
    public void setInstructor(Persona instructor) { this.instructor = instructor; }

    public List<Persona> getInscripciones() {
        return inscripciones;
    }
}