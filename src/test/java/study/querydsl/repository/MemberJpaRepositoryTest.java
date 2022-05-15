package study.querydsl.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    TeamJpaRepository teamJpaRepository;

    @Test
    public void basic_test() throws Exception {
        // given
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        // when
        Member findMember = memberJpaRepository.findById(member1.getId()).get();
        List<Member> members = memberJpaRepository.findAllQuerydsl();
        List<Member> member1s = memberJpaRepository.findByUsernameQuerydsl("member1");

        // then
        Assertions.assertEquals(member1, findMember);
        Assertions.assertEquals(member1, members.get(0));
        Assertions.assertEquals(member1, member1s.get(0));
    }

    @Test
    public void builder_test() throws Exception{
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamJpaRepository.save(teamA);
        teamJpaRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        memberJpaRepository.save(member4);

        // when
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(
                new MemberSearchCondition(null, "teamA", 11, 22)
        );

        // then
        Assertions.assertEquals(result.get(0).getUsername(), member2.getUsername());
    }

    @Test
    public void where_test() throws Exception{
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamJpaRepository.save(teamA);
        teamJpaRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        memberJpaRepository.save(member4);

        // when
        List<MemberTeamDto> result = memberJpaRepository.search(
                new MemberSearchCondition(null, "teamA", 11, 22)
        );

        // then
        Assertions.assertEquals(result.get(0).getUsername(), member2.getUsername());
    }
}