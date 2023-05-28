package com.github.pjfanning.pekkohttpspi

import java.util.concurrent.CompletionStage
import scala.concurrent.Future
import scala.jdk.javaapi

private[pekkohttpspi] object FutureConverters {
  @inline final def asJava[T](f: Future[T]): CompletionStage[T] = javaapi.FutureConverters.asJava(f)
}
