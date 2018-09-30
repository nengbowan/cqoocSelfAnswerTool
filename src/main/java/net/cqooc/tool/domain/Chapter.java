package net.cqooc.tool.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    private String id;

    private String title;

    private String status;

    private String level;
}
