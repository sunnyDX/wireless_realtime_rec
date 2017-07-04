package com.bf.dt.wireless.streaming

import java.io.{ObjectInputStream, ObjectOutputStream}

import com.bf.dt.wireless.config.WirelessConfig
import com.bf.dt.wireless.formator.WirelessFormator
import com.bf.dt.wireless.storage.MysqlConnectionPool
import com.bf.dt.wireless.utils.DateUtils
import kafka.serializer.StringDecoder
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s._
import org.slf4j.LoggerFactory

import scala.collection.mutable


object WirelessLogAnalysis {


  object BroadcastWrapper {

    @volatile private var instance: Broadcast[Map[String, List[String]]] = null

    private val map = mutable.LinkedHashMap[String, List[String]]()

    def getMysql(): Map[String, List[String]] = {
      //1.获取mysql连接池的一个连接
      val conn = MysqlConnectionPool.getConnection.get
      //2.查询新的数据
      val sql = "select aid_type,aids from cf_similarity"
      val ps = conn.prepareStatement(sql)
      val rs = ps.executeQuery()

      while (rs.next()) {
        val aid = rs.getString("aid_type")
        val aids = rs.getString("aids").split(",").toList
        map += (aid -> aids)
      }
      //3.连接池回收连接
      MysqlConnectionPool.closeConnection(conn)
      map.toMap
    }

    def update(sc: SparkContext, blocking: Boolean = false): Unit = {
      if (instance != null)
        instance.unpersist(blocking)
      instance = sc.broadcast(getMysql())
    }

    def getInstance(sc: SparkContext): Broadcast[Map[String, List[String]]] = {
      if (instance == null) {
        synchronized {
          if (instance == null) {
            instance = sc.broadcast(getMysql)
          }
        }
      }
      instance
    }

    private def writeObject(out: ObjectOutputStream): Unit = {
      out.writeObject(instance)
    }

    private def readObject(in: ObjectInputStream): Unit = {
      instance = in.readObject().asInstanceOf[Broadcast[Map[String, List[String]]]]
    }
  }


  def main(args: Array[String]): Unit = {

    val logger = LoggerFactory.getLogger(this.getClass)
    val conf = new SparkConf()
      .setAppName("wirelessLogAnalysis")

    val ssc = new StreamingContext(conf, Seconds(10))

    val kafkaConfig: Map[String, String] = Map(
      "metadata.broker.list" -> WirelessConfig.getConf.get.getString("wireless.metadata.broker.list"),
      "group.id" -> WirelessConfig.getConf.get.getString("wireless.group.id"),
      "zookeeper.connect" -> WirelessConfig.getConf.get.getString("wireless.zookeeper.connect"),
      "auto.offset.reset" -> WirelessConfig.getConf.get.getString("wireless.auto.offset.reset")
    )

    val androidvvTopic = WirelessConfig.getConf.get.getString("wireless.androidvv.topic")
    val iphonevvToplic = WirelessConfig.getConf.get.getString("wireless.iphonevv.topic")
    //    val ipadvvToplic = WirelessConfig.getConf.get.getString("wireless.ipadvv.topic")

    val kafkaDStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc,
      kafkaConfig,
      Set(androidvvTopic, iphonevvToplic)
    )

    kafkaDStream.print()
    val jsonDstream = kafkaDStream.map(x =>
      //解析日志流
      WirelessFormator.format(x._2)
    ).filter(x => (x._3 == "1") || x._3 == "2" || x._3 == "3" || x._3 == "4")

    jsonDstream.print()


    jsonDstream.foreachRDD {
      rdd => {
        // driver端运行，涉及操作：广播变量的初始化和更新
        val cf = BroadcastWrapper.getInstance(rdd.sparkContext)
        if ((DateUtils.getNowTime().split(" ")(1) >= "08:00:00") && (DateUtils.getNowTime().split(" ")(1) <= "10:10:00")) {
          BroadcastWrapper.update(rdd.sparkContext, true)
          logger.info("广播变量更新成功： " + DateUtils.getNowTime())
        }
        //worker端运行，涉及操作：Dstream数据的处理和Redis更新
        rdd.foreachPartition {
          partitionRecords =>
            //1.获取redis连接，保证每个partition建立一次连接，避免每个记录建立/关闭连接的性能消耗
            partitionRecords.foreach(
              record => {
                //2.解析日志流
                val uid = record._1
                val aid_type = record._2 + "_" + record._3
                if (cf.value.keySet.contains(aid_type)) {
                  (uid, cf.value.get(aid_type))
                  println((uid, cf.value.get(aid_type)))
                }
                else
                  (uid, "-1")
              }
              //3.redis更新数据
            )
          //4.关闭redis连接
        }
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }
}
