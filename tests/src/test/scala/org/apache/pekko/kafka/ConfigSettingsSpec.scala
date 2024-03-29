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

package org.apache.pekko.kafka

import org.apache.pekko
import pekko.kafka.internal.ConfigSettings
import pekko.kafka.tests.scaladsl.LogCapturing
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConfigSettingsSpec extends AnyWordSpec with Matchers with LogCapturing {

  "ConfigSettings" must {

    "handle nested properties" in {
      val conf = ConfigFactory
        .parseString(
          """
        kafka-client.bootstrap.servers = "localhost:9092"
        kafka-client.bootstrap.foo = baz
        kafka-client.foo = bar
        kafka-client.client.id = client1
        """)
        .withFallback(ConfigFactory.load())
        .getConfig("kafka-client")
      val settings = ConfigSettings.parseKafkaClientsProperties(conf)
      settings("bootstrap.servers") should ===("localhost:9092")
      settings("client.id") should ===("client1")
      settings("foo") should ===("bar")
      settings("bootstrap.foo") should ===("baz")
    }
  }
}
