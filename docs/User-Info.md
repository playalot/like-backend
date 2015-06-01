##GET User

查看用户界面 获取用户信息

######URL

http://api.likeorz.com/v1/user/{id:[0-9]+}

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|user/{id:[0-9]+}|true|int|用户ID|

######请求示例
<pre>
curl -i -X GET -H "LIKE-SESSION-TOKEN:bd625d6a6b6fe5bc0b128954ad5ca39d" http://api.likeorz.com/v2/user/1/
</pre>

######返回结果

######JSON示例

<pre>
{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "user_id": "1", 
        "nickname": "小改", 
        "avatar": "http://likeorz.qiniucdn.com/avatar_8.jpg", 
        "likes": 0, 
        "is_following": "1"
        "count": {
            "post": "6", 
            "follow": "1", 
            "fan": "0"
        }
    }
}

</pre>

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|int|用户ID|
|nickname|string|昵称|
|avatar|string|头像|
|likes|int|获得总赞数|
|is_following|int|是否关注|
|count|array|统计信息|
|post|int|内容总数|
|follow|int|关注数|
|fan|int|粉丝数|