package models

import play.api.i18n.Messages

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Tag(
    id: Option[Long],
    tagName: String,
    userId: Long,
    created: Long = System.currentTimeMillis / 1000,
    updated: Long = System.currentTimeMillis / 1000,
    likes: Long = 1) extends Identifier {

  // TODO
  //  require(tag.length >= 1, Messages("tag.minLength"))
  //  require(tag.length <= 12, Messages("tag.maxLength"))
}

