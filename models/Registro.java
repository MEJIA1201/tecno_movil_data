package models;

import java.time.LocalDateTime;

/**
 * 3.1 Estructura de datos a procesar.
 * La clase es inmutable: todos los atributos son final y no hay setters.
 */
public final class Registro {
    private final int idUsuario;
    private final String ruta;
    private final String estacion;
    private final String accion; // "entrada" o "salida"
    private final LocalDateTime timestamp;

    public Registro(int idUsuario, String ruta, String estacion,
                    String accion, LocalDateTime timestamp) {
        this.idUsuario = idUsuario;
        this.ruta = ruta;
        this.estacion = estacion;
        this.accion = accion;
        this.timestamp = timestamp;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getRuta() { return ruta; }
    public String getEstacion() { return estacion; }
    public String getAccion() { return accion; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return idUsuario + " | " + ruta + " | " + estacion + " | "
                + accion + " | " + timestamp;
    }
}
