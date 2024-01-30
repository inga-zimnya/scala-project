package http4scontrollers

import org.http4s.MediaType
import cats.effect.IO
import fs2.Stream
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.multipart.Multipart
import org.typelevel.ci._
import tokens.TokenQueryParamMatcher
import java.nio.file.{Files, Paths}
import database.DatabaseManager._

import java.nio.file.{Files, Paths}
import scala.util.Random

object FileController {
  private val fileStoragePath = "uploads"

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "upload" =>
      // Decode the request body as Multipart[IO].
      req.decode[Multipart[IO]] { m =>
        // Process each part of the multipart request.
        m.parts.collectFirst {
          case part if part.filename.isDefined =>
            // Extract the filename from the file part.
            val uploadedFilename = part.filename.get

            // Construct the file path using a combination of user-provided filename and a unique identifier.
            val uniqueIdentifier = generateRandomFileName()
           // val filePath = s"$fileStoragePath/$uniqueIdentifier-$uploadedFilename"
            //val filePath = s"C:/Users/Inga/Desktop/uploads/$uniqueIdentifier-$uploadedFilename"


            // Write the content of the file part to the specified file path.
            val filePath = s"C:/Users/Inga/Desktop/uploads/$uniqueIdentifier-$uploadedFilename"
            println(s"File will be written to: $filePath")

            // Write the content of the file part to the specified file path.
            import cats.effect.unsafe.implicits.global

            part.body
              .through(fs2.io.file.writeAll[IO](Paths.get(filePath)))
              .compile
              .drain
              .unsafeRunSync()
            val token = generateRandomToken(uploadedFilename, filePath)

            // Store file information in the database
            storeToken(uploadedFilename, token, filePath)

            // Respond with a success message indicating the uploaded filename.
            Ok(s"File uploaded successfully: $uploadedFilename, Token: $token")
        }.getOrElse(BadRequest("No file part found"))
      }

    case GET -> Root / "download" / filename :? TokenQueryParamMatcher(token) =>
      // Check token validity here
      println(token)
      val filePath = s"C:/Users/Inga/Desktop/uploads/$filename"
      val validityCheck: IO[Boolean] = IO.fromFuture(IO(isValidToken(token, filename, filePath)))
      println(validityCheck)

      val contentType = `Content-Type`(MediaType.application.`octet-stream`)

      validityCheck.flatMap { isValid =>
        if (isValid) {
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
      val filename = generateRandomFileName()
      val uniqueIdentifier = generateRandomFileName()
      val filePath = s"$fileStoragePath/$uniqueIdentifier-$filename"
      val token = generateRandomToken(filename, filePath)

      // Respond with the generated token
      Ok(s"Generated Token: $token")
  }

  val allRoutes: HttpRoutes[IO] = routes
  private def generateRandomFileName(): String = {
    val randomString = Random.alphanumeric.take(10).mkString
    s"file_$randomString"
  }

  private def generateRandomToken(filename: String, filePath: String): String = {
    val token = Random.alphanumeric.take(10).mkString
    // Store the pair (filename, token) in the database
    //storeToken(filename, token, filePath)
    token
  }
}