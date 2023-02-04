package com.my.sandbox.wallet

import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.util.Timeout
import com.my.sandbox.model.Events.Deposit
import com.my.sandbox.wallet.actors.Supervisor
import com.my.sandbox.wallet.actors.Supervisor.MyCommand
import com.my.sandbox.wallet.actors.WalletsManager.{TransactionCmd, TransactionConfirm, TransactionFailed, WalletResp}
import com.my.sandbox.wallet.kafka.UserTransactionProducer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object WalletApp extends App with LazyLogging {
  val conf = ConfigFactory.load("application.conf").getConfig("myConf")

  val userTransactionProducer = new UserTransactionProducer()
  val supervisor = Supervisor(userTransactionProducer)
  val system = ActorSystem[MyCommand](supervisor, "WalletSupervisor", conf)

  import akka.actor.typed.scaladsl.AskPattern._
  import scala.concurrent.duration._

  implicit val timeout: Timeout = Timeout(2.seconds)
  implicit val scheduler: Scheduler = system.scheduler
  implicit val ec: ExecutionContext = system.executionContext

  private def cmd(replyTo: ActorRef[WalletResp]): TransactionCmd = TransactionCmd(UUID.randomUUID().toString, UUID.randomUUID().toString, 100.0, Deposit, LocalDateTime.now(), UUID.randomUUID().toString, replyTo)

  system.ask[WalletResp](replyTo => cmd(replyTo)).onComplete {
    case Success(value: TransactionConfirm) => logger.info(value.toString)
    case Success(value: TransactionFailed) => logger.info(value.toString)
    case Failure(exception) => logger.info("Unsuccessful transaction", exception)
  }

}
