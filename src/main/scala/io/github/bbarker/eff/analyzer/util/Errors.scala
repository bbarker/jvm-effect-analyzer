package io.github.bbarker.eff.analyzer.util

import org.objectweb.asm.tree.analysis.AnalyzerException
import zio.*

object Errors {
  def analyzerException(ex: Throwable): AnalyzerException = ex match {
    case e: AnalyzerException => e
  }
}
