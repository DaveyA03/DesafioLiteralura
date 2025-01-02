package com.example.aluradesafios.Literalura.repository;

import com.example.aluradesafios.Literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    //Buscar el autor por nombre
    Optional<Autor> findByNombreIgnoreCase(String nombre);

    //Busca los autores  por un determinado año
    @Query("SELECT a FROM Autor a WHERE a.fechaNacimiento < :anioAutor AND a.fechaFallecido > :anioAutor")
    List<Autor> buscarAutoresVivosPorAnio(int anioAutor);

    // busca por nombre usando jpql
    @Query("SELECT a FROM Autor a WHERE a.nombre ILIKE %:nombreAutor%")
    List<Autor> buscarAutorPorNombre(String nombreAutor);
}