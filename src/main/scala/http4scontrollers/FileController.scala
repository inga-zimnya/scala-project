package http4scontrollers

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import sttp.tapir.EndpointOutput.StatusCode
import sttp.tapir._
import sttp.tapir.server.http4s._

object FileController {
  private val fileStoragePath = "uploads"

  // Endpoint descriptions
  val uploadEndpoint: Endpoint[(String, String), Unit, Unit, Any] =
    endpoint.post
      .in("upload")
      .in(multipartBody[(String, String)]("file", "token"))
      .out(statusCode(StatusCode.Ok))
      .errorOut(statusCode(StatusCode.BadRequest))

  val downloadEndpoint: Endpoint[(String, String), String, Unit, Any] =
    endpoint.get
      .in("download" / path[String]("filename"))
      .in(query[String]("token"))
      .out(stringBody)
      .errorOut(statusCode(StatusCode.NotFound))
      .errorOut(statusCode(StatusCode.Forbidden))

  val generateTokenEndpoint: Endpoint[Unit, String, Unit, Any] =
    endpoint.get
      .in("generateToken")
      .out(stringBody)

  // Convert Tapir endpoints to Http4s routes
  val routes: HttpRoutes[IO] =
    uploadEndpoint.toRoutes { case (filename, token) => ??? /* Implement your logic here */ } <+>
      downloadEndpoint.toRoutes { case (filename, token) => ??? /* Implement your logic here */ } <+>
      generateTokenEndpoint.toRoutes(_ => ??? /* Implement your logic here */ ) <+>
      new SwaggerHttp4s(new SwaggerUI).routes[IO]

  // Combine your routes with the Swagger UI routes
  val allRoutes: HttpRoutes[IO] = routes

  // Optionally, expose the OpenAPI documentation
  val openAPI: String = List(uploadEndpoint, downloadEndpoint, generateTokenEndpoint)
    .toOpenAPI("Your API", "1.0")
    .toYaml
}
