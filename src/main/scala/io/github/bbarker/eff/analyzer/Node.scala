package io.github.bbarker.eff.analyzer

import scala.collection.mutable

import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis._
import zio.*
import zio.config.*
import zio.stream.*

final case class Node[V <: Value] private (
    var nLocals: Int,
    var nStack: Int,
    successors: mutable.Set[Node[V]]
) extends Frame[V](nLocals, nStack) {}

object Node {
  def apply[V <: Value](
      nLocals: Int,
      nStack: Int
  ): Node[V] = Node(nLocals, nStack, mutable.Set.empty)

  def fromFrame[V <: Value](
      frame: Frame[V]
  ): Node[V] = {
    val node = Node[V](
      frame.getLocals,
      frame.getMaxStackSize
    ) // TODO: test, not sure this is correct
    node.init(frame)
    node
  }
}
