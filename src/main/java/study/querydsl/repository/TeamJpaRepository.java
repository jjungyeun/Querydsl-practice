package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

@Repository
public class TeamJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public TeamJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Team team) {
        em.persist(team);
    }
}
