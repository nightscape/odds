package ch.epfl.lamp.odds

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import inference._

trait AppendModel extends OddsLang {

  def randomList: Rand[List[Boolean]] =
    if (flip(0.5)) flip(0.5) :: randomList
    else always(Nil)

  val t3 = List(true, true, true)
  val f2 = List(false, false)

  val appendModel1 = always(t3) ++ always(f2)

  val appendModel2 = (flip(0.5) :: always(Nil)) ++ f2

  // needs lazy strategy
  def appendModel3 = always(t3) ++ randomList

  def appendModel4 = {
    // query: X:::f2 == t3:::f2 solve for X
    val x = randomList
    val xf2 = x ::: always(f2)
    (x, always(f2), xf2) when (xf2 === t3 ::: f2)
  }

  def appendModel5 = {
    // query: X:::Y == t3:::f2 solve for X,Y
    val x = randomList
    val y = randomList
    val xy = x ::: y
    (x, y) when (xy === always(t3 ::: f2))
  }

  def appendModel6 = {
    // query: X ++ Y == t3 ++ f2 solve for X,Y
    val x = randomList
    val y = randomList
    val xy = x ++ y
    (x, y) when (xy === t3 ++ f2)
  }
}

trait AppendModelLazyTail extends OddsLang {

  import Rand.ToScalaMonadic

  // now try lists where the tail itself is a random var

  abstract class CList[+A]
  case object CNil extends CList[Nothing]
  case class CCons[+A](hd: A, tl: Rand[CList[A]]) extends CList[A]

  def asCList[A](x: List[A]): Rand[CList[A]] = x match {
    case Nil => always(CNil)
    case x::xs => always(CCons(x, asCList(xs)))
  }
  def asLists[A](x: Rand[CList[A]]): Rand[List[A]] = x flatMap {
    case CNil => always(Nil)
    case CCons(x, xs) => asLists(xs).map(xs=>x::xs)
  }

  def randomCList(): Rand[CList[Boolean]] = flip(0.5).flatMap {
    case false => always(CNil)
    case true  =>
      val x = flip(0.5)
      val tail = randomCList()
      x.map(x => CCons(x, tail))
  }

  def appendC[T](x: Rand[CList[T]], y: Rand[CList[T]]): Rand[CList[T]] = x flatMap {
    case CNil => y
    case CCons(h,t) => always(CCons(h, appendC(t,y)))
  }

  def listSameC[T](x: Rand[CList[T]], y: Rand[CList[T]]): Rand[Boolean] =
    x.flatMap { u => y.flatMap { v => (u,v) match {
      case (CCons(a,x),CCons(b,y)) if a == b => listSameC(x,y)
      case (CNil,CNil) => always(true)
      case _ => always(false)
    }}}


  val t3 = List(true, true, true)
  val f2 = List(false, false)
  val t3c = asCList(t3)
  val f2c = asCList(f2)

  def appendModel3b = {
    asLists(appendC(t3c,randomCList()))
  }

  def appendModel4b = {
    // query: X:::f2 == t3:::f2 solve for X
    val x = randomCList()
    val t3f2 = t3++f2
    listSameC(appendC(x,f2c), asCList(t3f2)).flatMap {
      case true =>
        // here we rely on memoization: otherwise x.tail would be making new choices all the time,
        asLists(x).map(x=>(x,f2,t3f2))
      case _ => never
    }
  }

  def appendModel5b = {
    // query: X:::Y == t3:::f2 solve for X,Y
    val x = randomCList()
    val y = randomCList()
    listSameC(appendC(x,y), asCList(t3++f2)).flatMap {
      case true => for (a <- asLists(x); b <- asLists(y)) yield (a,b)
      case _    => never
    }
  }

}


class AppendModelSpec
    extends AppendModel
    with DepthBoundInference
    with OddsPrettyPrint
    with FlatSpec
    with ShouldMatchers {

  behavior of "AppendModel"

  it should "show the results of appendModel1" in {
    val (d, e) = reify(1000)(appendModel1)
    show(d, "appendModel1")
    d.toMap should equal (Map(List(true, true, true, false, false) -> 1.0))
    e should equal (0)
  }

  it should "show the results of appendModel2" in {
    val (d, e) = reify(1000)(appendModel2)
    show(d, "appendModel2")
    d.toMap should equal (Map(
      List(false, false, false) -> 0.5,
      List(true, false, false) -> 0.5))
    e should equal (0)
  }

  it should "show the results of appendModel3" in {
    val (d, e) = reify(5)(appendModel3)
    show(d, "appendModel3")
    d.size should be >= 5
    d should contain (t3, 0.5)
    d foreach {
      case (l, _) => l.take(3) should equal (t3)
    }
    e should be < 0.5
  }

  it should "show the results of appendModel4" in {
    val (d, e) = reify(1)(appendModel4)
    show(d, "appendModel4")
    d.size should be >= 1
    d foreach {
      case ((x, y, res), _) =>
        y should equal (f2)
        (x ::: y) should equal (res)
        res should equal (t3 ::: f2)
    }
    e should be < 0.5
  }

  it should "show the results of appendModel5" in {
    val (d, e) = reify(5)(appendModel5)
    show(d, "appendModel5")
    d.size should be >= 5
    d foreach {
      case ((x, y), _) =>
        (x ::: y) should equal (t3 ::: f2)
    }
    e should be < 0.5
  }

  it should "show the results of appendModel6" in {
    val (d, e) = reify(5)(appendModel6)
    show(d, "appendModel6")
    d.size should be >= 5
    d foreach {
      case ((x, y), _) =>
        (x ::: y) should equal (t3 ::: f2)
    }
    e should be < 0.5
  }
}

class AppendModelLazyTailSpec
    extends AppendModelLazyTail
    with DepthBoundInference
    with OddsPrettyPrint
    with FlatSpec
    with ShouldMatchers {

  it should "show the results of appendModel3b" in {
    val (d, e) = reify(5)(appendModel3b)
    show(d, "appendModel3b")
    d.size should be >= 5
    d should contain (t3, 0.5)
    d foreach {
      case (l, _) => l.take(3) should equal (t3)
    }
    e should be < 0.5
  }

  it should "show the results of appendModel4b" in {
    val (d, e) = reify(1)(appendModel4b)
    show(d, "appendModel4b")
    d.size should be >= 1
    d foreach {
      case ((x, y, res), _) =>
        y should equal (f2)
        (x ::: y) should equal (res)
        res should equal (t3 ::: f2)
    }
    e should be < 0.5
  }

  it should "show the results of appendModel5b" in {
    val (d, e) = reify(5)(appendModel5b)
    show(d, "appendModel5b")
    d.size should be >= 5
    d foreach {
      case ((x, y), _) =>
        (x ::: y) should equal (t3 ::: f2)
    }
    e should be < 0.5
  }
}