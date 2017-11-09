package io.zenandroid.onlinego.mygames

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.zenandroid.onlinego.model.ogs.Game
import io.zenandroid.onlinego.ogs.ActiveGameService
import io.zenandroid.onlinego.ogs.OGSService

/**
 * Created by alex on 05/11/2017.
 */
class MyGamesPresenter(val view: MyGamesContract.View, private val service: OGSService, private val activeGameService: ActiveGameService) : MyGamesContract.Presenter {

    private val subscriptions = CompositeDisposable()

    override fun subscribe() {
        subscriptions.add(
                activeGameService.activeGamesObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()) // TODO: remove me!!!
                        .subscribe(this::addGame)
        )
    }

    private fun addGame(game: Game) {
        view.addGame(game)
        val gameConnection = service.connectToGame(game.id)
        subscriptions.add(gameConnection)
        subscriptions.add(gameConnection.gameData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // TODO: remove me!!!
                .subscribe({ gameData -> view.setGameData(game.id, gameData) }))
        subscriptions.add(gameConnection.moves
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) // TODO: remove me!!!
                .subscribe({ move -> view.doMove(game.id, move) }))
    }

    override fun unsubscribe() {
        subscriptions.clear()
    }
}