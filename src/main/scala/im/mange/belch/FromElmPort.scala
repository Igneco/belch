package im.mange.belch

case class FromElmPort(onMessageFromElm: (PortMessage) => Unit, name: String = "messagesFromElm")


