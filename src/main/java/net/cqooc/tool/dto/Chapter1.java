package net.cqooc.tool.dto;

import lombok.*;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter1 {

    private String id;

    private String title;

    private String unitId;

    private Chapter1 chapter;

    private String submitEnd;

}
