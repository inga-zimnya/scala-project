package database

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.model.ReplaceOptions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object DatabaseManager {
  // MongoDB connection settings
  private val connectionString = "mongodb+srv://engaezik:63850342enga@cluster0.goiqv0u.mongodb.net/"
  private val databaseName = "tokens_organization"
  private val collectionName = "tokens"

  // Connect to MongoDB
  private val mongoClient: MongoClient = MongoClient(connectionString)
  private val database: MongoDatabase = mongoClient.getDatabase(databaseName)
  private val collection: MongoCollection[Document] = database.getCollection(collectionName)

  // Define the case class for file tokens
  case class FileToken(filename: String, token: String, path: String)

  def storeToken(filename: String, token: String, path: String): Future[Unit] = {
    val document = Document("filename" -> filename, "token" -> token, "path" -> path)
    val replaceOptions = ReplaceOptions().upsert(true)
    val observable: Observable[Document] = collection.replaceOne(equal("filename", filename), document, replaceOptions)
      .flatMap(_ => collection.find(equal("filename", filename)).first())
    observable.toFuture().map { _ =>
      println(s"Token stored for $filename at path: $path")
    }
  }

  def isValidToken(filename: String, token: String, path: String): Future[Boolean] = {
    val filter = and(equal("filename", filename), equal("token", token))
    val countFuture: Future[Long] = collection.countDocuments(filter).toFuture()
    countFuture.map { count =>
      count > 0
    }.recover {
      case ex =>
        // Handle any exceptions that might occur during the Future execution
        println(s"Error while checking token validity: ${ex.getMessage}")
        false
    }
  }


  sys.addShutdownHook {
    mongoClient.close()
  }
}

