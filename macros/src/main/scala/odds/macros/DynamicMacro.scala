package ch.epfl.lamp.odds
package macros

import language.dynamics

import reflect.macros.Context


/**
 * Dynamic macro base trait.
 *
 * This is the base trait for implementing dynamic macros, also know
 * as "Poor man's type macros".
 *
 * @todo Is there a less awful way to handle type parameter lists of
 *   various lengths?
 *
 * @todo FIXME: SI-7776 prevents implementation of type parameters.
 */
trait DynamicMacro[R] extends Dynamic {

  import DynamicMacro.Handler._

  def applyDynamic(name: String)(args: Any*): R =
    macro applyDynamicMacro[R]
  // def applyDynamic[T1](name: String)(args: Any*)(
  //   implicit d1: DummyImplicit): R =
  //   macro applyDynamicMacro[T1, R]
  // def applyDynamic[T1, T2](name: String)(args: Any*)(
  //   implicit d1: DummyImplicit, d2: DummyImplicit): R =
  //   macro applyDynamicMacro[T1, T2, R]
  // def applyDynamic[T1, T2, T3](name: String)(args: Any*)(
  //   implicit d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit): R =
  //   macro applyDynamicMacro[T1, T2, T3, R]

  def applyDynamicNamed(name: String)(args: (String, Any)*): R =
    macro applyDynamicNamedMacro[R]
  // def applyDynamicNamed[T1](name: String)(args: (String, Any)*)(
  //   implicit d1: DummyImplicit): R =
  //   macro applyDynamicNamedMacro[T1, R]
  // def applyDynamicNamed[T1, T2](name: String)(args: (String, Any)*)(
  //   implicit d1: DummyImplicit, d2: DummyImplicit): R =
  //   macro applyDynamicNamedMacro[T1, T2, R]
  // def applyDynamicNamed[T1, T2, T3](name: String)(args: (String, Any)*)(
  //   implicit d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit): R =
  //   macro applyDynamicNamedMacro[T1, T2, T3, R]

  def selectDynamic(name: String): R =
    macro selectDynamicMacro[R]
  // def selectDynamic[T1](name: String)(
  //   implicit d1: DummyImplicit): R =
  //   macro selectDynamicMacro[T1, R]
  // def selectDynamic[T1, T2](name: String)(
  //   implicit d1: DummyImplicit, d2: DummyImplicit): R =
  //   macro selectDynamicMacro[T1, T2, R]
  // def selectDynamic[T1, T2, T3](name: String)(
  //   implicit d1: DummyImplicit, d2: DummyImplicit, d3: DummyImplicit): R =
  //   macro selectDynamicMacro[T1, T2, T3, R]
}

/**
 * Companion object of [[DynamicMacro]].
 *
 * This object contains the macro implementations of the
 * [[DynamicMacro]] trait.
 */
object DynamicMacro {

  /** Default dynamic macro handler. */
  trait Handler {

    /** Default `applyDynamic` handler. */
    def applyDynamic[R](c: Context)(
      name: String, args: List[c.Expr[Any]], targs: List[c.Type])(
      implicit r: c.WeakTypeTag[R]): c.Expr[R] =
      c.abort(c.enclosingPosition,
        "unhandled call to applyDynamic form member '" + name + "'")

    /** Default `applyDynamicNamed` handler. */
    def applyDynamicNamed[R](c: Context)(
      name: String, args: List[(String, c.Expr[Any])], targs: List[c.Type])(
      implicit r: c.WeakTypeTag[R]): c.Expr[R] =
      c.abort(c.enclosingPosition,
        "unhandled call to applyDynamicNamed for method '" + name + "'.")

    /** Default `selectDynamic` handler. */
    def selectDynamic[R](c: Context)(
      name: String, targs: List[c.Type])(
      implicit r: c.WeakTypeTag[R]): c.Expr[R] =
      c.abort(c.enclosingPosition,
        "unhandled call to selectDynamic for member '" + name + "'.")


    /** Boilerplate type parameter extraction for `applyDynamic` macro. */
    def applyDynamicMacro[R: c.WeakTypeTag](c: Context)(
      name: c.Expr[String])(args: c.Expr[Any]*): c.Expr[R] =
      this.applyDynamicParams[R](c)(name, args, Nil)

    // /** Boilerplate type parameter extraction for `applyDynamic` macro. */
    // def applyDynamicMacro[T1, R](c: Context)(
    //   name: c.Expr[String])(args: c.Expr[Any]*)(d1: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.applyDynamicParams[R](c)(name, args, List(t1.tpe))

    // /** Boilerplate type parameter extraction for `applyDynamic` macro. */
    // def applyDynamicMacro[T1, T2, R](c: Context)(
    //   name: c.Expr[String])(args: c.Expr[Any]*)(
    //   d1: c.Expr[DummyImplicit], d2: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], t2: c.WeakTypeTag[T2],
    //     r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.applyDynamicParams[R](c)(name, args, List(t1.tpe, t2.tpe))

