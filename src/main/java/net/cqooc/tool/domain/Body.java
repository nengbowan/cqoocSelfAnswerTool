package net.cqooc.tool.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Body {

    private String desc;

    private String type;

    private String total;

    private List<Question> questions;
}
