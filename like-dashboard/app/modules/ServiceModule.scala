package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import services.{ PostServiceImpl, PostService }

/**
 * Created by Guan Guan
 * Date: 7/7/15
 */
class ServiceModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[PostService].to[PostServiceImpl]
    ()
  }

}
