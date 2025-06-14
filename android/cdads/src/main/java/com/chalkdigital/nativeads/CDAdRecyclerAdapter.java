package com.chalkdigital.nativeads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.chalkdigital.common.CDAdRequest;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.nativeads.CDAdNativeAdPositioning.CDAdClientPositioning;
import com.chalkdigital.nativeads.CDAdNativeAdPositioning.CDAdServerPositioning;

import java.util.List;
import java.util.WeakHashMap;

import static com.chalkdigital.nativeads.CDAdRecyclerAdapter.ContentChangeStrategy.INSERT_AT_END;
import static com.chalkdigital.nativeads.CDAdRecyclerAdapter.ContentChangeStrategy.KEEP_ADS_FIXED;


public final class CDAdRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // RecyclerView ad views will have negative types to avoid colliding with original view types.
    static final int NATIVE_AD_VIEW_TYPE_BASE = -56;

    public enum ContentChangeStrategy {
        INSERT_AT_END, MOVE_ALL_ADS_WITH_CONTENT, KEEP_ADS_FIXED
    }

    @NonNull private final RecyclerView.AdapterDataObserver mAdapterDataObserver;
    @Nullable private RecyclerView mRecyclerView;
    @NonNull private final CDAdStreamAdPlacer mStreamAdPlacer;
    @NonNull private final RecyclerView.Adapter mOriginalAdapter;
    @NonNull private final VisibilityTracker mVisibilityTracker;
    @NonNull private final WeakHashMap<View, Integer> mViewPositionMap;

    @NonNull private ContentChangeStrategy mStrategy = INSERT_AT_END;
    @Nullable private CDAdNativeAdLoadedListener mAdLoadedListener;

    public CDAdRecyclerAdapter(@NonNull Activity activity,
                                @NonNull RecyclerView.Adapter originalAdapter) {
        this(activity, originalAdapter, CDAdNativeAdPositioning.serverPositioning());
    }

    public CDAdRecyclerAdapter(@NonNull Activity activity,
                                @NonNull RecyclerView.Adapter originalAdapter,
                                @NonNull CDAdServerPositioning adPositioning) {
        this(new CDAdStreamAdPlacer(activity, adPositioning), originalAdapter,
                new VisibilityTracker(activity));
    }

    public CDAdRecyclerAdapter(@NonNull Activity activity,
                                @NonNull RecyclerView.Adapter originalAdapter,
                                @NonNull CDAdClientPositioning adPositioning) {
        this(new CDAdStreamAdPlacer(activity, adPositioning), originalAdapter,
                new VisibilityTracker(activity));
    }

    @VisibleForTesting
    CDAdRecyclerAdapter(@NonNull final CDAdStreamAdPlacer streamAdPlacer,
                         @NonNull final RecyclerView.Adapter originalAdapter,
                         @NonNull final VisibilityTracker visibilityTracker) {
        mViewPositionMap = new WeakHashMap<>();
        mOriginalAdapter = originalAdapter;
        mVisibilityTracker = visibilityTracker;
        mVisibilityTracker.setVisibilityTrackerListener(new VisibilityTracker.VisibilityTrackerListener() {
            @Override
            public void onVisibilityChanged(final List<View> visibleViews,
                    final List<View> invisibleViews) {
                handleVisibilityChanged(visibleViews, invisibleViews);
            }
        });

        setHasStableIdsInternal(mOriginalAdapter.hasStableIds());

        mStreamAdPlacer = streamAdPlacer;
        mStreamAdPlacer.setAdLoadedListener(new CDAdNativeAdLoadedListener() {
            @Override
            public void onAdLoaded(final int position) {
                handleAdLoaded(position);
            }

            @Override
            public void onAdRemoved(final int position) {
                handleAdRemoved(position);
            }
        });
        mStreamAdPlacer.setItemCount(mOriginalAdapter.getItemCount());

        mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mStreamAdPlacer.setItemCount(mOriginalAdapter.getItemCount());
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(final int positionStart, final int itemCount) {
                int adjustedEndPosition = mStreamAdPlacer.getAdjustedPosition(positionStart + itemCount - 1);
                int adjustedStartPosition = mStreamAdPlacer.getAdjustedPosition(positionStart);
                int adjustedCount = adjustedEndPosition - adjustedStartPosition + 1;
                notifyItemRangeChanged(adjustedStartPosition, adjustedCount);
            }

            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                final int adjustedStartPosition = mStreamAdPlacer.getAdjustedPosition(positionStart);
                final int newOriginalCount = mOriginalAdapter.getItemCount();
                mStreamAdPlacer.setItemCount(newOriginalCount);
                final boolean addingToEnd = positionStart + itemCount >= newOriginalCount;
                if (KEEP_ADS_FIXED == mStrategy
                        || (INSERT_AT_END == mStrategy
                        && addingToEnd)) {
                    notifyDataSetChanged();
                } else {
                    for (int i = 0; i < itemCount; i++) {
                        // We insert itemCount items at the original position, moving ads downstream.
                        mStreamAdPlacer.insertItem(positionStart);
                    }
                    notifyItemRangeInserted(adjustedStartPosition, itemCount);
                }
            }

            @Override
            public void onItemRangeRemoved(final int positionStart, final int itemsRemoved) {
                int adjustedStartPosition = mStreamAdPlacer.getAdjustedPosition(positionStart);
                final int newOriginalCount = mOriginalAdapter.getItemCount();
                mStreamAdPlacer.setItemCount(newOriginalCount);
                final boolean removingFromEnd = positionStart + itemsRemoved >= newOriginalCount;
                if (KEEP_ADS_FIXED == mStrategy
                        || (INSERT_AT_END == mStrategy
                        && removingFromEnd)) {
                    notifyDataSetChanged();
                } else {
                    final int oldAdjustedCount = mStreamAdPlacer.getAdjustedCount(newOriginalCount + itemsRemoved);
                    for (int i = 0; i < itemsRemoved; i++) {
                        // We remove itemsRemoved items at the original position.
                        mStreamAdPlacer.removeItem(positionStart);
                    }

                    final int itemsRemovedIncludingAds = oldAdjustedCount - mStreamAdPlacer.getAdjustedCount(newOriginalCount);
                    // Need to move the start position back by the # of ads removed.
                    adjustedStartPosition -= itemsRemovedIncludingAds - itemsRemoved;
                    notifyItemRangeRemoved(adjustedStartPosition, itemsRemovedIncludingAds);
                }
            }

            @Override
            public void onItemRangeMoved(final int fromPosition, final int toPosition,
                    final int itemCount) {
                notifyDataSetChanged();
            }
        };

        mOriginalAdapter.registerAdapterDataObserver(mAdapterDataObserver);
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    /**
     * Sets a listener that will be called after the SDK loads new ads from the server and places
     * them into your stream.
     *
     * The listener will be active between when you call {@link #loadAds} and when you call
     * destroy(). You can also set the listener to {@code null} to remove the listener.
     *
     * Note that there is not a one to one correspondence between calls to {@link #loadAds} and this
     * listener. The SDK will call the listener every time an ad loads.
     *
     * @param listener The listener.
     */
    public void setAdLoadedListener(@Nullable final CDAdNativeAdLoadedListener listener) {
        mAdLoadedListener = listener;
    }

    /**
     * Registers an ad renderer for rendering a specific native ad format. Note that if multiple ad
     * renderers support a specific native ad format, the first one registered will be used.
     */
    public void registerAdRenderer(@NonNull CDAdAdRenderer adRenderer) {
        if (!Preconditions.NoThrow.checkNotNull(adRenderer, "Cannot register a null adRenderer")) {
            return;
        }
        mStreamAdPlacer.registerAdRenderer(adRenderer);
    }

    /**
     * Start loading ads from the CDAd server.
     *
     * We recommend using {@link #loadAds(String, CDAdRequest)} instead of this method, in
     * order to pass targeting information to the server.
     *
     * @param adUnitId The ad unit ID to use when loading ads.
     */
    public void loadAds(@NonNull String adUnitId) {
        mStreamAdPlacer.loadAds(adUnitId);
    }

    /**
     * Start loading ads from the CDAd server, using the given request targeting information.
     *
     * When loading ads, {@link CDAdNativeAdLoadedListener#onAdLoaded(int)} will be called for each
     * ad that is added to the stream.
     *
     * @param adUnitId The ad unit ID to use when loading ads.
     * @param cdAdRequest Targeting information to pass to the ad server.
     */
    public void loadAds(@NonNull String adUnitId, @Nullable CDAdRequest cdAdRequest) {
        mStreamAdPlacer.loadAds(adUnitId, cdAdRequest);
    }

    public static int computeScrollOffset(@NonNull final LinearLayoutManager linearLayoutManager,
            @Nullable final RecyclerView.ViewHolder holder) {
        if (holder == null) {
            return 0;
        }
        final View view = holder.itemView;

        int offset = 0;
        if (linearLayoutManager.canScrollVertically()) {
            if (linearLayoutManager.getStackFromEnd()) {
                offset = view.getBottom();
            } else {
                offset = view.getTop();
            }
        } else if (linearLayoutManager.canScrollHorizontally()) {
            if (linearLayoutManager.getStackFromEnd()) {
                offset = view.getRight();
            } else {
                offset = view.getLeft();
            }
        }

        return offset;
    }

    /**
     * Refreshes ads in the adapter while preserving the scroll position.
     *
     * Call this instead of {@link #loadAds(String, CDAdRequest)} in order to preserve the
     * scroll position in your view. Only usable with LinearLayoutManager or GridLayoutManager.
     *
     * @param adUnitId The ad unit ID to use when loading ads.
     */
    public void refreshAds(@NonNull String adUnitId) {
        refreshAds(adUnitId, null);
    }

    /**
     * Refreshes ads in the adapter while preserving the scroll position.
     *
     * Call this instead of {@link #loadAds(String, CDAdRequest)} in order to preserve the
     * scroll position in your view. Only usable with LinearLayoutManager or GridLayoutManager.
     *
     * @param adUnitId The ad unit ID to use when loading ads.
     * @param cdAdRequest Targeting information to pass to the ad server.
     */
    public void refreshAds(@NonNull String adUnitId,
            @Nullable CDAdRequest cdAdRequest) {
        if (mRecyclerView == null) {
            CDAdLog.w("This adapter is not attached to a RecyclerView and cannot be refreshed.");
            return;
        }

        final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            CDAdLog.w("Can't refresh ads when there is no layout manager on a RecyclerView.");
            return;
        }

        if (layoutManager instanceof LinearLayoutManager) {
            // Includes GridLayoutManager

            // Get the range & offset of scroll position.
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            final int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForLayoutPosition(firstPosition);
            final int scrollOffset = computeScrollOffset(linearLayoutManager, holder);

            // Calculate the range of ads not to remove ads from.
            int startOfRange = Math.max(0, firstPosition - 1);
            while (mStreamAdPlacer.isAd(startOfRange) && startOfRange > 0) {
                startOfRange--;
            }


            final int itemCount = getItemCount();
            int endOfRange = linearLayoutManager.findLastVisibleItemPosition();
            while (mStreamAdPlacer.isAd(endOfRange) && endOfRange < itemCount - 1) {
                endOfRange++;
            }

            final int originalStartOfRange = mStreamAdPlacer.getOriginalPosition(startOfRange);
            final int originalEndOfRange = mStreamAdPlacer.getOriginalPosition(endOfRange);
            final int endCount = mOriginalAdapter.getItemCount();

            mStreamAdPlacer.removeAdsInRange(originalEndOfRange, endCount);
            final int numAdsRemoved = mStreamAdPlacer.removeAdsInRange(0, originalStartOfRange);

            if (numAdsRemoved > 0) {
                linearLayoutManager.scrollToPositionWithOffset(firstPosition - numAdsRemoved, scrollOffset);
            }

            loadAds(adUnitId, cdAdRequest);
        } else {
            CDAdLog.w("This LayoutManager can't be refreshed.");
        }
    }

    /**
     * Stops loading ads, immediately clearing any ads currently in the stream.
     *
     * This method also stops ads from loading as the user moves through the stream. When ads
     * are cleared, {@link CDAdNativeAdLoadedListener#onAdRemoved} will be called for each ad
     * that is removed from the stream.
     */
    public void clearAds() {
        mStreamAdPlacer.clearAds();
    }

    /**
     * Whether the given position is an ad.
     *
     * This will return {@code true} only if there is an ad loaded for this position. You can also
     * listen for ads to load using {@link CDAdNativeAdLoadedListener#onAdLoaded(int)}.
     *
     * @param position The position to check for an ad, expressed in terms of the position in the
     * stream including ads.
     * @return Whether there is an ad at the given position.
     */
    public boolean isAd(final int position) {
        return mStreamAdPlacer.isAd(position);
    }

    /**
     * Returns the position of an item considering ads in the stream.
     *
     * @see {@link CDAdStreamAdPlacer#getAdjustedPosition(int)}
     * @param originalPosition The original position.
     * @return The position adjusted by placing ads.
     */
    public int getAdjustedPosition(final int originalPosition) {
        return mStreamAdPlacer.getAdjustedPosition(originalPosition);
    }

    /**
     * Returns the original position of an item considering ads in the stream.
     *
     * @see {@link CDAdStreamAdPlacer#getOriginalPosition(int)}
     * @param position The adjusted position.
     * @return The original position before placing ads.
     */
    public int getOriginalPosition(final int position) {
        return mStreamAdPlacer.getOriginalPosition(position);
    }

    /**
     * Sets the strategy this adapter should use for moving ads when content is added or removed
     * from the wrapped original adapter. This strategy can be set at any time to change the
     * behavior of the adapter.
     * <ul>
     * <li>{@link CDAdRecyclerAdapter.ContentChangeStrategy#INSERT_AT_END}
     *     will insert ads when content is added to the end of the stream. This is the default behavior
     *     and the recommended strategy.</li>
     * <li>{@link CDAdRecyclerAdapter.ContentChangeStrategy#MOVE_ALL_ADS_WITH_CONTENT}
     *     will cause all ad positions after an insertion or deletion to be adjusted. New
     *     ads will not be displayed when items are added to the end of the stream.</li>
     * <li>{@link CDAdRecyclerAdapter.ContentChangeStrategy#KEEP_ADS_FIXED}
     *     will never adjust ad positions when items are inserted or removed.</li>
     * </ul>
     */
    public void setContentChangeStrategy(@NonNull ContentChangeStrategy strategy) {
        if (!Preconditions.NoThrow.checkNotNull(strategy)) {
            return;
        }
        mStrategy = strategy;
    }

    @Override
    public int getItemCount() {
        return mStreamAdPlacer.getAdjustedCount(mOriginalAdapter.getItemCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType >= NATIVE_AD_VIEW_TYPE_BASE && viewType <= NATIVE_AD_VIEW_TYPE_BASE + mStreamAdPlacer.getAdViewTypeCount()) {
            // Create the view and a view holder.
            final CDAdAdRenderer adRenderer = mStreamAdPlacer.getAdRendererForViewType(viewType - NATIVE_AD_VIEW_TYPE_BASE);
            if (adRenderer == null) {
                CDAdLog.w("No view binder was registered for ads in CDAdRecyclerAdapter.");
                // This will cause a null pointer exception.
                return null;
            }
            return new CDAdRecyclerViewHolder(
                    adRenderer.createAdView((Activity) parent.getContext(), parent));
        }

        return mOriginalAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Object adResponse = mStreamAdPlacer.getAdData(position);
        if (adResponse != null) {
            mStreamAdPlacer.bindAdView((NativeAd) adResponse, holder.itemView);
            return;
        }

        mViewPositionMap.put(holder.itemView, position);
        mVisibilityTracker.addView(holder.itemView, 0, null);

        //noinspection unchecked
        mOriginalAdapter.onBindViewHolder(holder, mStreamAdPlacer.getOriginalPosition(position));
    }

    @Override
    public int getItemViewType(final int position) {
        int type = mStreamAdPlacer.getAdViewType(position);
        if (type != CDAdStreamAdPlacer.CONTENT_VIEW_TYPE) {
            return NATIVE_AD_VIEW_TYPE_BASE + type;
        }

        return mOriginalAdapter.getItemViewType(mStreamAdPlacer.getOriginalPosition(position));
    }

    @Override
    public void setHasStableIds(final boolean hasStableIds) {
        setHasStableIdsInternal(hasStableIds);

        // We can only setHasStableIds when there are no observers on the adapter.
        mOriginalAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        mOriginalAdapter.setHasStableIds(hasStableIds);
        mOriginalAdapter.registerAdapterDataObserver(mAdapterDataObserver);
    }

    public void destroy() {
        mOriginalAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        mStreamAdPlacer.destroy();
        mVisibilityTracker.destroy();
    }

    /**
     * Returns a stable negative item ID for ad items & calls getItemId on your original adapter for
     * non-ad items.
     *
     * Returns {@link androidx.recyclerview.widget.RecyclerView#NO_ID} if your original adapter does
     * not have stable IDs.
     *
     * @inheritDoc
     */
    @Override
    public long getItemId(final int position) {
        if (!mOriginalAdapter.hasStableIds()) {
            return RecyclerView.NO_ID;
        }

        final Object adData = mStreamAdPlacer.getAdData(position);
        if (adData != null) {
            return -System.identityHashCode(adData);
        }

        return mOriginalAdapter.getItemId(mStreamAdPlacer.getOriginalPosition(position));
    }

    // Notification methods to forward to the original adapter.
    @Override
    public boolean onFailedToRecycleView(final RecyclerView.ViewHolder holder) {
        if (holder instanceof CDAdRecyclerViewHolder) {
            return super.onFailedToRecycleView(holder);
        }

        // noinspection unchecked
        return mOriginalAdapter.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(final RecyclerView.ViewHolder holder) {
        if (holder instanceof CDAdRecyclerViewHolder) {
            super.onViewAttachedToWindow(holder);
            return;
        }

        // noinspection unchecked
        mOriginalAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder) {
        if (holder instanceof CDAdRecyclerViewHolder) {
            super.onViewDetachedFromWindow(holder);
            return;
        }

        // noinspection unchecked
        mOriginalAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(final RecyclerView.ViewHolder holder) {
        if (holder instanceof CDAdRecyclerViewHolder) {
            super.onViewRecycled(holder);
            return;
        }

        // noinspection unchecked
        mOriginalAdapter.onViewRecycled(holder);
    }
    // End forwarded methods.

    @VisibleForTesting
    void handleAdLoaded(final int position) {
        if (mAdLoadedListener != null) {
            mAdLoadedListener.onAdLoaded(position);
        }

        notifyItemInserted(position);
    }

    @VisibleForTesting
    void handleAdRemoved(final int position) {
        if (mAdLoadedListener != null) {
            mAdLoadedListener.onAdRemoved(position);
        }

        notifyItemRemoved(position);
    }

    private void handleVisibilityChanged(final List<View> visibleViews,
            final List<View> invisibleViews) {
        // Loop through all visible positions in order to build a max and min range, and then
        // place ads into that range.
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (final View view : visibleViews) {
            final Integer pos = mViewPositionMap.get(view);
            if (pos == null) {
                continue;
            }
            min = Math.min(pos, min);
            max = Math.max(pos, max);
        }
        mStreamAdPlacer.placeAdsInRange(min, max + 1);
    }

    /**
     * Sets the hasStableIds value on this adapter only, not also on the wrapped adapter.
     */
    private void setHasStableIdsInternal(final boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }
}
