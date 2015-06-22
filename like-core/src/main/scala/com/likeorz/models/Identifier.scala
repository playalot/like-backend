package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 6/2/15
 */
trait Identifier {

  val id: Option[Long]

  def identify = id.getOrElse(-1L).toString

}
