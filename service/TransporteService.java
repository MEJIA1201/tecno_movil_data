package service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import models.Registro;

/**
 * 3.2 Tareas del caso de estudio.
 * Todas son funciones puras: no modifican la lista original ni ninguna
 * variable externa, solo reciben datos y devuelven un resultado.
 */
public class TransporteService {

    // a) Afluencia por estacion: cuantos usuarios ingresan a cada estacion
    public static Map<String, Long> afluenciaPorEstacion(List<Registro> registros) {
        return registros.stream()
                .filter(r -> r.getAccion().equals("entrada"))
                .collect(Collectors.groupingBy(Registro::getEstacion, Collectors.counting()));
    }

    // Misma tarea a) pero con parallelStream() para comprobar que se puede paralelizar
    public static Map<String, Long> afluenciaPorEstacionParalelo(List<Registro> registros) {
        return registros.parallelStream()
                .filter(r -> r.getAccion().equals("entrada"))
                .collect(Collectors.groupingBy(Registro::getEstacion, Collectors.counting()));
    }

    // b) Horas pico: agrupar por hora del dia y contar el flujo
    public static Map<Integer, Long> flujoPorHora(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(r -> r.getTimestamp().getHour(),
                        Collectors.counting()));
    }

    // Toma el mapa de flujo por hora y devuelve las N horas con mas flujo
    public static List<Map.Entry<Integer, Long>> horasPico(Map<Integer, Long> flujo, int n) {
        return flujo.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toUnmodifiableList());
    }

    // c) Rutas mas utilizadas: contar registros por ruta
    public static Map<String, Long> usoPorRuta(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(Registro::getRuta, Collectors.counting()));
    }

    // Ordena las rutas de mayor a menor uso
    public static List<Map.Entry<String, Long>> rutasMasUsadas(Map<String, Long> uso) {
        return uso.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    // d) Patrones de viaje: para cada usuario, lista de estaciones en orden de aparicion
    public static Map<Integer, List<String>> patronesPorUsuario(List<Registro> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(Registro::getIdUsuario,
                        Collectors.mapping(Registro::getEstacion,
                                Collectors.toUnmodifiableList())));
    }

    // e) Tiempo promedio (en minutos) entre estaciones consecutivas
    // Para cada usuario se ordenan sus registros por hora y se toman parejas
    // seguidas (registro i y registro i+1). La clave es "EstacionA -> EstacionB".
    public static Map<String, Double> tiempoPromedioEntreEstaciones(List<Registro> registros) {
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
    public static Map<String, String> estadoPorRuta(List<Registro> registros, long umbral) {
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
}
