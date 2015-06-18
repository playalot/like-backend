##User List Accounts

显示用户全部绑定账号

######URL

http://api.likeorz.com/v1/account/list

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|


######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "成功获取数据",
    "data": {
        "linked_accounts": {
            "facebook": "1590099831258017",
            "mobile": "86 13041292219"
        }
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|provider|string|账户类型|
|key|string|账户ID|