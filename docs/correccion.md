# Informe de corrección — Taller 3: registro sísmico

Fundamentos de Programación Funcional y Concurrente.

## 0. Notación y herramientas

Sea $r = [s_1, s_2, \ldots, s_n]$ un registro, con $s_i = (\text{fecha}_i, \text{region}_i, \text{mag}_i, \text{prof}_i)$.
Escribimos $\text{reg}(s)$ y $\text{mag}(s)$ para la región y la magnitud de un sismo $s$.
Para una lista $l$ y una región $k$ definimos:

```math
R(l) = \{\, \text{reg}(s) \mid s \in l \,\}
\qquad
S_k(l) = [\, s_i \mid s_i \in l,\ \text{reg}(s_i) = k \,]
```

$S_k(l)$ es la sublista de los sismos de la región $k$, **conservando el orden** original.

**Traducción de la expresión `for`.** El compilador traduce

```scala
for { x <- l; if p(x) } yield g(x)   ==>   l.withFilter(p).map(g)
```

### Lema 1 (filtrar y transformar)

Para toda lista $l = [x_1, \ldots, x_n]$, predicado $p$ y función $g$:

```math
\texttt{for}\{x \leftarrow l;\ \texttt{if}\ p(x)\}\ \texttt{yield}\ g(x) \;=\; [\, g(x_i) \mid 1 \le i \le n,\ p(x_i) \,]
```

**Demostración** (inducción estructural sobre $l$). Sea $Q(l)$ el lado izquierdo.

- **Caso base:** $l = \text{Nil}$. $Q(\text{Nil}) = \text{Nil}$, y el lado derecho también es la lista vacía.
- **Caso de inducción:** $l = x :: t$, con $Q(t) = [\, g(y) \mid y \in t,\ p(y) \,]$ (HI). Por la definición de `withFilter` y `map` sobre listas:

```math
Q(x :: t) =
\begin{cases}
g(x) :: Q(t) & \text{si } p(x) \\
Q(t) & \text{si } \neg p(x)
\end{cases}
```

Aplicando la HI, en ambos casos se obtiene $[\, g(y) \mid y \in x::t,\ p(y) \,]$, en el mismo orden. $\blacksquare$

## 1. Punto 1: consultas con expresiones `for`

### 1.1 `fuertes`

**Especificación:** $\text{fuertes}(r, u) = [\, \text{reg}(s) \mid s \in r,\ \text{mag}(s) > u \,]$, con repetidos y en orden.

El código es `for { s <- r; if s.magnitud > umbral } yield s.region`. Es el Lema 1 con $p(s) \equiv \text{mag}(s) > u$ y $g(s) = \text{reg}(s)$. Por lo tanto $P_{\text{fuertes}}(r, u) = \text{fuertes}(r, u)$. $\blacksquare$

### 1.2 `enRegion`

**Especificación:** $\text{enRegion}(r, k) = [\, \text{mag}(s) \mid s \in S_k(r) \,]$.

El código es `for { s <- r; if s.region == region } yield s.magnitud`. Es el Lema 1 con $p(s) \equiv \text{reg}(s) = k$ y $g(s) = \text{mag}(s)$. Si $k \notin R(r)$ ningún elemento cumple $p$ y el resultado es $[\,]$. $\blacksquare$

### 1.3 `paresCercanos`

Sea $R(r)$ las regiones distintas y $[g_1, \ldots, g_m]$ su orden de **primera aparición** en $r$. Sea $M_k = \text{enRegion}(r, k)$. Definimos:

```math
\text{cerc}(a, b) \iff \exists\, x \in M_a,\ \exists\, y \in M_b : |x - y| \le \text{dif}
```

**Especificación:**

```math
\text{paresCercanos}(r, \text{dif}) = [\, (g_i, g_j) \mid 1 \le i < j \le m,\ \text{cerc}(g_i, g_j) \,]
```

ordenada lexicográficamente por $(i, j)$.

**Argumento.**

1. `(for { s <- r } yield s.region).distinct` es $[g_1, \ldots, g_m]$. Por el Lema 1 el `for` produce las regiones de $r$ en orden, y `distinct` conserva la primera aparición de cada una.
2. `regiones.tails` produce las listas $T_j = [g_j, \ldots, g_m]$ para $j = 1, \ldots, m+1$, con $T_{m+1} = \text{Nil}$.
3. El patrón `a :: resto <- ...` descarta $T_{m+1}$ (no encaja con `::`). Para $T_j$ con $j \le m$ liga $a = g_j$ y $\text{resto} = [g_{j+1}, \ldots, g_m]$.
4. Para cada $T_j$, el generador `b <- resto` recorre $g_{j+1}, \ldots, g_m$ en orden, y el filtro deja los que cumplen $\text{cerc}(a, b)$.
5. La función `cercanas(a, b)` implementa $\text{cerc}(a, b)$ porque `exists` sobre una lista equivale a $\exists$ sobre sus elementos.

