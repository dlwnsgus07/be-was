package service;

import db.Database;
import dto.PostDto;
import handler.StaticResponseHandler;
import model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;

public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    public void create(PostDto postDto) {
        Post post = Post.fromDto(postDto);
        Database.addPost(post);
        logger.debug("포스트 저장완료");
        logger.debug(post.toString());
    }

    private static class PostServiceHolder{
        private static final PostService INSTANCE = new PostService();
    }

    public static PostService getInstance() {
        return PostServiceHolder.INSTANCE;
    }

}
