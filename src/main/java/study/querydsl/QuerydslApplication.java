package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

//	// 이걸 설정하고 사용하는 곳에서 주입받아 사용해도 됨
//	@Bean
//	JPAQueryFactory jpaQueryFactory(EntityManager em){
//		return new JPAQueryFactory(em);
//	}
}
