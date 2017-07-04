package com.bf.dt.wireless.formator

import java.nio.charset.Charset
import java.util
import com.bf.dt.wireless.utils.DateUtils
import org.apache.http
import org.apache.http.client.utils.URLEncodedUtils
import org.json4s.JsonAST.JValue
import org.slf4j.LoggerFactory
import scala.collection.mutable.Map
import org.json4s.native.JsonMethods._

object WirelessFormator extends Serializable {
  val logger = LoggerFactory.getLogger(this.getClass)
  val vvRegex = """(\d+\.\d+\.\d+\.\d+).*?logger.php\?(.*?) HTTP.*""".r

  def format(logStr: String) = {
    var paramMap: Map[String, String] = Map()
    var uid = "-1"
    var aid = "-1"
    var atype = "-1"
    try {
      val vvRegex(ip, query) = logStr
      val params: util.List[http.NameValuePair] = URLEncodedUtils.parse(query, Charset.forName("UTF-8"))
      for (i <- 0 until params.size()) {
        paramMap += (params.get(i).getName -> params.get(i).getValue)
      }
      uid = paramMap.getOrElse("uid", "-")
      if (paramMap.getOrElse("ltype", "-") == "iphonevv") {
        val json = parse(paramMap.getOrElse("msg", "-").toString)
        val aid1 = (json \ "aid").values.toString
        aid = if (aid1 == null) {
          "-1"
        } else {
          aid1
        }
        val atype1 = (json \ "atype").values.toString
        atype = if (atype1 == null) {
          "-1"
        } else {
          atype1
        }
      }
      else if (paramMap.getOrElse("ltype", "-") == "mvv") {
        val json = parse(paramMap.getOrElse("msg", "-").toString)
        val aid1 = (json \ "aid").values.toString
        if (aid1 == null) {
          "-1"
        } else {
          aid1
        }
        val atype1 = (json \ "atype").values.toString
        atype = if (atype1 == null) {
          "-1"
        } else {
          atype1
        }
      }
    } catch {
      case e: Exception => logger.error("Fail to format log:" + logStr, e)
    }
    (uid, aid, atype)
  }

  def main(args: Array[String]) {
    val str = "120.210.184.207 - - [03/Jul/2017:17:51:59 +0800] \"GET /logger.php?ltype=iphonevv&pid=iphone&uid=77726E29-A137-4223-B36D-862018A186C3&gid=appstore&ver=5.1.5&msg=%7B%22vendorid%22:%2277726E29-A137-4223-B36D-862018A186C3%22,%22mtype%22:%22iPhone9,2%22,%22mos%22:%2210.3.2%22,%22unet%22:%220%22,%22id%22:%223635%22,%22aid%22:%2213826734%22,%22atype%22:%2211%22,%22vid%22:%2227472397%22,%22wid%22:%22funshion%22,%22pre_cid%22:%22jingxuan%22,%22cid%22:%22%E5%90%8C%E6%AD%A5%E5%88%97%E8%A1%A8%22,%22topic%22:%220%22,%22ptype%22:%222%22,%22type%22:%222%22,%22ltime%22:%221.819%22,%22date%22:%222017-07-03%2017:51:59%22,%22hell%22:%2237%22,%22style%22:%221%22,%22utype%22:%220%22,%22idfa%22:%22B8A1BDFB-AD29-4470-A0D1-711B01FE8F06%22,%22errorcode%22:%22%22,%22cstype%22:%22%22,%22changesource%22:%22%22,%22vtype%22:%221%22,%22shortfrom%22:%22%22,%22pausetime%22:%22%22,%22format%22:%22mp4%22,%22clarity%22:%222%22,%22datafrom%22:%220%22,%22vtitle%22:%22%22,%22sub_cid%22:%22jinriredian%22,%22pretime%22:%220.405%22,%22parsetime%22:%220.522%22,%22loadingtime%22:%220.880%22,%22user_id%22:%22%22,%22position%22:%225%22,%22status%22:%223%22,%22active_id%22:%22BC0C8F5E-ECAD-475A-B326-46B8A05DF30D%22,%22section_id%22:%2211004%22,%22ui_type%22:%222%22,%22card_type%22:%221%22,%22ver_switch%22:%221%22,%22order_id%22:%222%22%7D HTTP/1.1\" 200 43 \"-\" \"\\xE6\\x9A\\xB4\\xE9\\xA3\\x8E\\xE5\\xBD\\xB1\\xE9\\x9F\\xB3 5.1.5 rv:5.1.5.2 (iPhone; iOS 10.3.2; zh_CN)\" \"-\" \"-\" \"-\""
    print(format(str))
  }
}
