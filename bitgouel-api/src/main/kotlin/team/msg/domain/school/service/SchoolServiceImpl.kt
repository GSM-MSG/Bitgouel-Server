package team.msg.domain.school.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.msg.domain.club.presentation.data.response.ClubResponse
import team.msg.domain.club.repository.ClubRepository
import team.msg.domain.school.exception.AlreadyExistSchoolException
import team.msg.domain.school.exception.InvalidExtensionException
import team.msg.domain.school.exception.SchoolNotFoundException
import team.msg.domain.school.model.School
import team.msg.domain.school.presentation.data.request.CreateSchoolRequest
import team.msg.domain.school.presentation.data.request.UpdateSchoolRequest
import team.msg.domain.school.presentation.data.response.SchoolResponse
import team.msg.domain.school.presentation.data.response.SchoolsResponse
import team.msg.domain.school.repository.SchoolRepository
import team.msg.thirdparty.aws.s3.AwsS3Util
import java.util.*

@Service
class SchoolServiceImpl(
    private val schoolRepository: SchoolRepository,
    private val clubRepository: ClubRepository,
    private val awsS3Util: AwsS3Util
) : SchoolService {

    /**
     * 학교를 전체 조회하는 비즈니스 로직
     * @return 학교의 정보를 담은 dto
     */
    @Transactional(readOnly = true)
    override fun querySchools(): SchoolsResponse {
        val schools = schoolRepository.findAll()

        val response = SchoolsResponse(
            schools = schools.map {
                val clubs = clubRepository.findAllBySchool(it)
                SchoolResponse(
                    id = it.id,
                    schoolName = it.name,
                    field = it.field,
                    line = it.line,
                    logoImageUrl = it.logoImageUrl,
                    clubs = ClubResponse.schoolOf(clubs)
                )
            }
        )

        return response
    }

    /**
     * 학교를 생성하는 비지니스 로직
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun createSchool(request: CreateSchoolRequest, logoImage: MultipartFile) {
        if (schoolRepository.existsByName(request.schoolName)) {
            throw AlreadyExistSchoolException("이미 존재하는 학교입니다. info [ schoolName = ${request.schoolName} ]")
        }

        val imageName = awsS3Util.uploadImage(logoImage, UUID.randomUUID().toString())

        val school = School(
            logoImageUrl = imageName,
            name = request.schoolName,
            field = request.field,
            line = request.line
        )

        schoolRepository.save(school)
    }

    /**
     * 학교를 수정하는 비지니스 로직
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun updateSchool(id: Long, request: UpdateSchoolRequest, logoImage: MultipartFile) {
        if (schoolRepository.existsByName(request.schoolName)) {
            throw AlreadyExistSchoolException("이미 존재하는 학교입니다. info : [ schoolName = ${request.schoolName} ]")
        }

        if (logoImage.contentType !in listOf(JPEG, JPG, PNG, HEIC)) {
            throw InvalidExtensionException("유효하지 않은 확장자입니다. info : [ contentType = ${logoImage.contentType}")
        }

        val school = schoolRepository.findByIdOrNull(id)
            ?: throw SchoolNotFoundException("존재하지 않는 학교입니다. info : [ schoolId = $id ]")

        awsS3Util.deleteImage(school.logoImageUrl)

        val imageName = awsS3Util.uploadImage(logoImage, UUID.randomUUID().toString())

        val updateSchool = School(
            id = id,
            logoImageUrl = imageName,
            name = request.schoolName,
            field = request.field,
            line = request.line
        )
        schoolRepository.save(updateSchool)
    }

    /**
     * 학교를 삭제하는 비지니스 로직
     */
    @Transactional(rollbackFor = [Exception::class])
    override fun deleteSchool(id: Long) {
        val school = schoolRepository.findByIdOrNull(id)
            ?: throw SchoolNotFoundException("존재하지 않는 학교입니다. info [ schoolId = $id ]")

        awsS3Util.deleteImage(school.logoImageUrl)
        clubRepository.deleteAllBySchoolId(id)
        schoolRepository.deleteById(id)
    }

    companion object {
        const val JPEG = "image/jpeg"
        const val PNG = "image/png"
        const val JPG = "image/jpg"
        const val HEIC = "image/heic"
    }

}