Los pares generados son exactamente los $(g_i, g_j)$ con $i < j$ y $\text{cerc}(g_i, g_j)$, y salen por $i$ creciente y luego por $j$ creciente.

**Sin repetidos ni simétricos.** Como los $g_i$ son distintos, $g_i \ne g_j$ siempre. El par $\{g_i, g_j\}$ solo se genera cuando $a = g_i$ y $b = g_j$ con $i < j$. Nunca se genera $(g_j, g_i)$, porque $g_i$ no está en el $\text{resto}$ de $T_j$. Además $\text{cerc}$ es simétrica, pues $|x - y| = |y - x|$, así que elegir esta orientación no pierde ningún par. $\blacksquare$

## 2. Punto 2: agrupar con mapas

### 2.1 `porRegion`

**Especificación:** $\text{porRegion}(r)$ es el mapa $M$ con $\text{dom}(M) = R(r)$ y $M(k) = S_k(r)$.

`r.groupBy(_.region)` procesa $r$ elemento por elemento, agregando cada sismo al final de la lista de su región. Lo formalizamos como un proceso iterativo:

- **Estado:** $s = (i, M)$, donde $i$ es la cantidad de sismos procesados y $M$ un mapa.
- **Estado inicial:** $s_0 = (0, \text{Map}())$.
- **Estado final:** $i = n$.
- **Transformación:** $\text{transformar}(i, M) = (i+1,\ M[\text{reg}(s_{i+1}) \mapsto M(\text{reg}(s_{i+1})) \mathbin{+\!\!+} [s_{i+1}]\,])$, tomando $M(k) = [\,]$ si $k \notin \text{dom}(M)$.
- **Invariante:**

```math
\text{Inv}(i, M) \equiv \text{dom}(M) = R([s_1..s_i]) \ \land\ \forall k \in \text{dom}(M) : M(k) = S_k([s_1..s_i])
```

**Demostración.**

1. $\text{Inv}(s_0)$: $\text{dom}(\text{Map}()) = \emptyset = R([\,])$ y no hay llaves que verificar.
2. Si $\text{Inv}(i, M)$ y $i < n$, sea $k = \text{reg}(s_{i+1})$. Después de transformar, la llave $k$ queda en el dominio y $M'(k) = S_k([s_1..s_i]) \mathbin{+\!\!+} [s_{i+1}] = S_k([s_1..s_{i+1}])$. Las demás llaves no cambian y siguen cumpliendo la invariante. Entonces $\text{Inv}(i+1, M')$.
3. En el estado final, $\text{Inv}(n, M)$ da $\text{dom}(M) = R(r)$ y $M(k) = S_k(r)$.
4. En cada paso $i$ aumenta en 1, así que tras $n$ pasos se llega al estado final.

Por lo tanto $P_{\text{porRegion}}(r) = \text{porRegion}(r)$. Si $r = [\,]$ el resultado es $\text{Map}()$, y las regiones que no están en $r$ no aparecen. $\blacksquare$

### 2.2 `conteoPorRegion`

**Especificación:** $\text{conteoPorRegion}(r) = \{\, k \mapsto |S_k(r)| \mid k \in R(r) \,\}$.

El código `for ((region, ss) <- porRegion(r)) yield region -> ss.size` se traduce a

```scala
porRegion(r).withFilter { case (region, ss) => true }.map { case (region, ss) => region -> ss.size }
```

Es decir, a un `map` sobre las entradas de $\text{porRegion}(r)$ que reemplaza cada lista $S_k(r)$ por su tamaño. Por 2.1, las llaves son $R(r)$ y la lista de la llave $k$ es $S_k(r)$, así que el valor es $|S_k(r)|$. $\blacksquare$

### 2.3 `magnitudMaxima`

**Especificación:** $\text{magnitudMaxima}(r) = \{\, k \mapsto \max\{\text{mag}(s) \mid s \in S_k(r)\} \mid k \in R(r) \,\}$.

Es el mismo `map` sobre las entradas de $\text{porRegion}(r)$, reemplazando $S_k(r)$ por `ss.map(_.magnitud).max`. Este `.max` está bien definido: si $k \in R(r)$, entonces $S_k(r) \neq [\,]$ (existe al menos un sismo de la región $k$), y el máximo de una lista no vacía existe. $\blacksquare$

## 3. Punto 3: consultas seguras

### 3.1 `conteoSeguro`

**Especificación:**

```math
\text{conteoSeguro}(r)(k) =
\begin{cases}
|S_k(r)| & \text{si } k \in R(r) \\
0 & \text{si } k \notin R(r)
\end{cases}
```

