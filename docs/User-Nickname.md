##User Nickname
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|nickname|true|string|用户昵称|

######请求示例
<pre>
curl -X PUT -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39" -d 'nickname=test' http://api.likeorz.com/v2/user/nickname
</pre>

    "code": "1", 
    "message": "Change Nickname Success"
}
</pre>