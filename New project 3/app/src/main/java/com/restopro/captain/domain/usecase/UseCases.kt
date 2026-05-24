package com.restopro.captain.domain.usecase

import com.restopro.captain.data.repository.AuthRepository
import com.restopro.captain.data.repository.MenuRepository
import com.restopro.captain.data.repository.TableRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(code: String, username: String, password: String, server: String, remember: Boolean) =
        repository.login(code, username, password, server, remember)
}

class RefreshOperationalDataUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val tableRepository: TableRepository
) {
    suspend operator fun invoke() {
        tableRepository.refreshTables()
        menuRepository.refreshMenu()
    }
}
