package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Feedback(
  id: Long,
  userId: Long,
  feedback: String,
  created: Long)
