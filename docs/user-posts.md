##User Posts

获取用户作品

######URL

http://api.likeorz.com/v2/user/{id:[0-9]+}/posts/{page:[0-9]+}

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{id:[0-9]+}|true|int|用户ID|
|{page:[0-9]+}|true|int|分页|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v2/user/1/posts/0
</pre>
######返回结果

######JSON示例

`json
{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "posts": [
            {
                "post_id": "3065", 
                "type": "PHOTO", 
                "content": "http://storage.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg", 
                "created": "1428571869", 
                "marks": [
                    {
                        "mark_id": "8014", 
                        "tag": "标签3", 
                        "likes": "1", 
                        "is_liked": "0"
                    }, 
                    {
                        "mark_id": "8013", 
                        "tag": "标签2", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8012", 
                        "tag": "标签1", 
                        "likes": "1", 
                        "is_liked": "1"
                    }
                ]
            }, 
            {
                "post_id": "3064", 
                "type": "PHOTO", 
                "content": "http://storage.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg", 
                "created": "1428571849", 
                "marks": [
                    {
                        "mark_id": "8011", 
                        "tag": "标签3", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8010", 
                        "tag": "标签2", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8009", 
                        "tag": "标签1", 
                        "likes": "1", 
                        "is_liked": "1"
                    }
                ]
            }, 
            {
                "post_id": "3061", 
                "type": "PHOTO", 
                "content": "http://storage.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg", 
                "created": "1428569376", 
                "marks": [
                    {
                        "mark_id": "8006", 
                        "tag": "标签3", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8005", 
                        "tag": "标签2", 
                        "likes": "1", 
                        "is_liked": "1"
                    }, 
                    {
                        "mark_id": "8004", 
                        "tag": "标签1", 
                        "likes": "1", 
                        "is_liked": "1"
                    }
                ]
            }
        ], 
        "next": 1
    }
}
`

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|post_id|int|内容ID|
|type|string|类型|
|content|string|内容|
|created|int|发布时间|
|marks|array|标签信息|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|当前访问用户是否赞过|
|next|int|下一页页号（没有下一页返回 false）|