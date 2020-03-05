package dope.nathan.ecollo

import java.time.Instant
import java.util.UUID

import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic, _}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import dope.nathan.ecollo.dataModels.{Foo, FooEncoded}
import frameless.TypedEncoder.usingInjection
import frameless.{Injection, TypedEncoder, TypedExpressionEncoder}
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{DataType, ObjectType}
import org.apache.spark.sql.{Dataset, Encoder, Encoders}

class EcoDataReportPrinter extends SparkStreamlet {

  System.setProperty("hadoop.home.dir", "D:\\spark-2.4.4-bin-hadoop2.7")

  implicit def typedEncoder[T: TypedEncoder]: Encoder[T] = TypedExpressionEncoder[T]

  implicit val typed: Encoder[FooEncoded] = Encoders.product[FooEncoded]

  implicit def fooTypedEncoder: TypedEncoder[Foo] = new TypedEncoder[Foo] {

    implicit val encoder: Injection[Foo, FooEncoded] = new Injection[Foo, FooEncoded] {
      override def apply(foo: Foo): FooEncoded = FooEncoded(foo.time.getEpochSecond, foo.description.toString)
      override def invert(fooEncoded: FooEncoded): Foo = Foo(Instant.now(), UUID.fromString(fooEncoded.description))
    }

    val underlying: TypedEncoder[Foo] = usingInjection[Foo, FooEncoded]

    def nullable: Boolean = false

    def jvmRepr: DataType = ObjectType(classOf[Foo])

    def catalystRepr: DataType = TypedEncoder[FooEncoded].catalystRepr

    def toCatalyst(path: Expression): Expression = underlying.toCatalyst(path)

    def fromCatalyst(path: Expression): Expression = underlying.fromCatalyst(path)
  }

  val in: AvroInlet[Foo] = AvroInlet[Foo]("in")
  val out: AvroOutlet[FooEncoded] = AvroOutlet[FooEncoded]("out")

  override def shape(): StreamletShape = StreamletShape.withInlets(in).withOutlets(out)

  override protected def createLogic(): SparkStreamletLogic = new SparkStreamletLogic() {

    override def buildStreamingQueries: StreamletQueryExecution = {
      val stream: Dataset[Foo] = readStream(in)
      val fooEncoded = stream.select("time", "description").as[FooEncoded]
      writeStream(fooEncoded, out, OutputMode.Append).toQueryExecution
    }
  }
}
