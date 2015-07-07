package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
case class PromoteEntity(
  id: Option[Long],
  entityId: Long,
  created: Long = System.currentTimeMillis() / 1000)
