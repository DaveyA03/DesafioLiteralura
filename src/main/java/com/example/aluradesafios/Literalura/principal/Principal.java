package com.example.aluradesafios.Literalura.principal;

import com.example.aluradesafios.Literalura.model.*;
import com.example.aluradesafios.Literalura.service.AutorService;
import com.example.aluradesafios.Literalura.service.ConsumoAPI;
import com.example.aluradesafios.Literalura.service.ConvierteDatos;
import com.example.aluradesafios.Literalura.service.LibroService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {


    public static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private LibroService libroServicio;
    private AutorService autorServicio;

    public Principal(LibroService libroService, AutorService autorService) {
        this.libroServicio = libroService;
        this.autorServicio = autorService;
    }

    public void muestraMenu() {
        var opcion = -1;
        while (opcion != 0) {

            try {
                String menu = """
                --------------
                **Catálogo de libros en Literalura**
                1.- Buscar libro por título
                2.- Listar libros registrados
                3.- Listar autores registrados
                4.- Listar autores vivos en un determinado año
                5.- Listar libros por idioma
                6.- Estadísticas de libros por número de descargas
                7.- Top 10 libros más descargados
                8.- Buscar autor por nombre
                0.- Salir
                --------------
                
                Elija la opción a través de su número:""";

                System.out.println(menu);
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {

                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        buscarAutoresVivosPorAnio();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 6:
                        estadisticasLibrosPorNumDescargas();
                        break;
                    case 7:
                        top10LibrosMasDescargados();
                        break;
                    case 8:
                        buscarAutorPorNombre();
                        break;
                    case 0:
                        System.out.println("Cerrando la aplicación...");
                        break;
                    default:
                        System.out.println("Opción inválida. Favor de introducir un número del menú.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Opción inválida. Favor de introducir un número del menú.");
                teclado.nextLine();
            }
        }
    }

    // Consulta informacion de la api guten
    private DatosResultados obtenerDatosResultados(String tituloLibro) {
        var json = consumoAPI.obtenerDatos(URL_BASE+"?search="+tituloLibro.replace(" ", "%20"));
        var datos = conversor.obtenerDatos(json, DatosResultados.class);
        return datos;
    }


    private void buscarLibroPorTitulo() {

        System.out.print("Escribe el título del libro que deseas buscar: ");
        var tituloLibro = teclado.nextLine().toUpperCase();


        Optional<Libro> libroRegistrado = libroServicio.buscarLibroPorTitulo(tituloLibro);


        if (libroRegistrado.isPresent()) {
            System.out.println("El libro buscado ya está registrado.");
        } else {
            //Busca los datos del libro en la API
            var datos = obtenerDatosResultados(tituloLibro);


            if (datos.listaLibros().isEmpty()){
                System.out.println("No se encontró el libro buscado en Gutendex API.");
            } else {

                DatosLibros datosLibros = datos.listaLibros().get(0);
                DatosAutores datosAutores = datosLibros.autores().get(0);
                String idioma = datosLibros.idiomas().get(0);
                Idiomas idiomas = Idiomas.fromString(idioma);

                //Se guarda toda la info de datosLibros en la clase Libro.
                Libro libro = new Libro(datosLibros);
                libro.setIdiomas(idiomas);


                Optional<Autor> autorRegistrado = autorServicio.buscarAutorRegistrado(datosAutores.nombre());


                if (autorRegistrado.isPresent()) {
                    System.out.println("El autor ya está registrado.");
                    Autor autorExiste = autorRegistrado.get();
                    libro.setAutor(autorExiste);
                } else {
                    Autor autor = new Autor(datosAutores);
                    autor = autorServicio.guardarAutor(autor);
                    libro.setAutor(autor);
                    autor.getLibros().add(libro);
                }

                try {
                    //Se guarda el libro buscado en la base de datos
                    libroServicio.guardarLibro(libro);
                    System.out.println("\nLibro encontrado.\n");
                    System.out.println(libro+"\n");
                    System.out.println("Libro guardado.\n");
                } catch (DataIntegrityViolationException e){
                    System.out.println("El libro ya está registrado.");
                }
            }
        }

    }

    // listar los libros registrados
    private void listarLibrosRegistrados() {

        //Obtiene  todos los libros registrados
        List<Libro> libros = libroServicio.listarLibrosRegistrados();

        //Verifica si existen libros registrados
        if (libros.isEmpty()) {
            //Si no existen libros , muestra el  mensaje
            System.out.println("No se encontraron libros registrados.");
        } else {
            System.out.println("Los libros registrados son los siguientes:\n");

            libros.stream()
                    .sorted(Comparator.comparing(Libro::getTitulo))
                    .forEach(System.out::println);
        }

    }


    private void listarAutoresRegistrados() {


        List<Autor> autores = autorServicio.listarAutoresRegistrados();


        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores registrados.");
        } else {
            System.out.println("Los autores registrados son los siguientes:\n");
            for (Autor autor : autores) {
                List<Libro> librosPorAutorId = libroServicio.buscarLibrosPorAutorId(autor.getId());

                //Muestra la info de  los autores registrados con sus libros
                System.out.println("----- AUTOR -----");
                System.out.println("Autor: "+autor.getNombre());
                System.out.println("Fecha de Nacimiento: "+autor.getFechaNacimiento());
                System.out.println("Fecha de Fallecido: "+autor.getFechaFallecido());

                if (librosPorAutorId.isEmpty()) {
                    System.out.println("No se encontraron libros registrados para este autor.");
                } else {
                    String librosRegistrados = librosPorAutorId.stream()
                            .map(Libro::getTitulo)
                            .collect(Collectors.joining(", "));
                    System.out.println("Libros: ["+librosRegistrados+"]");
                    System.out.println("-----------------\n");
                }
            }
        }

    }


    private void buscarAutoresVivosPorAnio() {

        System.out.print("Escribe el año vivo del autor(es) que desea buscar: ");
        var anioDelAutor = teclado.nextInt();

        List<Autor> buscarAutoresPorAnio = autorServicio.buscarAutoresVivosPorAnio(anioDelAutor);

        //Verifica si existen autores que estaban vivos por su año
        if (buscarAutoresPorAnio.isEmpty()) {
            //Si no existen  autores, muestra el siguiente mensaje
            System.out.println("No se encontraron autores vivos por el año buscado.");
        } else {
            System.out.printf("El autor o los autores vivos del año %d son los siguientes:\n", anioDelAutor);
            for (Autor autoresVivos : buscarAutoresPorAnio) {
                List<Libro> librosAutoresVivosPorId = libroServicio.buscarLibrosPorAutorId(autoresVivos.getId());

                System.out.println("----- AUTOR -----");
                System.out.println("Autor: "+autoresVivos.getNombre());
                System.out.println("Fecha de Nacimiento: "+autoresVivos.getFechaNacimiento());
                System.out.println("Fecha de Fallecido: "+autoresVivos.getFechaFallecido());

                if (librosAutoresVivosPorId.isEmpty()) {
                    System.out.println("No se encontraron libros registrados para este autor.");
                } else {
                    String librosRegistrados = librosAutoresVivosPorId.stream()
                            .map(Libro::getTitulo)
                            .collect(Collectors.joining(", "));
                    System.out.println("Libros: ["+librosRegistrados+"]");
                    System.out.println("-----------------\n");
                }
            }
        }

    }

    // listar libros por idioma
    private void listarLibrosPorIdioma() {

        System.out.println("""
                Estos son los idiomas disponibles:
                - es -> Español
                - en -> Inglés
                - fr -> Francés
                - pt -> Portugués
                """);

        System.out.print("Escribe el idioma abreviado para buscar los libros: ");
        var nombreIdioma = teclado.nextLine();


        try {

            List<Libro> buscarLibrosPorIdioma = libroServicio.buscarLibroPorIdiomas(Idiomas.fromString(nombreIdioma));


            if (buscarLibrosPorIdioma.isEmpty()) {
                System.out.println("No se encontraron los libros del idioma buscado.");
            } else {
                System.out.printf("Los libros del idioma '%s' son los siguientes:\n", nombreIdioma);
                buscarLibrosPorIdioma.forEach(l -> System.out.print(l.toString()));
            }
        } catch (Exception e) {
            System.out.println("Opción inválida. Favor de escribir un idioma abreviado del menú.");
        }

    }

    // listar estadísticas de los libros por número de descargas
    private void estadisticasLibrosPorNumDescargas() {

        List<Libro> todosLosLibros = libroServicio.listarLibrosRegistrados();

        if (todosLosLibros.isEmpty()){
            System.out.println("No se encontraron libros registrados.");
        } else {
            System.out.println("Estadísticas de los libros por número de descargas:\n");
            DoubleSummaryStatistics est = todosLosLibros.stream()
                    .filter(libro -> libro.getNumeroDescargas() > 0)
                    .collect(Collectors.summarizingDouble(Libro::getNumeroDescargas));
            System.out.println("Cantidad media de descargas: " + est.getAverage());
            System.out.println("Cantidad máxima de descargas: "+ est.getMax());
            System.out.println("Cantidad mínima de descargas: " + est.getMin());
        }

    }

    // listar top 10 libros más descargados
    private void top10LibrosMasDescargados() {

        List<Libro> top10LibrosMasDescargados = libroServicio.listarTop10LibrosMasDescargados();

        if (top10LibrosMasDescargados.isEmpty()) {
            System.out.println("No se encontraron libros suficientes para mostrar.");
        } else {
            System.out.println("Top 10 libros más descargados:\n");
            top10LibrosMasDescargados.forEach(libro -> System.out.println(libro.toString()));
        }

    }

    //buscar autores por el nombre
    private void buscarAutorPorNombre() {

        System.out.print("Escribe el nombre del autor que deseas buscar: ");
        var nombreAutor = teclado.nextLine().toUpperCase();

        List<Autor> autorBuscado = autorServicio.buscarAutorPorNombre(nombreAutor);

        //Verifica si el autor está registrado
        if (autorBuscado.isEmpty()) {
            // si no
            System.out.println("No se encontraron autores registrados.");
        } else {
            System.out.printf("El autor encontrado de nombre '%s' es el siguiente:\n", nombreAutor);
            for (Autor autor : autorBuscado) {
                List<Libro> librosPorAutorId = libroServicio.buscarLibrosPorAutorId(autor.getId());

                System.out.println("----- AUTOR -----");
                System.out.println("Autor: "+autor.getNombre());
                System.out.println("Fecha de Nacimiento: "+autor.getFechaNacimiento());
                System.out.println("Fecha de Fallecido: "+autor.getFechaFallecido());

                if (librosPorAutorId.isEmpty()) {
                    System.out.println("No se encontraron libros registrados para este autor.");
                } else {
                    String librosRegistrados = librosPorAutorId.stream()
                            .map(Libro::getTitulo)
                            .collect(Collectors.joining(", "));
                    System.out.println("Libros: ["+librosRegistrados+"]");
                    System.out.println("-----------------\n");
                }
            }
        }

    }

}