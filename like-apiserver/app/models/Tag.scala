package models

import play.api.i18n.Messages

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Tag(
    id: Long,
    tagName: String,
    userId: Long,
    created: Long,
    updated: Long,
    likes: Long) {

  // TODO
  //  require(tag.length >= 1, Messages("tag.minLength"))
  //  require(tag.length <= 12, Messages("tag.maxLength"))
}

