package com.eventos.modelo;

import com.eventos.enums.Modalidad;
import com.eventos.interfaz.Inscribible;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

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

    // USAMOS SET: Esto evita que Bauer Luciano aparezca dos veces
    @OneToMany(mappedBy = "taller", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Inscripcion> inscripciones = new LinkedHashSet<>();

    public Taller() { super(); }

    public Taller(String nombre, java.time.LocalDateTime fecha, int duracion, int cupo, Modalidad mod) {
        super();
        this.setNombre(nombre);
        this.setFechaInicio(fecha);
        this.setDuracionEstimada(duracion);
        this.cupoMaximo = cupo;
        this.modalidad = mod;
    }

    @Override
    public boolean hayCupo() {
        return inscripciones.size() < cupoMaximo;
    }

    @Override
    public void inscribir(Persona persona) {
        if (this.getEstado() != com.eventos.enums.EstadoEvento.CONFIRMADO) {
            throw new RuntimeException("Solo se pueden inscribir a eventos CONFIRMADOS.");
        }

        if (!hayCupo()) { 
            throw new RuntimeException("No hay cupo disponible.");
        }

        Inscripcion nueva = new Inscripcion(this, persona);
        // Si el Set no lo agrega, es porque ya existía el DNI en este taller
        if (!inscripciones.add(nueva)) {
            throw new RuntimeException("Esta persona YA está inscripta.");
        }
    }

    // --- GETTERS Y SETTERS ---
    public int getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(int cupoMaximo) { this.cupoMaximo = cupoMaximo; }
    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }
    public Persona getInstructor() { return instructor; }
    public void setInstructor(Persona instructor) { this.instructor = instructor; }
    public Set<Inscripcion> getInscripciones() { return inscripciones; }
}