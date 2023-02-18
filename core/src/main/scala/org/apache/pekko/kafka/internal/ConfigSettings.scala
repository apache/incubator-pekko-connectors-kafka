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

package org.apache.pekko.kafka.internal

import java.util

import org.apache.pekko.annotation.InternalApi
import com.typesafe.config.{ Config, ConfigObject }

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.Duration
import org.apache.pekko.util.JavaDurationConverters._

/**
 * INTERNAL API
 *
 * Converts a [[com.typesafe.config.Config]] section to a Map for use with Kafka Consumer or Producer.
 */
@InternalApi private[kafka] object ConfigSettings {

  def parseKafkaClientsProperties(config: Config): Map[String, String] = {
    @tailrec
    def collectKeys(c: ConfigObject, processedKeys: Set[String], unprocessedKeys: List[String]): Set[String] =
      if (unprocessedKeys.isEmpty) processedKeys
      else {
        c.toConfig.getAnyRef(unprocessedKeys.head) match {
          case o: util.Map[_, _] =>
            collectKeys(c,
              processedKeys,
              unprocessedKeys.tail ::: o.keySet().asScala.toList.map(unprocessedKeys.head + "." + _))
          case _ =>
            collectKeys(c, processedKeys + unprocessedKeys.head, unprocessedKeys.tail)
        }
      }

    val keys = collectKeys(config.root, Set.empty[String], config.root().keySet().asScala.toList)
    keys.map(key => key -> config.getString(key)).toMap
  }

  def getPotentiallyInfiniteDuration(underlying: Config, path: String): Duration = underlying.getString(path) match {
    case "infinite" => Duration.Inf
    case _          => underlying.getDuration(path).asScala
  }

}
