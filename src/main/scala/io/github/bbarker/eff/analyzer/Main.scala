package io.github.bbarker.eff.analyzer


import zio.*
import zio.config.*
import zio.stream.*
import io.github.bbarker.eff.analyzer.config.Configuration.*

object Main extends ZIOAppDefault:


  val program = 
    for
      config <- getConfig[ServerConfig]
      _      <- Console.printLine(config)
    yield ()

  override val run = 
    program.provide(ServerConfig.layer)
