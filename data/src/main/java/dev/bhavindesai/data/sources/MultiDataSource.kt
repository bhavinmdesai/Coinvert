package dev.bhavindesai.data.sources

import dev.bhavindesai.data.utils.InternetUtil
import kotlinx.coroutines.flow.flow

abstract class MultiDataSource<LocalType, RequestType, ResponseType>(
    private val internetUtil: InternetUtil
) : LocalDataSource<LocalType>,
    RemoteDataSource<RequestType, ResponseType> {

    abstract suspend fun mapper(remoteData: ResponseType) : LocalType

    fun fetch(request: RequestType) = flow {
        val localData = getLocalData()

        localData?.let { emit(localData) }

        if (internetUtil.isInternetOn()) {
            val remoteData = getRemoteData(request)
            if (remoteData != null) {
                val mappedData = mapper(remoteData)
                storeLocalData(mappedData)

                emit(mappedData)
            } else if (localData == null) {
                emit(null)
            }
        } else if (localData == null) {
            emit(null)
        }
    }
}