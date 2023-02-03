package com.my.sandbox.wallet.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import com.my.sandbox.model.Events.{TransactionType, UserTransactionEvent}
import com.my.sandbox.wallet.actors.Supervisor.MyCommand
import com.my.sandbox.wallet.actors.WalletsManager.{TransactionCmd, WalletCmd}
import com.my.sandbox.wallet.kafka.UserTransactionProducer
import com.typesafe.scalalogging.LazyLogging

import java.time.LocalDateTime
import scala.collection.mutable

object WalletsManager {
  def apply(userTransactionProducer: UserTransactionProducer): Behavior[WalletCmd] = {
    Behaviors.setup[WalletCmd](context => new WalletsManager(context, userTransactionProducer))
  }

  trait WalletCmd extends MyCommand


  case class TransactionCmd(uuid: String, userUUID: String, value: BigDecimal, transactionType: TransactionType, time: LocalDateTime, invoiceUUID: String) extends WalletCmd

  object TransactionCmd {
    implicit class TransactionOps(cmd: TransactionCmd) {

      import cmd._

      def toEvent = UserTransactionEvent(uuid, userUUID, value, transactionType, time, invoiceUUID)
    }
  }
}

class WalletsManager(context: ActorContext[WalletCmd], userTransactionProducer: UserTransactionProducer) extends AbstractBehavior[WalletCmd](context) with LazyLogging {
  logger.info("WalletWalletsManager started")

  val userWalletsMap = mutable.Map[String, ActorRef[WalletCmd]]()

  override def onMessage(msg: WalletCmd): Behavior[WalletCmd] = msg match {
    case transactionCmd: TransactionCmd =>
      userWalletsMap.get(transactionCmd.userUUID) match {
        case Some(userActor) => userActor ! transactionCmd
        case None =>
          val userActor = context.spawn(UserWallet(userTransactionProducer), s"user-wallet-${transactionCmd.userUUID}")
          userWalletsMap.put(transactionCmd.userUUID, userActor)
          userActor ! transactionCmd
      }
      Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[WalletCmd]] = {
    case PostStop =>
      logger.info("IoT Application stopped")
      this
  }
}


