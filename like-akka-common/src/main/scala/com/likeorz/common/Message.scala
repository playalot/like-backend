package com.likeorz.common


case class Tag(tag: String)
case class Tags(tags: Seq[String])
case class ClassifyPost(id: Long, tags: Seq[String], timestamp: Long)
case class Event(json: String)
case class Notification()

case object JoinApiServer
case object ApiServerRemoteCount
case object ApiServerRemoteMembers
case object Ping
case object PushUnreadLikes