package com.eventos.modelo;

import com.eventos.enums.EstadoEvento; 
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "eventos")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "duracion_minutos")
    private int duracionEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "evento_organizadores",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private Set<Persona> organizadores = new HashSet<>();

    public Evento() {
        this.estado = EstadoEvento.EN_PLANIFICACION;
    }

    public void agregarOrganizador(Persona responsable) {
        if (responsable != null) this.organizadores.add(responsable);
    }

    // --- GETTERS Y SETTERS SINCRONIZADOS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public int getDuracionEstimada() { return duracionEstimada; }
    public void setDuracionEstimada(int duracionEstimada) { this.duracionEstimada = duracionEstimada; }
    public EstadoEvento getEstado() { return estado; }
    public void setEstado(EstadoEvento estado) { this.estado = estado; }
    
    // Cambio a Set para evitar duplicados según el UML
    public Set<Persona> getOrganizadores() { return organizadores; }
    public void setOrganizadores(Set<Persona> organizadores) { this.organizadores = organizadores; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return id != null && Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}