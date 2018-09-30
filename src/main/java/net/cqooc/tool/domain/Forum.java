package net.cqooc.tool.domain;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Forum {

    private String id;

    private String title;

    private String status;

    private String content;


}
