package dev.bhavindesai.data.sources

interface LocalDataSource<LocalType> {

    suspend fun getLocalData() : LocalType?

    suspend fun storeLocalData(data: LocalType)

}