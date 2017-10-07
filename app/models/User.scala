package models

import play.api.libs.json.Json

/**
  * Created by moglix on 4/6/17.
  */
case class User(_id:String,
               name:Option[String],
               email:String,
               password:String,
               address:Address
               )

object User{
  implicit val formatter = Json.format[User]
}