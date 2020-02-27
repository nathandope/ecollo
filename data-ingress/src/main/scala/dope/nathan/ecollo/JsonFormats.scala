package dope.nathan.ecollo

import java.util.UUID

import dope.nathan.ecollo.dataModels.{EcoData, File, Location}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError}

import scala.util.Try

trait UUIDJsonSupport extends DefaultJsonProtocol {
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)

    def read(json: JsValue): UUID = json match {
      case JsString(uuid) ⇒ Try(UUID.fromString(uuid)).getOrElse(deserializationError(s"Expected valid UUID but got '$uuid'."))
      case other          ⇒ deserializationError(s"Expected UUID as JsString, but got: $other")
    }
  }
}

object LocationJsonSupport extends DefaultJsonProtocol {
  implicit val locationFormat: RootJsonFormat[Location] = jsonFormat4(Location.apply)
}

object FileJsonSupport extends DefaultJsonProtocol {
  implicit val fileFormat: RootJsonFormat[File] = jsonFormat4(File.apply)
}

object EcoDataJsonSupport extends DefaultJsonProtocol with UUIDJsonSupport{
  import FileJsonSupport._
  import LocationJsonSupport._
  implicit val sensorDataFormat: RootJsonFormat[EcoData] = jsonFormat5(EcoData.apply)
}
