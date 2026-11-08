package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Punto4Test extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  test("replicas: desde 10.0 produce 10.0, 8.0, 6.4 y 5.12") {
    assert(replicas(10.0).take(4).toList == List(10.0, 8.0, 6.4, 5.12))
  }

  test("replicas: redondea a dos decimales sin acumular el error del redondeo") {
    // Valores exactos: 3.1, 3.1*0.8, 3.1*0.8^2, 3.1*0.8^3, 3.1*0.8^4.
    assert(replicas(3.1).take(5).toList == List(3.1, 2.48, 1.98, 1.59, 1.27))
  }

  test("replicas: es perezosa, infinita y empieza en la magnitud dada") {
    assert(replicas(5.0).head == 5.0)
    assert(replicas(5.0).take(1000).size == 1000)
  }

  test("replicasSensibles: el mínimo es estricto y excluye el valor igual") {
    assert(replicasSensibles(5.0, 3.2) == List(5.0, 4.0))
  }

  test("replicasSensibles: si la magnitud inicial no supera el mínimo no hay réplicas") {
    assert(replicasSensibles(4.0, 4.0) == List())
    assert(replicasSensibles(3.9, 4.0) == List())
  }

  test("replicasSensibles: con mínimo bajo decrece estrictamente y todas superan el mínimo") {
    val rs = replicasSensibles(5.0, 0.5)
    assert(rs.nonEmpty)
    assert(rs == rs.sorted.reverse)
    assert(rs.tail.zip(rs).forall { case (actual, anterior) => actual < anterior })
    assert(rs.forall(_ > 0.5))
  }

  test("replicasSensibles: cada réplica es aproximadamente 0.8 veces la anterior") {
    val pares = replicasSensibles(5.0, 0.5).sliding(2).toList
    assert(pares.forall {
      case List(anterior, actual) => math.abs(actual - 0.8 * anterior) <= 0.01
      case _                      => true
    })
  }
}
