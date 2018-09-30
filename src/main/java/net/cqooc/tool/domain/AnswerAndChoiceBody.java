package net.cqooc.tool.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerAndChoiceBody {

    //直接将 ["0","1","2"] 映射到 answer
    private String answer;

    private String[] choices;
}
