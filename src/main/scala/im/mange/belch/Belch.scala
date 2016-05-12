package im.mange.belch

import im.mange.belch.PortMessageJson._
import im.mange.jetpac.{Html, Js, R, Renderable}
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{Function, Script, _}
import net.liftweb.http.js.{JE, JsCmd}
import Html._

//TODO: (later) make messagesToElm port be optional
//TODO: (later) support multiple ports (in each direction)
case class Belch(divId: String, elmModule: String,
                 fromElmPort: Option[FromElmPort] = None,
                 toElmPort: ToElmPort = ToElmPort(),
                 debug: Boolean = false) extends Renderable {

  private val embedVar = s"_${divId}".replaceAll("-", "_").replaceAll("\\.", "_")
  private val embedCallbackMethod = s"${embedVar}_callback" //TODO: ultimately should include the fromElmPort.name
  private val description = s"toElm: [${toElmPort.name}], fromElm: [${fromElmPort.fold("N/A")(_.name)}]"

  if (debug) log(description)
  if (debug) log("\n" + generateBridge.toString())

  override def render = R(renderBridge, renderCallback).render

  def sendMessageToElm(portMessage: PortMessage): JsCmd = {
    val portMessageJson = toJson(portMessage)
    if (debug) log(s"sendMessageToElm ${toElmPort.name}: ${portMessage.typeName} -> ${portMessage.payload} = $portMessageJson")

    JsRaw(s"$embedVar.ports.${toElmPort.name}.send(" + portMessageJson + ");")
  }

  private def renderBridge = div(Some(divId), R(generateBridge))

  private def generateBridge =
    <script type="text/javascript">{
    s"""
    function log(message) { if ($debug) console.log('BELCH: $embedVar -> ' + message); }
    log('$description');

    var ${embedVar} = Elm.$elmModule.embed(document.getElementById('$divId'));
    ${messagesFromElmSubscriber(fromElmPort)}
    """
    }</script>

  private def messagesFromElmSubscriber(maybeFromElmPort: Option[FromElmPort]) = maybeFromElmPort match {
    case Some(port) =>
      s"""
      $embedVar.ports.${port.name}.subscribe(function(model) {
        var portMessage = JSON.stringify(model);
        log('subscribe receiveMessageFromElm: ' + portMessage);
        $embedCallbackMethod(portMessage);
      });
        """
    case None => ""
  }

  private def renderCallback = fromElmPort match {
    case Some(outgoingPort) => R(Script(receiveCallbackFromElm(outgoingPort)))
    case None => R()
  }

  private def receiveCallbackFromElm(fromElmPort: FromElmPort) = Function(embedCallbackMethod, List("portMessage"),
    SHtml.ajaxCall(JE.JsRaw("""portMessage"""), (json: String) => {
      if (debug) log(s"ajaxCallback ${fromElmPort.name} raw <- $json")
      fromElmPort.onMessageFromElm(fromJson(json))
      Js.nothing
    } )._2.cmd
  )

  private def log(message: String): Unit = {
    if (debug) println(s"BELCH $embedVar -> $message")
  }

  //TIP: this was useful - https://fmpwizard.telegr.am/blog/textile-and-lift
}
