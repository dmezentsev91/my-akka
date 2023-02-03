package com.my.sandbox.wallet.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import com.my.sandbox.model.Events.{Deposit, UserTransactionEvent, Withdrawal}
import com.my.sandbox.wallet.actors.WalletsManager.{TransactionCmd, WalletCmd}
import com.my.sandbox.wallet.kafka.UserTransactionProducer
import com.typesafe.scalalogging.LazyLogging

object UserWallet {
  def apply(userTransactionProducer: UserTransactionProducer): Behavior[WalletCmd] = {
    Behaviors.setup[WalletCmd](context => new UserWallet(context, 0.0)(userTransactionProducer))
  }

}

class UserWallet(context: ActorContext[WalletCmd], balance: BigDecimal)
                (implicit userTransactionProducer: UserTransactionProducer) extends AbstractBehavior[WalletCmd](context) with LazyLogging {
  logger.info("UserWallet started")

  override def onMessage(msg: WalletCmd): Behavior[WalletCmd] = msg match {
    case transactionCmd: TransactionCmd =>
      logger.info(s"Transaction: $transactionCmd")
      val updatedWallet = transactionCmd.transactionType match {
        case Deposit =>
          userTransactionProducer.sendEvent(transactionCmd.toEvent)
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