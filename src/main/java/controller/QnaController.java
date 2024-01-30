package controller;

import config.HTTPRequest;
import config.HTTPResponse;
import db.Database;
import model.Qna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static webserver.RequestHandler.threadUuid;

public class QnaController {
    public static HTTPResponse createQna(HTTPRequest request) {

        if(threadUuid.get() == null)
            return PageController.RedirectStaticPage("/user/login.html");

        System.out.println("{{{");
        System.out.println(request.getBody().keySet());
        Qna qna = parseToQna(request);
        Database.addQna(qna);




        return PageController.RedirectStaticPage("/index.html");

    }
    private static Qna parseToQna(HTTPRequest request){

        String title = null;
        String writer = null;
        String content = null;

        title = request.getBody().get("title");
        writer = request.getBody().get("writer");
        content = request.getBody().get("contents");


        return new Qna(writer,title,content);
    }
}

