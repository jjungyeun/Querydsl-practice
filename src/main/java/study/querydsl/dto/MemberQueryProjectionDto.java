package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MemberQueryProjectionDto {
    private String username;
    private int age;

    @QueryProjection    // -> compileQuerydsl 하면 Q파일이 생성됨
    public MemberQueryProjectionDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
