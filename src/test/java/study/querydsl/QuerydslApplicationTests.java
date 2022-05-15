package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
//@Commit	// 공부용. DB에 넣고 롤백 안함
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QHello qHello = QHello.hello;

		Hello selectedHello = queryFactory.selectFrom(qHello)
				.fetchOne();

		Assertions.assertEquals(hello, selectedHello);
		Assertions.assertEquals(hello.getId(), selectedHello.getId());
	}

}
