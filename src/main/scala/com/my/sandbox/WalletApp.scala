package com.my.sandbox

import akka.actor.typed.ActorSystem
import com.my.sandbox.Supervisor.MyCommand
import com.my.sandbox.WalletsManager.{Deposit, TransactionCmd}
import com.typesafe.config.ConfigFactory

import java.time.LocalDateTime
import java.util.UUID

object WalletApp extends App {
  val conf = ConfigFactory.load("application.conf").getConfig("myConf")

  private val supervisor = Supervisor()
  private val system = ActorSystem[MyCommand](supervisor, "WalletSupervisor", conf)
  system ! TransactionCmd(UUID.randomUUID().toString, UUID.randomUUID().toString, 100.0, Deposit, LocalDateTime.now(), UUID.randomUUID().toString)

}
