package io.github.bbarker.eff.analyzer

import java.util.UUID
import scala.collection.mutable

import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import zio.*
import zio.config.*
import zio.stream.*

final case class Node[V <: Value] private (
    var nLocals: Int,
    var nStack: Int,
    successors: mutable.Set[Node[V]],
    nodeUUID: UUID = java.util.UUID.randomUUID()
) extends Frame[V](nLocals, nStack):
  println(s"New Node: ${this.hashCode}")

object Node {
  def apply[V <: Value](
      nLocals: Int,
      nStack: Int
  ): Node[V] =
    println("New Node: apply, hash")
    Node(nLocals, nStack, mutable.Set.empty)

  def fromFrame[V <: Value](
      frame: Frame[V]
  ): Node[V] = {
    val node = Node[V](
      frame.getLocals,
      frame.getMaxStackSize
    ) // TODO: test, not sure this is correct
    println("New Node: fromFrame")
    node.init(frame)
    node
  }
}
