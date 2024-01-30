package handler;

import config.AppConfig;
import db.Database;
import dto.HttpResponseDto;
import exception.BadRequestException;
import model.Post;
import model.User;
import model.http.ContentType;
import model.http.Status;
import model.http.request.HttpRequest;
import session.Session;
import util.FileDetector;
import util.HtmlParser;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String pathUrl = httpRequest.getStartLine().getPathUrl();
        if(pathUrl.endsWith("index.html")){
            handleMainPageRequest(httpRequest, httpResponseDto);
        }
        if(pathUrl.startsWith("/post/show")){
            handlePostShowRequest(httpRequest, httpResponseDto);
        }
        if(pathUrl.endsWith(".html")){
            handleAllRequest(httpRequest, httpResponseDto, isLogin);
        }
        if (httpResponseDto.getStatus() == Status.BAD_REQUEST | httpResponseDto.getStatus() == Status.INTERNAL_SERVER_ERROR) {
            handleErrorRequest(httpResponseDto);
        }
        if (httpResponseDto.getStatus() == Status.NOT_FOUND) {
            handleNotFoundRequest(httpResponseDto);
        }
    }

    private void handleNotFoundRequest( HttpResponseDto httpResponseDto) {
        HtmlParser htmlParser;
        htmlParser = new HtmlParser(new String(fileDetector.getFile("/error/not_found.html")));
        httpResponseDto.setContentType(ContentType.HTML);
        httpResponseDto.setContent(htmlParser.getHtml());
        httpResponseDto.setContentLength(htmlParser.getHtml().getBytes().length);
    }

    private void handleErrorRequest(HttpResponseDto httpResponseDto) {
        HtmlParser htmlParser;
        htmlParser = new HtmlParser(new String(fileDetector.getFile("/error/not_allowed.html")));
        String errorMessage = httpResponseDto.getContent();
        httpResponseDto.setStatus(Status.NOT_ALLOWED);
        htmlParser.appendContentById("message", errorMessage);
        httpResponseDto.setContentType(ContentType.HTML);
        httpResponseDto.setContent(htmlParser.getHtml());
        httpResponseDto.setContentLength(htmlParser.getHtml().getBytes().length);
    }

    public void handlePostShowRequest(HttpRequest httpRequest, HttpResponseDto httpResponseDto) {
        HtmlParser htmlParser;
        if (httpResponseDto.getContent() == null) {
            htmlParser = new HtmlParser(new String(fileDetector.getFile("/post/show.html")));
        } else {
            htmlParser = new HtmlParser(httpResponseDto.getContent());
        }
        String pathUrl = httpRequest.getStartLine().getPathUrl();
        String id = parseIdFromUrl(pathUrl);
        Optional<Post> postById = Database.findPostById(Integer.parseInt(id));

        if(postById.isPresent()){
            Post post = postById.get();
            htmlParser.appendContentByClass("post-title", post.getTitle());
            htmlParser.appendContentByClass("article-author-name", post.getAuthor());
            htmlParser.appendContentByClass("article-header-time", post.getCreateTime().toLocalDate().toString());
            htmlParser.appendContentByClass("article-doc", post.getContent());
        }else{
            throw new BadRequestException("잘못된 ID를 입력하였습니다.");
        }
        httpResponseDto.setContentType(ContentType.HTML);
        httpResponseDto.setContent(htmlParser.getHtml());
        httpResponseDto.setContentLength(htmlParser.getHtml().getBytes().length);
    }

    private String parseIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("\\?id=(\\d+)");
        Matcher matcher = pattern.matcher(url);

        return matcher.find() ? matcher.group(1) : null;
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
                    .append("<a href=\"./post/show?id=").append(post.getId()).append("\">").append(post.getTitle())
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
        htmlParser.appendContentById("posts", stringBuilder.toString());
        httpResponseDto.setContentType(fileDetector.getContentType(httpRequest.getHeaders().getAccept(), httpRequest.getStartLine().getPathUrl()));
        httpResponseDto.setContent(htmlParser.getHtml());
        httpResponseDto.setContentLength(htmlParser.getHtml().getBytes().length);
    }


    private void handleAllRequest(HttpRequest httpRequest, HttpResponseDto httpResponseDto, boolean isLogin) {
        HtmlParser htmlParser;
        if (httpResponseDto.getContent() == null) {
            htmlParser = new HtmlParser(new String(fileDetector.getFile("/post.show.html")));
        } else {
            htmlParser = new HtmlParser(httpResponseDto.getContent());
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<ul class=\"nav navbar-nav navbar-right\">");
        if (isLogin) {
            String sessionId = httpRequest.getHeaders().getUserSessionId();
            String loginUserId = Session.getLoginUserId(UUID.fromString(sessionId));
            Optional<User> user = Database.findUserById(loginUserId);
            stringBuilder.append("<li class=\"active\"><a href=\"../index.html\">Posts</a></li>")
                    .append("<li><a>").append(user.get().getName()).append("님 환영합니다.</a></li>")
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
