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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import com.infmme.githubtracker.app.util.GHThreadPreview;
import org.kohsuke.github.GHNotificationStream;
import org.kohsuke.github.GHThread;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomePageFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ViewFlipper mViewFlipper;
    private ListView mListView;
    private View mEmptyView;
    private View mErrorView;

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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW)
                                      .setData(Uri.parse(mAdapter.getItem(position).threadUrl)));
            }
        });

        mViewFlipper.showNext();
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
        mViewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        mListView = (ListView) mViewFlipper.findViewById(R.id.listViewHomePage);
        mEmptyView = mViewFlipper.findViewById(R.id.listViewHomePageEmptyView);
        mErrorView = mViewFlipper.findViewById(R.id.listViewHomePageErrorView);
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
                        Runnable task;
                        if (null != github) {
                            Log.d(logTag, "Me: " + github.getMyself().getName());
                            GHNotificationStream stream = github.listNotifications();
                            stream.nonBlocking(true);
                            final List<GHThreadPreview> threadList =
                                    new ArrayList<GHThreadPreview>();
                            for (GHThread thread : stream)
                                threadList.add(GHThreadPreview.fromGHThread(thread));
                            Collections.reverse(threadList);
                            task = new Runnable() {
                                @Override
                                public void run() {
                                    adapter.clear();
                                    mAdapter.addAll(threadList);
                                    adapter.notifyDataSetChanged();
                                    while (mViewFlipper.getCurrentView() != mListView)
                                        mViewFlipper.showPrevious();
                                }
                            };
                        } else {
                            PreferenceManager.getDefaultSharedPreferences(context)
                                             .edit().putString("accessToken", "invalid").commit();
                            task = new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(context, LoginActivity.class));
                                    while (mViewFlipper.getCurrentView() != mErrorView)
                                        mViewFlipper.showNext();
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
