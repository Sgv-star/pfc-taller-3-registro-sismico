package taller

/**
 * Taller 3 — consultas sobre un registro sísmico con expresiones for, mapas y
 * secuencias perezosas.
 *
 * Complete cada función. Las firmas y los tipos no se modifican.
 */
class RegistroSismico {

  type Registro = List[Sismo]

  // Punto 1: consultas con expresiones for --------------------------------------

  /** Las regiones de los sismos que superan el umbral, en orden y con repetidos. */
  def fuertes(r: Registro, umbral: Double): List[String] =
    for {
      s <- r
      if s.magnitud > umbral
    } yield s.region

  /** Las magnitudes registradas en una región. */
  def enRegion(r: Registro, region: String): List[Double] =
    for {
      s <- r
      if s.region == region
    } yield s.magnitud

  /** Parejas de regiones distintas con algún sismo de magnitudes cercanas. */
  def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
    val regiones: List[String] =
      (for { s <- r } yield s.region).distinct

    def cercanas(a: String, b: String): Boolean =
      enRegion(r, a).exists(ma => enRegion(r, b).exists(mb => Math.abs(ma - mb) <= dif))

    (for {
      a :: resto <- regiones.tails
      b <- resto
      if cercanas(a, b)
    } yield (a, b)).toList
  }

  // Punto 2: agrupar con mapas --------------------------------------------------

  def porRegion(r: Registro): Map[String, List[Sismo]] = {
    (for {
      r1 <- r
    }yield r1.region -> (for{
      r2 <- r
      if r1.region == r2.region
    }yield r2)).toMap
  }

  def conteoPorRegion(r: Registro): Map[String, Int] =
    (for {
      s1 <- r
    } yield s1.region -> (for {
      s2 <- r
      if s2.region == s1.region
    } yield s2).size).toMap

  def magnitudMaxima(r: Registro): Map[String, Double] =
    (for {
      s1 <- r
    } yield s1.region -> (for {
      s2 <- r
      if s2.region == s1.region
    } yield s2.magnitud).max).toMap

  // Punto 3: consultas seguras --------------------------------------------------

  /** Un mapa que responde cero por las regiones que no están en el registro. */
  def conteoSeguro(r: Registro): Map[String, Int] = {
    conteoPorRegion(r).withDefaultValue(0)
  }


  /** La magnitud máxima de una región, o vacío si no hay registros suyos. */
  def maximaDe(r: Registro, region: String): Option[Double] = {
    (for {
      s <- r
      if s.region == region
    } yield s.magnitud).maxOption
  }

  /** La región con más sismos; en empate, la primera en orden alfabético. */
  def regionMasActiva(r: Registro): Option[String] = {
    val regiones = conteoSeguro(r)
    (for {
      (region, n) <- regiones
      if regiones.forall { case (otra, m) => m < n || (m == n && region <= otra)
      }
    } yield region).headOption
  }


  // Punto 4: réplicas perezosas -------------------------------------------------

  def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

  /** La secuencia infinita que empieza en la magnitud y multiplica por 0.8. */
  def replicas(magnitud: Double): LazyList[Double] = {
    def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0
    LazyList.iterate(magnitud)(x => x * 0.8).map(redondear)
  }

  /** Las réplicas mientras superen el mínimo, incluida la primera. */
  def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
    replicas(magnitud).takeWhile(_ > minimo).toList

  // Punto 5: informe combinado --------------------------------------------------

  /** Conteo, magnitud máxima y réplicas esperadas del sismo más fuerte, por región. */
  def informe(r: Registro, minimo: Double): Map[String, Resumen] = ???
}
