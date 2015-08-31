##User Login

用户手机号登录注册

######URL

http://api.likeorz.com/v1/user/login    (deprecated)

http://api.likeorz.com/v1/authenticate/mobile/mob

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|mobile|true|string|手机号|
|zone|true|string|国家代号|
|code|true|string|短信验证码|

######请求示例json
```json
{
    "mobile": "13333333333",
    "zone": "86",
    "code": "1234"
}
```


######返回结果

######JSON示例

```json
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
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|int|用户ID|
|session_token|string|用户授权|
|refresh_token|string|刷新授权|
|expires_in|int|过期时间|
