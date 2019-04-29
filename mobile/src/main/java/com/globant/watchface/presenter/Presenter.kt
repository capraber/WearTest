package com.globant.watchface.presenter

import com.globant.watchface.view.MvpView

interface Presenter<T : MvpView> {

    fun register(view: T)

    fun unregister(view: T)

}
