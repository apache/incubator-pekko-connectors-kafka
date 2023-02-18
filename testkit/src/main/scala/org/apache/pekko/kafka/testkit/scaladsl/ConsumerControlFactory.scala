/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2014 - 2016 Softwaremill <https://softwaremill.com>
 * Copyright (C) 2016 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.kafka.testkit.scaladsl

import org.apache.pekko.Done
import org.apache.pekko.annotation.ApiMayChange
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.stream.scaladsl.{ Flow, Keep, Source }
import org.apache.pekko.stream.{ KillSwitch, KillSwitches }
import org.apache.kafka.common.{ Metric, MetricName }

import scala.concurrent.{ Future, Promise }

/**
 * Helper factory to create [[org.apache.pekko.kafka.scaladsl.Consumer.Control]] instances when
 * testing without a Kafka broker.
 */
@ApiMayChange
object ConsumerControlFactory {

  def attachControl[A, B](source: Source[A, B]): Source[A, Consumer.Control] =
    source
      .viaMat(controlFlow())(Keep.right)

  def controlFlow[A](): Flow[A, A, Consumer.Control] =
    Flow[A]
      .viaMat(KillSwitches.single[A])(Keep.right)
      .mapMaterializedValue(killSwitch => control(killSwitch))

  def control(killSwitch: KillSwitch): Consumer.Control = new FakeControl(killSwitch)

  class FakeControl(val killSwitch: KillSwitch) extends Consumer.Control {

    val shutdownPromise = Promise[Done]()

    override def stop(): Future[Done] = {
      killSwitch.shutdown()
      shutdownPromise.trySuccess(Done)
      shutdownPromise.future
    }

    override def shutdown(): Future[Done] = {
      killSwitch.shutdown()
      shutdownPromise.trySuccess(Done)
      shutdownPromise.future
    }

    override def isShutdown: Future[Done] = shutdownPromise.future

    override def metrics: Future[Map[MetricName, Metric]] = ???
  }

}
