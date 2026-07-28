package cc.samlab.rss.domain.model.general

enum class SyncWarning {
    None,
    Metered,
    NotCharging,
    Overheat,
    MeteredNotCharging,
    MeteredOverheat,
    NotChargingOverheat,
    All
}
