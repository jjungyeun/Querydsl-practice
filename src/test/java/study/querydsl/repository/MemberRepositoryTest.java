package study.querydsl.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Test
    public void basic_test() throws Exception {
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);

        // when
        Member findMember = memberRepository.findById(member1.getId()).get();
        List<Member> members = memberRepository.findAll();
        List<Member> member1s = memberRepository.findByUsername("member1");

        // then
        Assertions.assertEquals(member1, findMember);
        Assertions.assertEquals(member1, members.get(0));
        Assertions.assertEquals(member1, member1s.get(0));
    }

    @Test
    public void where_test() throws Exception{
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        // when
        List<MemberTeamDto> result = memberRepository.search(
                new MemberSearchCondition(null, "teamA", 11, 22)
        );

        // then
        Assertions.assertEquals(result.get(0).getUsername(), member2.getUsername());
    }

    @Test
    public void search_page_simple() throws Exception{
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        PageRequest pageRequest = PageRequest.of(0, 3);

        // when
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(new MemberSearchCondition(), pageRequest);

        // then
        Assertions.assertEquals(3, result.getSize());
    }

    @Test
    public void search_page_complex() throws Exception{
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        PageRequest pageRequest = PageRequest.of(0, 3);

        // when
        Page<MemberTeamDto> result = memberRepository.searchPageComplex(new MemberSearchCondition(), pageRequest);

        // then
        Assertions.assertEquals(3, result.getSize());
    }
}