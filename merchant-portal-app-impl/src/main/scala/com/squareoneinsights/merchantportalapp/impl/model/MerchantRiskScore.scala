package com.squareoneinsights.merchantportalapp.impl.model

import java.time.LocalDateTime

case class MerchantRiskScore(requestId: Int,
                             merchantId: String,
                             oldSliderPosition: String,
                             updatedSliderPosition: String,
                             approvalFlag: String,
                             updateTimestamp: LocalDateTime)
