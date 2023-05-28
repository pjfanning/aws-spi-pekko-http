package com.github.pjfanning.pekkohttpspi

import java.util.concurrent.CompletionStage
import scala.concurrent.Future

private[pekkohttpspi] object FutureConverters {
  @inline final def asJava[T](f: Future[T]): CompletionStage[T] = scala.compat.java8.FutureConverters.toJava(f)

}
