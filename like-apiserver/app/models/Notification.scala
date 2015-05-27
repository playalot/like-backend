package models

object NotifyTypeEnum extends Enumeration {

  //  def enum2StringMapper(enum: Enumeration) = MappedColumnType.base[enum.Value, String](
  //    b => b.toString,
  //    i => enum.withName(i))
  //
  //  implicit val NotifyTypeEnumMapper = enum2StringMapper(NotifyTypeEnum)

  type NotifyTypeEnum = Value
  val Like = Value("LIKE")
  val Comment = Value("COMMENT")
  val Follow = Value("FOLLOW")
  val Mark = Value("MARK")

}

case class Notification(
  id: Long,
  `type`: NotifyTypeEnum.NotifyTypeEnum,
  userId: Long,
  `new`: Boolean,
  postIds: Option[String],
  num: Int,
  fromUserId: Long,
  updated: Long)
