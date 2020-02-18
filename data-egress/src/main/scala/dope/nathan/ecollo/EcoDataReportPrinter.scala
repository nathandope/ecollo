package dope.nathan.ecollo

import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, _}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import org.apache.spark.sql.streaming.OutputMode
import cloudflow.spark.sql.SQLImplicits._ // don't touch

class EcoDataReportPrinter extends SparkStreamlet {

  val in = AvroInlet[EcoData]("in")

  override def shape(): StreamletShape = StreamletShape.withInlets(in)

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic() {
    override def buildStreamingQueries: StreamletQueryExecution = {
      readStream(in).writeStream
        .format("console")
        .option("checkpointLocation", context.checkpointDir("console-egress"))
        .outputMode(OutputMode.Append())
        .start()
        .toQueryExecution
    }
  }
}
