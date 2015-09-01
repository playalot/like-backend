package utils

import org.scalatest.{ FlatSpec, Matchers }

class HelperUtilsSpec extends FlatSpec with Matchers {

  it should "handle normal timestamp" in {
    val ts = Some("1435190198,1435185748,1435157044")
    HelperUtils.parseTimestamp(ts) shouldEqual Seq(Some(1435190198), Some(1435185748), Some(1435157044))
  }

  it should "handle single missing timestamp" in {
    val ts = Some("1435190198,,1435157044")
    HelperUtils.parseTimestamp(ts) shouldEqual Seq(Some(1435190198), None, Some(1435157044))
  }

  it should "handle tail missing timestamp" in {
    val ts = Some("1435190198,,")
    HelperUtils.parseTimestamp(ts) shouldEqual Seq(Some(1435190198), None, None)
  }

  it should "handle all invalid timestamp" in {
    val ts = Some(",,")
    HelperUtils.parseTimestamp(ts) shouldEqual Seq(None, None, None)
  }

  it should "handle none timestamp" in {
    val ts = None
    HelperUtils.parseTimestamp(ts) shouldEqual Seq(None, None, None)
  }

  it should "compare version correctly" in {
    HelperUtils.compareVersion("1.1.2", "1.1.1.1") shouldEqual true
    HelperUtils.compareVersion("1.1.1", "1.1.1.2") shouldEqual false
    HelperUtils.compareVersion("1.1.2.1", "1.1.1") shouldEqual true
  }

}
