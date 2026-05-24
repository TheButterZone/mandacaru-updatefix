package com.github.jvsena42.mandacaru.data

import com.github.jvsena42.mandacaru.domain.model.UpdateStatus
import kotlinx.coroutines.flow.StateFlow

interface AppUpdateRepository {
    val updateStatus: StateFlow<UpdateStatus>

    suspend fun refresh(force: Boolean = false)

    suspend fun markUpdateSeen()
}
