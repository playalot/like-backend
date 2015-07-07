##Post Marks

帖子标签

######URL

http://api.likeorz.com/v1/post/{id:[0-9]+}/marks/{page:[0-9]+}

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{id:[0-9]+}|true|int|作品ID|
|{page:[0-9]+}|true|int|分页|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v2/post/3061/marks/0
</pre>
######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data": {
        "marks": [
            {
                "mark_id": 25904,
                "tag": "手办",
                "likes": 7,
                "is_liked": false,
                "created": 1434428028,
                "user": {
                    "user_id": 611,
                    "nickname": "Resid橘子",
                    "avatar": "http://cdn.likeorz.com/avatar_611_1435765248.jpg?imageView2/1/w/80/h/80",
                    "likes": 1351
                },
                "total_comments": 2,
                "comments": [
                    {
                        "comment_id": 3497,
                        "content": "123123.jpg",
                        "created": 1436300726,
                        "place": "Beijing",
                        "user": {
                            "user_id": 715,
                            "nickname": "进击の关关",
                            "avatar": "http://cdn.likeorz.com/avatar_715_1435894969.jpg?imageView2/1/w/80/h/80",
                            "likes": 1148
                        },
                        "reply": null
                    },
                    {
                        "comment_id": 3496,
                        "content": "123123.jpg",
                        "created": 1436300725,
                        "place": "Beijing",
                        "user": {
                            "user_id": 715,
                            "nickname": "进击の关关",
                            "avatar": "http://cdn.likeorz.com/avatar_715_1435894969.jpg?imageView2/1/w/80/h/80",
                            "likes": 1148
                        },
                        "reply": null
                    }
                ],
                "likers": [
                    {
                        "user_id": 872,
                        "nickname": "SL__小黑",
                        "avatar": "http://cdn.likeorz.com/avatar_872_1434105061.jpg?imageView2/1/w/80/h/80",
                        "likes": 605
                    },
                    {
                        "user_id": 39,
                        "nickname": "like-卖萌机器人",
                        "avatar": "http://cdn.likeorz.com/avatar_39_1434619446.jpg?imageView2/1/w/80/h/80",
                        "likes": 1140
                    },
                    {
                        "user_id": 1072,
                        "nickname": "WGG",
                        "avatar": "http://cdn.likeorz.com/avatar_1072_1434431953.jpg?imageView2/1/w/80/h/80",
                        "likes": 1
                    },
                    {
                        "user_id": 178,
                        "nickname": "果酱",
                        "avatar": "http://cdn.likeorz.com/avatar_178_1433040430.jpg?imageView2/1/w/80/h/80",
                        "likes": 1889
                    },
                    {
                        "user_id": 401,
                        "nickname": "超高校級のガレージキットコン",
                        "avatar": "http://cdn.likeorz.com/avatar_401_1435058689.jpg?imageView2/1/w/80/h/80",
                        "likes": 8092
                    },
                    {
                        "user_id": 22,
                        "nickname": "Tilor",
                        "avatar": "http://cdn.likeorz.com/avatar_22_1433027475.jpg?imageView2/1/w/80/h/80",
                        "likes": 215
                    }
                ]
            },
            {
                "mark_id": 25902,
                "tag": "福利",
                "likes": 3,
                "is_liked": false,
                "created": 1434428028,
                "user": {
                    "user_id": 611,
                    "nickname": "Resid橘子",
                    "avatar": "http://cdn.likeorz.com/avatar_611_1435765248.jpg?imageView2/1/w/80/h/80",
                    "likes": 1351
                },
                "total_comments": 0,
                "comments": [
                ],
                "likers": [
                    {
                        "user_id": 173,
                        "nickname": "akaaa",
                        "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/1/w/80/h/80",
                        "likes": 1325
                    },
                    {
                        "user_id": 401,
                        "nickname": "超高校級のガレージキットコン",
                        "avatar": "http://cdn.likeorz.com/avatar_401_1435058689.jpg?imageView2/1/w/80/h/80",
                        "likes": 8092
                    }
                ]
            }
        ]
    }
}
```


######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|当前访问用户是否赞过|
|created|int|标签添加时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户获得赞数|
|next|int|下一页页号（没有下一页返回 false）|
