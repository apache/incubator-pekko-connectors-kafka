/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2014 - 2016 Softwaremill <https://softwaremill.com>
 * Copyright (C) 2016 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.kafka.scaladsl

import java.util.concurrent.atomic.AtomicBoolean

import org.apache.pekko
import pekko.Done
import pekko.kafka.scaladsl.Consumer.DrainingControl
import pekko.kafka.tests.scaladsl.LogCapturing
import org.apache.kafka.common.{ Metric, MetricName }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ControlSpec {
  class ControlImpl(stopFuture: Future[Done] = Future.successful(Done),
      shutdownFuture: Future[Done] = Future.successful(Done))
      extends Consumer.Control {
    val shutdownCalled = new AtomicBoolean(false)

    override def stop(): Future[Done] = stopFuture
    override def shutdown(): Future[Done] = {
      shutdownCalled.set(true)
      shutdownFuture
    }
    override def isShutdown: Future[Done] = ???
    override def metrics: Future[Map[MetricName, Metric]] = ???
  }
}

class ControlSpec extends AnyWordSpec with ScalaFutures with Matchers with LogCapturing {
  import ControlSpec._

  "Control" should {
    "drain to stream result" in {
      val control = new ControlImpl
      val drainingControl = DrainingControl.apply(control, Future.successful("expected"))
      drainingControl.drainAndShutdown().futureValue should be("expected")
      control.shutdownCalled.get() should be(true)
    }

    "drain to stream failure" in {
      val control = new ControlImpl

      val drainingControl = DrainingControl.apply(control, Future.failed(new RuntimeException("expected")))
      val value = drainingControl.drainAndShutdown().failed.futureValue
      value shouldBe a[RuntimeException]
      value.getMessage should be("expected")
      control.shutdownCalled.get() should be(true)
    }

    "drain to stream failure even if shutdown fails" in {
      val control = new ControlImpl(shutdownFuture = Future.failed(new RuntimeException("not this")))

      val drainingControl = DrainingControl.apply(control, Future.failed(new RuntimeException("expected")))
      val value = drainingControl.drainAndShutdown().failed.futureValue
      value shouldBe a[RuntimeException]
      value.getMessage should be("expected")
      control.shutdownCalled.get() should be(true)
    }

    "drain to shutdown failure when stream succeeds" in {
      val control = new ControlImpl(shutdownFuture = Future.failed(new RuntimeException("expected")))

      val drainingControl = DrainingControl.apply(control, Future.successful(Done))
      val value = drainingControl.drainAndShutdown().failed.futureValue
      value shouldBe a[RuntimeException]
      value.getMessage should be("expected")
      control.shutdownCalled.get() should be(true)
    }
  }
}
