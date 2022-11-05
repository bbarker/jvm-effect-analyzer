package io.github.bbarker.eff.analyzer

import java.util.UUID
import scala.collection.mutable

import io.github.bbarker.eff.analyzer.CallGraph.*
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import zio.*
import zio.config.*
import zio.stream.*

final case class Node[V <: Value] private (
    var nLocals: Int,
    var nStack: Int,
    var callGraphNode: Option[CGNode],
    successors: mutable.Set[Node[V]]
) extends Frame[V](nLocals, nStack):

  /** A fun side-note is that most hashCode functions are based on the object's
    * identity. Thus the constructors becomes impure when such a hash code is
    * employed, while the hashCode method itself remains pure.
    */
  override def hashCode(): RuntimeFlags = super.hashCode()

object Node {
  def apply[V <: Value](
      nLocals: Int,
      nStack: Int
  ): Node[V] = Node(nLocals, nStack, None, mutable.Set.empty)

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
