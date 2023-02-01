package com.my.sandbox

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.my.sandbox.Supervisor.MyCommand
import com.my.sandbox.WalletsManager.{TransactionCmd, WalletCmd}
import com.typesafe.scalalogging.LazyLogging

import java.time.LocalDateTime
import scala.collection.mutable

object WalletsManager {
  def apply(): Behavior[WalletCmd] = {
    //    ActorSystem[WalletCmd](IotWalletsManager(), "iot-system")
    Behaviors.setup[WalletCmd](context => new WalletsManager(context))
  }

  trait WalletCmd extends MyCommand

  trait TransactionType

  case object Deposit extends TransactionType

  case object Withdrawal extends TransactionType

  case class TransactionCmd(uuid: String, userUUID: String, value: BigDecimal, transactionType: TransactionType, time: LocalDateTime, invoiceUUID: String) extends WalletCmd
  
}

class WalletsManager(context: ActorContext[WalletCmd]) extends AbstractBehavior[WalletCmd](context) with LazyLogging {
  logger.info("WalletWalletsManager started")
  
  val userWalletsMap = mutable.Map[String, ActorRef[WalletCmd]]()

  override def onMessage(msg: WalletCmd): Behavior[WalletCmd] = msg match {
    case transactionCmd: TransactionCmd =>
      userWalletsMap.get(transactionCmd.userUUID) match {
        case Some(userActor) => userActor ! transactionCmd
        case None =>
          val userActor = context.spawn(UserWallet(), s"user-wallet-${transactionCmd.userUUID}")
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


