package controllers

import javax.inject.Inject

import play.api.i18n.MessagesApi

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class CommentController @Inject() (
    val messagesApi: MessagesApi) extends BaseController {

}
