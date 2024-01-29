package handler;

import config.AppConfig;
import db.Database;
import dto.HttpResponseDto;
import model.Post;
import model.http.request.HttpRequest;
import util.FileDetector;
import util.HtmlParser;

import java.io.File;
import java.util.Collection;

public class DynamicHtmlHandler{
    private static class DynamicHtmlHandlerHolder {
        private static DynamicHtmlHandler INSTANCE = new DynamicHtmlHandler(AppConfig.fileDetector());
    }

    private final FileDetector fileDetector;

    public DynamicHtmlHandler(FileDetector fileDetector) {
        this.fileDetector = fileDetector;
    }

    public static DynamicHtmlHandler getInstance() {
        return DynamicHtmlHandlerHolder.INSTANCE;
    }

    public void handle(HttpRequest httpRequest, HttpResponseDto httpResponseDto, boolean isLogin) {
        handleAllRequest(httpRequest, httpResponseDto, isLogin);
        handleMainPageRequest(httpRequest, httpResponseDto);
    }

    private void handleMainPageRequest(HttpRequest httpRequest, HttpResponseDto httpResponseDto) {
        HtmlParser htmlParser;
        if (httpResponseDto.getContent() == null) {
            htmlParser = new HtmlParser(new String(fileDetector.getFile(httpRequest.getStartLine().getPathUrl())));
        } else {
            htmlParser = new HtmlParser(httpResponseDto.getContent());
        }
        StringBuilder stringBuilder = new StringBuilder();
        Collection<Post> posts = Database.findAllPosts();
        for (Post post : posts) {
            stringBuilder.append("<li>")
                    .append("<div class=\"wrap\">")
                    .append("<div class=\"main\">")
                    .append("<strong class=\"subject\">")
                    .append("<a href=\"./qna/show.html\">").append(post.getTitle())
                    .append("</a>")
                    .append("</strong>")
                    .append("<div class=\"auth-info\">")
                    .append("<i class=\"icon-add-comment\"></i>")
                    .append("<span class=\"time\">").append(post.getCreateTime().toLocalDate().toString())
                    .append("</span>")
                    .append("<a href=\"\" class=\"author\">").append(post.getAuthor()).append("</a>")
                    .append("</div>")
                    .append("</div>")
                    .append("</li>");
        }
//        <li>
//                  <div class="wrap">
//                      <div class="main">
//                          <strong class="subject">
//                              <a href="./qna/show.html">runtime 에 reflect 발동 주체 객체가 뭔지 알 방법이 있을까요?</a>
//                          </strong>
//                          <div class="auth-info">
//                              <i class="icon-add-comment"></i>
//                              <span class="time">2016-01-05 18:47</span>
//                              <a href="./user/profile.html" class="author">김문수</a>
//                          </div>
//                          <div class="reply" title="댓글">
//                              <i class="icon-reply"></i>
//                              <span class="point">12</span>
//                          </div>
//                      </div>
//                  </div>
//              </li>
        htmlParser.appendContentById("posts", stringBuilder.toString());
        httpResponseDto.setContentType(fileDetector.getContentType(httpRequest.getHeaders().getAccept(), httpRequest.getStartLine().getPathUrl()));
        httpResponseDto.setContent(htmlParser.getHtml());
        httpResponseDto.setContentLength(htmlParser.getHtml().getBytes().length);
    }


    private void handleAllRequest(HttpRequest httpRequest, HttpResponseDto httpResponseDto, boolean isLogin) {
        HtmlParser htmlParser;
        if (httpResponseDto.getContent() == null) {
            htmlParser = new HtmlParser(new String(fileDetector.getFile(httpRequest.getStartLine().getPathUrl())));
        } else {
            htmlParser = new HtmlParser(httpResponseDto.getContent());
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<ul class=\"nav navbar-nav navbar-right\">");
        if (isLogin) {
            stringBuilder.append("<li class=\"active\"><a href=\"../index.html\">Posts</a></li>")
                    .append("<li><a href=\"../user/logout\" role=\"button\">로그아웃</a></li>")
                    .append("<li><a href=\"#\" role=\"button\">개인정보수정</a></li>");
        } else {
            stringBuilder.append("<li class=\"active\"><a href=\"../index.html\">Posts</a></li>")
                    .append("<li><a href=\"../user/login.html\" role=\"button\">로그인</a></li>")
                    .append("<li><a href=\"../user/form.html\" role=\"button\">회원가입</a></li>");
        }
        stringBuilder.append("</ul>");
        htmlParser.appendContentById("navbar-collapse2", stringBuilder.toString());
        httpResponseDto.setContentType(fileDetector.getContentType(httpRequest.getHeaders().getAccept(), httpRequest.getStartLine().getPathUrl()));
        httpResponseDto.setContent(htmlParser.getHtml());
        httpResponseDto.setContentLength(htmlParser.getHtml().getBytes().length);
    }
}
