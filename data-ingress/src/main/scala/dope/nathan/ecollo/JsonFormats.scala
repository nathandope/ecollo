package dope.nathan.ecollo

import java.time.Instant

import dope.nathan.ecollo.dataModels.{EcoData, File, Location}
import spray.json._

trait InstantJsonSupport extends DefaultJsonProtocol {
  implicit object InstantFormat extends JsonFormat[Instant] {
    def write(instant: Instant) = JsNumber(instant.getEpochSecond)

    def read(json: JsValue): Instant = json match {
      case JsNumber(_) ⇒ Instant.now()
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

object EcoDataJsonSupport extends DefaultJsonProtocol with InstantJsonSupport{
  import FileJsonSupport._
  import LocationJsonSupport._
  implicit val sensorDataFormat: RootJsonFormat[EcoData] = jsonFormat5(EcoData.apply)
}