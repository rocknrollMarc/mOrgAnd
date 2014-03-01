package com.hdweiss.morgand.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.orgdata.OrgNodeRepository;

/**
 * A fragment representing a single Node detail screen.
 * This fragment is either contained in a {@link NodeListActivity}
 * in two-pane mode (on tablets) or a {@link NodeDetailActivity}
 * on handsets.
 */
public class NodeDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_node_detail, container, false);

        // Show the dummy content as text in a TextView.
        //if (mItem != null) {
            //((TextView) rootView.findViewById(R.id.node_detail)).setText(mItem.content);

            String allNodes = new OrgNodeRepository(getActivity()).allToString();
            ((TextView) rootView.findViewById(R.id.node_detail)).setText(allNodes);
        //}

        return rootView;
    }
}