`withDefaultValue(0)` devuelve un mapa que se comporta igual que el original para las llaves presentes y devuelve $0$ para las ausentes. Por 2.2, el mapa original tiene $k \mapsto |S_k(r)|$ para $k \in R(r)$, así que se cumple la especificación. $\blacksquare$

### 3.2 `maximaDe`

**Especificación:**

```math
\text{maximaDe}(r, k) =
\begin{cases}
\text{Some}\left(\max\{\text{mag}(s) \mid s \in S_k(r)\}\right) & \text{si } S_k(r) \ne [\,] \\
\text{None} & \text{si } S_k(r) = [\,]
\end{cases}
```

Por el Lema 1, el `for` produce $L = [\, \text{mag}(s) \mid s \in S_k(r) \,]$. La función `maxOption` devuelve `None` si $L = [\,]$ y `Some(max L)` si no. Como $L = [\,]$ exactamente cuando $S_k(r) = [\,]$, se cumple la especificación. $\blacksquare$

### 3.3 `regionMasActiva`

Sea $c(k) = |S_k(r)|$. **Especificación:** si $r = [\,]$ el resultado es `None`. Si no, es `Some(k*)` donde $k^*$ es la región con mayor $c$ y, en empate, la menor alfabéticamente:

```math
k^* = \min_{\prec} R(r), \qquad a \prec b \iff c(a) > c(b) \ \lor\ (c(a) = c(b) \land a < b)
```

**Argumento.** Con $c$ = `conteoSeguro(r)`, el filtro del `for` deja una región $k$ si

```math
\text{Cand}(k) \equiv \forall (o, m) \in c : \ m < c(k)\ \lor\ (m = c(k) \land k \le o)
```

1. **$k^*$ cumple $\text{Cand}$.** Para toda otra región $o$, $k^* \prec o$, es decir $c(k^*) > c(o)$ o ($c(k^*) = c(o)$ y $k^* < o$). Para $o = k^*$ la condición se cumple por $k^* \le k^*$.
2. **Ninguna otra región cumple $\text{Cand}$.** Supongamos que $a \ne b$ cumplen $\text{Cand}$. Aplicando $\text{Cand}(a)$ con $o = b$ y $\text{Cand}(b)$ con $o = a$, las desigualdades estrictas de conteo son contradictorias, así que $c(a) = c(b)$. Entonces $a \le b$ y $b \le a$, es decir $a = b$, una contradicción.
3. **Existencia.** $\prec$ es un orden total sobre un conjunto finito y no vacío (si $r \neq [\,]$), así que $k^*$ existe.

El `for` produce entonces exactamente un elemento, $k^*$, y `headOption` lo devuelve como `Some(k*)`. Si $r = [\,]$ el mapa está vacío, el `for` no produce nada y el resultado es `None`. $\blacksquare$

## 4. Punto 4: réplicas perezosas

Sea $m > 0$ la magnitud inicial y $\rho(x) = \dfrac{\lfloor 100x + 0.5 \rfloor}{100}$ el redondeo a dos decimales. Definimos la sucesión ideal $a_0 = m,\ a_{n+1} = 0.8\, a_n$, es decir $a_n = m \cdot 0.8^n$.

### 4.1 `replicas`

**Especificación:** $\text{replicas}(m)$ es la sucesión infinita $[\rho(a_0), \rho(a_1), \rho(a_2), \ldots]$.

`LazyList.iterate(m)(x => x * 0.8)` produce $x_0 = m,\ x_{n+1} = x_n \cdot 0.8$. Por inducción sobre $n$, $x_n = a_n$ (con la salvedad habitual de la aritmética de punto flotante, sobre la que se razona abajo):

- **Caso base:** $x_0 = m = a_0$.
- **Caso de inducción:** $x_{n+1} = x_n \cdot 0.8 \overset{HI}{=} a_n \cdot 0.8 = a_{n+1}$.

Luego `.map(redondear)` aplica $\rho$ a cada elemento, y el elemento $n$ queda como $\rho(a_n)$. Se redondea *después* de iterar, así que el error de redondeo no se acumula de un paso al siguiente. $\blacksquare$

**Salvedad (punto flotante).** Los `Double` acumulan un error relativo del orden de $10^{-16}$ por paso. El resultado difiere de $\rho(a_n)$ solo si $a_n$ está a ese orden de distancia de un punto medio $\frac{2j+1}{200}$. Con los datos del taller no ocurre.

**Pereza.** `LazyList.iterate` y `map` sobre un `LazyList` no calculan el elemento $n$ hasta que alguien lo pide. Al llamar `replicas(m).take(4).toList` solo se evalúan los elementos $0, 1, 2, 3$. Los demás nunca se materializan, y por eso la memoria usada es proporcional a lo pedido y no al tamaño de la secuencia infinita.

