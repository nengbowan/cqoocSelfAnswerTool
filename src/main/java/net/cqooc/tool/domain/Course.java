package net.cqooc.tool.domain;

import lombok.*;

/**
 * 课程  一个人 可以有多个选的课程
 */

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private String id ;

    private String title;

    private String coursePicUrl ;

    private String isQuality;

    private String school;

    private String isBuild;

    private String startDate;

    private String endDate;

    private String cmbody;

    private String publishDate;

    private String allSchool;

    private String schema;

}
