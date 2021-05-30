package dev.bhavindesai.data.sources

interface RemoteDataSource<RequestType, ResponseType> {
    suspend fun getRemoteData(requestData: RequestType) : ResponseType?
}