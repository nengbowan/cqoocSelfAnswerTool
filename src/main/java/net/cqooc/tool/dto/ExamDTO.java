package net.cqooc.tool.dto;

import lombok.*;
import net.cqooc.tool.domain.Meta;
import net.cqooc.tool.domain.Paper;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    private Meta meta;

    private List<Paper> data;
}
