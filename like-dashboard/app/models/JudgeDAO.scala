package models

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

case class JudgePost(id: Long, judge: Boolean)

trait JudgePostsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class JudgePostsTable(tag: Tag) extends Table[JudgePost](tag, "judge_posts") {
    def id = column[Long]("id", O.PrimaryKey)
    def judge = column[Boolean]("judge")
    override def * = (id, judge) <> (JudgePost.tupled, JudgePost.unapply _)
  }

  protected val justPosts = TableQuery[JudgePostsTable]
}
