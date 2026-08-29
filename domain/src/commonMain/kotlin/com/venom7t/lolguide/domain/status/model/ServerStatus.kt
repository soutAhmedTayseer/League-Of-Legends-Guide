package com.venom7t.lolguide.domain.status.model

data class ServerStatus(
    val region: String,
    val incidents: List<ServerIncident>,
) {
    val hasActiveIncidents: Boolean get() = incidents.isNotEmpty()
}

data class ServerIncident(
    val id: Long,
    val titleEn: String,
    val severity: IncidentSeverity,
)

enum class IncidentSeverity {
    INFO, WARNING, CRITICAL, UNKNOWN,
    ;

    companion object {
        fun fromRiotValue(value: String?): IncidentSeverity = when (value) {
            "info" -> INFO
            "warning" -> WARNING
            "critical" -> CRITICAL
            else -> UNKNOWN
        }
    }
}
