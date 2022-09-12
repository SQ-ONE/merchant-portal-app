package com.squareoneinsights.merchantportalapp.impl.repository

import com.squareoneinsights.merchantportalapp.impl.model.{BusinessImpactWithType, PaymentTypeDetail}
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile.api._
import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}

class BusinessImpactRepo(db: Database)(implicit ec: ExecutionContext) extends BusinessImpactTrait {

  //val db = Database.forConfig("db.default")
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val businessImpactTable  = TableQuery[BusinessImpactTable]

  def fetchBusinessDetail(merchantId: String, paymentType: String): Future[Either[String, BusinessImpactWithType]] = {
    val businessImpact = businessImpactTable.filter(col => (col.merchantId === merchantId && col.paymentType === paymentType))
    db.run(businessImpact.result).map { x =>
      Either.fromOption(x.headOption, s"No Business Impact found for merchantId: ${merchantId}")
    }
  }
}

trait BusinessImpactTrait {

  class BusinessImpactTable(tag: Tag) extends Table[BusinessImpactWithType](tag, "BUSINESS_IMPACT") {

    def * = (merchantId, paymentProjection, paymentType) <> ((BusinessImpactWithType.apply _).tupled, BusinessImpactWithType.unapply)

    def paymentProjection =
      (paymentBlock, paymentInReview, paymentAllow) <> ((PaymentTypeDetail.apply _).tupled, PaymentTypeDetail.unapply)

    def merchantId = column[String]("MERCHANT_ID", O.Unique)

    def paymentType = column[String]("PAYMENT_TYPE")

    def paymentBlock = column[Int]("PAYMENT_BLOCK")

    def paymentInReview = column[Int]("PAYMENT_IN_REVIEW")

    def paymentAllow = column[Int]("PAYMENT_ALLOW")
  }
}
