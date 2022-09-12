package com.squareoneinsights.merchantportalapp.impl

import com.squareoneinsights.merchantportalapp.api
import com.squareoneinsights.merchantportalapp.api.MerchantportalappService
import akka.Done
import akka.NotUsed
import cats.data.EitherT
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceCall}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.squareoneinsights.merchantportalapp.api.request.MerchantRiskScoreReq

import scala.concurrent.ExecutionContext
import com.squareoneinsights.merchantportalapp.api.response.MerchantRiskScoreResp
import com.squareoneinsights.merchantportalapp.impl.kafkaService.{KafkaConsumeService, KafkaProduceService}
import com.squareoneinsights.merchantportalapp.impl.repository.MerchantRiskScoreDetailRepo
import play.api.libs.json.Json

/**
  * Implementation of the MerchantportalappService.
  */
class MerchantPortalAppServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                                   merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
                                   kafkaProduceService: KafkaProduceService,
                                   kafkaConsumeService: KafkaConsumeService)
                                  (implicit ec: ExecutionContext)
  extends MerchantportalappService {

  override def getRiskScore(merchantId: String): ServiceCall[NotUsed, MerchantRiskScoreResp] =
   ServerServiceCall { _ =>
     merchantRiskScoreDetailRepo.fetchRiskScore(merchantId).map {
       case Left(err) => throw BadRequest(s"Error: ${err}")
       case Right(data) => data
     }
   }

  override def addRiskType: ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp] =
    ServerServiceCall { riskJson =>
      val resp = for {
        toRedis <- EitherT(merchantRiskScoreDetailRepo.insertRiskScore(riskJson))
        //toRdbms <- EitherT(addRiskToRedis.publishMerchantRiskType(riskJson.merchantId, riskJson.riskType))
        toKafka <- EitherT(kafkaProduceService.sendMessage(riskJson.merchantId, riskJson.updatedRisk))
      } yield(toKafka)
      resp.value.map {
        case Left(err) => throw new MatchError(err)
        case Right(_) => {
          val merchantRiskResp = MerchantRiskScoreResp.apply(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk, "approved")
          if (riskJson.updatedRisk == "High") merchantRiskResp.copy(approvalFlag = "pending") else merchantRiskResp
        }
      }
    }
}
