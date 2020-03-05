package dope.nathan.ecollo

import java.sql.Timestamp

import cloudflow.spark.sql.SQLImplicits._
import cloudflow.spark.testkit.{SparkInletTap, SparkScalaTestSupport, SparkStreamletTestkit}
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, StreamletQueryExecution}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import dope.nathan.ecollo.dataModels.EcoDataSpec
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{Dataset, Encoder, SparkSession}

import scala.concurrent.duration._

class EcoDataReportPrinterSpec extends SparkScalaTestSupport {

  "SparkEgress" should {
    "egress streaming data" in {

      val testKit = SparkStreamletTestkit(session)

      def asCollection[T: Encoder](session: SparkSession, queryName: String): List[T] =
        session.sql(s"select * from $queryName").as[T].collect().toList

      object MySparkEgress extends SparkStreamlet {

        System.setProperty("hadoop.home.dir", "C:\\spark-2.4.4-bin-hadoop2.7")

        val in = AvroInlet[EcoDataSpec]("in")

        val shape = StreamletShape(in)

        override def createLogic() = new SparkStreamletLogic {

          override def buildStreamingQueries: StreamletQueryExecution = process(readStream(in))

          private def process(inDataset: Dataset[EcoDataSpec]): StreamletQueryExecution = {
            readStream(in).map{ d => println(d); d }.writeStream
              .format("console")
              .option("checkpointLocation", context.checkpointDir("console-egress"))
              .outputMode(OutputMode.Append())
              .start()
              .toQueryExecution
          }
        }
      }

      val in: SparkInletTap[EcoDataSpec] = testKit.inletAsTap[EcoDataSpec](MySparkEgress.in)

      // build data and send to inlet tap
      val data = (1 to 10).map(i ⇒ EcoDataSpec(s"$i", None, Some(new Timestamp(0L)), None, None))
      in.addData(data)

      testKit.run(MySparkEgress, Seq(in), Seq.empty, 10.seconds)

      // get data from outlet tap
      val results = asCollection[String](session, "eco_data")

      results.foreach(println)

      // assert
      results must contain(EcoDataSpec("1", None, Some(new Timestamp(0L)), None, None))
    }
  }
}