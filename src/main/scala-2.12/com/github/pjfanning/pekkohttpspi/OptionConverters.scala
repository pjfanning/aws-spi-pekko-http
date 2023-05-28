package com.github.pjfanning.pekkohttpspi

import java.util.Optional

private[pekkohttpspi] object OptionConverters {
  @inline final def toScala[A](o: Optional[A]): Option[A] = scala.compat.java8.OptionConverters.toScala(o)
}
