##Home
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{page:[0-9]+}|true|int|分页|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v2/home/0
</pre>
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "posts": [
            {
                "post_id": "3061", 
                "type": "PHOTO", 
                "content": "http://storage.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg", 
                "created": "1428569376", 
                "user": {
                    "user_id": "1", 
                    "nickname": "小改", 
                    "avatar": "http://storage.likeorz.com/avatar_8.jpg", 
                    "likes": "362"
                }, 
                "marks": [
                    {
                        "mark_id": "8006", 
                        "tag": "标签3", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8005", 
                        "tag": "标签2", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8004", 
                        "tag": "标签1", 
                        "likes": "1", 
                        "is_liked": "1"
                    }
                ]
            }
        ], 
        "next": 1
    }
}
</pre>
|--------|-------|-------|
|post_id|int|内容ID|
|type|string|类型|
|content|string|内容|
|created|int|发布时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户获得赞数|
|marks|array|标签信息|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|当前访问用户是否赞过|
|next|int|下一页页号（没有下一页返回 false）|