package name.zeno.ktrxpermission

data class Permission @JvmOverloads constructor(
    val name: String,
    val granted: Boolean,
    val shouldShowRequestPermissionRationale: Boolean = false
)
