package io.zenandroid.onlinego.ogs

import android.util.Log
import com.crashlytics.android.Crashlytics
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.zenandroid.onlinego.OnlineGoApplication
import io.zenandroid.onlinego.model.Position
import io.zenandroid.onlinego.model.ogs.JosekiPosition
import io.zenandroid.onlinego.model.ogs.PlayCategory
import java.util.concurrent.TimeUnit

object JosekiRepository {

    private val disposable = CompositeDisposable()
    private val customMarkPattern = "<(.):([A-H]|[J-T]\\d{1,2})>".toPattern()
    private val headerWithMissingSpaceRegex = "#(?!\\s|#)".toRegex()
    private val dao = OnlineGoApplication.instance.db.gameDao()

    internal fun subscribe() {
    }

    fun unsubscribe() {
    }

    fun getJosekiPosition(id: Long?): Flowable<JosekiPosition> {
        disposable += OGSServiceImpl.getJosekiPositions(id)
                .subscribe(this::savePositionsToDB, this::onError)

        val dbObservable =
                if(id == null) dao.getJosekiRootPosition()
                else dao.getJosekiPostion(id)

        return dbObservable
                .map(this::extractLabelsFromDescription)
                .doOnNext {
                    it.next_moves = dao.getChildrenPositions(it.node_id ?: 0).map(this::extractLabelsFromDescription)
                }
                .distinctUntilChanged()
    }

    private fun savePositionsToDB(list: List<JosekiPosition>) {
        val children = mutableListOf<JosekiPosition>()
        list.forEach { pos ->
            pos.next_moves?.forEach {
                it.parent_id = pos.node_id
            }
            val isRoot = pos.play == ".root"
            pos.parent_id = if(isRoot) null else pos.parent?.node_id

            pos.next_moves?.let {
                children += pos
            }
        }
        dao.insertJosekiPositionsWithChildren(list, children)
    }

    private fun onError(error: Throwable) {
        Log.e("JosekiRepository", error.message, error)
        Crashlytics.logException(error)
    }

    private fun extractLabelsFromDescription(originalPos: JosekiPosition): JosekiPosition {
        var newDescription: String? = null
        originalPos.description?.let {
            newDescription = it.replace(headerWithMissingSpaceRegex, "# ")
            val matcher = customMarkPattern.matcher(newDescription)
            val sb = StringBuffer()
            val labels = mutableListOf<Position.Mark>()
            while(matcher.find()) {
                val label = matcher.group(1)
                val coordinate = matcher.group(2)

                labels.add(Position.Mark(Position.coordinateToPoint(coordinate), label, PlayCategory.LABEL))

                matcher.appendReplacement(sb, "**$label**")
            }
            originalPos.labels = labels
            matcher.appendTail(sb)
            newDescription = sb.toString()
        }

        originalPos.description = newDescription
        return originalPos
    }
}