### 4.2 `replicasSensibles`

**Especificación:** $\text{replicasSensibles}(m, \min) = [\, \rho(a_i) \mid 0 \le i < N \,]$, con $N = \min\{\, i \mid \rho(a_i) \le \min \,\}$, es decir, el prefijo de réplicas mayores que el mínimo. Supone $m > 0$ y $\min \ge 0$.

**Argumento.**

1. **Monotonía.** Como $a_{n+1} = 0.8\, a_n < a_n$ para $a_n > 0$, la sucesión $a_n$ es estrictamente decreciente. Como $\rho$ es monótona no decreciente, $\rho(a_n)$ es no creciente. Por tanto, el conjunto $\{ n \mid \rho(a_n) > \min \}$ es un **prefijo**: si $\rho(a_n) \le \min$, todos los siguientes también.
2. **`takeWhile` coincide con ese prefijo.** `takeWhile(_ > minimo)` toma elementos mientras cumplan la condición y se detiene en el primero que no la cumple. Por el punto 1, ese primer fallo es el índice $N$ y todo lo que sigue tampoco la cumpliría. Así que `takeWhile` devuelve exactamente $[\rho(a_0), \ldots, \rho(a_{N-1})]$.
3. **Terminación.** Como $a_n \to 0$, existe $n_0$ con $a_{n_0} < 0.005$, y entonces $\rho(a_{n_0}) = 0 \le \min$. Luego $N \le n_0$ y `takeWhile` evalúa a lo sumo $N + 1$ elementos de la secuencia, así que `toList` termina. (Si $\min < 0$ esta prueba no aplica y la función no terminaría; está fuera de la especificación.)
4. **Caso vacío.** Si $\rho(a_0) \le \min$, entonces $N = 0$ y el resultado es $[\,]$.

$\blacksquare$

## 5. Punto 5: informe combinado

**Especificación:** para cada $k \in R(r)$,

```math
\text{informe}(r, \min)(k) = \text{Resumen}\big(\, |S_k(r)|,\ \mu_k,\ |\text{replicasSensibles}(\mu_k, \min)| \,\big),
\qquad \mu_k = \max\{\text{mag}(s) \mid s \in S_k(r)\}
```

**Argumento.**

1. `conteos` y `maximas` se construyen sobre $\text{porRegion}(r)$, así que ambos tienen las mismas llaves, $R(r)$. Por lo tanto `maximas(region)` está definido para toda llave de `conteos`.
2. Por 2.2, `conteos(k)` $= |S_k(r)|$. Por 2.3, `maximas(k)` $= \mu_k$.
3. Por 4.2, `replicasSensibles(maximas(k), minimo).length` es $|\text{replicasSensibles}(\mu_k, \min)|$.
4. El `for` sobre `conteos` con `yield region -> Resumen(...)` es un `map` sobre las entradas, que construye una entrada por cada $k \in R(r)$. Las llaves son las mismas y cada valor es el pedido.
5. Si $r = [\,]$, $R(r) = \emptyset$ y el resultado es `Map()`.

$\blacksquare$

## 6. Independencia del orden de recorrido

Los resultados de las funciones de agrupación no dependen del orden en que se recorran el registro ni los mapas, por las siguientes razones:

- **`conteoPorRegion`:** el valor $|S_k(r)|$ es una cardinalidad, que no depende del orden de los elementos.
- **`magnitudMaxima`:** $\max$ es conmutativa y asociativa, así que $\max\{x_1, \ldots, x_n\}$ es el mismo para cualquier permutación de los $x_i$.
- **`porRegion`:** como mapa, su igualdad no depende del orden de las entradas: dos mapas son iguales si tienen las mismas parejas llave-valor. Dentro de cada grupo el orden sí importa, y `groupBy` lo fija de forma determinista como el orden del registro (invariante de 2.1).
- **`regionMasActiva`:** `forall` es una conjunción, y la conjunción es conmutativa. Además, por el punto 2 del argumento 3.3, a lo sumo una región cumple $\text{Cand}$. Por eso el resultado no depende del orden en que el `for` recorra las entradas del mapa. Esto importa porque el orden de iteración de un `Map` no está especificado.
- **`informe`:** cada entrada se calcula solo a partir de los datos de su propia región, así que el orden en que se procesen las regiones no cambia el resultado.
- **`paresCercanos`:** el orden de salida es una elección de especificación (orden de primera aparición), no un efecto del recorrido. Y como $\text{cerc}$ es simétrica, el conjunto de pares no cambia si se invierte el orden de los elementos dentro de cada pareja.
- **`replicas` / `replicasSensibles`:** la sucesión está definida por $a_{n+1} = 0.8\, a_n$, que fija el orden de los elementos. No hay recorrido libre.