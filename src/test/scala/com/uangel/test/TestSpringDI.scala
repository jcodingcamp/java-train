package com.uangel.test

import com.uangel.training.hello.Hello
import org.scalatest.funsuite.AnyFunSuite
import org.springframework.context.annotation.{AnnotationConfigApplicationContext, Bean, Configuration, Lazy, Primary, Profile}
import org.springframework.stereotype.Component

import java.io.Closeable


case class HelloImpl() extends Hello with Closeable{
  override def say(): String = "hello"

  override def close(): Unit = {
    println("close")
  }
}

trait Hi {
  def say() : String
}

trait Speaker {
  def say() : String
}

@Component
case class SpeakerImpl(hello : Hello) extends Speaker {

  def say() : String = {
    hello.say()
  }
}

@Configuration
class OtherModule {
  @Bean
  @Primary
  def hello2() : Hello = {
    new Hello {
      override def say(): String = "world"
    }
  }
}

@Configuration
class SomeModule {

  @Bean
    def hello() : Hello = {
      HelloImpl()
    }

  @Bean
  def hi(hello : Hello) : Hi = {
    println("hi")
    new Hi with Closeable {
      override def say(): String = hello.say()

      override def close(): Unit = "close hi"
    }
  }
}

class Test extends AnyFunSuite{
  test("hello") {

    println("before context")
    val context = new AnnotationConfigApplicationContext()

    context.scan("com.uangel.test")
//    context.register(classOf[SomeModule])
//    context.register(classOf[SpeakerImpl])

//    context.register(classOf[SomeModule], classOf[SpeakerImpl])
//    context.register(classOf[OtherModule])

    context.refresh()
    // new AnnotationConfigApplicationContext(SomeModule.class)

    //context.refresh()

    println("after context")

    val h = context.getBean(classOf[Hello])
    //h.close()
    //
    assert(h.say() == "world")

  }
}