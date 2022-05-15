package study.querydsl.dto;

import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private String username;
    private int age;
}
