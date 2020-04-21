package io.zenandroid.onlinego.joseki

import android.graphics.Point
import io.zenandroid.onlinego.model.ogs.JosekiPosition

sealed class JosekiExplorerAction {
    object ViewReady: JosekiExplorerAction()

    class DataLoadingError(
            val e: Throwable
    ): JosekiExplorerAction()

    class PositionLoaded(val position: JosekiPosition): JosekiExplorerAction()
    class LoadPosition(val id: Long?): JosekiExplorerAction()
    class StartDataLoading(val id: Long?): JosekiExplorerAction()
    class ShowCandidateMove(val placement: Point?): JosekiExplorerAction()
    object Finish: JosekiExplorerAction()

    // User actions
    class UserTappedCoordinate(val coordinate: Point): JosekiExplorerAction()
    class UserHotTrackedCoordinate(val coordinate: Point): JosekiExplorerAction()
    object UserPressedPrevious: JosekiExplorerAction()
    object UserPressedBack: JosekiExplorerAction()
    object UserPressedNext: JosekiExplorerAction()
    object UserPressedPass: JosekiExplorerAction()
}