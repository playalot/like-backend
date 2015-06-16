##User Login

用户第三方账号登录注册

######URL

http://api.likeorz.com/v1/authenticate/:provider

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|uid|true|string|第三方账号ID|
|access_token|true|string|第三方账号授权码|

######请求示例json
`json
{
    "uid": "13333333333",
    "access_token": "12341231231"
}
`

######返回结果

######JSON示例

<pre>
{
    "code": "1", 
    "message": "Login Success", 
    "data": {
        "user_id": "1", 
        "session_token": "cd636e8367b7fb87452c3fe3ab71b5c8", 
        "refresh_token": "JMNU4XayPlU2OPR", 
        "expires_in": 604800
    }
}
</pre>

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|int|用户ID|
|session_token|string|用户授权|
|refresh_token|string|刷新授权|
|expires_in|int|过期时间|