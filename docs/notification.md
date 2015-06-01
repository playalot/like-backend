##Notification消息中心列表######URLhttp://api.likeorz.com/v2/notification/{page:[0-9]+}######支持格式JSON######HTTP请求方式GET######请求参数|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|{page:[0-9]+}|true|int|分页|
######返回结果######JSON示例<pre>{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "notes": [
            {
                "type": "MARK", 
                "user": {
                    "user_id": "3", 
                    "nickname": "Like", 
                    "avatar": "http://storage.likeorz.com/avatar_3_1427032014.jpg?imageView2/1/w/120/h/120/q/85"
                }, 
                "posts": [
                    {
                        "post_id": "10", 
                        "content": "http://storage.likeorz.com/580353ea2464ca8b_1422601844_w_1536_h_1024_4.jpg?imageView2/1/w/408/h/408/q/85"
                    }, 
                    {
                        "post_id": "9", 
                        "content": "http://storage.likeorz.com/0151411c8274f391_1422601702_w_1536_h_1536_4.jpg?imageView2/1/w/408/h/408/q/85"
                    }
                ], 
                "num": "2", 
                "is_new": "0", 
                "updated": "1428824852"
            }, 
            {
                "type": "LIKE", 
                "user": {
                    "user_id": "3", 
                    "nickname": "Like", 
                    "avatar": "http://storage.likeorz.com/avatar_3_1427032014.jpg?imageView2/1/w/120/h/120/q/85"
                }, 
                "posts": [
                    {
                        "post_id": "9", 
                        "content": "http://storage.likeorz.com/0151411c8274f391_1422601702_w_1536_h_1536_4.jpg?imageView2/1/w/408/h/408/q/85"
                    }, 
                    {
                        "post_id": "8", 
                        "content": "http://storage.likeorz.com/5970e13ec442384e_1422601189_w_1536_h_1536_4.jpg?imageView2/1/w/408/h/408/q/85"
                    }
                ], 
                "num": "4", 
                "is_new": "0", 
                "updated": "1428824293"
            }
        ], 
        "next": false
    }
}
</pre>######返回字段说明|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|notes|array|消息列表|
|type|string|消息类型(LIKE MARK FOLLOW)|
|num|string|消息数量|
|is_new|int|是否是新消息|
|updated|int|消息产生时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|posts|array|作品信息|
|content|string|作品缩略图|
|next|int|下一页页号（没有下一页返回 false）|