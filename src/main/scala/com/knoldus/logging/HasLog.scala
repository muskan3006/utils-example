package com.knoldus.logging

import org.slf4j.{Logger, LoggerFactory}

trait HasLog {
  protected val log: Logger = LoggerFactory.getLogger(this.getClass)
}
