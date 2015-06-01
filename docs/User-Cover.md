##User Cover修改用户封面图片
######URLhttp://api.likeorz.com/v2/user/cover######支持格式JSON######HTTP请求方式PUT######请求参数|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|cover|true|string|七牛直传返回来的用户封面文件名|

######请求示例
<pre>
curl -X PUT -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39" -d 'cover=cover_8.jpg' http://api.likeorz.com/v2/user/cover
</pre>
######返回结果######JSON示例<pre>{
    "code": "1", 
    "message": "Change Cover Success"
}
</pre>