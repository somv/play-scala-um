package controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller}
import services.UmService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by moglix on 4/6/17.
  */
@Singleton
class UmController @Inject()(umService:UmService)
                            (implicit ec: ExecutionContext) extends Controller {

  def ping = Action{
    Ok("ok from um controller.")
  }

  def getUser(id:String) = Action.async{
    Logger.debug(s"_id to get user at UmController is ${id}")
    umService.getUserById(id).map{response=>Ok(response)}
  }

  def setUser = Action.async(parse.json){ implicit request=>
    Logger.debug(s"Request at UmController is ${request.body}")
    umService.createUser(request.body).map{response=>Ok(response)}
  }

  def updateUser = Action.async(parse.json){implicit request=>
    Logger.debug(s"Request for update user at UmController is ${request.body}")
    umService.updateUser(request.body).map{response=>Ok(response)}
  }

  def deleteUser(id:String) = Action.async{
    Logger.debug(s"_id to delete at UmController is ${id}")
    umService.deleteUser(id).map{response=>Ok(response)}
  }

}
