##Post Marks
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{id:[0-9]+}|true|int|作品ID|
|{page:[0-9]+}|true|int|分页|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v2/post/3061/marks/0
</pre>
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "marks": [
            {
                "mark_id": "8004", 
                "tag": "标签1", 
                "likes": "1", 
                "is_liked": "1", 
                "created": "1428569376", 
                "user": {
                    "user_id": "1", 
                    "nickname": "小改", 
                    "avatar": "http://storage.likeorz.com/avatar_8.jpg", 
                    "likes": "362"
                }
            }, 
            {
                "mark_id": "8005", 
                "tag": "标签2", 
                "likes": "1", 
                "is_liked": "1", 
                "created": "1428569376", 
                "user": {
                    "user_id": "1", 
                    "nickname": "小改", 
                    "avatar": "http://storage.likeorz.com/avatar_8.jpg", 
                    "likes": "362"
                }
            }, 
            {
                "mark_id": "8006", 
                "tag": "标签3", 
                "likes": "1", 
                "is_liked": "1", 
                "created": "1428569376", 
                "user": {
                    "user_id": "1", 
                    "nickname": "小改", 
                    "avatar": "http://storage.likeorz.com/avatar_8.jpg", 
                    "likes": "362"
                }
            }
        ], 
        "next": false
    }
}
</pre>
|--------|-------|-------|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|当前访问用户是否赞过|
|created|int|标签添加时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户获得赞数|
|next|int|下一页页号（没有下一页返回 false）|