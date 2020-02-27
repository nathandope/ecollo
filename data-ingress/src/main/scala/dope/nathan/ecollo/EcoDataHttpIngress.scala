package dope.nathan.ecollo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cloudflow.akkastream._
import cloudflow.akkastream.util.scaladsl.HttpServerLogic
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import dope.nathan.ecollo.EcoDataJsonSupport._
import dope.nathan.ecollo.dataModels.EcoData

class EcoDataHttpIngress extends AkkaServerStreamlet {

  val out: AvroOutlet[EcoData] = AvroOutlet[EcoData]("out").withPartitioner(RoundRobinPartitioner)

  override def shape(): StreamletShape = StreamletShape.withOutlets(out)

  override protected def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, out)

}
