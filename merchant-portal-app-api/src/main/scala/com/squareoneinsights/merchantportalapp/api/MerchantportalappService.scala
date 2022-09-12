package com.squareoneinsights.merchantportalapp.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceAcl, ServiceCall}
import com.squareoneinsights.merchantportalapp.api.request.MerchantRiskScoreReq
import com.squareoneinsights.merchantportalapp.api.response.MerchantRiskScoreResp
import play.api.libs.json.{Format, Json}

trait MerchantportalappService extends Service {

  def getRiskScore(merchantId: String): ServiceCall[NotUsed, MerchantRiskScoreResp]

  def addRiskType : ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  override final def descriptor = {
    import Service._
    named("merchantportal-api")
      .withCalls(
        restCall(Method.GET, "/api/v1/merchantportal/risksetting/merchant/:merchantId",  getRiskScore _),
        restCall(Method.POST, "/api/v1/merchantportal/risksetting/merchant/:merchantId",  addRiskType)
      ).withAcls(
      ServiceAcl(pathRegex = Some("/api/v1/merchantportal/risksetting/"))
    )
  }
}