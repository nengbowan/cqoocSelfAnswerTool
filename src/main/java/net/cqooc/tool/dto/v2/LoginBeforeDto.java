package net.cqooc.tool.dto.v2;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginBeforeDto {

    private String nonce;

}
