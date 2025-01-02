package com.example.aluradesafios.Literalura.service;

import com.example.aluradesafios.Literalura.model.Idiomas;
import com.example.aluradesafios.Literalura.model.Libro;
import com.example.aluradesafios.Literalura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepositorio;

    public Optional<Libro> buscarLibroPorTitulo(String titulo) {
        return libroRepositorio.findByTituloIgnoreCase(titulo);
    }

    public Libro guardarLibro(Libro libro) {
        return libroRepositorio.save(libro);
    }

    public List<Libro> listarLibrosRegistrados() {
        return libroRepositorio.findAll();
    }

    public List<Libro> buscarLibrosPorAutorId(Long id) {
        return libroRepositorio.buscarLibrosPorAutorId(id);
    }

    public List<Libro> buscarLibroPorIdiomas(Idiomas nombreIdioma) {
        return libroRepositorio.findByIdiomas(nombreIdioma);
    }

    public List<Libro> listarTop10LibrosMasDescargados() {
        return libroRepositorio.top10LibrosMasDescargados();
    }
}