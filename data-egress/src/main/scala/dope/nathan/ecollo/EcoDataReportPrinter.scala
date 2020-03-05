package dope.nathan.ecollo

import java.time.Instant

import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, _}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import dope.nathan.ecollo.dataModels.{Foo, FooEncoded}
import frameless.{TypedEncoder, TypedExpressionEncoder}
import org.apache.spark.sql.catalyst.expressions.objects.{Invoke, NewInstance}
import org.apache.spark.sql.catalyst.expressions.{CreateNamedStruct, Expression, GetStructField, Literal}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{DataType, LongType, ObjectType}
import org.apache.spark.sql.{Dataset, Encoder, Encoders}

class EcoDataReportPrinter extends SparkStreamlet {

  implicit val encoder: Encoder[FooEncoded] = Encoders.product[FooEncoded]

  implicit def typedEncoder[T: TypedEncoder]: Encoder[T] = TypedExpressionEncoder[T]

  implicit def uuidTypedEncoder: TypedEncoder[Foo] = new TypedEncoder[Foo] {
    def nullable: Boolean = false

    def jvmRepr: DataType = ObjectType(classOf[Foo])

    def catalystRepr: DataType = TypedEncoder[FooEncoded].catalystRepr

    def toCatalyst(path: Expression): Expression = {
      val instant = Invoke(path, "time", ObjectType(classOf[Instant]))
      val time = Invoke(instant, "getEpochSecond", LongType)
      CreateNamedStruct(Seq(Literal("time"), time))
    }

    def fromCatalyst(path: Expression): Expression = {
      val time = GetStructField(path, 0, Some("time"))
      NewInstance(classOf[Foo], Seq(time), jvmRepr)
    }
  }

  System.setProperty("hadoop.home.dir", "D:\\spark-2.4.4-bin-hadoop2.7")

  val in: AvroInlet[Foo] = AvroInlet[Foo]("in")
  val out: AvroOutlet[FooEncoded] = AvroOutlet[FooEncoded]("out")

  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(out)

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic() {

    override def buildStreamingQueries: StreamletQueryExecution = {
      val stream: Dataset[Foo] = readStream(in)
      val fooEncoded = stream.select("time").as[FooEncoded]
      writeStream(fooEncoded, out, OutputMode.Append).toQueryExecution
    }
  }
}
