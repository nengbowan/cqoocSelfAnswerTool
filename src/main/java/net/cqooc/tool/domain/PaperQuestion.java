package net.cqooc.tool.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperQuestion {
    private String desc;

    private String type; // 0 单选题

    private String total ; //2

    private List<Question> questions;

}
