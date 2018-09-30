package net.cqooc.tool.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paper {

    private String id;

    private String created;

    private String lastUpated;

    private String ownerId;

    private String title;

    private String courseId;

    private String parentId;

    private String status;

    private String submitEnd;

    private String number;

    private String content;

    private String gradeId;

    private String owner;

    private String score;

    private List<PaperQuestion> body;

}
