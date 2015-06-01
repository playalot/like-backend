##Notification Count获取用户未读消息######URLhttp://api.likeorz.com/v2/notification/count######支持格式JSON######HTTP请求方式GET######请求参数|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
######返回结果######JSON示例<pre>{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "count": 0
    }
}
</pre>######返回字段说明|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|count|int|未读消息总数|