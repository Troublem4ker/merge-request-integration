package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class TwoSideTextDiffView(
    private val applicationService: ApplicationService,
    override val viewer: TwosideTextDiffViewer,
    private val change: Change
) : AbstractDiffView<TwosideTextDiffViewer>(applicationService, viewer) {

    override fun displayAddGutterIcons() {
        println("displayAddGutterIcons")
        for (logicalLine in 0 until viewer.editor1.document.lineCount) {
            val lineHighlighter = viewer.editor1.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                viewer.editor1,
                applicationService.settings.showAddCommentIconsInDiffViewGutter && !hasCommentsGutter(logicalLine, DiffView.ContentType.BEFORE),
                logicalLine + 1,
                logicalLine,
                this::onAddGutterIconInEditor1Clicked
            )
        }
        for (logicalLine in 0 until viewer.editor2.document.lineCount) {
            val lineHighlighter = viewer.editor2.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                viewer.editor2,
                applicationService.settings.showAddCommentIconsInDiffViewGutter && !hasCommentsGutter(logicalLine, DiffView.ContentType.AFTER),
                logicalLine + 1,
                logicalLine,
                this::onAddGutterIconInEditor2Clicked
            )
        }
    }

    private fun onAddGutterIconInEditor1Clicked(renderer: AddGutterIconRenderer, changeType: DiffView.ChangeType) {
        val result = viewer.syncScrollSupport!!.scrollable.transfer(Side.LEFT, renderer.visibleLine)
        dispatcher.multicaster.onAddGutterIconClicked(renderer, AddCommentRequestedPosition(
            editorType = DiffView.EditorType.TWO_SIDE_LEFT,
            changeType = changeType,
            oldLine = renderer.visibleLine,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = result,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        ))
    }

    private fun onAddGutterIconInEditor2Clicked(renderer: AddGutterIconRenderer, changeType: DiffView.ChangeType) {
        val result = viewer.syncScrollSupport!!.scrollable.transfer(Side.RIGHT, renderer.visibleLine)
        dispatcher.multicaster.onAddGutterIconClicked(renderer, AddCommentRequestedPosition(
            editorType = DiffView.EditorType.TWO_SIDE_RIGHT,
            changeType = changeType,
            oldLine = result,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = renderer.visibleLine,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        ))
    }

    override fun displayCommentsGutterIcon(visibleLine: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val editor = if (contentType == DiffView.ContentType.BEFORE) {
            viewer.editor1
        } else {
            viewer.editor2
        }

        val logicalLine = visibleLine - 1
        if (!hasCommentsGutter(logicalLine, contentType)) {
            val lineHighlighter = editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                visibleLine, logicalLine, contentType, dispatcher.multicaster::onCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)
    }

    override fun displayCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val editor = if (contentType == DiffView.ContentType.BEFORE) {
            viewer.editor1
        } else {
            viewer.editor2
        }
        displayCommentsOnLine(providerData, mergeRequest, editor, visibleLine - 1, comments)
    }
}