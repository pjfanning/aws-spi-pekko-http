package com.github.pjfanning.pekkohttpspi

import java.util.Optional

object OptionConverters {
  final inline def toScala[A](o: Optional[A]): Option[A] = scala.jdk.javaapi.OptionConverters.toScala(o)
}
