import com.google.gson.Gson;
import javafx.geometry.Pos;
import jdk.nashorn.internal.scripts.JD;
import spark.Filter;
import spark.Request;
import spark.Response;
import sun.security.jgss.GSSCaller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        enableCORS("*", "*", "*");

        get("/hello", (request, response) -> "Hello World");

        get("/login", (request, response) -> {
            return "login page";
        });

        post("/login", ((request, response) -> {
            String user_id = request.queryParams("user_id");
            String password = request.queryParams("password");
            log(user_id + " " + password);
            if(password == null || user_id == null)
                return "empty value";
            User user = new User();
            user.setPassword(password);
            user.setUser_id(user_id);
            if(user.isExists()) {
                user = user.getUser();
                if (password.equals(user.getPassword())){
                    response.cookie("user_id", user.getUser_id());
                    response.cookie("password", user.getPassword());
                    Gson gson = new Gson();
                    return "{\"isOk\":true, \"msg\":\"登录成功\",\"user\":"+gson.toJson(user)+"}";
                }
                else return "{\"isOk\":false, \"msg\":\"密码错误\"}";
            }
            else
                return "{\"isOk\":false, \"msg\":\"用户不存在\"}";
        }));

        /**
         * 注册1：发送验证码
         */
        post("/register-1", (((request, response) -> {
            String user_id = request.queryParams("user_id");
            String password = request.queryParams("password");
            String user_name = request.queryParams("user_name");
            Functions.log(user_id+ "\t"+user_name + "\t"+password);
            if (user_id==null||user_name==null||password==null)
                return "empty value";
            User user = new User();
            user.setUser_id(user_id);
            user.setPassword(password);
            String code = null;
            if (user.isExists()){
                user = user.getUser();
                return "{\"isOk\":false, \"msg\":\"用户已存在\""+"}";
            }
            else {
                user.setToken(Functions.getRandomString(30));
                code = Functions.getRandomString(6);
                Functions.sendEmail("验证码", code, user_id);
            }
            return "{\"isOk\":true, \"msg\":\""+code+"\"}";
        })));

        /**
         * 登录2：插入数据库
         */
        post("/register-2", (((request, response) -> {
            String user_id = request.queryParams("user_id");
            String password = request.queryParams("password");
            String user_name = request.queryParams("user_name");
            Functions.log(user_id+ "\t"+user_name + "\t"+password);
            if (user_id==null||user_name==null||password==null)
                return "empty value";
            User user = new User();
            user.setPassword(password);
            user.setUser_id(user_id);
            user.setUser_name(user_name);
            Jdbc jdbc = new Jdbc();
            while (true) {
                String token = Functions.getRandomString(30);
                String sql = "select * from public.user where token='" + token + "'";
                Functions.log(sql);
                ResultSet rs = jdbc.querydata(sql);
                List<User> users = Functions.getUserList(rs);
                if (users.size() == 0) {
                    user.setToken(token);
                    break;
                }
            }
            String sql = String.format("insert into public.user(user_id, user_name, password, token) values('%s', '%s', '%s', '%s')"
                    , user.getUser_id(), user.getUser_name(), user.getPassword(), user.getToken());
            Functions.log(sql);
            jdbc.save(sql);
            return String.format("{\"isOk\":true, \"msg\":\"%s\",\"user\":%s}", "注册成功", user.toString());
        })));

        /**
         * 获得帖子的列表
         */
        get("/post", ((((request, response) -> {
            int num = 3;
            Jdbc jdbc = new Jdbc();
            String sql = "SELECT user_id, user_name, pic, post_id, title, time, content, liked FROM public.user NATURAL JOIN public.post ORDER BY time DESC";
            ResultSet rs = jdbc.querydata(sql);
            List<Post> posts = new ArrayList<>();
            int i = 0;
            Gson gson = new Gson();
            while (rs.next() && i<num){
                Post post = new Post();
                post.setContent(rs.getString("content"));
                post.setPost_id(rs.getString("post_id"));
                post.setTime(rs.getDate("time"));
                post.setTitle(rs.getString("title"));
                post.setAuthor_name(rs.getString("user_name"));
                post.setAuthor_id(rs.getString("user_id"));
                post.setAuthor_pic(rs.getString("pic"));
                post.setLiked(rs.getInt("liked"));
                posts.add(post);
                i++;
            }
            System.out.println("h3");
            return String.format("{"+
                    "\"isOk\": true,"+
            "\"msg\": \"获取成功\","+
                    "\"posts:\": %s}", gson.toJson(posts)).toString();
        }))));


        post("/post", ((((request, response) -> {
            String title = request.queryParams("title");
            String content = request.queryParams("content");
            String user_id = request.queryParams("user_id");
            if (title==null||content==null||user_id==null)
                return "empty value";
            Date date = new Date();
            Post post = new Post();
            post.setContent(content);
            post.setTitle(title);
            post.setTime(date);
            Jdbc jdbc = new Jdbc();
            while (true) {
                String post_id = Functions.getRandomString(30);
                String sql = "select * from public.post where post_id='" + post_id + "'";
                Functions.log(sql);
                ResultSet rs = jdbc.querydata(sql);
                List<String> posts = new ArrayList<>();
                try{
                    while (rs.next())
                        posts.add(rs.getString(1));
                } catch (SQLException e){
                    e.printStackTrace();
                }
                if (posts.size() == 0) {
                    post.setPost_id(post_id);
                    break;
                }
            }
            Gson gson = new Gson();
            String sql = String.format("insert into public.post values('%s', '%s', '%s', '%s', '%s')", post.getAuthor_id(), post.getPost_id(), post.getTitle(), post.getTime(), post.getContent());
            Functions.log(sql);
            jdbc.save(sql);
            return "{\"isOk\":true, \"msg\":\"发表成功\", \"post\":"+gson.toJson(post)+"}";
        }))));

    }

    private static void log(String s){
        System.out.println(s);
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        before(new Filter() {
            @Override
            public void handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", origin);
                response.header("Access-Control-Request-Method", methods);
                response.header("Access-Control-Allow-Headers", headers);
            }
        });
    }

}