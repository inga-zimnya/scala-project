// Import necessary libraries
package http4scontrollers

import scala.concurrent.{ExecutionContext, Future}
import org.http4s.MediaType
import cats.effect.IO
import fs2.Stream
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.multipart.Multipart
import org.typelevel.ci._
import tokens.TokenQueryParamMatcher
import tokens.TokenQueryParamMatcher._
import database.DatabaseManager._

import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.file.{Files, Paths}
import scala.util.Random

object FileController {
  private val fileStoragePath = "uploads/"
  private var filename: String = _

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "upload" =>
      // Decode the request body as Multipart[IO].
      req.decode[Multipart[IO]] { m =>
        // Process each part of the multipart request.
        m.parts.collect {
          case part if part.filename.isDefined =>
            // Extract the filename from the file part.
            val filename = part.filename.get

            // Construct the file path using a combination of user-provided filename and a unique identifier.
            val uniqueIdentifier = generateRandomFileName()
            val filePath = s"$fileStoragePath$uniqueIdentifier-$filename"

            // Write the content of the file part to the specified file path.
            part.body
              .through(fs2.io.file.writeAll(Paths.get(filePath)))
              .compile
              .drain

            val token = generateRandomToken(filename)

            // Store file information in the database
            storeToken(filename, token, filePath)

            // Respond with a success message indicating the uploaded filename.
            Ok(s"File uploaded successfully: $filename")
        }.headOption.getOrElse(BadRequest("No file part found"))
      }

    case GET -> Root / "download" / filename :? TokenQueryParamMatcher(token) =>
      // Check token validity here
      val validityCheck: IO[Boolean] = IO.fromFuture(IO(isValidToken(token, filename, filePath)))

      val contentType = `Content-Type`(MediaType.application.`octet-stream`)

      validityCheck.flatMap { isValid =>
        if (isValid) {
          val filePath = s"$fileStoragePath$filename"
          if (Files.exists(Paths.get(filePath))) {
            val contentDisposition = `Content-Disposition`("attachment", Map(ci"name" -> filename))

            // Use fs2.io.file.readAll to create a Stream[IO, Byte] from the file
            val fileStream: Stream[IO, Byte] = fs2.io.file.readAll[IO](Paths.get(filePath), 4096)

            // Attach the stream to the response using `withEntity`
            Ok(fileStream).map(_.putHeaders(contentDisposition, contentType))
          } else {
            NotFound(s"File not found: $filename")
          }
        } else {
          // Extract the body of Forbidden response
          Forbidden("Error checking token validity").flatMap { forbiddenResponse =>
            EntityDecoder[IO, String].decode(forbiddenResponse, strict = false).value.flatMap {
              case Right(body) => InternalServerError(body).map(_.putHeaders(contentType))
              case Left(_) => InternalServerError("Error checking token validity").map(_.putHeaders(contentType))
            }
          }
        }
      }

    case GET -> Root / "generateToken" =>
      // Generate a random token
      val token = generateRandomToken(filename)

      // Respond with the generated token
      Ok(s"Generated Token: $token")


  }

  private def generateRandomFileName(): String = {
    val randomString = Random.alphanumeric.take(10).mkString
    s"file_$randomString"
  }

  private def generateRandomToken(filename: String): String = {
    val token = Random.alphanumeric.take(10).mkString
    // Store the pair (filename, token) in the database
    storeToken(filename, token)
    token
  }


}
