package io.github.bbarker.eff.analyzer.config

import com.typesafe.config.ConfigFactory
import io.github.bbarker.eff.analyzer.config.Configuration.ServerConfig
import zio.*
import zio.config.ConfigDescriptor.*
import zio.config.ConfigSource.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigSource

object Configuration:

  final case class ServerConfig(port: Int)

  object ServerConfig:

    private val serverConfigDescription =
      nested("server-config") {
        int("port").default(8090)
      }.to[ServerConfig]

    val layer = ZLayer(
      read(
        serverConfigDescription.from(
          TypesafeConfigSource.fromTypesafeConfig(
            ZIO.attempt(ConfigFactory.defaultApplication())
          )
        )
      )
    )
