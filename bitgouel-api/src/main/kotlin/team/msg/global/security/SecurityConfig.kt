package team.msg.global.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.cors.CorsUtils
import team.msg.global.config.FilterConfig
import team.msg.global.security.handler.CustomAccessDeniedHandler
import team.msg.global.security.handler.CustomAuthenticationEntryPointHandler
import team.msg.global.security.jwt.JwtTokenParser


@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenParser: JwtTokenParser
) {
    companion object {
        const val ADMIN = "ADMIN"
        const val STUDENT = "STUDENT"
        const val TEACHER = "TEACHER"
        const val BBOZZAK = "BBOZZAK"
        const val PROFESSOR = "PROFESSOR"
        const val COMPANY_INSTRUCTOR = "COMPANY_INSTRUCTOR"
        const val GOVERNMENT = "GOVERNMENT"
    }

    @Bean
    protected fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors()
            .and()
            .csrf().disable()

            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .requestMatchers(RequestMatcher { request ->
                CorsUtils.isPreFlightRequest(request)
            }).permitAll()

            // health
            .mvcMatchers(HttpMethod.GET, "/").permitAll()

            // auth
            .mvcMatchers(HttpMethod.POST, "/auth/student").permitAll()
            .mvcMatchers(HttpMethod.POST, "/auth/teacher").permitAll()
            .mvcMatchers(HttpMethod.POST, "/auth/bbozzak").permitAll()
            .mvcMatchers(HttpMethod.POST, "/auth/professor").permitAll()
            .mvcMatchers(HttpMethod.POST, "/auth/government").permitAll()
            .mvcMatchers(HttpMethod.POST, "/auth/company-instructor").permitAll()
            .mvcMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .mvcMatchers(HttpMethod.PATCH, "/auth/password").permitAll()
            .mvcMatchers(HttpMethod.PATCH, "/auth").permitAll()
            .mvcMatchers(HttpMethod.DELETE, "/auth").authenticated()
            .mvcMatchers(HttpMethod.DELETE, "/auth/withdraw").authenticated()

            // email
            .mvcMatchers(HttpMethod.POST, "/email").permitAll()
            .mvcMatchers(HttpMethod.GET, "/email").permitAll()
            .mvcMatchers(HttpMethod.GET, "/email/authentication").permitAll()

            // club
            .mvcMatchers(HttpMethod.GET, "/club").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.GET, "/club/my").hasAnyRole(STUDENT, PROFESSOR, COMPANY_INSTRUCTOR, BBOZZAK, TEACHER, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/club/{id}").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.GET, "/club/{id}/{student_id}").hasAnyRole(STUDENT, ADMIN, PROFESSOR, COMPANY_INSTRUCTOR, BBOZZAK, TEACHER, GOVERNMENT)

            // school
            .mvcMatchers(HttpMethod.GET, "/school").hasRole(ADMIN)

            // activity
            .mvcMatchers(HttpMethod.POST, "/activity").hasAnyRole(STUDENT, TEACHER, BBOZZAK)
            .mvcMatchers(HttpMethod.PATCH, "/activity/{id}").hasAnyRole(STUDENT, TEACHER, BBOZZAK)
            .mvcMatchers(HttpMethod.DELETE, "/activity/{id}").hasAnyRole(STUDENT, TEACHER, BBOZZAK)
            .mvcMatchers(HttpMethod.GET, "/activity").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.GET, "/activity/my").hasRole(STUDENT)
            .mvcMatchers(HttpMethod.GET, "/activity/{student_id}").hasAnyRole(TEACHER, ADMIN)
            .mvcMatchers(HttpMethod.GET, "/activity/{id}/detail").hasAnyRole(STUDENT, TEACHER, ADMIN, BBOZZAK)

            // post
            .mvcMatchers(HttpMethod.POST, "/post").hasAnyRole(COMPANY_INSTRUCTOR, BBOZZAK, PROFESSOR, GOVERNMENT, ADMIN)
            .mvcMatchers(HttpMethod.GET, "/post").authenticated()
            .mvcMatchers(HttpMethod.GET, "/post/all").authenticated()
            .mvcMatchers(HttpMethod.GET, "/post/{id}").authenticated()
            .mvcMatchers(HttpMethod.PATCH, "/post/{id}").hasAnyRole(COMPANY_INSTRUCTOR, BBOZZAK, PROFESSOR, GOVERNMENT, ADMIN)
            .mvcMatchers(HttpMethod.DELETE, "/post/{id}").hasAnyRole(COMPANY_INSTRUCTOR, BBOZZAK, PROFESSOR, GOVERNMENT, ADMIN)

            // lecture
            .mvcMatchers(HttpMethod.POST, "/lecture").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/lecture").authenticated()
            .mvcMatchers(HttpMethod.PATCH, "/lecture/{id}").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.DELETE, "/lecture/{id}/soft").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.POST, "/lecture/{id}").hasRole(STUDENT)
            .mvcMatchers(HttpMethod.DELETE, "/lecture/{id}").hasRole(STUDENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/{id}").authenticated()
            .mvcMatchers(HttpMethod.GET, "/lecture/instructor").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/line").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/department").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/{student_id}/signup").hasAnyRole(ADMIN, TEACHER, STUDENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/student/{id}").hasAnyRole(ADMIN, TEACHER, BBOZZAK, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.PATCH, "/lecture/{id}/{student_id}").hasAnyRole(ADMIN, TEACHER, BBOZZAK, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/division").hasAnyRole(ADMIN, COMPANY_INSTRUCTOR, PROFESSOR, GOVERNMENT)
            .mvcMatchers(HttpMethod.GET, "/lecture/excel").hasRole(ADMIN)

            // faq
            .mvcMatchers(HttpMethod.POST, "/faq").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.GET, "/faq").permitAll()

            // certification
            .mvcMatchers(HttpMethod.POST, "/certification").hasRole(STUDENT)
            .mvcMatchers(HttpMethod.GET, "/certification").hasRole(STUDENT)
            .mvcMatchers(HttpMethod.GET, "/certification/{student_id}").hasAnyRole(ADMIN, PROFESSOR, COMPANY_INSTRUCTOR, BBOZZAK, TEACHER, GOVERNMENT)
            .mvcMatchers(HttpMethod.PATCH, "/certification/{id}").hasRole(STUDENT)

            // user
            .mvcMatchers(HttpMethod.GET, "/user").authenticated()
            .mvcMatchers(HttpMethod.PATCH, "/user").authenticated()

            // admin
            .mvcMatchers(HttpMethod.GET, "/admin").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.PATCH, "/admin").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.DELETE, "/admin/reject").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.GET, "/admin/{user_id}").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.DELETE, "/admin/withdraw").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.POST, "/admin/excel").hasRole(ADMIN)

            // inquiry
            .mvcMatchers(HttpMethod.POST, "/inquiry").authenticated()
            .mvcMatchers(HttpMethod.GET, "/inquiry").hasAnyRole(STUDENT, TEACHER, BBOZZAK, PROFESSOR, GOVERNMENT, COMPANY_INSTRUCTOR)
            .mvcMatchers(HttpMethod.GET, "/inquiry/all").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.GET, "/inquiry/{id}").authenticated()
            .mvcMatchers(HttpMethod.DELETE, "/inquiry/{id}").authenticated()
            .mvcMatchers(HttpMethod.DELETE, "/inquiry/{id}/reject").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.PATCH, "/inquiry/{id}").authenticated()
            .mvcMatchers(HttpMethod.POST, "/inquiry/{id}/answer").hasRole(ADMIN)

             // withdraw
            .mvcMatchers(HttpMethod.GET, "/withdraw").hasRole(ADMIN)

            // school
            .mvcMatchers(HttpMethod.GET, "/school").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.POST, "/school").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.PATCH, "/school/{id}").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.DELETE, "/school/{id}").hasRole(ADMIN)

            // government
            .mvcMatchers(HttpMethod.GET, "/government").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.POST, "/government").hasRole(ADMIN)
            .mvcMatchers(HttpMethod.DELETE, "/government/{id}").hasRole(ADMIN)

            // university 
            .mvcMatchers(HttpMethod.GET, "/university").permitAll()

            // actuator
            .mvcMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()

            .anyRequest().denyAll()
            .and()

            .exceptionHandling()
            .authenticationEntryPoint(CustomAuthenticationEntryPointHandler())
            .accessDeniedHandler(CustomAccessDeniedHandler())
            .and()

            .apply(FilterConfig(jwtTokenParser))
            .and()
            .build()

    @Bean
    protected fun passwordEncode() = BCryptPasswordEncoder()
}
