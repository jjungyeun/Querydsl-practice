package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberQueryProjectionDto;
import study.querydsl.dto.QMemberQueryProjectionDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    public void setUp() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void start_jpql() throws Exception {
        // member1 ??????
        String qlString = "select m from Member m " +
                "where m.username = :username";

        Member findMember1 = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertEquals("member1", findMember1.getUsername());
    }

    @Test
    public void start_querydsl() throws Exception {
        // QMember??? ????????? ???????????? ??????
//        QMember m = QMember.member;
//
//        Member findMember1 = queryFactory
//                .select(m)
//                .from(m)
//                .where(m.username.eq("member1"))    // ???????????? ????????? ??????
//                .fetchOne();

        // QMember??? static import??? ???????????? ??????
        Member findMember1 = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))    // ???????????? ????????? ??????
                .fetchOne();

        Assertions.assertEquals("member1", findMember1.getUsername());
    }

    @Test
    public void search_and() throws Exception {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        Assertions.assertEquals("member1", findMember.getUsername());
        Assertions.assertEquals(10, findMember.getAge());
    }

    @Test
    public void search_and_param() throws Exception {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),   // where??? ????????? ????????? ????????? and??? ?????????
                        (member.age.eq(10)))
                .fetchOne();

        Assertions.assertEquals("member1", findMember.getUsername());
        Assertions.assertEquals(10, findMember.getAge());
    }

    @Test
    public void result_fetch_test() throws Exception {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Assertions.assertThrows(NonUniqueResultException.class, () -> {
            Member fetchOne = queryFactory
                    .selectFrom(member)
                    .fetchOne();
        });

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        long count = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();
        Assertions.assertEquals(4, count);
    }

    /**
     * ?????? ?????? ??????
     * 1. ?????? ?????? ???????????? (desc)
     * 2. ?????? ?????? ???????????? (asc)
     * ???, 2?????? ?????? ????????? ????????? ???????????? ?????? (nulls last)
     */
    @Test
    public void sort() throws Exception {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        // when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        // then
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        Assertions.assertEquals("member5", member5.getUsername());
        Assertions.assertEquals("member6", member6.getUsername());
        Assertions.assertNull(memberNull.getUsername());
    }

    @Test
    public void paging1() throws Exception {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  // 0?????? ?????? (zero index)
                .limit(2)   // ?????? 2??? ??????
                .fetch();

        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void aggregation() throws Exception {
        Tuple result  // querydsl??? ???????????? ??????
                = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetchOne();

        assert result != null;
        Assertions.assertEquals(4, result.get(member.count()));
        Assertions.assertEquals(100, result.get(member.age.sum()));
        Assertions.assertEquals(25, result.get(member.age.avg()));
        Assertions.assertEquals(40, result.get(member.age.max()));
        Assertions.assertEquals(10, result.get(member.age.min()));
    }

    /**
     * ?????? ????????? ??? ?????? ?????? ????????? ?????????.
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertEquals("teamA", teamA.get(team.name));
        Assertions.assertEquals(15, teamA.get(member.age.avg()));
        Assertions.assertEquals("teamB", teamB.get(team.name));
        Assertions.assertEquals(35, teamB.get(member.age.avg()));
    }

    /**
     * ??? A??? ????????? ?????? ??????
     */
    @Test
    public void join() throws Exception{
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .orderBy(member.username.asc())
                .fetch();
        Assertions.assertEquals("member1", result.get(0).getUsername());
        Assertions.assertEquals("member2", result.get(1).getUsername());
    }


    /**
     * ?????? ??????
     * ????????? ????????? ??? ????????? ?????? ????????? ??????
     * (???????????? ?????? ?????? ??????)
     */
    @Test
    public void theta_join() throws Exception{
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .orderBy(member.username.asc())
                .fetch();

        Assertions.assertEquals("teamA", result.get(0).getUsername());
        Assertions.assertEquals("teamB", result.get(1).getUsername());
    }

    /**
     * ????????? ?????? ???????????????, ??? ????????? teamA??? ?????? ????????????, ????????? ?????? ??????
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     * -- ?????? --
     * [Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
     * [Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
     * [Member(id=5, username=member3, age=30), null]
     * [Member(id=6, username=member4, age=40), null]
     */
    @Test
    public void join_on_filtering() throws Exception{
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("" + tuple);
        }
    }

    /**
     * ???????????? ?????? ????????? ?????? ??????
     * ????????? ????????? ??? ????????? ?????? ????????? ?????? ??????
     * -- ?????? --
     * [Member(id=3, username=member1, age=10), null]
     * [Member(id=4, username=member2, age=20), null]
     * [Member(id=5, username=member3, age=30), null]
     * [Member(id=6, username=member4, age=40), null]
     * [Member(id=7, username=teamA, age=0), Team(id=1, name=teamA)]
     * [Member(id=8, username=teamB, age=0), Team(id=2, name=teamB)]
     * [Member(id=9, username=teamC, age=0), null]
     */
    @Test
    public void join_on_no_relation() throws Exception{
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("" + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void no_fetch_join() throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertFalse(loaded);
    }

    @Test
    public void use_fetch_join() throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertTrue(loaded);
    }

    /**
     * ????????? ?????? ?????? ?????? ??????
     */
    @Test
    public void sub_query() throws Exception{
        QMember subM = new QMember("subM");

        Member member = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq( // 40
//                        JPAExpressions.select(subM.age.max())
                        select(subM.age.max())  // static import ??????
                                .from(subM)
                ))
                .fetchOne();
        Assertions.assertEquals(40, member.getAge());
    }

    /**
     * ????????? ?????? ????????? ??????
     */
    @Test
    public void sub_query_goe() throws Exception{
        QMember subM = new QMember("subM");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(subM.age.avg())
                                .from(subM)
                ))
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * ????????? 10??? ????????? ??????
     */
    @Test
    public void sub_query_in() throws Exception{
        QMember subM = new QMember("subM");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(subM.age)
                                .from(subM)
                                .where(subM.age.gt(10))
                ))
                .fetch();
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void select_sub_query() throws Exception{
        QMember subM = new QMember("subM");

        List<Tuple> result = queryFactory
                .select(member.username,
                        select(subM.age.avg())
                                .from(subM)
                )
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("" + tuple);
        }
    }

    @Test
    public void basic_case() throws Exception{
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("??????")
                        .when(20).then("?????????")
                        .otherwise("??????")
                )
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complex_case() throws Exception{
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0 ~ 20???")
                        .when(member.age.between(21, 30)).then("21 ~ 30???")
                        .otherwise("??????")
                )
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * ????????? ?????? ????????? ????????? ???????????? ??????????
     * 1. 0~30?????? ?????? ??????
     * 2. 0~20?????? ??????
     * 3. 21~30?????? ??????
     * -- ?????? -- 
     * [member4, 40, 3]
     * [member1, 10, 2]
     * [member2, 20, 2]
     * [member3, 30, 1]
     */
    @Test
    public void case_with_order_by() throws Exception{
        NumberExpression<Integer> ranker = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = queryFactory
                .select(member.username, member.age, ranker)
                .from(member)
                .orderBy(ranker.desc())
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("" + tuple);
        }
    }

    @Test
    public void constant() throws Exception{
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("" + tuple);
        }
    }

    @Test
    public void concat() throws Exception{
        // username_age ??????
        String result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        System.out.println("result = " + result);
    }

    @Test
    public void simple_projection() throws Exception{
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tuple_projection() throws Exception{
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void find_dto_pure_jpa() throws Exception{
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void find_dto_by_setter() throws Exception{
        List<MemberDto> result = queryFactory
                .select(Projections.bean(
                        MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void find_dto_by_field() throws Exception{
        List<MemberDto> result = queryFactory
                .select(Projections.fields(
                        MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void find_dto_by_constructor() throws Exception{
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(
                        MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void find_UserDto_by_field() throws Exception{
        List<UserDto> result = queryFactory
                .select(Projections.fields(
                        UserDto.class,
                        // ?????? ????????? ?????? ?????? alias ??????????????? ???
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void find_UserDto_by_field_with_sub_query() throws Exception{
        QMember subM = new QMember("subM");
        List<MemberDto> result = queryFactory
                .select(Projections.fields(
                        MemberDto.class,
                        member.username,
                        // age ?????? ??????????????? ??????
                        ExpressionUtils.as(JPAExpressions
                                .select(subM.age.max())
                                .from(subM), "age")
                ))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("userDto = " + memberDto);
        }
    }

    @Test
    public void find_UserDto_dto_by_constructor() throws Exception{
        List<UserDto> result = queryFactory
                .select(Projections.constructor(
                        UserDto.class,
                        // ????????? ??????????????? ???????????? ???????????? ????????? ????????? ???
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void find_dto_by_query_projection() throws Exception{
        List<MemberQueryProjectionDto> result = queryFactory
                .select(new QMemberQueryProjectionDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberQueryProjectionDto memberQueryProjectionDto : result) {
            System.out.println("memberQueryProjectionDto = " + memberQueryProjectionDto);
        }
    }

    @Test
    public void dynamic_query_by_boolean_builder() throws Exception{
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertEquals(1, result.size());
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null)
            builder.and(member.username.eq(usernameCond));
        if (ageCond != null)
            builder.and(member.age.eq(ageCond));

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamic_query_by_where_param() throws Exception{
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember3(usernameParam, ageParam);
        Assertions.assertEquals(1, result.size());
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null?  member.username.eq(usernameCond): null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null?  member.age.eq(ageCond): null;
    }

    private List<Member> searchMember3(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    public void bulk_update() throws Exception{
        // EC             DB
        // member1, 10 -> member1
        // member2, 20 -> member2
        // member3, 30 -> member3
        // member4, 40 -> member4
        
        long count = queryFactory
                .update(member)
                .set(member.username, "?????????")
                .where(member.age.lt(28))
                .execute();

        // EC             DB
        // member1, 10 -> ?????????
        // member2, 20 -> ?????????
        // member3, 30 -> member3
        // member4, 40 -> member4
        // ????????? ??????????????? DB?????? ????????? ?????? ?????????,
        // ????????? ?????? ???????????? ????????? ??????????????? ?????? ????????? ?????????

        List<Member> result = queryFactory.selectFrom(member)
                .fetch();
        for (Member member1 : result) {
            System.out.println("member at EC = " + member1);
        }

        // ?????? ?????? ????????? ????????? ????????? ??????????????? ????????? ?????? ??????
        em.flush();
        em.clear();


        List<Member> result2 = queryFactory.selectFrom(member)
                .fetch();
        for (Member member1 : result2) {
            System.out.println("member at DB = " + member1);
        }

        Assertions.assertEquals(2, count);
    }

    @Test
    public void bulk_add() throws Exception{
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    @Test
    public void bulk_delete() throws Exception{
        long count = queryFactory
                .delete(member)
                .where(member.age.lt(18))
                .execute();
    }

    @Test
    public void sql_function_replace() throws Exception{
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username,
                        "member",
                        "M"
                ))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void sql_function_upper() throws Exception{
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('upper', {0})",
                        member.username
                ))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
}
