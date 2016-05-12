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
  private val description = s"created with fromLiftPort: '${fromLiftPort.fqn(divId)}', toLiftPort: '${toLiftPort.fold("N/A")(_.fqn(divId))}'"

  if (debug) log(description)
  if (debug) log("\n" + generateBridge.toString())

  override def render = R(renderBridge, renderCallback).render

  //TODO: should have error handling around toJson
  def sendToElm(portMessage: PortMessage): JsCmd = {
    val json = toJson(portMessage)
    if (debug) log(s"sendToElm: " + describe(portMessage))

    //TODO: should probably escape any ' that might occur in the log message ..
    JsRaw(
      s"""
         ${if (debug) Seq(loggerFunction, "log('receiveFromLift: ');").mkString("\n")}
         //log('receiveFromLift: ');
         //log('receiveFromLift: ${portMessage.typeName} -> ${portMessage.payload}');
         $embedVar.ports.${fromLiftPort.fqn(divId)}.send($json);
        """)
  }

  private def describe(portMessage: PortMessage) =
    s"${portMessage.typeName} -> ${portMessage.payload}"

  private def renderBridge = div(Some(divId), R(generateBridge))

  private def generateBridge =
    <script type="text/javascript">{
s"""
    ${loggerFunction}
    log('$description');

    var ${embedVar} = Elm.$elmModule.embed(document.getElementById('$divId'));
    ${sendToLiftSubscriber(toLiftPort)}
"""
    }</script>

  private def loggerFunction = s"""function log(message) { if ($debug) console.log('BELCH: [$divId] ' + message); }"""

  private def sendToLiftSubscriber(maybeToLiftPort: Option[ToLiftPort]) = maybeToLiftPort match {
    case Some(port) =>
s"""
    $embedVar.ports.${port.fqn(divId)}.subscribe(function(model) {
      log('sendToLift: ' + model['typeName']);
      //log('sendToLift: ' + model);
      //log('sendToLift: ');
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
      if (debug) log(s"receiveFromElm: " + describe(portMessage))
      toLiftPort.receiveFromElm(portMessage)
      Js.nothing
    } )._2.cmd
  )

  private def log(message: String) {
    if (debug) println(s"BELCH [$divId] $message")
  }

  //TIP: this was useful - https://fmpwizard.telegr.am/blog/textile-and-lift
}
