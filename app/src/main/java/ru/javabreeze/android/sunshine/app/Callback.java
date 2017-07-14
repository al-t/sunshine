package ru.javabreeze.android.sunshine.app;

import android.net.Uri;

/**
 * Created by Алексей on 08.03.2017.
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */
public interface Callback {
    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onItemSelected(Uri dateUri);
}
