package net.cqooc.tool.domain;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    private String id;

    private String parentId;

    private Chapter chapter;

    private String courseId;

    private int category;

    private String title;

    private String resid;

    private Resource resource;

    private String testId;

//    private Test test;

    private String forumId;

    private Forum forum;

    private String ownerId;

    private String created ;

    private String lastUpdated;

    private String owner;

    private String chapterId;

    private String selfId;

    private String isLeader;
}
