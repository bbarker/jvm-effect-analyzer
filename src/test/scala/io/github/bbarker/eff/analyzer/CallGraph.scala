package io.github.bbarker.eff.analyzer

object CallGraph:

  sealed trait CGIncompletenessError
  enum CGError:
    case CGIncomplete extends CGError with CGIncompletenessError

    /** Similar to CGIncomplete, but less expected.
      */
    case PathKeyNotFound extends CGError with CGIncompletenessError
  import CGError.*

  // TODO: may alias this to an ASM type instead
  opaque type JVMPath = String

  trait CGNode:
    val nodePath: JVMPath
    def callees: Set[JVMPath]
    def calleeNode(path: JVMPath): Either[CGError, CGNode]

  final case class CGMapImpl(
      nodePath: JVMPath,
      cgMap: Map[JVMPath, CGMapImpl],
      completed: Boolean
  ) extends CGNode:
    def callees: Set[JVMPath] = cgMap.keySet
    def calleeNode(path: JVMPath): Either[CGError, CGNode] = completed match {
      case true =>
        cgMap.get(path) match {
          case None         => Left(PathKeyNotFound)
          case Some(cgNode) => Right(cgNode)
        }
      case false => Left(CGIncomplete)
    }
