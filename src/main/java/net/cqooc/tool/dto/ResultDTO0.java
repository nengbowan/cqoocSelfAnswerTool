package net.cqooc.tool.dto;

import lombok.*;
import net.cqooc.tool.domain.Meta;

import java.util.List;


@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO0 {

    private Meta meta;

    private List<Chapter1> data;
}
