package im.mange.belch

object PortMessageJson {
  import net.liftweb.json._
  import net.liftweb.json.Serialization._
  import net.liftweb.json.{NoTypeHints, Serialization}

  implicit val formats = Serialization.formats(NoTypeHints)

  def fromJson(value: String) = parse(value).extract[PortMessage]
  def toJson(value: PortMessage) = write(value)(formats)
}
