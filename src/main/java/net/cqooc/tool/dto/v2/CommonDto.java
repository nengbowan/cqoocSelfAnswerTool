package net.cqooc.tool.dto.v2;
import lombok.*;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonDto {
    private LoginResponseDto response;

    private String cookie;

    private int code;

    private String captchaToken;

}
