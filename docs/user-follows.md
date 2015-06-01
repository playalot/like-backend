##User Follows
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{id:[0-9]+}|true|int|用户ID|
|{page:[0-9]+}|true|int|分页|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v2/user/1/follows/0
</pre>
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "follows": [
            {
                "user_id": "173", 
                "nickname": "akaaa", 
                "avatar": "http://storage.likeorz.com/avatar_173_1426760459.jpg", 
                "likes": "229", 
                "is_following": "1"
            }, 
            {
                "user_id": "154", 
                "nickname": "雨人", 
                "avatar": "http://storage.likeorz.com/avatar_154_1425736533.jpg", 
                "likes": "5", 
                "is_following": "1"
            }, 
        ], 
        "next": 1
    }
}
</pre>
|--------|-------|-------|
|user_id|int|用户ID|
|nickname|string|昵称|
|avatar|string|头像|
|likes|int|点赞数|
|is_following|int|当前浏览者是否关注|
|next|int|下一页页号（没有下一页返回 false）|