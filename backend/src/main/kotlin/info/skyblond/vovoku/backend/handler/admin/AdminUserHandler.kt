package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.User
import info.skyblond.vovoku.backend.database.Users
import info.skyblond.vovoku.commons.models.AdminRequest
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.*
import org.slf4j.LoggerFactory

object AdminUserHandler : AdminCRUDHandler<AdminRequest>(AdminRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminUserHandler::class.java)
    private val database = DatabaseUtil.database

    private fun query(
        userId: Int?,
        username: String?,
        page: Page?
    ): EntitySequence<User, Users> {
        return database.sequenceOf(Users)
            .let { sequence ->
                if (userId != null) {
                    sequence.filter { it.userId eq userId }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (username != null) {
                    sequence.filter { it.username like username }
                } else {
                    sequence
                }
            }
            .sortedBy { it.userId }
            .let {
                if (page != null) {
                    it.drop(page.offset)
                        .take(page.limit)
                } else {
                    it
                }
            }
    }

    override fun handleRead(ctx: Context, request: AdminRequest) {
        val result = query(
            request.typedParameter(AdminRequest.USER_ID_KEY),
            request.typedParameter(AdminRequest.USERNAME_KEY),
            request.page
        ).map { it.toPojo() }
        ctx.json(result)
    }

    /**
     * Create new user.
     * */
    override fun handleCreate(ctx: Context, request: AdminRequest) {
        val username = request.typedParameter<String>(AdminRequest.USERNAME_KEY)
            ?: throw BadRequestResponse("Username can't be null")
        val password = request.typedParameter<String>(AdminRequest.USER_PASSWORD_KEY)
            ?: throw BadRequestResponse("Password can't be null")
        val entity = User {
            this.username = username
            this.password = password
        }

        if (database.sequenceOf(Users).find { it.username eq username } != null) {
            throw BadRequestResponse("Duplicate entity in database")
        }

        val count = database.sequenceOf(Users).add(entity)

        if (count != 1)
            throw BadRequestResponse("Unable to update database")
        // return inserted entity
        ctx.json(entity.toPojo())
    }

    /**
     * Updating a user by id.
     * */
    override fun handleUpdate(ctx: Context, request: AdminRequest) {
        val userId = request.typedParameter<Int>(AdminRequest.USER_ID_KEY)
        val username = request.typedParameter<String>(AdminRequest.USERNAME_KEY)
        val password = request.typedParameter<String>(AdminRequest.USER_PASSWORD_KEY)
        val query = query(userId, null, null)
        if (query.totalRecords != 1) {
            throw BadRequestResponse("Cannot update multiple entity at once")
        }
        val entity = query.first()
        entity.update(username, password)
        ctx.json(entity.toPojo())
    }

    /**
     * Delete user.
     * */
    override fun handleDelete(ctx: Context, request: AdminRequest) {
        val userId = request.typedParameter<Int>(AdminRequest.USER_ID_KEY)
        val username = request.typedParameter<String>(AdminRequest.USER_ID_KEY)

        val result = query(userId, username, null)
            .map {
                it.delete()
                it.toPojo()
            }
        ctx.json(result)
    }
}