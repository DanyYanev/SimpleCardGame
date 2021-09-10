import cats.effect.{ContextShift, IO, Timer}
import cats.implicits.{catsSyntaxEitherId, catsSyntaxFlatMapOps}
import com.typesafe.scalalogging.StrictLogging
import core.dto.{Request, Response}
import games.SingleCardGame
import org.http4s.HttpRoutes
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.{CodecFormat, endpoint, stringBody, webSocketBody}

import scala.concurrent.ExecutionContext

object Main extends StrictLogging {

  private val SingleGameEndpoint =
    endpoint
      .in("single-card")
      .errorOut(stringBody)
      .out(webSocketBody[Request, CodecFormat.Json, Response, CodecFormat.Json](Fs2Streams[IO]))

  private val singleGameServerEndpoint = SingleGameEndpoint.serverLogic(_ => SingleCardGame().map(_.asRight[String]))

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO] = IO.timer(ec)

  val singleGameServerRoutes: HttpRoutes[IO] = Http4sServerInterpreter.toRoutes(singleGameServerEndpoint)

  def main(args: Array[String]): Unit = {
    BlazeServerBuilder[IO](ec)
      .bindHttp(8080, "localhost")
      .withHttpApp(Router("/" -> singleGameServerRoutes).orNotFound)
      .resource
      .use { _ => IO(logger.info("Server started")) >> IO.never }
      .unsafeRunSync()
  }

}
