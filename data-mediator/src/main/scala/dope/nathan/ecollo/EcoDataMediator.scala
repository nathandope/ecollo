package dope.nathan.ecollo

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import dope.nathan.ecollo.dataModels.{EcoData, Foo}
import org.apache.flink.streaming.api.scala._

class EcoDataMediator extends FlinkStreamlet {

  val in: AvroInlet[EcoData] = AvroInlet[EcoData]("in")
  val out: AvroOutlet[Foo] = AvroOutlet[Foo]("out")

  override def shape(): StreamletShape = StreamletShape
    .withInlets(in)
    .withOutlets(out)

  override protected def createLogic(): FlinkStreamletLogic =
    new FlinkStreamletLogic() {
      override def buildExecutionGraph(): Unit = {
        val data: DataStream[EcoData] = readStream(in)

        val encoded = data.map { _ => Foo(java.time.Instant.now()) }

        writeStream(out, encoded)
      }
    }
}
