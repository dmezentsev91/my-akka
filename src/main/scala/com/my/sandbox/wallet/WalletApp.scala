package com.my.sandbox.wallet

import akka.actor.typed.ActorSystem
import com.my.sandbox.model.Events.Deposit
import com.my.sandbox.wallet.actors.Supervisor
import com.my.sandbox.wallet.actors.Supervisor.MyCommand
import com.my.sandbox.wallet.actors.WalletsManager.TransactionCmd
import com.my.sandbox.wallet.kafka.UserTransactionProducer
import com.typesafe.config.ConfigFactory

import java.time.LocalDateTime
import java.util.UUID

object WalletApp extends App {
  val conf = ConfigFactory.load("application.conf").getConfig("myConf")

  val userTransactionProducer = new UserTransactionProducer()
  val supervisor = Supervisor(userTransactionProducer)
  val system = ActorSystem[MyCommand](supervisor, "WalletSupervisor", conf)
  system ! TransactionCmd(UUID.randomUUID().toString, UUID.randomUUID().toString, 100.0, Deposit, LocalDateTime.now(), UUID.randomUUID().toString)

}
