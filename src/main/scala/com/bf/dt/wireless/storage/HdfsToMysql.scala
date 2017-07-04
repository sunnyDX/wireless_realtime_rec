package com.bf.dt.wireless.storage

import java.sql.{Connection, PreparedStatement}

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

case class Record(aid1: String, aid2: String, score: Double)

object HdfsToMysql {

  def saveSimilarity(conn: Connection, row: Row): Unit = {
    var ps: PreparedStatement = null
    try {
      val insert_sql = "insert into cf_similarity (aid_type,aids) values (?,?)"
      //插入新的数据
      ps = conn.prepareStatement(insert_sql)
      ps.setString(1, row.getString(0))
      ps.setString(2, row.getString(1))
      ps.executeUpdate()
    } catch {
      case e: Exception => println(e)
    } finally {
      if (ps != null) {
        ps.close()
      }
    }
  }

  def getTopk(sc: SparkContext, data: RDD[String]): DataFrame = {
    val sqlContext = new HiveContext(sc)
    import sqlContext.implicits._
    val df = data.map(p => Record(p.split("\t")(0), p.split("\t")(1), p.split("\t")(2).toDouble)).toDF
    df.registerTempTable("tmp")
    val sql = "select aid1,concat_ws(',',collect_set(aid2)) as aids from (select aid1,aid2,score,row_number() over (partition by aid1 order by score desc) rank from tmp)main where rank <= 3 group by aid1"
    val df_topK = df.sqlContext.sql(sql)
    df_topK
  }

  def main(args: Array[String]): Unit = {
    val path = args(0) //hdfs 文件路径
    val conf = new SparkConf().setAppName("hdfs to mysql")
    val sc = new SparkContext(conf)
    val data = sc.textFile(path)
    val df = getTopk(sc, data)
    val conn = MysqlConnectionPool.getConnection.get
    df.collect().map(
      row => saveSimilarity(conn, row)
    )
    MysqlConnectionPool.closeConnection(conn)
    sc.stop()
  }
}