package im.mange.belch

case class FromLiftPort(private val name: String = "FromLift") {
  def fqn(base: String) = s"$base$name"
}
