package team.msg.domain.lecture.repository.custom.impl

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import team.msg.domain.lecture.enums.Division
import team.msg.domain.lecture.enums.LectureType
import team.msg.domain.lecture.model.QLecture.lecture
import team.msg.domain.lecture.repository.custom.CustomLectureRepository
import team.msg.domain.user.model.QUser.user
import java.util.*
import java.util.Objects.isNull

@Component
class CustomLectureRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomLectureRepository {
    override fun findAllByLectureType(pageable: Pageable,lectureType: LectureType?) = PageImpl(
        queryFactory
            .selectFrom(lecture)
            .leftJoin(lecture.user, user)
            .fetchJoin()
            .where(
                eqLectureType(lectureType)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch())

    override fun deleteAllByUserId(userId: UUID) {
        queryFactory.delete(lecture)
            .where(lecture.user.id.eq(userId))
            .execute()
    }

    override fun findAllLineByDivision(division: Division, keyword: String?): List<String> =
        queryFactory
            .select(lecture.line)
            .from(lecture)
            .where(
                lecture.division.eq(division),
                eqLine(keyword)
            )
            .fetch()
            .distinct()

    override fun findAllDepartment(keyword: String?): List<String> =
        queryFactory
            .select(lecture.department)
            .from(lecture)
            .where(eqDepartment(keyword))
            .fetch()
            .distinct()

    private fun eqLectureType(lectureType: LectureType?): BooleanExpression? =
        if(isNull(lectureType)) null else lecture.lectureType.eq(lectureType)

    private fun eqLine(keyword: String?): BooleanExpression? =
        if(keyword.isNullOrBlank()) null else lecture.line.contains(keyword)
    private fun eqDepartment(keyword: String?): BooleanExpression? =
        if(keyword.isNullOrBlank()) null else lecture.department.contains(keyword)
}