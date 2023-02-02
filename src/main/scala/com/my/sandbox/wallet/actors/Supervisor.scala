package com.my.sandbox.wallet.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import com.my.sandbox.wallet.actors.Supervisor.MyCommand
import com.my.sandbox.wallet.actors.WalletsManager.WalletCmd
import com.my.sandbox.wallet.kafka.UserTransactionProducer
import com.typesafe.scalalogging.LazyLogging

object Supervisor {
  def apply(userTransactionProducer: UserTransactionProducer): Behavior[MyCommand] = {
    Behaviors.setup[MyCommand] { context =>
      val walletsManager = context.spawn(WalletsManager(userTransactionProducer), "wallet-manager")
      new Supervisor(context, walletsManager)
    }
  }

  trait MyCommand
}

class Supervisor(context: ActorContext[MyCommand], walletActor: ActorRef[WalletCmd]) extends AbstractBehavior[MyCommand](context) with LazyLogging {
  logger.info("WalletSupervisor started")

  override def onMessage(msg: MyCommand): Behavior[MyCommand] = msg match {
    case walletCmd: WalletCmd =>
      walletActor ! walletCmd
      Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[MyCommand]] = {
    case PostStop =>
      logger.info("Wallet Application stopped")
      this
  }
}
