##Search Posts

搜索图片

######URL

http://api.likeorz.com/v1/search/tag/高达?ts=123123

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|

######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data": {
        "info": {
            "id": 123,
            "name": "xxxx",
            "description": "xxxxxxxx",
            "avatar": "avatar_123_xxxxxxx.jpg"
        },
        "posts": [
            {
                "post_id": 8298,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/w/620/h/620/q/85",
                "thumbnail": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/w/320/h/320/q/85",
                "preview": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/w/960/q/85",
                "raw_image": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/q/85",
                "created": 1434688257,
                "user": {
                    "user_id": 401,
                    "nickname": "Kamiko丸",
                    "avatar": "http://cdn.likeorz.com/avatar_401_1435058689.jpg?imageView2/1/w/80/h/80",
                    "likes": 6105
                },
               "marks": [
                   {
                       "mark_id": 409295,
                       "tag": "其实右上角有举报的",
                       "likes": 61,
                       "is_liked": false
                   },
                   {
                       "mark_id": 409294,
                       "tag": "警察正在治我",
                       "likes": 46,
                       "is_liked": false
                   }
               ]
            },
            {
                "post_id": 8332,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/b3e6ad1a26d740a8910d4d985d0bcde8_1434723645_w_640_h_640_110.jpg?imageView2/1/w/620/h/620",
                "content": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/w/620/h/620/q/85",
                "thumbnail": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/w/320/h/320/q/85",
                "preview": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/w/960/q/85",
                "raw_image": "http://cdn.likeorz.com/758304e5bdec42d782b01c7926231f41_1442632646_w_1280_h_1707_13473.jpg?imageView2/0/q/85",
                "created": 1434723646,
                "user": {
                    "user_id": 110,
                    "nickname": "Amo",
                    "avatar": "http://cdn.likeorz.com/avatar_110_1423842618.jpg?imageView2/1/w/80/h/80",
                    "likes": 484
                },
                "marks": [
                    {
                        "mark_id": 409295,
                        "tag": "其实右上角有举报的",
                        "likes": 61,
                        "is_liked": false
                    },
                    {
                        "mark_id": 409294,
                        "tag": "警察正在治我",
                        "likes": 46,
                        "is_liked": false
                    },
                ]
            }
        ]
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
