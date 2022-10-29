package io.github.bbarker.eff.analyzer.util

object ClassPath {
  def asmClassFile(className: String): String =
    className.replace('.', '/') + ".class"

  def asmClassPath[T](cls: Class[T]): String = cls.getName
}
