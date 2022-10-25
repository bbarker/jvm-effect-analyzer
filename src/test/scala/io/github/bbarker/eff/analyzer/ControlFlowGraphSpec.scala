package io.github.bbarker.eff.analyzer

import java.io.InputStream
import scala.jdk.CollectionConverters.*

import io.github.bbarker.eff.analyzer.util.ClassPath.asmClassPath
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import zio.*
import zio.test.*

object ControlFlowGraphSpec extends ZIOSpecDefault:

  /** Nodes correspond to frames (i.e. method calls), so we need each branch to
    * call a method if we wish to count that as a node.
    */
  class SingleFrameMethods:
    def one(x: Int): Int = x
    def two(x: Int): Boolean = if (x < 0) then true else false
    def three(x: Int): Boolean =
      if (x < 0) then true else if (x == 1) then true else false

  final case class TestClassInfo(
      stream: InputStream,
      path: String,
      expectedCC: Map[String, Int]
  ) {
    val name: String = path.split(Array('$', '/')).last
  }
  val singleFrameMethodsPath =
    "io/github/bbarker/eff/analyzer/ControlFlowGraphSpec$SingleFrameMethods"
  val singleFrameMethodsStream = this.getClass.getClassLoader
    .getResourceAsStream(asmClassPath(singleFrameMethodsPath))

  def ccTripleTest(
      info: TestClassInfo
  ): Spec[Any, Throwable] =
    test(s"Testing cyclomatic complexity for ${info.name})") {
      for {
        classNode <- ZIO.succeed(new ClassNode)
        classReader <- ZIO.attempt(
          new ClassReader(info.stream) {}
        )
        _ <- ZIO.succeed(classReader.accept(classNode, 0))
        mNodes = classNode.methods.asScala
        _ <- ZIO.succeed(mNodes.map(mn => mn.name)).debug

        ccOptMap <- ZIO
          .foreach(info.expectedCC.keys)(mn =>
            ControlFlowGraph
              .cyclomaticComplexity(classNode, mn)
              .map(cc => mn -> cc)
          )
          .map(_.toMap)
        expectedCCMap = info.expectedCC.view.mapValues(Some.apply).toMap
      } yield assertTrue(
        ccOptMap == expectedCCMap
      )

    }
  def spec = suite("ControlFlowGraphSpec")(
    suite("Cyclomatic Complexity")(
      test("Testing basic conditionals (if-else branching)") {
        for {
          classNode <- ZIO.succeed(new ClassNode)
          classReader <- ZIO.attempt(
            new ClassReader(singleFrameMethodsStream) {}
          )
          _ <- ZIO.succeed(classReader.accept(classNode, 0))
          mNodes = classNode.methods.asScala
          _ <- ZIO.succeed(mNodes.map(mn => mn.name)).debug

          oneCC <- ControlFlowGraph.cyclomaticComplexity(classNode, "one")
          twoCC <- ControlFlowGraph.cyclomaticComplexity(classNode, "two")
          // threeCC <- ControlFlowGraph.cyclomaticComplexity(classNode, "three")

        } yield assertTrue(oneCC.contains(1)) && assertTrue(
          twoCC.contains(2)
        ) // && assertTrue(threeCC == Some(3))
      }
    )
  )
