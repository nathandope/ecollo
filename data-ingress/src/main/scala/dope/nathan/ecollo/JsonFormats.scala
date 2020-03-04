package dope.nathan.ecollo

import java.sql.Timestamp

import dope.nathan.ecollo.dataModels.{EcoData, File, Location}
import spray.json._

trait TimestampJsonSupport extends DefaultJsonProtocol {
  implicit object InstantFormat extends JsonFormat[Timestamp] {
    def write(timestamp: Timestamp) = JsNumber(timestamp.getTime)

    def read(json: JsValue): Timestamp = json match {
      case JsNumber(value) ⇒ new Timestamp(value.longValue())
      case other           ⇒ deserializationError(s"Expected Timestamp as JsNumber, but got: $other")
    }
  }
}

object LocationJsonSupport extends DefaultJsonProtocol {
  implicit val locationFormat: RootJsonFormat[Location] = jsonFormat4(Location.apply)
}

object FileJsonSupport extends DefaultJsonProtocol {
  implicit val fileFormat: RootJsonFormat[File] = jsonFormat4(File.apply)
}

object EcoDataJsonSupport extends DefaultJsonProtocol with TimestampJsonSupport{
  import FileJsonSupport._
  import LocationJsonSupport._
  implicit val sensorDataFormat: RootJsonFormat[EcoData] = jsonFormat5(EcoData.apply)
}
