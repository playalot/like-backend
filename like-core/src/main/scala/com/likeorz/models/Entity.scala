package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
case class Entity(
  id: Option[Long] = None,
  name: String,
  description: String,
  avatar: String)
