package models

object DeviceTypeEnum extends Enumeration {

  //  import
  //
  //  def enum2StringMapper(enum: Enumeration) = MappedColumnType.base[enum.Value, String](
  //    b => b.toString,
  //    i => enum.withName(i))
  //
  //  implicit val DeviceTypeEnumMapper = enum2StringMapper(DeviceTypeEnum)

  type DeviceTypeEnum = Value
  val Ios = Value("ios")
  val Andriod = Value("andriod")

}

case class Installation(
  id: Long,
  userId: Long,
  objectId: Long,
  deviceToken: String,
  deviceType: DeviceTypeEnum.DeviceTypeEnum,
  created: Long,
  updated: Long)
