package com.suzuki.adapter;

import android.content.Context;
import android.location.Location;
import android.os.Handler;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.autosuggest.MapplsAutoSuggest;
import com.mappls.sdk.services.api.autosuggest.MapplsAutosuggestManager;
import com.mappls.sdk.services.api.autosuggest.model.AutoSuggestAtlasResponse;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.textsearch.MapplsTextSearch;
import com.mappls.sdk.services.api.textsearch.MapplsTextSearchManager;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.fragment.MapMainFragment;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.suzuki.activity.RouteActivity.stopAutoSuggest;

public class AutoCompleteTextWatcher implements TextWatcher {

    private Context context;
    private RecyclerView mSuggestionListView;
    private ProgressBar progressBar;
    private Handler _handler;
    private AutoSuggestAdapter adapter;
    private String mCurrentQuery = "";
    private MapplsAutoSuggest mCallForAutoSuggestNew;
    private MapplsTextSearch mCallForTextSearch;

    public AutoCompleteTextWatcher(Context context, RecyclerView suggestionListView, ProgressBar progressBar) {
        Log.d("AutoCompleteTW", "AutoCompleteTextWatcher: ");
        this.context = context;
        this.mSuggestionListView = suggestionListView;
        this.progressBar = progressBar;
        _handler = new Handler();
        adapter = new AutoSuggestAdapter(context, null, null);
        mSuggestionListView.setAdapter(adapter);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }


    @Override
    public void afterTextChanged(Editable s) {

    }


    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        if (!stopAutoSuggest) {

            mCurrentQuery = s.toString();
            Log.d("AutoCompleteTW", "onTextChanged: " + mCurrentQuery);

            _handler.removeCallbacksAndMessages(null);
            _handler.postDelayed(() -> {
                if (s.toString().trim().length() < 2) {
                    setAutoCompleteArrayAdapter(new ELocation[0]);
                    return;
                }
                hitAutoSuggestApiNew(mCurrentQuery);
            }, 300);
        }
    }

    /**
     * Sets the adapter for a given autocompletetextview
     *
     * @param list list to be populated
     */
    private synchronized void setAutoCompleteArrayAdapter(ELocation[] list) {
        setAutoCompleteArrayAdapter(Arrays.asList(list));
    }

    private synchronized void setAutoCompleteArrayAdapter(List<ELocation> list) {
        if (list == null)
            return;
        adapter.updateSearchList(list, null);
        if (mSuggestionListView.getAdapter() == null)
            mSuggestionListView.setAdapter(adapter);

    }

    public synchronized void setAutoCompleteArrayAdapter(AutoSuggestAtlasResponse response) {
        if (response == null) {
            return;
        }

        Log.d("sksksksks", "===" + response);
        adapter.updateSearchList(response);
        if (mSuggestionListView.getAdapter() == null)
            mSuggestionListView.setAdapter(adapter);
        mSuggestionListView.setVisibility(View.VISIBLE);
    }


    private void hitAutoSuggestApiNew(final String query) {
//        if (mCallForAutoSuggestNew != null && mCallForAutoSuggestNew.isExecuted()) {
//            mCallForAutoSuggestNew.cancelCall();
//        }
        double latitude = 0, longitude = 0;
        try {
            if (context != null && ((SuzukiApplication) context.getApplicationContext()).getCurrentLocation() != null) {
                Location location = ((SuzukiApplication) context.getApplicationContext()).getCurrentLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        mCallForAutoSuggestNew = MapplsAutoSuggest.builder()
                .query(query)
                .build();
        showHideProgress(true);

        MapplsAutosuggestManager.newInstance(mCallForAutoSuggestNew).call(new OnResponseCallback<AutoSuggestAtlasResponse>() {
            @Override
            public void onSuccess(AutoSuggestAtlasResponse autoSuggestAtlasResponse) {
                //handle response
                if (context == null)
                    return;
                showHideProgress(false);

                setAutoCompleteArrayAdapter((autoSuggestAtlasResponse));
            }

            @Override
            public void onError(int i, String s) {

                Toast.makeText(context, ""+s, Toast.LENGTH_SHORT).show();
                showHideProgress(false);
            }
        });

//        mCallForAutoSuggestNew = MapplsAutoSuggest.builder().query(query).setLocation(latitude, longitude).build();
//        showHideProgress(true);
//        mCallForAutoSuggestNew.enqueueCall(new Callback<AutoSuggestAtlasResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<AutoSuggestAtlasResponse> call, @NonNull Response<AutoSuggestAtlasResponse> response) {
//                if (context == null)
//                    return;
//                showHideProgress(false);
//
//                setAutoCompleteArrayAdapter((response.body()));
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AutoSuggestAtlasResponse> call, @NonNull Throwable t) {
//                if (!call.isCanceled()) {
//                    t.printStackTrace();
//                }
//                showHideProgress(false);
//            }
//        });

    }

    public void hitTextSearchApiNew(final String query, final MapMainFragment.TextSearchListener textSearchListener) {
//        if (mCallForTextSearch != null && mCallForTextSearch.isExecuted()) {
//            mCallForTextSearch.cancelCall();
//        }
        double latitude = 0, longitude = 0;
        try {
            if (context != null && ((SuzukiApplication) context.getApplicationContext()).getCurrentLocation() != null) {
                Location location = ((SuzukiApplication) context.getApplicationContext()).getCurrentLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        if (textSearchListener != null)
            textSearchListener.showProgress();

        mCallForTextSearch=MapplsTextSearch.builder().query(query).setLocation(latitude,longitude).build();

        MapplsTextSearchManager.newInstance(mCallForTextSearch).call(new OnResponseCallback<AutoSuggestAtlasResponse>() {
            @Override
            public void onSuccess(AutoSuggestAtlasResponse autoSuggestAtlasResponse) {
                if (context == null)
                    return;
                if (textSearchListener != null)
                    textSearchListener.hideProgress();
                setAutoCompleteArrayAdapter((autoSuggestAtlasResponse));
            }

            @Override
            public void onError(int i, String s) {
                if (textSearchListener != null)
                    textSearchListener.hideProgress();

            }
        });
       // mCallForTextSearch = MapplsTextSearch.builder().query(query).setLocation(latitude, longitude).build();
//        mCallForTextSearch.enqueueCall(new Callback<AutoSuggestAtlasResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<AutoSuggestAtlasResponse> call, @NonNull retrofit2.Response<AutoSuggestAtlasResponse> response) {
//                if (context == null)
//                    return;
//                if (textSearchListener != null)
//                    textSearchListener.hideProgress();
//                setAutoCompleteArrayAdapter((response.body()));
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AutoSuggestAtlasResponse> call, @NonNull Throwable t) {
//                if (textSearchListener != null)
//                    textSearchListener.hideProgress();
//                if (!call.isCanceled()) {
//                    t.printStackTrace();
//                }
//            }
//        });
    }

    private void showHideProgress(boolean showHide) {
        progressBar.setVisibility(showHide ? View.VISIBLE : View.GONE);
    }
}
