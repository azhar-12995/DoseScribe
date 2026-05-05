package com.azhar.dosescribe.domain.repository

import com.azhar.dosescribe.data.model.Banner
import kotlinx.coroutines.flow.Flow

interface BannerRepository {
    fun getBanners(): Flow<Result<List<Banner>>>
    fun addBanner(banner: Banner): Flow<Result<Unit>>
    fun updateBanner(banner: Banner): Flow<Result<Unit>>
    fun deleteBanner(bannerId: String): Flow<Result<Unit>>
}

