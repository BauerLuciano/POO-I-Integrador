package com.eventos.repo;

import com.eventos.modelo.Evento;
import com.eventos.enums.EstadoEvento;
import com.eventos.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class EventoRepositoryImpl implements EventoRepository {

    @Override
    public void guardar(Evento evento) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(evento);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Evento actualizar(Evento evento) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Evento managed = em.merge(evento);
            em.getTransaction().commit();
            return managed;   // 👈 DEVOLVEMOS LA GESTIONADA
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Evento buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Evento.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Evento> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Evento> buscarPorEstado(EstadoEvento estado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e WHERE e.estado = :estado", Evento.class)
                     .setParameter("estado", estado)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Evento evento = em.find(Evento.class, id);
            if (evento != null) {
                em.remove(evento);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}