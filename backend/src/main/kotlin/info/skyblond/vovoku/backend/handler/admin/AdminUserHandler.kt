package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.User
import info.skyblond.vovoku.backend.database.Users
import info.skyblond.vovoku.commons.models.AdminUserRequest
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.*
import org.slf4j.LoggerFactory

object AdminUserHandler : AdminCRUDHandler<AdminUserRequest>(AdminUserRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminUserHandler::class.java)
    private val database = DatabaseUtil.database

    private fun query(request: AdminUserRequest, paging: Boolean = true): EntitySequence<User, Users> {
        return database.sequenceOf(Users)
            .let { sequence ->
                if (request.pojo.userId != null) {
                    sequence.filter { it.userId eq request.pojo.userId!! }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (request.pojo.username != null) {
                    sequence.filter { it.username like request.pojo.username!! }
                } else {
                    sequence
                }
            }
            .sortedBy { it.userId }
            .let {
                if (paging) {
                    it.drop(request.page?.offset ?: Page(null, null).offset)
                        .take(request.page?.limit ?: Page(null, null).limit)
                } else {
                    it
                }
            }
    }

    override fun handleRead(ctx: Context, request: AdminUserRequest) {
        val result = query(request).map { it.toPojo() }
        ctx.json(result)
    }

    /**
     * Create new user.
     * */
    override fun handleCreate(ctx: Context, request: AdminUserRequest) {
        val entity = User {
            username = request.pojo.username!!
            password = request.pojo.password!!
        }

        if (database.sequenceOf(Users).find { it.username eq request.pojo.username!! } != null) {
            throw BadRequestResponse("Duplicate entity in database")
        }

        val count = database.sequenceOf(Users).add(entity)

        if (count != 1)
            throw BadRequestResponse("Unable to update database")
        // return inserted entity
        ctx.json(entity.toPojo())
    }

    /**
     * Updating a user.
     * */
    override fun handleUpdate(ctx: Context, request: AdminUserRequest) {
        val query = query(request, false)
        if (query.totalRecords != 1) {
            throw BadRequestResponse("Cannot update multiple entity at once")
        }
        val entity = query.first()
        entity.update(request.pojo)
        ctx.json(entity.toPojo())
    }

    /**
     * Delete user.
     * */
    override fun handleDelete(ctx: Context, request: AdminUserRequest) {
        val result = query(request, false)
            .map {
                it.delete()
                it.toPojo()
            }
        ctx.json(result)
    }
}