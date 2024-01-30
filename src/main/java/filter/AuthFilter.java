package filter;

import dto.HttpResponseDto;
import handler.DynamicHtmlHandler;
import handler.DynamicResponseHandler;
import handler.StaticResponseHandler;
import util.UrlControllerMapper;
import model.http.Status;
import model.http.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.Session;
import util.FileDetector;
import util.HtmlParser;

import java.util.*;

import static config.AppConfig.*;

public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private final StaticResponseHandler staticResponseHandler;
    private final DynamicResponseHandler dynamicResponseHandler;
    private static List<String> loginRequiredPath = List.of("/user/list", "/post/write", "/post/show");
    private final Set<String> dynamicUrl;
    private final DynamicHtmlHandler dynamicHtmlHandler;

    private static class AuthFilterHolder {
        public static final AuthFilter INSTANCE = new AuthFilter(staticResponseHandler(), dynamicResponseHandler(), dynamicHtmlHandler(), urlControllerMapper());
    }

    public static AuthFilter getInstance() {
        return AuthFilterHolder.INSTANCE;
    }

    public AuthFilter(StaticResponseHandler staticResponseHandler, DynamicResponseHandler dynamicResponseHandler, DynamicHtmlHandler dynamicHtmlHandler, UrlControllerMapper urlControllerMapper) {
        this.staticResponseHandler = staticResponseHandler;
        this.dynamicResponseHandler = dynamicResponseHandler;
        this.dynamicHtmlHandler = dynamicHtmlHandler;
        this.dynamicUrl = urlControllerMapper.getMappingUrls();
    }

    @Override
    public void init() {
        logger.debug("필터가 생성되었습니다.");
    }

    @Override
    public void doFilter(HttpRequest httpRequest, HttpResponseDto httpResponseDto) {
        boolean isLogin = false;
        String url = httpRequest.getStartLine().getPathUrl();

        if (httpRequest.getHeaders().hasCookie()) {
            isLogin = checkLogin(httpRequest);
        }
        if (loginRequiredPath.stream().anyMatch(path -> httpRequest.getStartLine().getPathUrl().startsWith(path)) && !isLogin) {
            httpResponseDto.setStatus(Status.REDIRECT);
            httpResponseDto.getOptionHeader().put("Location", "/user/login.html");
            return;
        }

        if(url.contains("?")){
            url = url.split("\\?")[0];
            logger.debug(url);
        }
        logger.debug(url);
        if (dynamicUrl.contains(url)) {
            logger.debug("동적인 response 전달");
            dynamicResponseHandler.handle(httpRequest, httpResponseDto);
        } else {
            logger.debug("정적인 response 전달");
            staticResponseHandler.handle(httpRequest, httpResponseDto);
        }
        dynamicHtmlHandler.handle(httpRequest, httpResponseDto, isLogin);
    }

    private boolean checkLogin(HttpRequest httpRequest) {
        String sessionId = httpRequest.getHeaders().getUserSessionId();
        return Session.loginCheck(UUID.fromString(sessionId));
    }

    @Override
    public void destroy() {
        logger.debug("필터가 삭제됩니다.");
    }
}
