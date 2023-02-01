package com.my.sandbox

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.my.sandbox.WalletsManager.{Deposit, TransactionCmd, WalletCmd, Withdrawal}
import com.typesafe.scalalogging.LazyLogging

object UserWallet {
  def apply(): Behavior[WalletCmd] = {
    Behaviors.setup[WalletCmd](context => new UserWallet(context, 0.0))
  }

}

class UserWallet(context: ActorContext[WalletCmd], balance: BigDecimal) extends AbstractBehavior[WalletCmd](context) with LazyLogging {
  logger.info("UserWallet started")

  override def onMessage(msg: WalletCmd): Behavior[WalletCmd] = msg match {
    case transactionCmd: TransactionCmd =>
      logger.info(s"Transaction: $transactionCmd")
      val updatedWallet = transactionCmd.transactionType match {
        case Deposit =>
          new UserWallet(context, balance + transactionCmd.value)
        case Withdrawal if transactionCmd.value <= balance =>
          new UserWallet(context, balance - transactionCmd.value)
        case Withdrawal if transactionCmd.value > balance =>
          //reject
          this
      }

      Behaviors.setup[WalletCmd](context => updatedWallet)
  }

  override def onSignal: PartialFunction[Signal, Behavior[WalletCmd]] = {
    case PostStop =>
      logger.info("IoT Application stopped")
      this
  }

}