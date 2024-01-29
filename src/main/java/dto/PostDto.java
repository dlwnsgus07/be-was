package dto;

import exception.BadRequestException;
import model.Post;
import model.http.request.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;

public class PostDto {
    LocalDateTime time;
    String title;
    String content;
    String author;
//    String file;

    public PostDto(String title, String content, String author) {
        this.time = LocalDateTime.now();
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public static PostDto fromBody(String requestBody, String user) {
        try {
            HashMap<String, String> map = new HashMap<>();
            if (!requestBody.isEmpty()) {
                String[] pairs = requestBody.split("&");

                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    String key = keyValue[0];
                    String value = keyValue[1];
                    map.put(key, value);
                }
            }
            map.forEach((key, value) -> map.put(key, URLDecoder.decode(value, StandardCharsets.UTF_8)));
            return new PostDto(map.get("title"), map.get("content"), user);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new BadRequestException("Please fill in all the necessary factors", e);
        }
    }
}
