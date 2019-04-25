package com.yalantis.watchface.presenter;

import com.yalantis.watchface.view.MvpView;

public interface Presenter<T extends MvpView> {

    void register(T view);

    void unregister(T view);

}
