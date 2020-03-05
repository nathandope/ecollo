package dope.nathan.ecollo

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import dope.nathan.ecollo.dataModels.FooEncoded
import org.apache.flink.streaming.api.scala._

class DataPrinter extends FlinkStreamlet {

  val in: AvroInlet[FooEncoded] = AvroInlet[FooEncoded]("in")

  override def shape(): StreamletShape = StreamletShape.withInlets(in)

  override protected def createLogic(): FlinkStreamletLogic =
    new FlinkStreamletLogic() {
      override def buildExecutionGraph(): Unit = {
        val stream = readStream(in)
        stream.map { record => println(s"ResultStream: $record") }
      }
    }

}
