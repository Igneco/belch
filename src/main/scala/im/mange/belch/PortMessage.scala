package im.mange.belch

object PortMessage {
  def apply(x: Any, payload: String): PortMessage = PortMessage(x.getClass.getSimpleName, payload)
}

case class PortMessage(typeName: String, payload: String)
