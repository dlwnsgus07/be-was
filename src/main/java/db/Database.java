package db;

import com.google.common.collect.Maps;
import model.Post;
import model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class Database {
    private static final Map<String, User> users = Maps.newHashMap();
    private static final Map<Integer, Post> posts = Maps.newHashMap();
    private static int cnt = 0;

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }
    public static Collection<Post> findAllPosts(){
        return posts.values();
    }

    public static void addPost(Post post) {
        posts.put(++cnt, post);
    }
    public static Optional<User> findUserById(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public static Collection<User> findAll() {
        return users.values();
    }
    public static void clear(){
        users.clear();
    }
}
