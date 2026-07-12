// User.kt — 用户模型
package com.marathonticket.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val nickname: String,
    val avatar: String,
    val phone: String,
    val idCard: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val tShirtSize: String? = null
) {
    val isProfileComplete: Boolean get() =
        !idCard.isNullOrEmpty() && !emergencyContact.isNullOrEmpty()
}
