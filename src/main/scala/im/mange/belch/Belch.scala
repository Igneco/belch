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
                 messageDebug: Boolean = false,
                 bridgeDebug: Boolean = false) extends Renderable {

  private val embedVar = s"_${divId}".replaceAll("-", "_").replaceAll("\\.", "_")
  private val embedCallbackMethod = s"${embedVar}Callback"
  private val description = s"created with fromLiftPort: ${fromLiftPort.fqn(divId)}, toLiftPort: ${toLiftPort.fold("N/A")(_.fqn(divId))}"

  if (bridgeDebug) log(description)
  if (bridgeDebug) log("main:\n\n" + generateMain.toString + "\n")
  if (bridgeDebug) log("callback:\n" + generateCallback(PortMessage("typeName", "payload"), "json"))

  override def render = R(renderMain, renderCallback).render

  //TODO: should have error handling around toJson
  def sendToElm(portMessage: PortMessage): JsCmd = {
    if (messageDebug) log(s"sendToElm: " + describe(portMessage))
    JsRaw(generateCallback(portMessage, toJson(portMessage)))
  }

  private def generateCallback(portMessage: PortMessage, json: String) =
s"""
    ${if (messageDebug) Seq(generateLogger, s"    log('receiveFromLift: ${portMessage.typeName} -> ${portMessage.payload}');").mkString("\n")}
    $embedVar.ports.${fromLiftPort.fqn(divId)}.send($json);
"""

  private def describe(portMessage: PortMessage) = s"${portMessage.typeName} -> ${portMessage.payload}"

  private def renderMain = div(Some(divId), R(generateMain))

  private def generateMain =
    <script type="text/javascript">{
s"""
    ${if (messageDebug) generateLogger}
    ${if (messageDebug) "log('$description');"}
    var ${embedVar} = Elm.$elmModule.embed(document.getElementById('$divId'));
    ${sendToLiftSubscriber(toLiftPort)}
"""
    }</script>

  private def generateLogger = s"""function log(message) { if ($messageDebug) console.log('BELCH: [$divId] ' + JSON.parse(JSON.stringify(String(message)))); }"""

  private def sendToLiftSubscriber(maybeToLiftPort: Option[ToLiftPort]) = maybeToLiftPort match {
    case Some(port) =>
s"""
    $embedVar.ports.${port.fqn(divId)}.subscribe(function(model) {
      ${if (messageDebug) "log('sendToLift: ' + model['typeName'] + ' -> ' + model['payload'] );"}
      var portMessage = JSON.stringify(model);
      $embedCallbackMethod(portMessage);
    });"""
    case None => ""
  }

  private def renderCallback = toLiftPort match {
    case Some(outgoingPort) => R(Script(receiveFromElmCallback(outgoingPort)))
    case None => R()
  }

  //TODO: should have error handling around fromJson
  private def receiveFromElmCallback(toLiftPort: ToLiftPort) = Function(embedCallbackMethod, List("portMessage"),
    SHtml.ajaxCall(JE.JsRaw("""portMessage"""), (json: String) => {
      val portMessage = fromJson(json)
      if (messageDebug) log(s"receiveFromElm: " + describe(portMessage))
      toLiftPort.receiveFromElm(portMessage)
      Js.nothing
    } )._2.cmd
  )

  private def log(message: String) {
    println(s"BELCH [$divId] $message")
  }

  //TIP: this was useful - https://fmpwizard.telegr.am/blog/textile-and-lift
}
