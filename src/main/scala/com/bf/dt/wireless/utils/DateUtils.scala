package com.bf.dt.wireless.utils

import java.text.SimpleDateFormat
import java.util.Date

/**
  * 日期、时间格式化类
  */
object DateUtils {
  val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val timeFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def getNowDate(): String = {
    var now: Date = new Date()
    var today = dateFormat.format(now)
    today
  }

  def getNowTime(): String = {
    var now: Date = new Date()
    var time = timeFormat.format(now)
    time
  }


}
