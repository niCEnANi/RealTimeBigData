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

    val wc = input.flatMap(line=>{line.split(" ")}).map( number => (number, 1)).cache()

    val output = wc.reduceByKey(_ + _).sortBy(_._2)

    val result = output.first()._1

    println("The odd man out: "+result)
  }
}
