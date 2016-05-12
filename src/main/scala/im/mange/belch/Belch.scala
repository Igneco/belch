package im.mange.belch

import im.mange.belch.PortMessageJson._
import im.mange.jetpac.{Html, Js, R, Renderable}
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Function, Script, _}
import net.liftweb.http.js.{JE, JsCmd}
import Html._

//TIP: BasicElmLiftCometHelper
case class Belch(divId: String, elmModule: String,
                 toLiftPort: Option[ToLiftPort] = None,
                 fromLiftPort: FromLiftPort = FromLiftPort(),
                 debug: Boolean = false) extends Renderable {

  private val embedVar = s"_${divId}".replaceAll("-", "_").replaceAll("\\.", "_")
  private val embedCallbackMethod = s"${embedVar}Callback"
  private val description = s"fromLiftPort: [${fromLiftPort.fqn(divId)}], toLiftPort: [${toLiftPort.fold("N/A")(_.fqn(divId))}]"

  if (debug) log(description)
  if (debug) log("\n" + generateBridge.toString())

  override def render = R(renderBridge, renderCallback).render

  def sendToElm(portMessage: PortMessage): JsCmd = {
    val portMessageJson = toJson(portMessage)
    if (debug) log(s"sendToElm ${fromLiftPort.fqn(divId)}: ${portMessage.typeName} -> ${portMessage.payload} = $portMessageJson")

    JsRaw(s"$embedVar.ports.${fromLiftPort.fqn(divId)}.send(" + portMessageJson + ");")
  }

  private def renderBridge = div(Some(divId), R(generateBridge))

  private def generateBridge =
    <script type="text/javascript">{
s"""
    function log(message) { if ($debug) console.log('BELCH: $embedVar -> ' + message); }
    log('$description');

    var ${embedVar} = Elm.$elmModule.embed(document.getElementById('$divId'));
    ${receiveFromElmSubscriber(toLiftPort)}
"""
    }</script>

  private def receiveFromElmSubscriber(maybeToLiftPort: Option[ToLiftPort]) = maybeToLiftPort match {
    case Some(port) =>
s"""
    $embedVar.ports.${port.fqn(divId)}.subscribe(function(model) {
      var portMessage = JSON.stringify(model);
      log('subscribe receiveMessageFromElm: ' + portMessage);
      $embedCallbackMethod(portMessage);
    });"""
    case None => ""
  }

  private def renderCallback = toLiftPort match {
    case Some(outgoingPort) => R(Script(receiveFromElmCallback(outgoingPort)))
    case None => R()
  }

  private def receiveFromElmCallback(toLiftPort: ToLiftPort) = Function(embedCallbackMethod, List("portMessage"),
    SHtml.ajaxCall(JE.JsRaw("""portMessage"""), (json: String) => {
      if (debug) log(s"ajaxCallback ${toLiftPort.fqn(divId)} raw <- $json")
      toLiftPort.receiveFromElm(fromJson(json))
      Js.nothing
    } )._2.cmd
  )

  private def log(message: String) {
    if (debug) println(s"BELCH $divId -> $message")
  }

  //TIP: this was useful - https://fmpwizard.telegr.am/blog/textile-and-lift
}
