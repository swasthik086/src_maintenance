package com.suzuki.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.mappls.sdk.services.api.autosuggest.model.AtlasExplaination;
import com.mappls.sdk.services.api.autosuggest.model.AutoSuggestAtlasResponse;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.autosuggest.model.SuggestedSearchAtlas;
import com.suzuki.R;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestAdapter extends SectionedRecyclerViewAdapter<AutoSuggestAdapter.MyItemViewHolder> {
    private final int SECTION_INDEX_SEARCH_SUGGESTIONS = 0;
    private final int SECTION_INDEX_SEARCH_RESULTS = 1;
    private final Context mContext;
    private final ArrayList<ELocation> mSearchList;
    private final ArrayList<SuggestedSearchAtlas> mSuggestionList;
    private AtlasExplaination mExplanation;

    public AutoSuggestAdapter(Context context, ArrayList<ELocation> searchList, ArrayList<SuggestedSearchAtlas> suggestionList) {

        Log.d("AutoCompleteTW", "AutoSuggestAdapter() called with: context = [" + context + "], searchList = [" + searchList + "], suggestionList = [" + suggestionList + "]");

        this.mContext = context;
        this.mSearchList = searchList != null ? searchList : new ArrayList<ELocation>();
        this.mSuggestionList = suggestionList != null ? suggestionList : new ArrayList<SuggestedSearchAtlas>();
        shouldShowHeadersForEmptySections(false);
    }

    @Override
    public int getSectionCount() {
        return 2;
    }

    @Override
    public int getItemCount(int section) {
        if (section == SECTION_INDEX_SEARCH_RESULTS) {
            if (mSearchList != null) {
                return mSearchList.size();
            }
        } else if (section == SECTION_INDEX_SEARCH_SUGGESTIONS) {
            if (mSuggestionList != null) {
                return mSuggestionList.size();
            }
        }
        return 0;
    }

    @Override
    public void onBindHeaderViewHolder(AutoSuggestAdapter.MyItemViewHolder holder, int section, boolean expanded) {
        //ignore
        holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
    }

    @Override
    public void onBindFooterViewHolder(MyItemViewHolder holder, int section) {
    }

    @Override
    public void onBindViewHolder(AutoSuggestAdapter.MyItemViewHolder holder, int section, int relativePosition, int absolutePosition) {
        //relativePosition is the index in the current section.
        holder.itemView.setTag(null);
        holder.itemView.setTag(R.string.AutoSuggestTagSuggestion, null);
        holder.itemView.setTag(R.string.AutoSuggestTagExplaination, null);
        if (section == SECTION_INDEX_SEARCH_SUGGESTIONS) {
            holder.itemView.setVisibility(View.VISIBLE);
            if (holder.itemView.getHeight() == 0) {
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            SuggestedSearchAtlas suggestionObj = mSuggestionList.get(relativePosition);
            holder.titleTextView.setText(suggestionObj.getSearchStringToShow());
            holder.itemView.setTag(R.string.AutoSuggestTagSuggestion, suggestionObj);
            holder.itemView.setTag(R.string.AutoSuggestTagExplaination, mExplanation);
        } else if (section == SECTION_INDEX_SEARCH_RESULTS) {
            holder.itemView.setVisibility(View.VISIBLE);
            if (holder.itemView.getHeight() == 0) {
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            ELocation entry = mSearchList.get(relativePosition);
            if (entry == null)
                return;
            holder.titleTextView.setText(entry.placeName);
            holder.addressTextView.setText(entry.placeAddress);
            if (entry.alternateName != null && !entry.alternateName.equalsIgnoreCase("")) {
                holder.aliasNameTextView.setText(Html.fromHtml("<i>(" + entry.alternateName + ")<i>"));
                holder.aliasNameTextView.setVisibility(View.VISIBLE);
            } else {
                holder.aliasNameTextView.setVisibility(View.GONE);
            }


            holder.itemView.setTag(new WrapperAutoSuggestResult(WrapperAutoSuggestResult.TYPE_SEARCH_ITEM, entry));
        }
    }

    @NonNull
    @Override
    public AutoSuggestAdapter.MyItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.auto_complete_list_item, parent, false);
        return new MyItemViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    void updateSearchList(AutoSuggestAtlasResponse response) {
        if (response == null) {
            return;
        }
        this.mExplanation = response.getExplaination();
        mSearchList.clear();
        if (response.getSuggestedLocations() != null) {
            mSearchList.addAll(response.getSuggestedLocations());
        }
        if (response.getUserAddedLocations() != null) {
            mSearchList.addAll(response.getUserAddedLocations());
        }
        mSuggestionList.clear();
        if (response.getSuggestedSearches() != null) {
            mSuggestionList.addAll(response.getSuggestedSearches());

            Log.d("sksksksks","===ada"+response);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    void updateSearchList(List<ELocation> searchList, ArrayList<SuggestedSearchAtlas> suggestionList) {
        mSearchList.clear();
        if (searchList != null) {
            mSearchList.addAll(searchList);
        }
        mSuggestionList.clear();
        if (suggestionList != null) {
            mSuggestionList.addAll(suggestionList);
        }
        notifyDataSetChanged();
    }

    static class MyItemViewHolder extends SectionedViewHolder {
        private final TextView titleTextView;
        private final TextView addressTextView;
        private final TextView aliasNameTextView;

        public MyItemViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.auto_list_item);
            addressTextView = itemView.findViewById(R.id.auto_list_item_address);
            aliasNameTextView = itemView.findViewById(R.id.auto_list_item_alternate_name);
        }
    }

    public static class WrapperAutoSuggestResult {
        public static final int TYPE_SEARCH_ITEM = 4;
        private int itemType;
        private ELocation eLocation;

        public WrapperAutoSuggestResult(int itemType, ELocation eLocation) {
            this.itemType = itemType;
            this.eLocation = eLocation;
        }

        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }

        public ELocation getElocation() {
            return eLocation;
        }

        public void setElocation(ELocation eLocation) {
            this.eLocation = eLocation;
        }
    }
}
