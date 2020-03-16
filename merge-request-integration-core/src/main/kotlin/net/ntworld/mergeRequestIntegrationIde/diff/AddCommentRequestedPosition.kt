package net.ntworld.mergeRequestIntegrationIde.diff

data class AddCommentRequestedPosition(
    val editorType: DiffView.EditorType,
    val changeType: DiffView.ChangeType,
    val oldLine: Int?,
    val newLine: Int?,
    val oldPath: String?,
    val newPath: String?,
    val baseHash: String? = null,
    val startHash: String? = null,
    val headHash: String? = null
)
