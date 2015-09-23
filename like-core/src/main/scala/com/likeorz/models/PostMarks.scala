package com.likeorz.models

import com.mongodb.casbah.Imports._

case class PostMarks(
  postId: Long,
  ts: Long,
  marks: Seq[MarkDetail])

object PostMarks {

  def fromDBObject(row: MongoDBObject): PostMarks = {
    val postId = row.as[Long]("_id")
    val ts = row.as[Long]("ts")
    val marks = row.as[MongoDBList]("marks").map(obj => MarkDetail.fromDBObject(obj.asInstanceOf[BasicDBObject]))
    PostMarks(postId, ts, marks)
  }

  def toDBObject(postMarks: PostMarks): MongoDBObject = {
    MongoDBObject(
      "_id" -> postMarks.postId,
      "ts" -> postMarks.ts,
      "marks" -> postMarks.marks.map(MarkDetail.toDBObject)
    )
  }

}

case class MarkDetail(
  markId: Long,
  userId: Long,
  tagId: Long,
  tag: String,
  likes: Seq[Long])

object MarkDetail {

  def fromDBObject(row: MongoDBObject): MarkDetail = {
    val markId = row.as[Long]("m_id")
    val userId = row.as[Long]("u_id")
    val tagId = row.as[Long]("t_id")
    val tag = row.as[String]("tag")
    val likes = row.as[Seq[Long]]("likes")
    MarkDetail(markId, userId, tagId, tag, likes)
  }

  def toDBObject(mark: MarkDetail): MongoDBObject = {
    MongoDBObject(
      "m_id" -> mark.markId,
      "u_id" -> mark.userId,
      "t_id" -> mark.tagId,
      "tag" -> mark.tag,
      "likes" -> mark.likes
    )
  }

}