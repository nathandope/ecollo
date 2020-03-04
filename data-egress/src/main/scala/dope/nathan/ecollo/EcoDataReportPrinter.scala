package dope.nathan.ecollo

import cloudflow.spark.sql.SQLImplicits._
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, _}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import dope.nathan.ecollo.dataModels.EcoData
import org.apache.spark.sql.streaming.OutputMode


class EcoDataReportPrinter extends SparkStreamlet {

  System.setProperty("hadoop.home.dir", "C:\\spark-2.4.4-bin-hadoop2.7") // todo make as environment

  val in: AvroInlet[EcoData] = AvroInlet[EcoData]("in")

  override def shape(): StreamletShape = StreamletShape.withInlets(in)

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic() {
    override def buildStreamingQueries: StreamletQueryExecution = {
      readStream(in).map{ d => println(d); d }.writeStream // todo println for quick testing
        .format("console")
        .option("checkpointLocation", context.checkpointDir("console-egress"))
        .outputMode(OutputMode.Append())
        .start()
        .toQueryExecution
    }
  }
}
