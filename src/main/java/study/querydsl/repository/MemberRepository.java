package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 메소드 이름으로 자동으로 만들어줌
    // select m from Member m where m.username = :username
    List<Member> findByUsername(String username);
}
