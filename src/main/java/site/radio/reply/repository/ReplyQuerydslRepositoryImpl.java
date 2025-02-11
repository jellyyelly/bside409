package site.radio.reply.repository;

import static site.radio.reply.domain.QLetter.letter;
import static site.radio.reply.domain.QReply.reply;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import site.radio.reply.domain.Reply;

public class ReplyQuerydslRepositoryImpl implements ReplyQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public ReplyQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Reply> findLatestRepliesBy(UUID userId, LocalDateTime startOfYear, LocalDateTime endOfYear,
                                           Boolean published,
                                           Pageable pageable) {
        List<Reply> content = queryFactory
                .selectFrom(reply)
                .join(reply.letter, letter).fetchJoin()
                .where(reply.letter.user.id.eq(userId), isInRange(startOfYear, endOfYear), isPublished(published))
                .orderBy(new OrderSpecifier<>(Order.DESC, reply.createdAt))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(reply.count())
                .from(reply)
                .join(reply.letter, letter)
                .where(reply.letter.user.id.eq(userId), isInRange(startOfYear, endOfYear), isPublished(published));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression isInRange(LocalDateTime startOfYear, LocalDateTime endOfYear) {
        return reply.createdAt.goe(startOfYear).and(reply.createdAt.loe(endOfYear));
    }

    private BooleanExpression isPublished(Boolean published) {
        return published != null ? reply.letter.published.eq(published) : null;
    }
}
