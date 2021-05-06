package info.skyblond.vovoku.commons.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class Page(
    private val _page: Int?,
    private val _limit: Int?
){
    init {
        require(_page == null || _page > 0)
        require(_limit == null || _limit > 0)
    }
    @JsonIgnore
    private val truePage = _page ?: 1
    @JsonIgnore
    val limit = _limit ?: 20
    @JsonIgnore
    val offset = (truePage - 1) *  limit
}

enum class CRUD{
    CREATE, READ, UPDATE, DELETE
}

interface AdminCRUDRequest{
    val operation: CRUD
}

data class AdminUserRequest(
    val pojo: DatabaseUserPojo,
    override val operation: CRUD,
    val page: Page? = null,
): AdminCRUDRequest

data class AdminPictureTagRequest(
    val pojo: DatabasePictureTagPojo,
    override val operation: CRUD,
    val page: Page? = null,
): AdminCRUDRequest

data class AdminModelRequest(
    val pojo: DatabaseModelInfoPojo,
    override val operation: CRUD,
    val page: Page? = null,
): AdminCRUDRequest