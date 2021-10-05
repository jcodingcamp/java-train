package com.uangel.test

import org.scalatest.funsuite.AnyFunSuite

import scala.util.Failure


class ASuper {}
class A extends ASuper {}

class AImpl extends A {}


class BSuper {}
class B extends BSuper {}

class BImpl extends B {}

class TestCovar extends AnyFunSuite{
  test("covar") {
    var f : Function1[A,B] =  (a) => new B()
    var f2 : Function1[A,BImpl] = (a) => new BImpl()

    f = f2

    var a : Option[A] = None
    var a2 : Option[A] = a.orElse( Some(new AImpl()))

    var a3 : Option[ASuper] = a.orElse( Some(new ASuper()))

    a3 = a2;

    var alist : List[A] = List()
    var superList : List[ASuper] = alist
    //f(new A())
  }
}
