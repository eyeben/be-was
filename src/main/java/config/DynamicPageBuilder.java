package config;

import controller.PageController;
import db.Database;
import model.Qna;
import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static webserver.RequestHandler.threadUuid;

public class DynamicPageBuilder {
    public static HashMap<String, PageBuilder> dynamicPageBuilders = new HashMap<>();
    private static String STATIC_FILE_PATH = "/Users/user/IdeaProjects/be-was/src/main/resources/static";
    private static String TEMPLATE_FILE_PATH = "/Users/user/IdeaProjects/be-was/src/main/resources/templates";
    static{

        dynamicPageBuilders.put("/index",DynamicPageBuilder::buildIndex);
        dynamicPageBuilders.put("/qna/show",DynamicPageBuilder::buildQnaShow);
        dynamicPageBuilders.put("/qna/form",DynamicPageBuilder::buildQnaForm);
    }

    private static byte[] buildQnaForm(HTTPRequest request) {
        HTTPResponse response = PageController.getPageStatic(request);
        return response.getBody();
    }

    private static byte[] buildQnaShow(HTTPRequest request) throws Exception {
        byte[] body = null;
        String userId = Session.getUserId(threadUuid.get());
        User user = Database.findUserById(userId);
        String url = request.getUrl();
        String[] urlSplit = url.split("/");
        Long qnaId = Long.parseLong(urlSplit[urlSplit.length-1]);
        File file = new File( TEMPLATE_FILE_PATH + url.replace("/"+urlSplit[urlSplit.length-1], ""));
        BufferedReader bf = new BufferedReader(new FileReader(file));
        Qna qna = Database.findQnaById(qnaId);
        String line;
        StringBuilder sb = new StringBuilder();




        while ((line = bf.readLine()) != null) {
            if (line.contains("role=\"button\">회원가입</a></li>")) {
                sb.append("<li><a href=\"#\" role=\"button\">로그아웃</a></li>\n" +
                        "                <li><a href=\"#\" role=\"button\">개인정보수정</a></li>");
                continue;
            }
            else if (line.contains("role=\"button\">로그인</a></li>")) {
                sb.append(line.replace("role=\"button\">", "class=\"disabled\" role=\"button\">").replace("로그인", user.getName()));
                sb.append(System.lineSeparator());
                continue;
            }
            if (line.contains("제목"))
                sb.append(line.replace("제목", qna.getTitle()));

            else if (line.contains("작성자"))
                sb.append(line.replace("작성자", qna.getWriter()));

            else if(line.contains("작성시간")) {
                String parsedLocalDateTimeNow = qna.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                sb.append(line.replace("작성시간", parsedLocalDateTimeNow));
            }

            else if(line.contains("내용"))
                sb.append(line.replace("내용", qna.getContent()));

            else
                sb.append(line);
            sb.append(System.lineSeparator());
        }
        body = sb.toString().getBytes();
        return body;
    }


    private static byte[] buildIndex(HTTPRequest request) throws Exception {

        byte[] body = null;
        String userId = Session.getUserId(threadUuid.get());
        User user = Database.findUserById(userId);

        String url = request.getUrl();
        File file = new File( TEMPLATE_FILE_PATH + url);
        BufferedReader bf = new BufferedReader(new FileReader(file));

        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = bf.readLine()) != null) {
            // html 일부분 수정
            if (line.contains("role=\"button\">회원가입</a></li>")) {
                sb.append("<li><a href=\"#\" role=\"button\">로그아웃</a></li>\n" +
                        "                <li><a href=\"#\" role=\"button\">개인정보수정</a></li>");
                continue;
            }
            else if (line.contains("role=\"button\">로그인</a></li>")) {
                sb.append(line.replace("role=\"button\">", "class=\"disabled\" role=\"button\">").replace("로그인", user.getName()));
                sb.append(System.lineSeparator());
                continue;
            }

            sb.append(line);
            sb.append(System.lineSeparator());
            // \n 을 사용하면 안되는 이유:
            // 운영체제마다 줄바꿈 형식이 다름, System.lineSeparator를 사용하면
            // 운영체제에 맞게 JVM가 설정해줌
        }

        body = sb.toString().getBytes();
        return body;
    }
}
