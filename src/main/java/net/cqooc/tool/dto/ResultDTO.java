package net.cqooc.tool.dto;

import lombok.*;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {
    private String response;

    private String cookie;

    private int code;

    private String captchaToken;
}
