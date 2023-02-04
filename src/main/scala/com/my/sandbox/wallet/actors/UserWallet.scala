package com.my.sandbox.wallet.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.my.sandbox.model.Events.{Deposit, TransactionType, UserTransactionKE, Withdrawal}
import com.my.sandbox.wallet.actors.WalletsManager.{TransactionCmd, TransactionConfirm, TransactionFailed, WalletCmd}
import com.my.sandbox.wallet.kafka.UserTransactionProducer
import com.typesafe.scalalogging.LazyLogging

import java.time.LocalDateTime

object UserWallet extends LazyLogging {

  trait UserWalletEvent

  case class TransactionAE(uuid: String, userUUID: String, value: BigDecimal, transactionType: TransactionType, time: LocalDateTime, invoiceUUID: String) extends UserWalletEvent

  case class UserWalletState(userId: String, balance: BigDecimal)


  def apply(userId: String, userTransactionProducer: UserTransactionProducer): Behavior[WalletCmd] = {
    EventSourcedBehavior[WalletCmd, UserWalletEvent, UserWalletState](
      persistenceId = PersistenceId.ofUniqueId(userId),
      emptyState = UserWalletState(userId, 0.0), // unused
      commandHandler = walletCommandHandler(userTransactionProducer),
      eventHandler = walletEventHandler)
  }

  def walletCommandHandler(userTransactionProducer: UserTransactionProducer)(state: UserWalletState, command: WalletCmd): Effect[UserWalletEvent, UserWalletState] = {

    command match {
      case transactionCmd: TransactionCmd if transactionCmd.transactionType == Withdrawal && state.balance < transactionCmd.value =>
        logger.info(s"Insufficient funds for transaction: $transactionCmd")
        Effect.none.thenReply(transactionCmd.replyTo)((state: UserWalletState) => TransactionFailed(s"Insufficient funds. Current balance: ${state.balance}"))

      case transactionCmd: TransactionCmd =>
        logger.info(s"Transaction: $transactionCmd")
        Effect.persist(transactionCmd.toAkkaEvent)
          .thenRun((_: UserWalletState) => userTransactionProducer.sendEvent(transactionCmd.toKafkaEvent))
          .thenReply(transactionCmd.replyTo)(state => TransactionConfirm(transactionCmd.uuid, state.balance))
    }
  }

  def walletEventHandler(state: UserWalletState, userWalletEvent: UserWalletEvent): UserWalletState = {
    userWalletEvent match {
      case transaction: TransactionAE if transaction.transactionType == Deposit =>
        state.copy(balance = state.balance + transaction.value)
      case transaction: TransactionAE if transaction.transactionType == Withdrawal =>
        state.copy(balance = state.balance - transaction.value)
    }
  }

}