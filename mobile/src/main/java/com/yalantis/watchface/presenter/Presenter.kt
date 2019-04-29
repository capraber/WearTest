package com.yalantis.watchface.presenter

import com.yalantis.watchface.view.MvpView

interface Presenter<T : MvpView> {

    fun register(view: T)

    fun unregister(view: T)

}
