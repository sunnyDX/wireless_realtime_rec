package com.bf.dt.wireless.storage

import java.sql.{Connection, ResultSet}

import com.bf.dt.wireless.config.WirelessConfig
import com.jolbox.bonecp.{BoneCP, BoneCPConfig}
import org.slf4j.LoggerFactory

object MysqlConnectionPool {
  val logger = LoggerFactory.getLogger(this.getClass)
  private val connectionPool = {
    val jdbcUrl = WirelessConfig.getConf.get.getString("mysql.url")
    val user = WirelessConfig.getConf.get.getString("mysql.user")
    val passwd = WirelessConfig.getConf.get.getString("mysql.passwd")
    val db = WirelessConfig.getConf.get.getString("mysql.db")
    val port = WirelessConfig.getConf.get.getString("mysql.port")
    try {
      Class.forName("com.mysql.jdbc.Driver")
      val config = new BoneCPConfig()
      config.setJdbcUrl("jdbc:mysql://" + jdbcUrl + ":" + port + "/" + db)
      config.setUsername(user)
      config.setPassword(passwd)
      config.setLazyInit(true)
      config.setMinConnectionsPerPartition(3)
      config.setMaxConnectionsPerPartition(50)
      config.setPartitionCount(5)
      config.setCloseConnectionWatch(false)
      config.setLogStatementsEnabled(false)
      config.setAcquireIncrement(20)
      Some(new BoneCP(config))
    } catch {
      case exception: Exception =>
        logger.warn("Error in creation of connection pool" + exception.printStackTrace())
        None
    }
  }

  def getConnection: Option[Connection] = {
    connectionPool match {
      case Some(connPool) => Some(connPool.getConnection)
      case None => None
    }
  }

  def closeConnection(connection: Connection): Unit = {
    if (!connection.isClosed) {
      connection.close()
    }
  }

}
