package com.imagem.poc.poccensoagrodmc;

/**
 * Created by Wlima on 29/03/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISPopupInfo;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.map.popup.PopupContainerView;
import com.esri.core.map.popup.PopupInfo;

public class CustomPopup extends Fragment {


        private PopupContainer mPopupContainer;
        private MapView mMapView;
        private boolean mIsInitialize, mIsDisplayed;


        public CustomPopup() {
            mIsInitialize = false;
            mIsDisplayed = false;
        }

        public CustomPopup(MapView mapView) {
            this.mMapView = mapView;
            mPopupContainer = new PopupContainer(mMapView);
            mIsInitialize = true;
            mIsDisplayed = false;
        }

        public CustomPopup(MapView mapView, PopupContainer container) {
            this.mMapView = mapView;
            this.mPopupContainer = container;
            if (mPopupContainer != null)
                mIsInitialize = true;
            else
                mIsInitialize = false;
            mIsDisplayed = false;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Create popupcontainer if it hasn't been created
            if (mPopupContainer == null) {
                mPopupContainer = new PopupContainer(mMapView);
                mIsInitialize = true;
            }

            // Fragment wants to add menu to action bar
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            PopupContainerView view = null;

            if (mPopupContainer != null) {
                view = mPopupContainer.getPopupContainerView();
                view.setOnPageChangelistener(new OnPageChangeListener() {

                    @Override
                    public void onPageSelected(int arg0) {

                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                        // Refresh menu item while swipping popups
                        Activity activity = (Activity) mMapView.getContext();
                        activity.invalidateOptionsMenu();
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {

                    }
                });
            }
            return view;
        }


        public void addPopup(Popup popup) {
            if (mPopupContainer != null)
                mPopupContainer.addPopup(popup);
        }

        // Indicate if popupcontainer has been created
        public boolean isInitialize() {
            return mIsInitialize;
        }

        public void setInitialize(boolean isInitialize) {
            this.mIsInitialize = isInitialize;
        }

        // Indicate if fragment is displayed
        public boolean isDisplayed() {
            return mIsDisplayed;
        }

        public void setDisplayed(boolean isDisplayed) {
            this.mIsDisplayed = isDisplayed;
        }

        //Display
        public void show() {
            if (mIsDisplayed)
                return;

            FragmentActivity activity = (FragmentActivity) mMapView.getContext();
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.add(android.R.id.content, this, null);
            transaction.addToBackStack(null);
            transaction.commit();
            setDisplayed(true);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if ((resultCode == Activity.RESULT_OK)
                    && (data != null) && (mPopupContainer != null)) {
                // Add the selected media as attachment.
                Uri selectedImage = data.getData();
                mPopupContainer.getCurrentPopup().addAttachment(selectedImage);
            }
        }


    }

