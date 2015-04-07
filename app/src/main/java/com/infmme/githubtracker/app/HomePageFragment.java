package com.infmme.githubtracker.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getActivity();
        if (null != context && null != mAdapter)
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
                        GitHub github = authorize(accessToken);
                        Runnable task = null;
                        if (null != github) {
                            Log.d(logTag, "Me: " + github.getMyself().getName());
                            GHNotificationStream stream = github.listNotifications();
                            stream.nonBlocking(true);
                            final List<GHThread> threadList = new ArrayList<GHThread>();
                            for (GHThread thread : stream)
                                threadList.add(thread);
                            task = new Runnable() {
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
                            };
                        } else {
                            PreferenceManager.getDefaultSharedPreferences(context)
                                             .edit().putString("accessToken", "invalid").commit();
                            task = new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(context, LoginActivity.class));
                                }
                            };
                        }
                        mHandler.post(task);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private GitHub authorize(String accessToken) {
        try {
            String meaningfulPart = accessToken
                    .substring(0, accessToken.indexOf('&'));
            GitHub github = GitHub.connectUsingOAuth(meaningfulPart);
            if (!github.isCredentialValid()) {
                Log.e("FetchData", "Auth failed " + github);
                return null;
            }
            return github;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
