package team.msg.domain.government.repository

import org.springframework.data.repository.CrudRepository
import team.msg.domain.government.model.Government

interface GovernmentRepository : CrudRepository<Government, Long> {
    fun findByName(name: String): Government?
    fun existsByName(name: String): Boolean
}