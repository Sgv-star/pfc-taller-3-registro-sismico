package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Datos.registro

@RunWith(classOf[JUnitRunner])
class RegistroSismicoTest extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: los que superan 5.0, en orden") {
    assert(fuertes(registro, 5.0) == List("Santander", "Choco", "Valle"))
  }

  test("fuertes: con un umbral alto no queda ninguno") {
    assert(fuertes(registro, 9.0) == List())
  }

  test("fuertes: conserva los repetidos") {
    assert(fuertes(registro, 3.0).length == 7)
  }

  test("fuertes: el umbral es estricto") {
    // Hay un sismo de magnitud 6.0 exacta; con umbral 6.0 no debe salir.
    assert(!fuertes(registro, 6.0).contains("Choco"))
  }

  test("enRegion: las magnitudes del Valle") {
    assert(enRegion(registro, "Valle") == List(4.2, 3.8, 5.3))
  }

  test("enRegion: una región que no está da la lista vacía") {
    assert(enRegion(registro, "Amazonas") == List())
  }

  test("enRegion: sobre el registro vacío no hay magnitudes") {
    assert(enRegion(vacio, "Valle") == List())
  }

  test("paresCercanos: con 0.6 solo Valle y Santander") {
    assert(paresCercanos(registro, 0.6) == List(("Valle", "Santander")))
  }

  test("paresCercanos: con 1.0 salen las tres parejas, en orden de aparición") {
    assert(paresCercanos(registro, 1.0) ==
      List(("Valle", "Santander"), ("Valle", "Choco"), ("Santander", "Choco")))
  }

  test("paresCercanos: con 0.1 no hay ninguna") {
    assert(paresCercanos(registro, 0.1) == List())
  }

  test("paresCercanos: cada pareja sale una sola vez") {
    val ps = paresCercanos(registro, 1.0)
    assert(ps.map { case (a, b) => Set(a, b) }.distinct.length == ps.length)
  }

  test("paresCercanos: nunca empareja una región consigo misma") {
    assert(paresCercanos(registro, 5.0).forall { case (a, b) => a != b })
  }
  test("fuertes: los que superan 4.0, en orden y con repetidos") {
    assert(fuertes(registro, 4.0) ==
      List("Valle", "Santander", "Choco", "Santander", "Valle"))
  }

  test("fuertes: sobre el registro vacío no hay ninguno") {
    assert(fuertes(vacio, 3.0) == List())
  }

  test("enRegion: las magnitudes de Santander") {
    assert(enRegion(registro, "Santander") == List(5.1, 4.7))
  }

  test("paresCercanos: con 0.8 salen Valle-Santander y Valle-Choco") {
    assert(paresCercanos(registro, 0.8) ==
      List(("Valle", "Santander"), ("Valle", "Choco")))
  }

  test("paresCercanos: sobre el registro vacío no hay parejas") {
    assert(paresCercanos(vacio, 1.0) == List())
  }

  test("enRegion: una región en minúsculas no coincide") {
    assert(enRegion(registro, "choco") == List())
  }

  test("enRegion: dos nombres que comparten una palabra no se confunden") {
    val parecidas = List(
      Sismo(Fecha(2026, 1), "Cauca", 4.0, 20),
      Sismo(Fecha(2026, 2), "Valle del Cauca", 5.0, 30))
    assert(enRegion(parecidas, "Cauca") == List(4.0))
    assert(enRegion(parecidas, "Valle del Cauca") == List(5.0))
  }

  test("fuertes: con un umbral negativo pasan todos, con repetidos") {
    assert(fuertes(registro, -1.0).length == 7)
  }

  test("fuertes: con 5.3 no sale el sismo de magnitud exacta") {
    assert(!fuertes(registro, 5.3).contains("Valle"))
  }

  test("paresCercanos: con dif 0.0 solo empareja magnitudes idénticas") {
    val iguales = List(
      Sismo(Fecha(2026, 1), "Huila", 4.5, 20),
      Sismo(Fecha(2026, 1), "Tolima", 4.5, 30),
      Sismo(Fecha(2026, 2), "Cauca", 4.6, 40))
    assert(paresCercanos(iguales, 0.0) == List(("Huila", "Tolima")))
  }

  test("paresCercanos: con una región sola no hay parejas") {
    val soloUna = List(
      Sismo(Fecha(2026, 1), "Cauca", 4.0, 20),
      Sismo(Fecha(2026, 2), "Cauca", 5.0, 30))
    assert(paresCercanos(soloUna, 5.0) == List())
  }

  test("enRegion: no se lleva las magnitudes de otras regiones") {
    assert(enRegion(registro, "Choco") == List(6.0, 3.1))
  }

  test("fuertes: un sismo justo por encima del umbral sí sale") {
    assert(fuertes(List(Sismo(Fecha(2026, 1), "Tolima", 4.01, 12)), 4.0) == List("Tolima"))
  }

  test("enRegion: dos regiones en el mismo registro no se mezclan") {
    val dos = List(
      Sismo(Fecha(2026, 1), "Huila", 3.5, 10),
      Sismo(Fecha(2026, 2), "Tolima", 4.5, 12),
      Sismo(Fecha(2026, 3), "Huila", 5.5, 14))
    assert(enRegion(dos, "Huila") == List(3.5, 5.5))
  }

  test("enRegion: conserva las magnitudes repetidas de una región") {
    val repetido = List(
      Sismo(Fecha(2026, 2), "Narino", 4.0, 20),
      Sismo(Fecha(2026, 3), "Narino", 4.0, 45))
    assert(enRegion(repetido, "Narino") == List(4.0, 4.0))
  }

  test("paresCercanos: la diferencia igual a dif también cuenta") {
    val pareja = List(
      Sismo(Fecha(2026, 1), "Huila", 4.0, 20),
      Sismo(Fecha(2026, 1), "Tolima", 4.5, 30))
    assert(paresCercanos(pareja, 0.5) == List(("Huila", "Tolima")))
  }

  // Punto 2 -------------------------------------------------------------------

  test("porRegion: agrupa los siete sismos en tres regiones") {
    assert(porRegion(registro).keySet == Set("Valle", "Santander", "Choco"))
    assert(porRegion(registro).values.map(_.length).sum == 7)
  }

  test("conteoPorRegion: tres, dos y dos") {
    assert(conteoPorRegion(registro) ==
      Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2))
  }

  test("magnitudMaxima: la mayor de cada región") {
    assert(magnitudMaxima(registro) ==
      Map("Valle" -> 5.3, "Santander" -> 5.1, "Choco" -> 6.0))
  }

  test("conteoPorRegion: el registro vacío da el mapa vacío") {
    assert(conteoPorRegion(vacio) == Map())
  }

  test("magnitudMaxima: las regiones ausentes no aparecen") {
    assert(!magnitudMaxima(registro).contains("Amazonas"))
  }
  test("porRegion: Santander agrupa sus dos sismos en orden") {
    assert(porRegion(registro)("Santander") ==
      List(Sismo(Fecha(2025, 3), "Santander", 5.1, 150),
        Sismo(Fecha(2025, 5), "Santander", 4.7, 160)))
  }

  test("porRegion: el registro vacío da el mapa vacío") {
    assert(porRegion(vacio) == Map())
  }

  test("conteoPorRegion: los conteos suman el total de sismos") {
    assert(conteoPorRegion(registro).values.sum == 7)
  }

  test("magnitudMaxima: el registro vacío da el mapa vacío") {
    assert(magnitudMaxima(vacio) == Map())
  }

  test("magnitudMaxima: una región con un solo sismo") {
    val uno = List(Sismo(Fecha(2025, 1), "Arauca", 4.0, 10))
    assert(magnitudMaxima(uno) == Map("Arauca" -> 4.0))
  }

  // Punto 3 -------------------------------------------------------------------

  test("conteoSeguro: una región desconocida responde cero") {
    assert(conteoSeguro(registro)("Amazonas") == 0)
  }

  test("conteoSeguro: una región conocida responde su conteo") {
    assert(conteoSeguro(registro)("Valle") == 3)
  }

  test("conteoSeguro: sobre el registro vacío todo responde cero") {
    assert(conteoSeguro(vacio)("Valle") == 0)
  }

  test("maximaDe: el Chocó llega a 6.0") {
    assert(maximaDe(registro, "Choco") == Some(6.0))
  }

  test("maximaDe: una región sin registros da vacío") {
    assert(maximaDe(registro, "Amazonas") == None)
  }

  test("regionMasActiva: el Valle, con tres sismos") {
    assert(regionMasActiva(registro) == Some("Valle"))
  }

  test("regionMasActiva: sobre el registro vacío da vacío") {
    assert(regionMasActiva(vacio) == None)
  }

  test("regionMasActiva: en empate gana la primera alfabéticamente") {
    val empate = List(
      Sismo(Fecha(2025, 1), "Zulia", 4.0, 10),
      Sismo(Fecha(2025, 1), "Arauca", 4.0, 10))
    assert(regionMasActiva(empate) == Some("Arauca"))
  }
  test("conteoSeguro: el Chocó responde dos") {
    assert(conteoSeguro(registro)("Choco") == 2)
  }

  test("conteoSeguro: conocida y desconocida en la misma consulta") {
    assert(conteoSeguro(registro)("Valle") + conteoSeguro(registro)("Amazonas") == 3)
  }

  test("maximaDe: Santander llega a 5.1") {
    assert(maximaDe(registro, "Santander") == Some(5.1))
  }

  test("maximaDe: sobre el registro vacío da vacío") {
    assert(maximaDe(vacio, "Valle") == None)
  }

  test("regionMasActiva: gana la región con más sismos aunque sea la última alfabéticamente") {
    val registroZulia = List(
      Sismo(Fecha(2025, 1), "Zulia", 4.0, 10),
      Sismo(Fecha(2025, 2), "Zulia", 3.5, 12),
      Sismo(Fecha(2025, 3), "Arauca", 4.0, 10))
    assert(regionMasActiva(registroZulia) == Some("Zulia"))
  }

  test("porRegion: cada región guarda solo sus propios sismos") {
    assert(porRegion(registro)("Choco").map(_.magnitud) == List(6.0, 3.1))
  }

  test("porRegion: conserva el orden de aparición dentro de cada región") {
    assert(porRegion(registro)("Valle").map(_.fecha) ==
      List(Fecha(2025, 3), Fecha(2025, 4), Fecha(2025, 5)))
  }

  test("conteoPorRegion: dos regiones con el mismo conteo quedan separadas") {
    val parejo = List(
      Sismo(Fecha(2026, 1), "Caldas", 4.0, 10),
      Sismo(Fecha(2026, 2), "Quindio", 3.5, 12))
    assert(conteoPorRegion(parejo) == Map("Caldas" -> 1, "Quindio" -> 1))
  }

  test("conteoPorRegion: una sola región acumula todos sus sismos") {
    val una = List(
      Sismo(Fecha(2026, 1), "Narino", 4.0, 10),
      Sismo(Fecha(2026, 2), "Narino", 3.5, 12),
      Sismo(Fecha(2026, 3), "Narino", 5.0, 14))
    assert(conteoPorRegion(una) == Map("Narino" -> 3))
  }

  test("magnitudMaxima: toma la primera aparición, no la última") {
    val orden = List(
      Sismo(Fecha(2026, 1), "Huila", 5.2, 10),
      Sismo(Fecha(2026, 2), "Huila", 3.1, 12))
    assert(magnitudMaxima(orden) == Map("Huila" -> 5.2))
  }

  test("conteoSeguro: cada región conocida trae su propio conteo") {
    assert(conteoSeguro(registro)("Santander") == 2)
    assert(conteoSeguro(registro)("Valle") == 3)
  }

  test("maximaDe: una región de un solo sismo devuelve su magnitud") {
    val uno = List(Sismo(Fecha(2026, 1), "Tolima", 4.4, 15))
    assert(maximaDe(uno, "Tolima") == Some(4.4))
  }

  test("regionMasActiva: sin empate gana la de más sismos aunque no sea la primera") {
    val otra = List(
      Sismo(Fecha(2026, 1), "Arauca", 4.0, 10),
      Sismo(Fecha(2026, 2), "Cauca", 3.9, 11),
      Sismo(Fecha(2026, 3), "Cauca", 4.1, 12))
    assert(regionMasActiva(otra) == Some("Cauca"))
  }

  test("porRegion: con una sola región el mapa tiene una entrada") {
    val una = List(
      Sismo(Fecha(2026, 1), "Caldas", 4.0, 10),
      Sismo(Fecha(2026, 2), "Caldas", 3.5, 12))
    assert(porRegion(una).keySet == Set("Caldas") && porRegion(una)("Caldas").length == 2)
  }

  test("conteoPorRegion: tres regiones con un sismo cada una") {
    val tres = List(
      Sismo(Fecha(2026, 1), "Huila", 4.0, 10),
      Sismo(Fecha(2026, 1), "Tolima", 4.1, 11),
      Sismo(Fecha(2026, 1), "Cauca", 4.2, 12))
    assert(conteoPorRegion(tres) == Map("Huila" -> 1, "Tolima" -> 1, "Cauca" -> 1))
  }

  test("conteoPorRegion: una región lejana en el tiempo cuenta igual") {
    val lejos = List(
      Sismo(Fecha(2025, 1), "Narino", 4.0, 10),
      Sismo(Fecha(2026, 12), "Narino", 4.1, 11))
    assert(conteoPorRegion(lejos) == Map("Narino" -> 2))
  }

  test("magnitudMaxima: el máximo no depende del orden de los sismos") {
    val primeroBajo = List(
      Sismo(Fecha(2026, 1), "Cauca", 3.0, 10),
      Sismo(Fecha(2026, 2), "Cauca", 6.2, 12))
    val primeroAlto = primeroBajo.reverse
    assert(magnitudMaxima(primeroBajo) == magnitudMaxima(primeroAlto))
    assert(magnitudMaxima(primeroBajo) == Map("Cauca" -> 6.2))
  }

  test("magnitudMaxima: varias regiones no comparten su máximo") {
    val varias = List(
      Sismo(Fecha(2026, 1), "Huila", 5.0, 10),
      Sismo(Fecha(2026, 1), "Tolima", 6.0, 11),
      Sismo(Fecha(2026, 2), "Huila", 4.0, 12))
    assert(magnitudMaxima(varias) == Map("Huila" -> 5.0, "Tolima" -> 6.0))
  }

  test("conteoSeguro: la región desconocida no se agrega al mapa") {
    assert(!conteoSeguro(registro).contains("Amazonas"))
  }

  test("conteoSeguro: responde cero también para nombres con otra capitalización") {
    assert(conteoSeguro(registro)("VALLE") == 0)
  }

  test("maximaDe: la región más activa y la de mayor magnitud pueden diferir") {
    assert(regionMasActiva(registro) == Some("Valle"))
    assert(maximaDe(registro, "Valle") == Some(5.3))
  }

  test("maximaDe: una región con dos sismos toma el mayor, no el último") {
    val dos = List(
      Sismo(Fecha(2026, 1), "Caldas", 5.8, 10),
      Sismo(Fecha(2026, 2), "Caldas", 2.9, 12))
    assert(maximaDe(dos, "Caldas") == Some(5.8))
  }

  test("regionMasActiva: tres regiones, gana la del medio por conteo") {
    val tres = List(
      Sismo(Fecha(2026, 1), "Huila", 4.0, 10),
      Sismo(Fecha(2026, 1), "Tolima", 4.1, 11),
      Sismo(Fecha(2026, 2), "Tolima", 4.2, 12),
      Sismo(Fecha(2026, 1), "Cauca", 4.3, 13))
    assert(regionMasActiva(tres) == Some("Tolima"))
  }

  test("regionMasActiva: en un empate de tres gana la primera alfabéticamente") {
    val empate = List(
      Sismo(Fecha(2026, 1), "Tolima", 4.0, 10),
      Sismo(Fecha(2026, 1), "Cauca", 4.1, 11),
      Sismo(Fecha(2026, 1), "Huila", 4.2, 12))
    assert(regionMasActiva(empate) == Some("Cauca"))
  }

  test("regionMasActiva: con un solo sismo devuelve su región") {
    val uno = List(Sismo(Fecha(2026, 1), "Quindio", 4.0, 10))
    assert(regionMasActiva(uno) == Some("Quindio"))
  }

  // Punto 4 -------------------------------------------------------------------

  test("replicas: las primeras cuatro de un sismo de 5.0") {
    assert(replicas(5.0).take(4).toList == List(5.0, 4.0, 3.2, 2.56))
  }

  test("replicas: la secuencia es infinita y no se cuelga") {
    assert(replicas(5.0).take(200).toList.length == 200)
  }

  test("replicas: empieza en la magnitud dada") {
    assert(replicas(7.3).head == 7.3)
  }

  test("replicas: los valores van redondeados a dos decimales") {
    assert(replicas(5.0).take(6).toList.forall(x => x == Math.round(x * 100.0) / 100.0))
  }

  test("replicasSensibles: las que superan 3.0 partiendo de 5.0") {
    assert(replicasSensibles(5.0, 3.0) == List(5.0, 4.0, 3.2))
  }

  test("replicasSensibles: si la primera no supera el mínimo, no hay ninguna") {
    assert(replicasSensibles(2.0, 3.0) == List())
  }

  test("replicasSensibles: el mínimo es estricto") {
    assert(replicasSensibles(3.0, 3.0) == List())
  }
  test("replicas: las primeras tres de un sismo de 4.0") {
    assert(replicas(4.0).take(3).toList == List(4.0, 3.2, 2.56))
  }

  test("replicas: las primeras tres de un sismo de 1.0") {
    assert(replicas(1.0).take(3).toList == List(1.0, 0.8, 0.64))
  }

  test("replicas: el elemento en la posición 10 de un sismo de 5.0") {
    assert(replicas(5.0)(10) == 0.54)
  }

  test("replicas: cada elemento es el ochenta por ciento del anterior") {
    val secuencia = replicas(5.0).take(5).toList
    assert(secuencia.tail.zip(secuencia).forall {
      case (siguiente, actual) => siguiente == Math.round(actual * 0.8 * 100.0) / 100.0
    })
  }

  test("replicas: pedir un elemento no exige conocer el siguiente") {
    assert(replicas(5.0).take(1).toList == List(5.0))
  }

  test("replicasSensibles: las que superan 3.0 partiendo de 6.0") {
    assert(replicasSensibles(6.0, 3.0) == List(6.0, 4.8, 3.84, 3.07))
  }

  test("replicasSensibles: con un mínimo muy alto no hay ninguna") {
    assert(replicasSensibles(5.0, 10.0) == List())
  }

  test("replicasSensibles: con un mínimo bajo se corta en el valor exacto") {
    assert(replicasSensibles(2.5, 1.6) == List(2.5, 2.0))
  }

  test("replicasSensibles: cada valor es el ochenta por ciento del anterior") {
    val lista = replicasSensibles(6.0, 2.0)
    assert(lista.tail.zip(lista).forall {
      case (siguiente, actual) => siguiente == Math.round(actual * 0.8 * 100.0) / 100.0
    })
  }

  test("replicasSensibles: las réplicas siempre decrecen") {
    val lista = replicasSensibles(7.0, 1.5)
    assert(lista.length > 1 && lista.tail.zip(lista).forall {
      case (siguiente, actual) => siguiente < actual
    })
  }

  // Punto 5 -------------------------------------------------------------------

  test("informe: el resumen de las tres regiones") {
    assert(informe(registro, 3.0) ==
      Map("Valle" -> Resumen(3, 5.3, 3),
          "Santander" -> Resumen(2, 5.1, 3),
          "Choco" -> Resumen(2, 6.0, 4)))
  }

  test("informe: el registro vacío da el mapa vacío") {
    assert(informe(vacio, 3.0) == Map())
  }

  test("informe: el conteo coincide con conteoPorRegion") {
    assert(informe(registro, 3.0).map { case (r, res) => (r, res.sismos) } ==
      conteoPorRegion(registro))
  }

  test("informe: la máxima coincide con magnitudMaxima") {
    assert(informe(registro, 3.0).map { case (r, res) => (r, res.maxima) } ==
      magnitudMaxima(registro))
  }

  test("informe: las réplicas coinciden con replicasSensibles del más fuerte") {
    assert(informe(registro, 3.0)("Choco").replicas ==
      replicasSensibles(6.0, 3.0).length)
  }
  test("informe: el resumen del Valle") {
    assert(informe(registro, 3.0)("Valle") == Resumen(3, 5.3, 3))
  }

  test("informe: tiene una entrada por cada región del registro") {
    assert(informe(registro, 3.0).keySet == Set("Valle", "Santander", "Choco"))
  }

  test("informe: con un mínimo muy alto no hay réplicas en ninguna región") {
    assert(informe(registro, 10.0) ==
      Map("Valle" -> Resumen(3, 5.3, 0),
        "Santander" -> Resumen(2, 5.1, 0),
        "Choco" -> Resumen(2, 6.0, 0)))
  }

  test("informe: una región con un solo sismo") {
    val uno = List(Sismo(Fecha(2025, 1), "Arauca", 5.0, 10))
    assert(informe(uno, 3.0) == Map("Arauca" -> Resumen(1, 5.0, 3)))
  }

  test("informe: con mínimo 5.0 cada región tiene una sola réplica") {
    assert(informe(registro, 5.0) ==
      Map("Valle" -> Resumen(3, 5.3, 1),
        "Santander" -> Resumen(2, 5.1, 1),
        "Choco" -> Resumen(2, 6.0, 1)))
  }
}
