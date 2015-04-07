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

public class HomePageFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ViewSwitcher mViewSwitcher;
    private ListView mListView;

    private Handler mHandler;

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
        fetchData(getActivity());
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
    //32f4406937f2808ea08302627b16e25915632842
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void findViews(View view) {
        mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);
        mListView = (ListView) view.findViewById(R.id.listViewHomePage);
    }
    //56dd50eb5a9fc8966693333e4b54aae59da58535&scope=notifications%2Crepo%2Cuser&token_type=bearer
    private void fetchData(final Context context) {
        final String accessToken = PreferenceManager.getDefaultSharedPreferences(context)
                                                    .getString("accessToken", "invalid");
        if (!"invalid".equals(accessToken)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String logTag = "FetchData";
                    try {
                        GitHub github =
                                GitHub.connectUsingOAuth("56dd50eb5a9fc8966693333e4b54aae59da58535");
                        if (!github.isCredentialValid()) {
                            Log.e(logTag, "Auth failed " + github);
                            return;
                        }
                        Log.d(logTag, "Me: " + github.getMyself().getName());
                        GHNotificationStream stream = github.listNotifications();
                        stream.nonBlocking(true);
                        for (GHThread thread : stream) {
                            Log.d(logTag, String.format("Repo: %s; Title: %s; type: %s; reason: " +
                                                                "%s;",
                                                        thread.getRepository(),
                                                        thread.getTitle(), thread.getType(),
                                                        thread.getReason()));
                        }
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
