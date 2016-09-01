/**
  * Created by nicen on 30-08-2016.
  */
import org.apache.spark.{SparkContext, SparkConf}

object ScalaApp {
  def main(args: Array[String]) {

    System.setProperty("hadoop.home.dir", "D:\\winutils");

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val input = sc.textFile("input")

    val wc = input.flatMap(line=>{line.split('.')}).map( line => (line, 1)).cache()

    val output = wc.reduceByKey(_ + _).sortByKey()

    output.saveAsTextFile("output")

    val o = output.collect()

    var s: String = "Sentence:Count & Alphabetical Ascending order \n"
    o.foreach { case (word, count) => {

      s += word + " : " + count + "\n"
    }}
    println(s)
  }

}
