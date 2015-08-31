##User Link Account

绑定手机

######URL

http://api.likeorz.com/v1/account/link/mobile/mob

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

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|string|用户ID|
|provider_id|string|账户类型|
|provider_key|string|第三方账户ID|