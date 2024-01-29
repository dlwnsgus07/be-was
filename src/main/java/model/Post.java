package model;

import dto.PostDto;

import java.time.LocalDateTime;

public class Post {
    private Integer id;
    private final LocalDateTime createTime;
    private final String title;
    private final String author;
    private final String content;
//    private String file;

    public Post(LocalDateTime createTime, String title, String author, String content) {
        this.createTime = createTime;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    public static Post fromDto(PostDto postDto) {
        return new Post(postDto.getTime(), postDto.getTitle(), postDto.getAuthor(), postDto.getContent());
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Post{" +
                "createTime=" + createTime +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
