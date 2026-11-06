import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Caso de estudio - Unidad 2
 * Proyecto TecnoMovil Data (TecnoValle)
 *
 * Modulo funcional para procesar los registros diarios del transporte urbano
 * usando Streams, lambdas, funciones puras y colecciones inmutables.
 */
public class TecnoMovilData {

    // ------------------------------------------------------------------
    // 3.1 Estructura de datos a procesar
    // ------------------------------------------------------------------
    /**
     * Clase Registro: representa un evento de entrada o salida en el sistema de transporte.
     * La clase es inmutable: todos los atributos son final y no hay setters.
     * Esto garantiza seguridad en entornos concurrentes y facilita el uso con streams.
     */
    static final class Registro {
        private final int idUsuario;
        private final String ruta;
        private final String estacion;
        private final String accion; // "entrada" o "salida"
        private final LocalDateTime timestamp;

        Registro(int idUsuario, String ruta, String estacion,
                 String accion, LocalDateTime timestamp) {
            this.idUsuario = idUsuario;
            this.ruta = ruta;
            this.estacion = estacion;
            this.accion = accion;
            this.timestamp = timestamp;
        }

        int getIdUsuario() { return idUsuario; }
        String getRuta() { return ruta; }
        String getEstacion() { return estacion; }
        String getAccion() { return accion; }
        LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return idUsuario + " | " + ruta + " | " + estacion + " | "
                    + accion + " | " + timestamp;
        }
    }

    // Datos de apoyo para la simulacion
    private static final List<String> RUTAS =
            List.of("R1", "R2", "R3", "R4", "R5", "R6");
    private static final List<String> ESTACIONES =
            List.of("Central", "Norte", "Sur", "Oriente", "Occidente",
                    "Universidad", "Hospital", "Terminal");

    // Umbral simulado: si una ruta tiene mas entradas que esto, es "critica"
    private static final long UMBRAL_OCUPACION = 2000;

    // ------------------------------------------------------------------
    // Generacion de datos simulados
    // ------------------------------------------------------------------
    // Cada usuario hace 10 registros (entrada, salida, entrada, ...)
    // avanzando el reloj entre 3 y 20 minutos en cada uno.
    static List<Registro> generarDatos(int cantidadUsuarios) {
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

    // ------------------------------------------------------------------
    // 3.2 Tareas (todas son funciones puras: no modifican la lista original
    //     ni ninguna variable externa, solo reciben datos y devuelven un resultado)
    // ------------------------------------------------------------------

    // a) Afluencia por estacion: cuantos usuarios ingresan a cada estacion
    static Map<String, Long> afluenciaPorEstacion(List<Registro> registros) {
        return registros.stream()
                .filter(r -> r.getAccion().equals("entrada"))
                .collect(Collectors.groupingBy(Registro::getEstacion, Collectors.counting()));
    }

    // Misma tarea a) pero con parallelStream() para comprobar que se puede paralelizar
    static Map<String, Long> afluenciaPorEstacionParalelo(List<Registro> registros) {
        return registros.parallelStream()
                .filter(r -> r.getAccion().equals("entrada"))
                .collect(Collectors.groupingBy(Registro::getEstacion, Collectors.counting()));
    }

    // b) Horas pico: agrupar por hora del dia y contar el flujo
    static Map<Integer, Long> flujoPorHora(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(r -> r.getTimestamp().getHour(),
                        Collectors.counting()));
    }

    // Toma el mapa de flujo por hora y devuelve las N horas con mas flujo
    static List<Map.Entry<Integer, Long>> horasPico(Map<Integer, Long> flujo, int n) {
        return flujo.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toUnmodifiableList());
    }

    // c) Rutas mas utilizadas: contar registros por ruta
    static Map<String, Long> usoPorRuta(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(Registro::getRuta, Collectors.counting()));
    }

    // Ordena las rutas de mayor a menor uso
    static List<Map.Entry<String, Long>> rutasMasUsadas(Map<String, Long> uso) {
        return uso.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    // d) Patrones de viaje: para cada usuario, lista de estaciones en orden de aparicion
    static Map<Integer, List<String>> patronesPorUsuario(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(Registro::getIdUsuario,
                        Collectors.mapping(Registro::getEstacion,
                                Collectors.toUnmodifiableList())));
    }

    // e) Tiempo promedio (en minutos) entre estaciones consecutivas
    // Para cada usuario se ordenan sus registros por hora y se toman parejas
    // seguidas (registro i y registro i+1). La clave es "EstacionA -> EstacionB".
    static Map<String, Double> tiempoPromedioEntreEstaciones(List<Registro> registros) {
        Map<Integer, List<Registro>> porUsuario = registros.stream()
                .collect(Collectors.groupingBy(Registro::getIdUsuario));

        return porUsuario.values().stream()
                .flatMap(lista -> {
                    // sorted() sobre un stream NO modifica la lista original
                    List<Registro> ordenada = lista.stream()
                            .sorted(Comparator.comparing(Registro::getTimestamp))
                            .collect(Collectors.toUnmodifiableList());
                    // parejas de registros consecutivos
                    return IntStream.range(1, ordenada.size())
                            .mapToObj(i -> new Registro[]{ordenada.get(i - 1), ordenada.get(i)});
                })
                .filter(par -> !par[0].getEstacion().equals(par[1].getEstacion()))
                .collect(Collectors.groupingBy(
                        par -> par[0].getEstacion() + " -> " + par[1].getEstacion(),
                        Collectors.averagingDouble(par ->
                                Duration.between(par[0].getTimestamp(),
                                        par[1].getTimestamp()).toMinutes())));
    }

    // f) Sobrecarga: rutas cuyo numero de entradas supera el umbral -> "critica"
    static Map<String, String> estadoPorRuta(List<Registro> registros, long umbral) {
        return registros.stream()
                .filter(r -> r.getAccion().equals("entrada"))
                .collect(Collectors.groupingBy(Registro::getRuta, Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> e.getValue() > umbral
                                ? "CRITICA (" + e.getValue() + " entradas)"
                                : "normal (" + e.getValue() + " entradas)"));
    }

    // ------------------------------------------------------------------
    // Programa principal: genera datos y muestra los resultros
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("    SISTEMA DE ANALISIS - TECNO MOVIL DATA");
        System.out.println("=".repeat(70));
        
        List<Registro> registros = generarDatos(2000); // 20.000 registros simulados
        System.out.println("\nRegistros simulados: " + registros.size());
        System.out.println("Ejemplo de registro: " + registros.get(0));

        // a)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("--- a) AFLUENCIA POR ESTACION (entradas) ---");
        System.out.println("=".repeat(70));
        afluenciaPorEstacion(registros).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %-20s: %,d usuarios%n", e.getKey(), e.getValue()));

        // Comprobacion de que el resultado paralelo es igual al secuencial
        boolean iguales = afluenciaPorEstacion(registros)
                .equals(afluenciaPorEstacionParalelo(registros));
        System.out.println("\n  ✓ Verificacion: Secuencial == Paralelo ? " + iguales);

        // b)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("--- b) HORAS PICO (top 3) ---");
        System.out.println("=".repeat(70));
        horasPico(flujoPorHora(registros), 3)
                .forEach(e -> System.out.printf("  %02d:00 horas -> %,d registros%n", e.getKey(), e.getValue()));

        // c)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("--- c) RUTAS MAS UTILIZADAS ---");
        System.out.println("=".repeat(70));
        rutasMasUsadas(usoPorRuta(registros))
                .forEach(e -> System.out.printf("  Ruta %-5s: %,d registros%n", e.getKey(), e.getValue()));

        // d)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("--- d) PATRON DE VIAJE (usuarios 1 a 3) ---");
        System.out.println("=".repeat(70));
        Map<Integer, List<String>> patrones = patronesPorUsuario(registros);
        Stream.of(1, 2, 3)
                .forEach(id -> System.out.println("  Usuario " + id + ": " + patrones.get(id)));

        // e)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("--- e) TIEMPO PROMEDIO ENTRE ESTACIONES (primeros 5 trayectos) ---");
        System.out.println("=".repeat(70));
        tiempoPromedioEntreEstaciones(registros).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(5)
                .forEach(e -> System.out.printf("  %-35s: %6.1f min%n", e.getKey(), e.getValue()));

        // f)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("--- f) ESTADO DE LAS RUTAS (umbral = " + UMBRAL_OCUPACION + " entradas) ---");
        System.out.println("=".repeat(70));
        estadoPorRuta(registros, UMBRAL_OCUPACION).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("    FIN DEL ANALISIS");
        System.out.println("=".repeat(70));
    }
}
