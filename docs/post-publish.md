##Post Publish
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|content|true|string|内容或资源链接（七牛文件名）|
|type|true|string|类型（暂时只支持 PHOTO）|
|tags|false|json|选定的标签数组（格式化成json格式）|

    "code": "1", 
    "message": "Publish Post Success", 
    "data": {
        "post_id": "3061", 
        "content": "http://storage.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg", 
        "type": "PHOTO", 
        "description": null, 
        "created": 1428569376, 
        "user": {
            "user_id": "1", 
            "nickname": "avatar_8.jpg", 
            "avatar": "http://storage.likeorz.com/avatar_8.jpg", 
            "likes": "362"
        }
    }
}
</pre>
|--------|-------|-------|
|post_id|int|内容ID|
|content|string|内容|
|type|string|类型|
|description|string|描述|
|created|int|发表时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户总赞数|