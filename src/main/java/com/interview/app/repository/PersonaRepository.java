package com.interview.app.repository;

import com.interview.app.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonaRepository
        extends JpaRepository<Persona, Long>,
                JpaSpecificationExecutor<Persona> {
    // JpaRepository porta: findById, findAll, save, deleteById, existsById, count, ecc.
    // JpaSpecificationExecutor porta: findAll(Specification, Pageable) per filtri dinamici
}
