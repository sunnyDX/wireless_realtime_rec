package com.bf.dt.wireless.utils

import java.net.URLDecoder

object SDKDecoder {
  val decryptStr = " !_#$%&'()*+,-.ABCDEFGHIJKLMNOP?@/0123456789:;<=>QRSTUVWXYZ[\\]^\"`nopqrstuvwxyzabcdefghijklm{|}~"

  def decode(url: String): String = {
    var newString = ""
    val decodeUrl: String = URLDecoder.decode(url, "UTF8")
    for (i <- 0 to decodeUrl.length - 1) {
      var ch = decodeUrl.charAt(i)
      if (ch.toInt >= 32 && ch.toInt <= 126) {
        newString += decryptStr.charAt(ch.toInt - 32)
      } else {
        newString += ch
      }
    }
    newString
  }

  def main(args: Array[String]) {
    var str = "%7B_hhvq_L_ssssssss-oppo-FHos-ssss-ssssrIIHEBBB_%2C_vzrv_L_JHIJDEBDGCBBGBC_%2C_hvq_L_JHIJDEBDGCBBGBC_%2C_hfrevq_L_-_%2C_naqebvqvq_L_pHnFJpnCnJEsrK_%2C_znp_L_FBLFGLqnLsBLFFLHJ_%2C_zglcr_L_V%3DT%3D%205TI_%2C_zbf_L_G.C_%2C_ire_L_D.B.D_%2C_tvq_L_qDB_%2C_harg_L_C_%2C_vgvzr_L_DBCH-CB-EC%20CILBELBH_%2C_inyhr_L%7B_tebhcvq_L__%2C_pbagragvq_L_KCJK_%2C_fgnghf_L_D_%2C_zbqr_L_C_%2C_fvgr_L_osbayvar_%2C_erfglcr_L_beqvanelivqrb_%2C_sebz_L_C_%2C_cynlgvzr_L_CK_%2C_cntr_L_CH_%2C_punaary_L_ubzrcntr_%2C_glcr_L_ivqrb_%2C_hfre%22vq_L_-_%7D%7D"
    println(SDKDecoder.decode(str))
  }
}
