package net.cqooc.tool.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    private String id;

    private String title;

    private String authorName;

    private String resSort;

    private String resMediaType;

    private String resSize;

    private String viewer;

    private String oid;

    private String username;

    private String resMediaType_lk_display;

    private String pages;

    private String duration;

    private String dimentsion;

    private String resourceType_lk_display;
}
