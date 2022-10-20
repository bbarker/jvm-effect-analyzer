package io.github.bbarker.eff.analyzer

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
    suite("simple check")(
      test("???") {
        for {
          classNode <- ZIO.succeed(new ClassNode)
          // cc <- ControlFlowGraph.cyclomaticComplexity(1, 2)

        } yield assertTrue(1 + 1 == 2)
      }
    )
  )
