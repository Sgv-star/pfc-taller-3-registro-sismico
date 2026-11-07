# Taller 3 — Consultas sobre un registro sísmico

Fundamentos de Programación Funcional y Concurrente
Escuela de Ingeniería de Sistemas y Computación, Universidad del Valle
Carlos Andrés Delgado Saavedra

El enunciado completo, con los ejemplos de cada punto, la rúbrica y la
ecuación de calificación, es el PDF publicado en el campus virtual. Este
archivo dice cómo está armado el proyecto y cómo se entrega.

## Integrantes

Llene esta tabla con el nombre completo y el código de cada integrante. Es
parte de la entrega: si falta alguno, la entrega se sanciona con el 20 % de
la nota.

| Nombre completo | Código |
|---|---|
|Omar Denilson Riascos Córdoba |2559990 |
|Juan Manuel Vásquez Segura |2559961 |
|Santiago Gonzalez Villada |2559994 |
|Manuel Alejandro Ortiz Zabala |2559767 |

## Cómo está organizado el proyecto

```
app/src/main/scala/taller/
    Modelo.scala                los tipos del taller, que no se modifican
    RegistroSismico.scala       aquí van los cinco puntos
    App.scala                   programa de arranque

app/src/test/scala/taller/
    RegistroSismicoTest.scala   las 37 pruebas dadas, que no se modifican

docs/                           los informes, en Markdown
```

Su código va en `RegistroSismico.scala`. Las 37 pruebas que vienen escritas
no se modifican: son el contrato con que se califica.

La rúbrica pide además **cinco casos de prueba propios por cada punto**,
distintos de los ejemplos del enunciado y de los que trae el material. Esos
van donde le resulte más cómodo: al final de `RegistroSismicoTest.scala`,
debajo de las que ya están, o en un archivo nuevo dentro de
`app/src/test/scala/taller/`. Las dos formas valen; lo que no se toca es lo
que ya estaba escrito.

Los informes de proceso y de corrección que pide el enunciado van en `docs/`,
en Markdown, con la notación matemática en LaTeX y los diagramas en
`mermaid`; no se aceptan imágenes insertadas ni archivos por fuera de esa
carpeta.

## Cómo se ejecuta

```bash
./gradlew test    # revisa las reglas del curso y corre las pruebas
./gradlew run     # corre el programa
```

La primera vez se demora: Gradle descarga el compilador de Scala y la
versión de Java que necesita. No hay que instalar nada a mano. Al terminar,
`./gradlew test` deja un informe navegable en
`app/build/reports/tests/test/index.html`.

## El punto de partida

Al clonar, las 37 pruebas están en rojo, porque las doce funciones dicen
`???`. Su trabajo es reemplazar cada `???` y ver las pruebas ponerse en
verde.

`Modelo.scala` trae los tipos ya escritos: `Fecha`, `Sismo`, `Resumen` y el
objeto `Datos`, con el registro de siete sismos que usan los ejemplos. No se
modifican.

## Los cinco puntos

| Punto | Funciones |
|---|---|
| 1 | `fuertes`, `enRegion` y `paresCercanos` |
| 2 | `porRegion`, `conteoPorRegion` y `magnitudMaxima` |
| 3 | `conteoSeguro`, `maximaDe` y `regionMasActiva` |
| 4 | `replicas` y `replicasSensibles` |
| 5 | `informe` |

El punto 1 se resuelve con expresiones `for`, no con `filter` y `map`
encadenados a mano. El 2 agrupa con mapas. El 3 consulta sin que el mapa
lance excepción, y devuelve el valor vacío cuando no hay respuesta. El 4
construye una secuencia infinita que no se materializa: `take(4)` sobre ella
tiene que devolver cuatro elementos sin colgarse. El 5 combina los
anteriores.

## Reglas del código

En este curso no se usa `var`, ni `while`, ni `return`, ni ningún estado que
cambie. Todo se resuelve con `val`, recursión y llamados a funciones. Si
necesita una secuencia de instrucciones, use un bloque `{ ... }`; si necesita
funciones auxiliares, escríbalas dentro de las funciones.

La expresión `for` con `yield`, que es la del punto 1, sí se usa: produce una
colección nueva y el compilador la traduce a `map`, `flatMap` y `filter`. Lo
que no entra es el `for` que no devuelve valor.

`./gradlew test` revisa estas reglas **antes** de correr las pruebas. Un
programa con `var`, `while` o `return` no llega a las pruebas: el flujo se
detiene y dice en qué línea está el problema.

No se editan los archivos de configuración del proyecto ni los flujos de
GitHub.

## Cómo se entrega

1. Presione **Fork** en este repositorio. Un solo integrante hace el fork y
   agrega a los demás como colaboradores; todo el equipo trabaja sobre ese
   mismo fork.
2. En su fork, entre a la pestaña **Actions** y habilítelas. GitHub las deja
   apagadas en los forks hasta que el dueño lo autoriza, y sin eso no ve el
   resultado de las pruebas al hacer push.
3. Clone su fork, resuelva, y haga commit y push a `main`. Cada integrante
   hace sus propios commits: la rúbrica de aporte al código se calcula con
   ellos.
4. Registre en la tarea **Entrega del taller 3** del campus virtual la
   dirección de su fork y el hash del último commit, que se obtiene con
   `git rev-parse HEAD`.

**La fecha de entrega es el jueves 12 de noviembre de 2026, a las 23:59.**

El hash se saca **después** del último merge a `main`. Si se toma antes, el
commit que queda registrado no tiene el trabajo que entró en ese merge, y es
ese commit el que se califica.

Las condiciones son estas, y son las del acta de inicio del curso:

- **La entrega es el enlace registrado en el campus dentro del plazo.** No se
  aceptan entregas por correo ni por ningún otro medio. Si el enlace no llega
  a tiempo, la entrega se califica con **0.0**.
- **El repositorio tiene que ser un fork de este.** Un repositorio creado
  aparte, aunque tenga el mismo código, se sanciona con el **30 %** de la
  nota: rompe la estructura con que se califica y la verificación automática.
- **Se califica el último commit anterior a la fecha de entrega.** Lo que se
  suba después no se revisa. El hash que se registra en el campus debe ser el
  de ese commit.
- **El `README.md` lleva el nombre y el código de todos los integrantes.**
  Si falta alguno, la entrega se sanciona con el **20 %** de la nota.
- Se puede trabajar en grupos de hasta cuatro personas.
- El docente puede pedir sustentación del taller. En ese caso la nota de la
  sustentación es individual, entre 0 y 1, y multiplica la nota del taller.
