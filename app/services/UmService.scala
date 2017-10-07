package services

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.User
import play.api.Logger
import play.api.libs.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by moglix on 4/6/17.
  */
@ImplementedBy(classOf[UmServiceImpl])
trait UmService {
  def getUserById(_id:String):Future[JsValue]
  def createUser(body:JsValue):Future[JsValue]
  def updateUser(body:JsValue):Future[JsValue]
  def deleteUser(_id:String):Future[JsValue]
}

@Singleton
class UmServiceImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                             (implicit ec:ExecutionContext) extends UmService with MongoController with ReactiveMongoComponents {

  private val logger: Logger = Logger(this.getClass())

  def dbInstanceFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("user"))

  override def getUserById(id: String): Future[JsValue] = {
    val futureUserList: Future[List[JsValue]] = dbInstanceFuture.flatMap{
      _.find(Json.obj("_id"->id))
      .cursor[JsValue](ReadPreference.primary)
      .collect[List]()
    }
    futureUserList.map{users=>
      if(users.nonEmpty) Json.toJson(users(0)).as[JsObject]
      else Json.obj();
    }
  }

  override def createUser(body: JsValue): Future[JsValue] = {
    Json.fromJson[User](body) match {

      case JsSuccess(entity,_)=>
        val fut:Future[WriteResult]=for{
          entities<-dbInstanceFuture
          lastError<-entities.insert(body.as[JsObject])
        } yield lastError

        fut.map( Success(_):Try[WriteResult]).recover{
          case t=>Failure(t)
        }.map{
          case Success(s)=>
            Logger.debug("Successfully inserted user.")
            Json.obj("status_cd"->1)
          case Failure(t)=>
            Logger.debug("Failed to insert user.")
            Json.obj("status_cd"->0)
        }

      case JsError(errors)=>
        Logger.debug(s"Could not create user from the json provided. ${Errors.show(errors)}")
        Future.successful(Json.obj("status_cd"->0, "error_msg"-> Errors.show(errors)))
    }
  }


  override def updateUser(body: JsValue): Future[JsValue] = {
    val selector = Json.obj("_id"-> (body \ "_id").as[String])
    val modifier = Json.obj("$set"->body.as[JsObject])

    val fut: Future[WriteResult] = for{
      entities<-dbInstanceFuture
      lastError<-entities.update(selector, modifier)
    } yield lastError

    fut.map(Success(_):Try[WriteResult]).recover{
      case t=> Failure(t)
    }.map{
      case Success(s)=>
        Logger.debug("Successfully updated the user.")
        Json.obj("status_cd"->1)
      case Failure(t)=>
        Logger.debug("Failed in updating user.")
        if(t.getMessage.contains("duplicate key error"))
          Json.obj("status_cd"->0, "error_msg"->"Duplicate key")
        else Json.obj("status_cd"->0, "error_msg"->t.getMessage)
    }

  }

  override def deleteUser(id: String): Future[JsValue] = {
    val selector = Json.obj("_id"->id)

    val fut: Future[WriteResult]=for{
      entities<-dbInstanceFuture
      lastError<-entities.remove(selector)
    } yield lastError

    fut.map(Success(_):Try[WriteResult]).recover{
      case t=>Failure(t)
    }.map{
      case Success(s)=>
        Logger.debug("Successfully deleted user.")
        Json.obj("status_cd"->1)
      case Failure(t)=>
        Logger.debug("Failed in deleting user.")
        Json.obj("status_cd"->0, "error_msg"->t.getMessage)
    }

  }

}
