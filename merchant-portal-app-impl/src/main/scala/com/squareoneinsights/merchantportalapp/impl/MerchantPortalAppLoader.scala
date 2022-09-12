package com.squareoneinsights.merchantportalapp.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.squareoneinsights.merchantportalapp.api.MerchantportalappService
import com.squareoneinsights.merchantportalapp.impl.kafkaService.{KafkaConsumeService, KafkaProduceService}
import com.squareoneinsights.merchantportalapp.impl.repository.{BusinessImpactRepo, MerchantRiskScoreDetailRepo}
import com.squareoneinsights.merchantportalapp.impl.service.AddRiskToRedis
import com.typesafe.config.ConfigFactory
import play.api.db.HikariCPComponents
import play.api.db.evolutions.EvolutionsComponents
import play.api.libs.ws.ahc.AhcWSComponents
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class MerchantportalappLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MerchantportalappApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MerchantportalappApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[MerchantportalappService])
}

abstract class MerchantportalappApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents
    with SlickPersistenceComponents
    with JdbcPersistenceComponents
    with EvolutionsComponents
    with HikariCPComponents {

  lazy val merchantConfig = ConfigFactory.load()
  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[MerchantportalappService](wire[MerchantPortalAppServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry  = MerchantPortalAppSerializerRegistry

  lazy val addRiskToRedis = wire[AddRiskToRedis]
  lazy val businessImpactRepo = wire[BusinessImpactRepo]
  lazy val merchantRiskScoreDetailRepo = wire[MerchantRiskScoreDetailRepo]
  lazy val kafkaConsumeService = wire[KafkaConsumeService]
  lazy val kafkaProduceService = wire[KafkaProduceService]

  val dbProfile = merchantConfig.getString("merchant.db.profile")
  lazy val dbConfig = DatabaseConfig.forConfig[JdbcProfile](dbProfile)

}
