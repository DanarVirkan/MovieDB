package com.example.core.data

import com.example.core.data.local.LocalSource
import com.example.core.data.remote.ApiResponse
import com.example.core.data.remote.RemoteSource
import com.example.core.data.remote.response.ItemResponse
import com.example.core.domain.IRepository
import com.example.core.domain.Item
import com.example.core.utils.DataMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Repository(
    private val local: LocalSource,
    private val remote: RemoteSource
) :
    IRepository {

    // REMOTE
    override fun getMovieById(id: Int) = remote.getMovieById(id).map {
        DataMapper.mapResponseToMovie(it)
    }

    override fun getTVById(id: Int) = remote.getTVById(id).map {
        DataMapper.mapResponseToTV(it)
    }

    override fun getTrending(): Flow<Resource<List<Item>>> =
        object : NetworkBoundResource<List<Item>, List<ItemResponse>>() {
            override fun loadFromDB(): Flow<List<Item>> =
                local.getTrending().map { DataMapper.mapEntitiesToDomain(it) }

            override fun shouldFetch(data: List<Item>?): Boolean =
                data == null || data.isEmpty()

            override suspend fun createCall(): Flow<ApiResponse<List<ItemResponse>>> =
                remote.getTrending()

            override suspend fun saveCallResult(data: List<ItemResponse>) {
                val converter = DataMapper.mapResponseToEntities(data)
                local.insertTrending(converter)
            }
        }.asFlow()

    override fun getError(): Flow<Boolean> = remote.getError()

    // LOCAL
    override fun getBookmarked(type: Int): Flow<List<Item>> =
        local.getBookmarked(type).map { DataMapper.mapEntitiesToDomain(it) }

    override fun updateBookmark(item: Item, newState: Boolean) {
        val converter = DataMapper.mapDomainToEntity(item)
        local.updateBookmark(converter, newState)
    }

    override fun removeAllBookmark() = local.removeAllBookmark()
}