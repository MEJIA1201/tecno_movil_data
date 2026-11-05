package service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import models.Registro;

/**
 * Generacion de datos simulados del transporte urbano.
 */
public class GeneradorDatos {

    // Datos de apoyo para la simulacion
    private static final List<String> RUTAS =
            List.of("R1", "R2", "R3", "R4", "R5", "R6");
    private static final List<String> ESTACIONES =
            List.of("Central", "Norte", "Sur", "Oriente", "Occidente",
                    "Universidad", "Hospital", "Terminal");

    // Cada usuario hace 10 registros (entrada, salida, entrada, ...)
    // avanzando el reloj entre 3 y 20 minutos en cada uno.
    public static List<Registro> generarDatos(int cantidadUsuarios) {
        Random random = new Random(42); // semilla fija para repetir resultados
        LocalDateTime inicioDia = LocalDateTime.of(2026, 11, 4, 5, 0);

        return IntStream.rangeClosed(1, cantidadUsuarios)
                .boxed()
                .flatMap(idUsuario -> {
                    // hora de inicio de este usuario entre las 5:00 y las 20:00
                    LocalDateTime reloj = inicioDia.plusMinutes(random.nextInt(15 * 60));
                    List<Registro> registrosUsuario = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        // la ruta se inclina hacia R1, R2... para que haya rutas mas usadas
                        String ruta = RUTAS.get(Math.min(random.nextInt(6), random.nextInt(6)));
                        String estacion = ESTACIONES.get(random.nextInt(ESTACIONES.size()));
                        String accion = (i % 2 == 0) ? "entrada" : "salida";
                        reloj = reloj.plusMinutes(3 + random.nextInt(18));
                        registrosUsuario.add(new Registro(idUsuario, ruta, estacion, accion, reloj));
                    }
                    return registrosUsuario.stream();
                })
                .collect(Collectors.toUnmodifiableList()); // lista inmutable
    }
}
