package io.github.bbarker.eff.analyzer

import java.io.InputStream
import scala.jdk.CollectionConverters.*

import io.github.bbarker.eff.analyzer.util.ClassPath.asmClassPath
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import zio.*
import zio.test.*

object ControlFlowGraphSpec extends ZIOSpecDefault:

  final case class TestClassInfo private (
      stream: InputStream,
      path: String,
      expectedCC: Map[String, Int]
  ):
    val name: String = path.split(Array('$', '/')).last

  object TestClassInfo:
    def apply(path: String, expectedCC: Map[String, Int]): TestClassInfo =
      TestClassInfo(
        stream =
          this.getClass.getClassLoader.getResourceAsStream(asmClassPath(path)),
        path = path,
        expectedCC = expectedCC
      )

  def ccMultiMethodTest(
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

  class MethodsWithoutMethodCalls:
    def one(x: Int): Int = x
    def two(x: Int): Boolean = if (x < 0) then true else false
    def three(x: Int): Boolean =
      if (x < 0) then true else if (x == 1) then true else false

  class MethodOnBranch:
    def one(x: Int): Int = x
    def two(x: Int): Boolean = if (x < 0) then aux1(x) else aux2(x)
    def three(x: Int): Boolean =
      if (x < 0) then aux1(x) else if (x == 1) then aux2(x) else aux3(2)
    def alsoThree(x: Int): Boolean =
      if (x < 0) then aux1(x) else if (x == 1) then aux1(x) else aux1(2)

    def aux1(x: Int): Boolean = if (x > 10) true else false
    def aux2(x: Int): Boolean = if (x > 20) true else false
    def aux3(x: Int): Boolean = if (x > 30) true else false

  // TODO: I think ASM provides a way to get the "path" from a class
  val singleFrameMethodsInfo: TestClassInfo =
    TestClassInfo(
      "io/github/bbarker/eff/analyzer/ControlFlowGraphSpec$MethodsWithoutMethodCalls",
      Map("one" -> 1, "two" -> 2, "three" -> 3)
    )

  val methodOnBranchInfo: TestClassInfo =
    TestClassInfo(
      "io/github/bbarker/eff/analyzer/ControlFlowGraphSpec$MethodOnBranch",
      Map("one" -> 1, "two" -> 2, "three" -> 3, "alsoThree" -> 3)
    )
  def spec = suite("ControlFlowGraphSpec")(
    suite("Cyclomatic Complexity")(
      ccMultiMethodTest(singleFrameMethodsInfo),
      ccMultiMethodTest(methodOnBranchInfo)
    )
  )
