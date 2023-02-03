package com.my.sandbox.wallet.kafka

import com.my.sandbox.model.Events.UserTransactionEvent
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

class UserTransactionProducer extends LazyLogging{

  private val kafkaProducerProps: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    props
  }
  private val producer = new KafkaProducer[String, String](kafkaProducerProps)


  def sendEvent(userTransaction: UserTransactionEvent) = {
    val record = new ProducerRecord[String, String]("users-transactions", userTransaction.uuid, userTransaction.asJson.noSpaces)
    val sent = producer.send(record, new compareProducerCallback)
    producer.flush()
    sent
  }

  private class compareProducerCallback extends Callback {
    @Override
    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
      if (exception != null) {
        logger.error("Error happen when UserTransaction was published")
      }
    }
  }
}
