package controller;

import config.*;
import db.Database;
import model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static webserver.RequestHandler.threadUuid;


public class UserController {

    public static HTTPResponse createAccount(HTTPRequest request) {
        HTTPResponse response = null;
        try {
            //request에서 구현된 Body의 hashMap을 이용
            String userId = request.getBody().get("userId");
            String password = request.getBody().get("password");
            String name = request.getBody().get("name");
            String email = request.getBody().get("email");

            //유저 아이디가 이미 존재하는 경우 예외 발생
            //그러나 현재 어떻게 처리할지 명시되지 않아서 catch문은 미구현
            if(Database.findUserById(userId)!=null)
                throw new ExceptionHandler.UserIdTaken("User id already in use");

            //유저 저장
            User user = new User(userId, password, name, email);
            Database.addUser(user);

            byte[] body = new byte[0];
            byte[] head = ("HTTP/1.1 " + ResponseCode.REDIRECT.code+ " " + ResponseCode.REDIRECT +"\r\n"+
                    "Location: /index.html" + "\r\n" +
                    "Content-Length: " + body.length  + "\r\n").getBytes();

            response = new HTTPResponse("HTTP/1.1",ResponseCode.REDIRECT.code, ResponseCode.REDIRECT.toString(), head, body);

        }catch(Exception e){
            byte[] body = new byte[0];
            byte[] head = ("HTTP/1.1" + ResponseCode.SERVER_ERROR.code + " " + ResponseCode.SERVER_ERROR +" \r\n"+
                    "Content-Length: " + body.length  + "\r\n").getBytes();
            response = new HTTPResponse("HTTP/1.1",ResponseCode.SERVER_ERROR.code, ResponseCode.SERVER_ERROR.toString(), head, body);
        }

        return response;
    }
    public static HTTPResponse login(HTTPRequest request) {
        System.out.println("OK0");
        //로그인이 되어 있다면 홈으로 리디렉션
        if (threadUuid.get() != null) {
            byte[] body = new byte[0];
            byte[] head = ("HTTP/1.1 " + ResponseCode.REDIRECT.code + " " + ResponseCode.REDIRECT + "\r\n" +
                    "Location: /index.html" + "\r\n" +
                    "Content-Length: " + body.length + "\r\n").getBytes();

            return new HTTPResponse("HTTP/1.1", ResponseCode.REDIRECT.code, ResponseCode.REDIRECT.toString(), head, body);
        }

        System.out.println("OK1");
        //유저 정보를 비교하기 위해 데이터베이스에서 불러온다
        String userId = request.getBody().get("userId");
        String password = request.getBody().get("password");
        User user = Database.findUserById(userId);

        HTTPResponse response = null;
        //로그인 실패하는 경우 리디렉션 (아이디 없거나 비밀번호 불일치)
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("OK2");
            byte[] body = new byte[0];
            byte[] head = ("HTTP/1.1 " + ResponseCode.REDIRECT.code + " " + ResponseCode.REDIRECT + "\r\n" +
                    "Location: /user/login_failed.html" + "\r\n" +
                    "Content-Length: " + body.length + "\r\n").getBytes();

            response = new HTTPResponse("HTTP/1.1", ResponseCode.REDIRECT.code, ResponseCode.REDIRECT.toString(), head, body);
        }
        // 로그인 성공시 세션 발급, index.html로 리디렉션
        else {
            System.out.println("OK3");
            UUID uuid = Session.createSession(userId);
            byte[] body = new byte[0];
            byte[] head = ("HTTP/1.1 " + ResponseCode.REDIRECT.code + " " + ResponseCode.REDIRECT + "\r\n" +
                    "Location: /index.html" + "\r\n" +
                    "Set-Cookie: sid=" + uuid +"; Path=/"+ "\r\n" +
                    "Content-Length: " + body.length + "\r\n").getBytes();
            response = new HTTPResponse("HTTP/1.1", ResponseCode.REDIRECT.code, ResponseCode.REDIRECT.toString(), head, body);
        }

        return response;

    }

    private Map<String,String> querytoMap(String query){
        Map<String, String> map = new HashMap<>();

        //&를 기준으로 한쌍으로 묶는다.
        String[] pairs = query.split("&");
        for(String pair:pairs){

            //url로 온 경우 @를 %40으로 인코딩 돼서 온다.
            // @는 url에서 의미를 갖기 때문. 따라서 다시 @로 변환해 주어야한다
            pair = pair.replace("%40","@");

            //=를 기준으로 분리한다
            String[] values = pair.split("=");

            //value값을 빈값으로 받았을 때는 널값으로 처리
            if(values.length == 1)
                map.put(values[0],null);
            else if(values.length == 2)
                map.put(values[0], values[1]);
        }
        return map;
    }
}
