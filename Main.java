import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import models.Registro;
import service.GeneradorDatos;
import service.TransporteService;

/**
 * Caso de estudio - Unidad 2
 * Proyecto TecnoMovil Data (TecnoValle)
 *
 * Programa principal: genera datos simulados y muestra los resultados
 * de cada tarea del modulo funcional.
 */
public class Main {

    // Umbral simulado: si una ruta tiene mas entradas que esto, es "critica"
    private static final long UMBRAL_OCUPACION = 2000;

    public static void main(String[] args) {
        List<Registro> registros = GeneradorDatos.generarDatos(2000); // 20.000 registros simulados
        System.out.println("Registros simulados: " + registros.size());
        System.out.println("Ejemplo: " + registros.get(0));

        // a)
        System.out.println("\n--- a) Afluencia por estacion (entradas) ---");
        TransporteService.afluenciaPorEstacion(registros).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

        // Comprobacion de que el resultado paralelo es igual al secuencial
        boolean iguales = TransporteService.afluenciaPorEstacion(registros)
                .equals(TransporteService.afluenciaPorEstacionParalelo(registros));
        System.out.println("Secuencial == Paralelo ? " + iguales);

        // b)
        System.out.println("\n--- b) Horas pico (top 3) ---");
        TransporteService.horasPico(TransporteService.flujoPorHora(registros), 3)
                .forEach(e -> System.out.println(e.getKey() + ":00 -> " + e.getValue() + " registros"));

        // c)
        System.out.println("\n--- c) Rutas mas utilizadas ---");
        TransporteService.rutasMasUsadas(TransporteService.usoPorRuta(registros))
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

        // d)
        System.out.println("\n--- d) Patron de viaje (usuarios 1 a 3) ---");
        Map<Integer, List<String>> patrones = TransporteService.patronesPorUsuario(registros);
        Stream.of(1, 2, 3)
                .forEach(id -> System.out.println("Usuario " + id + ": " + patrones.get(id)));

        // e)
        System.out.println("\n--- e) Tiempo promedio entre estaciones (5 primeras, A -> B) ---");
        TransporteService.tiempoPromedioEntreEstaciones(registros).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(5)
                .forEach(e -> System.out.printf("%s: %.1f min%n", e.getKey(), e.getValue()));

        // f)
        System.out.println("\n--- f) Estado de las rutas (umbral = " + UMBRAL_OCUPACION + ") ---");
        TransporteService.estadoPorRuta(registros, UMBRAL_OCUPACION).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
    }
}
