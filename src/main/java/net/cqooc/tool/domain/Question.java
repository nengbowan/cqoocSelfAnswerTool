package net.cqooc.tool.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String points;

    private String question;

    private AnswerAndChoiceBody body;

    private String type;

    private String id;
}
