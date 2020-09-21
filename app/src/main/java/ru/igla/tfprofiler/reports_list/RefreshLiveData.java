package ru.igla.tfprofiler.reports_list;

import androidx.lifecycle.MutableLiveData;

public class RefreshLiveData<T> extends MutableLiveData<T> {
    public interface RefreshAction<T> {
        interface Callback<T> {
             void onDataLoaded(T t);
        }

        void loadData(Callback<T> callback);
    }

    private final RefreshAction<T> refreshAction;
    private final RefreshAction.Callback<T> callback = new RefreshAction.Callback<T>() {
          @Override
          public void onDataLoaded(T t) {
               postValue(t);
          }
    };

    public RefreshLiveData(RefreshAction<T> refreshAction) {
        this.refreshAction = refreshAction;
    }

    public final void refresh() {
        refreshAction.loadData(callback);
    }
}