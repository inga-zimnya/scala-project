// src/main/scala/Server.scala
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import http4scontrollers.FileController

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(9000, "0.0.0.0")
      .withHttpApp(FileController.routes.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}