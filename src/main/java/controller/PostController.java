package controller;

import annotation.Controller;
import annotation.PostMapping;
import annotation.RequestMapping;
import config.AppConfig;
import dto.HttpResponseDto;
import dto.PostDto;
import model.http.Status;
import model.http.request.HttpRequest;
import service.PostService;
import session.Session;

import java.util.UUID;

import static config.AppConfig.*;

@Controller
@RequestMapping(value = "/post")
public class PostController {
    private static class PostControllerHolder{
        public static final PostController INSTANCE = new PostController(postService());
    }

    public static PostController getInstance() {
        return PostControllerHolder.INSTANCE;
    }

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public void handlePostCreateRequest(HttpRequest httpRequest, HttpResponseDto httpResponseDto) {
        String sessionId = httpRequest.getHeaders().getUserSessionId();
        String loginUserId = Session.getLoginUserId(UUID.fromString(sessionId));
        PostDto postDto = PostDto.fromBody(httpRequest.getBody().getContent(), loginUserId);
        postService.create(postDto);
        redirectToPath(httpResponseDto, "/index.html");
    }

    private void redirectToPath(HttpResponseDto httpResponseDto, String path) {
        httpResponseDto.setStatus(Status.REDIRECT);
        httpResponseDto.addHeader("Location", path);
    }
}
