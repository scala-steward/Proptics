package proptics.specs

import cats.Id
import cats.data.NonEmptyList
import cats.instances.option._
import cats.syntax.option._

import proptics.IndexedLens
import proptics.law.discipline._
import proptics.specs.compose._

class IndexedLensSpec extends PropticsSuite {
  val wholeIndexedLens: IndexedLens[Int, Whole, Int] =
    IndexedLens[Int, Whole, Int](w => (w.part, w.part))(w => p => w.copy(part = p))

  val nelIndexedLens: IndexedLens[Int, NonEmptyList[Int], Int] =
    IndexedLens[Int, NonEmptyList[Int], Int](ls => (ls.head, 0))(nel => i => nel.copy(head = i))

  checkAll("IndexedLens[Int, NonEmptyList[Int], Int] apply", IndexedLensTests(nelIndexedLens).indexedLens)
  checkAll("IndexedLens[Int, Whole, Int] asLens", LensTests(wholeIndexedLens.asLens).lens)
  checkAll("IndexedLens[Int, Int, Int] compose with IndexedLens", IndexedLensTests(indexedLens compose indexedLens).indexedLens)
  checkAll("IndexedLens[Int, Int, Int] compose with AnIndexedLens", AnIndexedLensTests(indexedLens compose anIndexedLens).anIndexedLens)
  checkAll("IndexedLens[Int, Int, Int] compose with IndexedTraversal", IndexedTraversalTests(indexedLens compose indexedTraversal).indexedTraversal)
  checkAll("IndexedLens[Int, Int, Int] compose with IndexedSetter", IndexedSetterTests(indexedLens compose indexedSetter).indexedSetter)

  test("view") {
    nelIndexedLens.view(nel) shouldEqual ((1, 0))
  }

  test("set") {
    nelIndexedLens.set(9)(nel) shouldEqual nel.copy(head = 9)
  }

  test("over") {
    nelIndexedLens.over(oneToNine)(nel) shouldEqual nel.copy(head = 9)
  }

  test("traverse") {
    val result = nelIndexedLens.traverse[Id](nel)(oneToNine)

    result shouldEqual nel.copy(head = 9)
    nelIndexedLens.overF[Id](oneToNine)(nel) shouldEqual result
  }

  test("exists") {
    nelIndexedLens.exists(_ === ((1, 0)))(nel)
  }

  test("notExists") {
    nelIndexedLens.notExists(_ === ((1, 0)))(nel) shouldEqual false
    nelIndexedLens.notExists(_ === ((1, 1)))(nel) shouldEqual true
    nelIndexedLens.notExists(_ === ((2, 0)))(nel) shouldEqual true
    nelIndexedLens.notExists(_ === ((1, 0)))(nel) shouldEqual !nelIndexedLens.exists(_ == ((1, 0)))(nel)
  }

  test("contains") {
    nelIndexedLens.contains((1, 0))(nel) shouldEqual true
    nelIndexedLens.contains((1, 1))(nel) shouldEqual false
  }

  test("notContains") {
    nelIndexedLens.notContains((1, 0))(nel) shouldEqual false
    nelIndexedLens.notContains((1, 1))(nel) shouldEqual true
    nelIndexedLens.notContains((1, 1))(nel) shouldEqual !nelIndexedLens.contains((1, 1))(nel)
  }

  test("find") {
    nelIndexedLens.find { case (a, i) => i === 0 && a === 1 }(nel) shouldEqual (1, 0).some
    nelIndexedLens.find(_ === ((1, 0)))(nel) shouldEqual (1, 0).some
    nelIndexedLens.find(_._1 === 0)(nel) shouldEqual None
    nelIndexedLens.find(_ === ((1, 1)))(nel) shouldEqual None
  }

  test("use") {
    nelIndexedLens.use.runA(nel).value shouldEqual ((1, 0))
  }

  test("failover") {
    val res = nelIndexedLens.failover[Option](nel)(_._1)(strongStarTupleOfDisj, catsStdInstancesForOption)
    val negativeRes = nelIndexedLens.failover[Option](nel)(_._1)(strongStarTupleOfNegativeDisj, catsStdInstancesForOption)

    res shouldEqual Some(nel)
    negativeRes shouldEqual None
  }

  test("zipWith") {
    val secondNel = NonEmptyList.fromListUnsafe(List(8, 9, 10))
    val result = NonEmptyList.fromListUnsafe(9 :: list.tail)

    nelIndexedLens.zipWith(nel, secondNel) { case ((a1, _), (a2, _)) => a1 + a2 } shouldEqual result
  }

  test("cotraverse") {
    val cotraversedNel = nelIndexedLens.cotraverse[Id](nel)(_._1)

    cotraversedNel shouldEqual nel
    nelIndexedLens.zipWithF[Id](_._1)(nel) shouldEqual cotraversedNel
  }

  test("reindex") {
    indexedLens.reindex(_.toString).view(9) shouldEqual ((9, "0"))
  }

  test("compose with IndexedGetter") {
    (indexedLens compose indexedGetter).view(9) shouldEqual ((9, 0))
  }

  test("compose with IndexedFold") {
    (indexedLens compose indexedFold).foldMap(9)(_._1) shouldEqual 9
  }
}
