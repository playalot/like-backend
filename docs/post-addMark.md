##Post Mark创建标签######URLhttp://api.likeorz.com/v2/post/{id:[0-9]+}/mark######支持格式JSON######HTTP请求方式POST######请求参数|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|{id:[0-9]+}|true|int|作品ID|
|tag|true|string|标签内容|
######返回结果######JSON示例<pre>{
    "code": "1", 
    "message": "Mark Success", 
    "data": {
        "mark_id": "8008", 
        "tag": "蛇精病", 
        "likes": 1, 
        "is_liked": "1"
    }
}
</pre>######返回字段说明|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|是否赞过|