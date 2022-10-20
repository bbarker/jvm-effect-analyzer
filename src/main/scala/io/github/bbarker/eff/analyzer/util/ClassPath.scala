package io.github.bbarker.eff.analyzer.util

object ClassPath {
  def asmClassPath(className: String): String =
    className.replace('.', '/') + ".class"
}
