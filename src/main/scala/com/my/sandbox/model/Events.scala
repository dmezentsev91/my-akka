package com.my.sandbox.model

import com.my.sandbox.wallet.WalletsManager.TransactionType
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.LocalDateTime

object Events {

  case class UserTransactionEvent(uuid: String, userUUID: String, value: BigDecimal, transactionType: TransactionType, time: LocalDateTime, invoiceUUID: String)

  object UserTransactionEvent {

    implicit val decoder: Codec[UserTransactionEvent] = deriveCodec[UserTransactionEvent]
  }
}
