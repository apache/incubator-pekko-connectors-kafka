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

package org.apache.pekko.kafka.tests.javadsl

import org.apache.pekko.kafka.tests.CapturingAppender
import org.junit.jupiter.api.extension.{ AfterTestExecutionCallback, BeforeTestExecutionCallback, ExtensionContext }

class LogCapturingExtension extends BeforeTestExecutionCallback with AfterTestExecutionCallback {

  // eager access of CapturingAppender to fail fast if misconfigured
  private val capturingAppender = CapturingAppender.get("")

  override def beforeTestExecution(context: ExtensionContext): Unit = {
    capturingAppender.clear()
  }

  override def afterTestExecution(context: ExtensionContext): Unit = {
    if (context.getExecutionException.isPresent) {
      val error = context.getExecutionException.get().toString
      val method =
        s"[${Console.BLUE}${context.getRequiredTestClass.getName}: ${context.getRequiredTestMethod.getName}${Console.RESET}]"
      System.out.println(
        s"--> $method Start of log messages of test that failed with $error")
      capturingAppender.flush()
      System.out.println(
        s"<-- $method End of log messages of test that failed with $error")
    }
  }
}
