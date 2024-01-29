import cats.effect.{ExitCode, IO}
import http4scontrollers.FileController.allRoutes
import org.http4s.server.blaze.BlazeServerBuilder

object YourServer {
  def main(args: Array[String]): Unit = {
    val httpApp = allRoutes.orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
