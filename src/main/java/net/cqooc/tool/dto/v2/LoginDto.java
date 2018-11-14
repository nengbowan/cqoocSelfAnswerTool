package net.cqooc.tool.dto.v2;

import lombok.*;

import java.util.HashMap;

@Builder
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
   private String status;

   private Integer code;

   private String xsid;
}
