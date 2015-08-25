package com.likeorz.services

import com.likeorz.models.{ TagGroup, User, Tag }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagService {

  def suggestTagsForUser(userId: Long): Future[(Seq[String], Seq[String])]

  def autoComplete(name: String): Future[Seq[Tag]]

  def hotTags(num: Int): Future[Seq[String]]

  def hotUsersForTag(tag: String, num: Int): Future[Seq[User]]

  def validTag(tag: String): Boolean

  def getTagGroups: Future[Seq[TagGroup]]

  def getGroupedTags: Future[Map[TagGroup, Seq[Tag]]]

  def getTagsForGroup(groupId: Long, pageSize: Int, page: Int): Future[Seq[Tag]]

  def setTagGroup(tagId: Long, groupId: Long): Future[Unit]

  def addTagGroup(name: String): Future[TagGroup]

}
