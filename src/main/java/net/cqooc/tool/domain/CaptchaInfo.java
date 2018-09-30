package net.cqooc.tool.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaInfo {

    private String key ;

    private String img ;
}
