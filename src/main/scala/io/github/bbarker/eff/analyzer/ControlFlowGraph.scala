package io.github.bbarker.eff.analyzer

import scala.annotation.{tailrec, targetName}

import io.github.bbarker.eff.analyzer.util.Errors
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis._
import zio.*
import zio.config.*
import zio.stream.*

class ControlFlowGraph {

  def analyzer(): Analyzer[BasicValue] = new Analyzer(new BasicInterpreter()) {

    override protected def newFrame(
        nLocals: Int,
        nStack: Int
    ): Frame[BasicValue] = Node(nLocals, nStack)

    @targetName("newFrameCG")
    protected def newFrame(
        frame: Frame[BasicValue]
    ): Frame[BasicValue] = Node.fromFrame[BasicValue](frame)

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
}
