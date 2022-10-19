package io.github.bbarker.eff.analyzer

import scala.jdk.CollectionConverters.*

import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import zio.*
import zio.config.*
import zio.stream.*

/*
Page 118:

> Although the primary goal of the framework is to perform data flow analyses,
the Analyzer class can also construct the control flow graph of the
analyzed method. This can be done by overriding the newControlFlowEdge
and newControlFlowExceptionEdge methods of this class, which by default
do nothing. The result can be used for doing control flow analyses.

Examples are given starting on page 125.

 */

//TODO: NonEmptyString
//TODO: custom type for Attribute
//TODO: Exceptions, but how?
//TODO: for callees, maybe look here: https://github.com/axt/jvm-callgraph/blob/master/src/main/java/com/axt/jvmcallgraph/MethodCallCollector.java#L30
final case class MethodData(
    name: String,
    attrs: List[Attribute],
    exceptions: List[String],
    tryCatchBlocks: List[
      TryCatchBlockNode
    ] // TODO: try to simplify as "internal" exceptions
    // instructions: InsnList // TODO: change to called methods
):
  val foo = ???

object MethodData:
  def fromMethodNode(asmMethodNode: MethodNode): MethodData = MethodData(
    name = asmMethodNode.name,
    attrs = asmMethodNode.attrs.asScala.toList,
    exceptions = asmMethodNode.exceptions.asScala.toList,
    tryCatchBlocks = asmMethodNode.tryCatchBlocks.asScala.toList
  )
