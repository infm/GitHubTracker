package com.infmme.githubtracker.app;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ViewSwitcher;
import org.kohsuke.github.GHNotificationStream;
import org.kohsuke.github.GHThread;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePageFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ViewSwitcher mViewSwitcher;
    private ListView mListView;
    private View mEmptyView;

    private Handler mHandler;

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

        mHandler = new Handler();
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
        mViewSwitcher.showNext();

        fetchData(context, mAdapter);
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
        mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);
        mListView = (ListView) mViewSwitcher.findViewById(R.id.listViewHomePage);
        mEmptyView = mViewSwitcher.findViewById(R.id.listViewHomePageEmptyView);
    }

    private void fetchData(final Context context, final NotificationsAdapter adapter) {
        final String accessToken = PreferenceManager.getDefaultSharedPreferences(context)
                                                    .getString("accessToken", "invalid");
        if (!"invalid".equals(accessToken)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String logTag = "FetchData";
                    try {
                        GitHub github =
                                GitHub.connectUsingOAuth(accessToken.substring(0, accessToken
                                        .indexOf('&')));
                        if (!github.isCredentialValid()) {
                            Log.e(logTag, "Auth failed " + github);
                            return;
                        }
                        Log.d(logTag, "Me: " + github.getMyself().getName());
                        GHNotificationStream stream = github.listNotifications();
                        stream.nonBlocking(true);
                        final List<GHThread> threadList = new ArrayList<GHThread>();
                        for (GHThread thread : stream)
                            threadList.add(thread);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.clear();
                                for (GHThread thread : threadList) {
                                    mAdapter.add(thread);
                                    Log.d(logTag, String.format("Repo: %s; Title: %s; type: %s; reason: " +
                                                                        "%s;",
                                                                thread.getRepository(),
                                                                thread.getTitle(), thread.getType(),
                                                                thread.getReason()));
                                }
                                adapter.notifyDataSetChanged();
                                if (mViewSwitcher.getCurrentView() == mEmptyView)
                                    mViewSwitcher.showPrevious();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
