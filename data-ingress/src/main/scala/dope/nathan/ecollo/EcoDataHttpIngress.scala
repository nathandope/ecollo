package dope.nathan.ecollo

import akka.stream.scaladsl.Sink
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.akkastream._
import cloudflow.akkastream.scaladsl._
import cloudflow.akkastream.util.scaladsl.HttpServerLogic

class EcoDataHttpIngress extends AkkaServerStreamlet {

  val out = AvroOutlet[EcoData]("out").withPartitioner(RoundRobinPartitioner)

  override def shape(): StreamletShape = StreamletShape.withOutlets(out)

  override protected def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, out)

}
