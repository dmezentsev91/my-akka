package com.my.sandbox

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.my.sandbox.Supervisor.MyCommand
import com.my.sandbox.WalletsManager.WalletCmd
import com.typesafe.scalalogging.LazyLogging

object Supervisor {
  def apply(): Behavior[MyCommand] = {
    Behaviors.setup[MyCommand] { context =>
      val walletsManager = context.spawn(WalletsManager(), "wallet-manager")
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
