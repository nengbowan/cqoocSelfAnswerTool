package net.cqooc.tool.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImportUser {

    private String username;

    private String password;

}
