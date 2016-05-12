package im.mange.belch

case class ToLiftPort(receiveFromElm: (PortMessage) => Unit, name: String = "ToLift")


