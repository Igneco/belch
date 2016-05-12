package im.mange.belch

case class ToElmPort(private val name: String = "FromLiftToElm") {
  def fqn(base: String) = s"$base$name"
}