    // /** Boilerplate type parameter extraction for `applyDynamic` macro. */
    // def applyDynamicMacro[T1, T2, T3, R](c: Context)(
    //   name: c.Expr[String])(args: c.Expr[Any]*)(
    //   d1: c.Expr[DummyImplicit], d2: c.Expr[DummyImplicit],
    //     d3: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], t2: c.WeakTypeTag[T2],
    //     t3: c.WeakTypeTag[T3], r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.applyDynamicParams[R](c)(name, args,
    //     List(t1.tpe, t2.tpe, t3.tpe))

    /** Boilerplate type parameter extraction `applyDynamicNamed` macro. */
    def applyDynamicNamedMacro[R: c.WeakTypeTag](c: Context)(
      name: c.Expr[String])(args: c.Expr[(String, Any)]*): c.Expr[R] =
      this.applyDynamicNamedParams[R](c)(name, args, Nil)

    // /** Boilerplate type parameter extraction `applyDynamicNamed` macro. */
    // def applyDynamicNamedMacro[T1, R](c: Context)(
    //   name: c.Expr[String])(args: c.Expr[(String, Any)]*)(
    //   d1: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.applyDynamicNamedParams[R](c)(name)(args)(List(t1.tpe))

    // /** Boilerplate type parameter extraction `applyDynamicNamed` macro. */
    // def applyDynamicNamedMacro[T1, T2, R](c: Context)(
    //   name: c.Expr[String])(args: c.Expr[(String, Any)]*)(
    //   d1: c.Expr[DummyImplicit], d2: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], t2: c.WeakTypeTag[T2],
    //     r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.applyDynamicNamedParams[R](c)(name, args,
    //     List(t1.tpe, t2.tpe))

    // /** Boilerplate type parameter extraction `applyDynamicNamed` macro. */
    // def applyDynamicNamedMacro[T1, T2, T3, R](c: Context)(
    //   name: c.Expr[String])(args: c.Expr[(String, Any)]*)(
    //   d1: c.Expr[DummyImplicit], d2: c.Expr[DummyImplicit],
    //     d3: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], t2: c.WeakTypeTag[T2],
    //     t3: c.WeakTypeTag[T3], r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.applyDynamicNamedParams[R](c)(name, args,
    //     List(t1.tpe, t2.tpe, t3.tpe))

    /** Boilerplate type parameter extraction for `selectDynamic` macro. */
    def selectDynamicMacro[R: c.WeakTypeTag](c: Context)(
      name: c.Expr[String]): c.Expr[R] =
      this.selectDynamicParams[R](c)(name, Nil)

    // /** Boilerplate type parameter extraction for `selectDynamic` macro. */
    // def selectDynamicMacro[T1, R](c: Context)(name: c.Expr[String])(
    //   d1: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.selectDynamicParams[R](c)(name, List(t1.tpe))

    // /** Boilerplate type parameter extraction for `selectDynamic` macro. */
    // def selectDynamicMacro[T1, T2, R](c: Context)(name: c.Expr[String])(
    //   d1: c.Expr[DummyImplicit], d2: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], t2: c.WeakTypeTag[T1],
    //   r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.selectDynamicParams[R](c)(name, List(t1.tpe, t2.tpe))

    // /** Boilerplate type parameter extraction for `selectDynamic` macro. */
    // def selectDynamicMacro[T1, T2, T3, R](c: Context)(name: c.Expr[String])(
    //   d1: c.Expr[DummyImplicit], d2: c.Expr[DummyImplicit],
    //     d3: c.Expr[DummyImplicit])(
    //   implicit t1: c.WeakTypeTag[T1], t2: c.WeakTypeTag[T2],
    //   t3: c.WeakTypeTag[T3], r: c.WeakTypeTag[R]): c.Expr[R] =
    //   this.selectDynamicParams[R](c)(name, List(t1.tpe, t2.tpe, t3.tpe))


    /** Boilerplate parameter extraction for `applyDynamic` macro. */
    def applyDynamicParams[R](c: Context)(
      name: c.Expr[String], args: Seq[c.Expr[Any]], targs: List[c.Type])(
      implicit r: c.WeakTypeTag[R]): c.Expr[R] = {

      import c.universe._

      val Literal(Constant(n: String)) = name.tree
      this.applyDynamic[R](c)(n, args.toList, targs)
    }

    /** Boilerplate parameter extraction `applyDynamicNamed` macro. */
    def applyDynamicNamedParams[R](c: Context)(
      name: c.Expr[String], args: Seq[c.Expr[(String, Any)]],
      targs: List[c.Type])(
      implicit r: c.WeakTypeTag[R]): c.Expr[R] = {

      import c.universe._

      val Literal(Constant(n: String)) = name.tree
      val as = args.toList map { a =>
        val q"scala.this.Tuple2.apply[..${_}]($ak, $av)" = a.tree
        val Literal(Constant(an: String)) = ak
        (an, c.Expr[Any](av))
      }
      this.applyDynamicNamed[R](c)(n, as, targs)
    }

    /** Boilerplate parameter extraction `selectDynamic` macro. */
    def selectDynamicParams[R](c: Context)(
      name: c.Expr[String], targs: List[c.Type])(
      implicit r: c.WeakTypeTag[R]): c.Expr[R] = {

      import c.universe._

      val Literal(Constant(n: String)) = name.tree
      this.selectDynamic[R](c)(n, targs)
    }
  }

  private object Handler extends Handler
}
