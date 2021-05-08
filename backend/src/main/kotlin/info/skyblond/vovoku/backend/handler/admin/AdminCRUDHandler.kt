package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.commons.models.AdminCRUDRequest
import info.skyblond.vovoku.commons.models.CRUD
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import org.postgresql.util.PSQLException

abstract class AdminCRUDHandler<T : AdminCRUDRequest>(private val clazz: Class<T>) : Handler {
    companion object {
        const val ENCRYPTION_IDENTIFIER_KEY = "result_encrypt"
    }

    abstract fun handleCreate(ctx: Context, request: T)
    abstract fun handleRead(ctx: Context, request: T)
    abstract fun handleUpdate(ctx: Context, request: T)
    abstract fun handleDelete(ctx: Context, request: T)

    override fun handle(ctx: Context) {
        try {
            val request = ctx.bodyAsClass(clazz)
            when (request.operation) {
                CRUD.CREATE -> handleCreate(ctx, request)
                CRUD.READ -> handleRead(ctx, request)
                CRUD.UPDATE -> handleUpdate(ctx, request)
                CRUD.DELETE -> handleDelete(ctx, request)
            }
        } catch (e: Exception) {
            ctx.attribute(ENCRYPTION_IDENTIFIER_KEY, false)
            if (e is PSQLException) {
                throw InternalServerErrorResponse("SQL execution error. Your request might be rejected by database.")
            } else
                throw e
        }
        ctx.attribute(ENCRYPTION_IDENTIFIER_KEY, true)
    }
}