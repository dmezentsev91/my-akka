package com.my.sandbox.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

import java.time.LocalDateTime

object Events {

  sealed trait TransactionType

  case object Deposit extends TransactionType

  case object Withdrawal extends TransactionType

  object TransactionType {
    implicit val modeCodec: Codec[TransactionType] = deriveEnumerationCodec[TransactionType]
  }


  case class UserTransactionKE(uuid: String, userUUID: String, value: BigDecimal, transactionType: TransactionType, time: LocalDateTime, invoiceUUID: String)

  object UserTransactionKE {

    implicit val decoder: Codec[UserTransactionKE] = deriveCodec[UserTransactionKE]
  }
}
