Mostly based on Spark.    
### How to use
#### login
post ```x-www-form-urlencoded``` data to ```"192.168.21.100:4567/login"```
```json
{
  "user_id": "zhizhonghwang@gmail.com",
  "password": "1111"
}
```
returns:    
if password is not correct
````json
{
    "isOk":false,
    "msg":"密码错误"
}
````
if password is correct(token is a string which **uniquely** identify one user)
```json
{
    "isOk":true,
    "msg":"登录成功",
    "user":{
        "user_id":"zhizhonghwang@gmail.com",
        "user_name":"Hzzone",
        "password":"1111",
        "token":"121212"
    }
}
```
if user does not exists
```json
{
    "isOk":false,
    "msg":"用户不存在"
}
```

#### register
First post data to ```"192.168.21.100:4567/register-1"```
```json
{
  "user_id": "1141408077@qq.com",
  "user_name": "Hzzone",
  "password": "1111"
}
```
if user exists
```json
{
    "isOk":false,
    "msg":"用户已存在"
}
```
if not exists, send verification email to user's email address, and returns
```json
{
    "isOk":true,
    "msg":"349952"
}
```

repeatedly post same data to ```"192.168.21.100:4567/register-2"```, returns
```json
{
    "isOk":true,
    "msg":"注册成功",
    "user":{
        "user_id":"1141408077@qq.com",
        "user_name":"Hzzone",
        "password":"1111",
        "token":"eevpxwfxdlaaldyymsggqasapawydk"
    }
}
```
```json
{
    "isOk":true,
    "user":{
        "user_id":"1141408077@qq.com",
        "user_name":"Hzzone",
        "password":"1111",
        "token":"nwvwyihjyjjabdiakogqdewdjadlaz"
    }
}
```