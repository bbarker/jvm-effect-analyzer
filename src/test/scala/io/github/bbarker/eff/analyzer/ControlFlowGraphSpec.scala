package io.github.bbarker.eff.analyzer

import scala.jdk.CollectionConverters.*

import io.github.bbarker.eff.analyzer.util.ClassPath.asmClassPath
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import zio.*
import zio.test.{ZIOSpecDefault, assertTrue}

object ControlFlowGraphSpec extends ZIOSpecDefault:

  class CycloTestClass:
    def one(x: Int): Int = x
    def two(x: Int): Boolean = if (x < 0) then true else false
    def three(x: Int): Boolean =
      if (x < 0) then true else if (x == 1) then true else false

  val cycloTestClassPath =
    "io/github/bbarker/eff/analyzer/ControlFlowGraphSpec$CycloTestClass"
  val cycloTestClassStream = this.getClass.getClassLoader
    .getResourceAsStream(asmClassPath(cycloTestClassPath))

  def spec = suite("ControlFlowGraphSpec")(
    suite("Cyclomatic Complexity")(
      test("Testing basic conditionals (if-else branching)") {
        for {
          classNode <- ZIO.succeed(new ClassNode)
          classReader <- ZIO.attempt(new ClassReader(cycloTestClassStream) {})
          _ <- ZIO.succeed(classReader.accept(classNode, 0))
          mNodes = classNode.methods.asScala
          _ <- ZIO.succeed(mNodes.map(mn => mn.name)).debug

          oneCC <- ControlFlowGraph.cyclomaticComplexity(classNode, "one")
          twoCC <- ControlFlowGraph.cyclomaticComplexity(classNode, "two")
          threeCC <- ControlFlowGraph.cyclomaticComplexity(classNode, "three")

        } yield assertTrue(oneCC == Some(1)) && assertTrue(
          twoCC == Some(2)
        ) && assertTrue(threeCC == Some(3))
      }
    )
  )
