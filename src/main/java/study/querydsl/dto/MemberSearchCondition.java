package study.querydsl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchCondition {
    // 회원명, 팀명, 나이(최소, 최대)
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
