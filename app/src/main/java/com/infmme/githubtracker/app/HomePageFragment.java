package com.infmme.githubtracker.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.infmme.githubtracker.app.data.NotificationsContentProvider;
import com.infmme.githubtracker.app.service.FetchService;


public class HomePageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;

    private ViewFlipper mViewFlipper;
    private ListView mListView;
    private View mEmptyView;
    private View mErrorView;

    private NotificationsAdapter mAdapter;

    // TODO: Rename and change types and number of parameters
    public static HomePageFragment newInstance() {
        HomePageFragment fragment = new HomePageFragment();
/*
        Bundle args = new Bundle();
        fragment.setArguments(args);
*/
        return fragment;
    }

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        findViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getActivity();
        mAdapter = new NotificationsAdapter(context);
        mListView.setAdapter(mAdapter);
/*
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW)
                                      .setData(Uri.parse(mAdapter.getItem(position).threadUrl)));
            }
        });
*/

        mViewFlipper.showNext();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
/*
        Context context = getActivity();
        if (null != context && null != mAdapter)
            fetchData(context, mAdapter);
*/
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void findViews(View view) {
        mViewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        mListView = (ListView) mViewFlipper.findViewById(R.id.listViewHomePage);
        mEmptyView = mViewFlipper.findViewById(R.id.listViewHomePageEmptyView);
        mErrorView = mViewFlipper.findViewById(R.id.listViewHomePageErrorView);
    }

    private void fetchData(@NonNull Context context) {
        context.startService(new Intent(context, FetchService.class));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.homepage_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fetch:
                Context context = getActivity();
                Toast.makeText(context, "Fetching started", Toast.LENGTH_SHORT).show();
                fetchData(context);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        while (mViewFlipper.getCurrentView() != mEmptyView)
            mViewFlipper.showPrevious();
        return new CursorLoader(getActivity(), NotificationsContentProvider.CONTENT_URI,
                                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        while (mViewFlipper.getCurrentView() != mListView)
            mViewFlipper.showPrevious();
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
        while (mViewFlipper.getCurrentView() != mErrorView)
            mViewFlipper.showNext();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
