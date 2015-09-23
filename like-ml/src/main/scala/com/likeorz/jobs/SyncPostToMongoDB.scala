package com.likeorz.jobs

import com.likeorz.mllib.utils.MongoDB
import com.likeorz.models.{ MarkDetail, PostMarks }
import scalikejdbc._
import scalikejdbc.config._

import scalikejdbc.AutoSession

object SyncPostToMongoDB {

  def SyncPostToMongoDB(): Unit = {

    DBs.setupAll()

    implicit val session = AutoSession

    var count = 0

    val posts = sql"select id, created from post".foreach { rs =>
      val postId = rs.long("id")
      val created = rs.long("created")

      val marks = sql"select id, user_id, tag_id, tag from mark where post_id=${postId}".map { mrs =>
        val markId = mrs.long("id")
        val userId = mrs.long("user_id")
        val tagId = mrs.long("tag_id")
        val tag = mrs.string("tag")
        val likes = sql"select user_id from `like` where mark_id=${markId}".map(lrs => lrs.long("user_id")).list.apply()
        MarkDetail(markId, userId, tagId, tag, likes)
      }.list.apply()

      val post = PostMarks(postId, created, marks)

      try {
        MongoDB.insertPostMarks(post)
      } catch {
        case e: Throwable => println(e.getMessage)
      }
      count += 1
      if (count % 1000 == 0) println("processed " + count + " posts...")

    }

  }

}
