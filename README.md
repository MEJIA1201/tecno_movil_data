# TecnoMovil Data

Módulo funcional en Java para el procesamiento y agregación de los datos diarios del sistema de transporte urbano de **TecnoValle**.

Caso de estudio de la **Unidad 2** — Tecnología en Desarrollo de Software, Institución Universitaria Digital de Antioquia.

## Contexto

El sistema actual de transporte de TecnoValle (más de 450.000 pasajeros al día y entre 6 y 12 millones de registros diarios) está construido con un enfoque imperativo y acoplado, lo que provoca procesamiento lento, errores de conteo por condiciones de carrera y código difícil de mantener.

Este proyecto rediseña el módulo de procesamiento de datos usando **programación funcional**: Streams, lambdas, funciones puras y colecciones inmutables.

## Estructura del proyecto

```
tarea/
├── Main.java                      # Punto de entrada: genera los datos y muestra los resultados
├── models/
│   └── Registro.java              # Clase inmutable que representa un registro de transporte
└── service/
    ├── GeneradorDatos.java        # Generación de datos simulados
    └── TransporteService.java     # Lógica de análisis (tareas a–f)
```

### `models/Registro`

Cada registro contiene:

| Campo       | Tipo            | Descripción                              |
|-------------|-----------------|------------------------------------------|
| `idUsuario` | `int`           | Identificador del usuario                |
| `ruta`      | `String`        | Ruta utilizada (R1 … R6)                 |
| `estacion`  | `String`        | Estación donde se registra el evento     |
| `accion`    | `String`        | `"entrada"` o `"salida"`                 |
| `timestamp` | `LocalDateTime` | Fecha y hora del registro                |

Todos los atributos son `final` y no hay setters, por lo que los objetos son inmutables.

### `service/GeneradorDatos`

Genera una lista **inmutable** de registros simulados. Cada usuario produce 10 registros alternando entrada/salida, con intervalos de 3 a 20 minutos, entre las 5:00 y las 20:00. Usa una semilla fija (`42`) para que los resultados sean reproducibles.

### `service/TransporteService`

Implementa las tareas del caso de estudio como **funciones puras**:

| Tarea | Método(s) | Descripción |
|-------|-----------|-------------|
| a) Afluencia por estación | `afluenciaPorEstacion`, `afluenciaPorEstacionParalelo` | Cuenta las entradas por estación (versión secuencial y con `parallelStream()`) |
| b) Horas pico | `flujoPorHora`, `horasPico` | Agrupa los registros por hora y devuelve las N horas con mayor flujo |
| c) Rutas más utilizadas | `usoPorRuta`, `rutasMasUsadas` | Cuenta los registros por ruta y las ordena de mayor a menor uso |
| d) Patrones de viaje | `patronesPorUsuario` | Lista, por usuario, las estaciones visitadas en orden de aparición |
| e) Tiempo entre estaciones | `tiempoPromedioEntreEstaciones` | Promedio en minutos entre estaciones consecutivas (`"A -> B"`) |
| f) Sobrecarga en rutas | `estadoPorRuta` | Marca como `CRITICA` las rutas cuyas entradas superan un umbral |

## Principios funcionales aplicados

- **Funciones puras:** cada método recibe datos y devuelve un resultado nuevo, sin modificar variables externas.
- **Inmutabilidad:** la lista original nunca se modifica; se usan `Collectors.toUnmodifiableList()` y `toUnmodifiableMap()`.
- **Enfoque declarativo:** todo el procesamiento se expresa con pipelines de Streams (`filter`, `map`, `groupingBy`, `sorted`, etc.).
- **Paralelización:** al no haber efectos secundarios, los pipelines pueden ejecutarse con `parallelStream()`; `Main` comprueba que el resultado secuencial y el paralelo sean iguales.


## Salida esperada

El programa simula 2.000 usuarios (20.000 registros) y muestra:

1. Número total de registros y un ejemplo.
2. **a)** Afluencia por estación y verificación secuencial vs. paralelo.
3. **b)** Top 3 de horas pico.
4. **c)** Rutas ordenadas por uso.
5. **d)** Patrón de viaje de los usuarios 1 a 3.
6. **e)** Tiempo promedio entre algunas parejas de estaciones.
7. **f)** Estado de cada ruta (`normal` o `CRITICA`) según el umbral de ocupación (2.000 entradas).

