package com.bf.dt.wireless.config

import com.typesafe.config.{Config, ConfigFactory}

object WirelessConfig {

  val conf = ConfigFactory.load("application.properties")

  def getConf: Option[Config] = {
    Some(conf)
  }
}
