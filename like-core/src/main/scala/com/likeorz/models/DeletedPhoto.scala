package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 6/29/15
 */
case class DeletedPhoto(
  id: Option[Long],
  photo: String,
  created: Long = System.currentTimeMillis() / 1000)
