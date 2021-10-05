package com.uangel.test

import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class ScalaPromise extends AnyFunSuite{

  def otherApi() : Future[Int] = {
    Future.successful(10)
  }

  def api() : Future[String] = {
    val promise = Promise[String]();

    otherApi().onComplete(t => {
      t match {
        case Success(value) => promise.success(s"$value")
        case Failure(exception) => promise.failure(exception)
      }
    })

    return promise.future
  }

  test("test") {
    val f = api()

  }
}
