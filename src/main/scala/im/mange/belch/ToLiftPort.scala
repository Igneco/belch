package im.mange.belch

case class ToLiftPort(receiveFromElm: (PortMessage) => Unit, private val name: String = "ToLift") {
  def fqn(base: String) = s"$base$name"
}


