package io.github.bbarker.eff.analyzer

import scala.annotation.{tailrec, targetName}
import scala.jdk.CollectionConverters.*

import io.github.bbarker.eff.analyzer.util.Errors
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import zio.*
import zio.config.*
import zio.stream.*

object ControlFlowGraph {

  def analyzer(): Analyzer[BasicValue] = new Analyzer(new BasicInterpreter()):

    override protected def newFrame(
        nLocals: Int,
        nStack: Int
    ): Frame[BasicValue] = Node(nLocals, nStack)

    override protected def newFrame(
        frame: Frame[? <: BasicValue]
    ): Frame[BasicValue] =
      Node.fromFrame[BasicValue](frame.asInstanceOf[Frame[BasicValue]])

    override protected def newControlFlowEdge(src: Int, dst: Int): Unit = {
      val srcFrame: Node[BasicValue] =
        (getFrames()(src)).asInstanceOf[Node[BasicValue]]
      srcFrame.successors.add((getFrames()(dst)).asInstanceOf[Node[BasicValue]])
    }

  def cyclomaticComplexity(
      owner: String,
      mn: MethodNode
  ): IO[AnalyzerException, Int] = for {
    ccAnalyzer <- ZIO.succeed(analyzer())
    _ <- ZIO
      .attempt(ccAnalyzer.analyze(owner, mn))
      .refineOrDie[AnalyzerException](Errors.analyzerException _)
    frames = ccAnalyzer.getFrames.map(_.asInstanceOf[Node[BasicValue]])
    cc = calculateCyclomaticComplexity(frames)
  } yield cc

  def cyclomaticComplexity(
      classNode: ClassNode,
      methodName: String
  ): IO[AnalyzerException, Option[Int]] =
    val classMethods = classNode.methods.asScala
    val methodNodeOpt = classMethods.find(mn => mn.name == methodName)
    ZIO.foreach(methodNodeOpt)(mn => cyclomaticComplexity(classNode.name, mn))

  def calculateCyclomaticComplexity(frames: Array[Node[BasicValue]]): Int = {
    @tailrec
    def go(edges: Int, nodes: Int, frameIx: Int): Int =
      if (frameIx == frames.length) {
        edges - nodes + 2
      } else if (frames(frameIx) == null) {
        go(edges, nodes, frameIx + 1)
      } else {
        go(edges + frames(frameIx).successors.size, nodes + 1, frameIx + 1)
      }
    go(0, 0, 0)
  }

}
