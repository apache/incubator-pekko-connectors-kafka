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

package org.apache.pekko.kafka.internal

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.kafka.scaladsl.Consumer._
import pekko.stream._
import pekko.stream.stage.{ GraphStageLogic, GraphStageWithMaterializedValue }

/**
 * INTERNAL API
 */
@InternalApi
private[kafka] abstract class KafkaSourceStage[K, V, Msg](stageName: String)
    extends GraphStageWithMaterializedValue[SourceShape[Msg], Control] {
  protected val out = Outlet[Msg]("out")
  val shape = new SourceShape(out)

  override protected def initialAttributes: Attributes = Attributes.name(stageName)

  protected def logic(shape: SourceShape[Msg]): GraphStageLogic with Control
  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes) = {
    val result = logic(shape)
    (result, result)
  }
}
