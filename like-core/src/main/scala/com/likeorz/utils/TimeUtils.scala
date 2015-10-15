package com.likeorz.utils

import org.joda.time.DateTime

object TimeUtils {

  def startOfDay: Long = {
    new DateTime().withTimeAtStartOfDay().getMillis / 1000
  }

}
