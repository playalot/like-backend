##Upload Token
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|

######请求示例
<pre>
curl -X POST -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39" http://api.likeorz.com/v2/upload/token
</pre>

    "code": "1", 
    "message": "Get Upload Token Success", 
    "data": {
        "upload_token": "_21aoq6xsdzUhwjBbjJs2MKtCcu2DwlOEF7N3YIz:vlxjasjo5CQYvoh4LeQrMURedkY=:eyJzY29wZSI6Imxpa2VvcnoiLCJkZWFkbGluZSI6MTQyODU2MDcwNX0="
    }
}
</pre>