package models

import play.api.libs.json.Json

/**
  * Created by moglix on 5/6/17.
  */
case class Address(city:String,
                  pincode:String,
                  state:String,
                  countory:String
                  )

object Address{
  implicit val formatter = Json.format[Address]
}
