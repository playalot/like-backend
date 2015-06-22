package com.likeorz.models

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
    likes: Long = 1) extends Identifier
