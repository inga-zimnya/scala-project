package http4scontrollers

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

case class ErrorResponse(message: String)

object ErrorResponse {
  // Implicit encoder for ErrorResponse
  implicit val errorResponseEncoder: Encoder[ErrorResponse] = deriveEncoder[ErrorResponse]

  // Implicit decoder for ErrorResponse
  implicit val errorResponseDecoder: Decoder[ErrorResponse] = deriveDecoder[ErrorResponse]
}
