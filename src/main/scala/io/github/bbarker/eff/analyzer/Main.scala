package io.github.bbarker.eff.analyzer

import scala.jdk.CollectionConverters.*

import io.github.bbarker.eff.analyzer.config.Configuration.*
import io.github.bbarker.eff.analyzer.util.ClassPath.asmClassFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import zio.*
import zio.config.*
import zio.stream.*

object Main extends ZIOAppDefault:

  class FooBar {
    def foo: String = "Foo"
    val bar: Int = 5
    // Note: a `val` generates a JVM method and a JVM field
  }

  val cn = new ClassNode
  val testClassName = "io/github/bbarker/eff/analyzer/Main$FooBar"

  println(s"testClassPath = ${asmClassFile(testClassName)}")

  val testClassStream = this.getClass.getClassLoader
    .getResourceAsStream(asmClassFile(testClassName))

  val myReader = new ClassReader(testClassStream) {}

  myReader.accept(cn, 0)

  println(s"methods: ${cn.methods.asScala.map(_.name)}")
  println(s"fields: ${cn.fields.asScala.map(_.name)}")

  val program =
    for
      config <- getConfig[ServerConfig]
      _ <- Console.printLine(config)
    yield ()

  override val run =
    program.provide(ServerConfig.layer)
