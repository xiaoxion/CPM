package com.stratazima.grocerlist;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.stratazima.grocerlist.processes.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        mTitle = getTitle();
        onRefreshData();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction().replace(R.id.container, DaListFragment.newInstance(), "listFrag").commit();
                break;
            case 1:
                DialogFragment dialogFragment = OmniDialogFragment.newInstance(false, true, null, 0);
                dialogFragment.show(fragmentManager, "logout");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_example) {
            DialogFragment dialogFragment = OmniDialogFragment.newInstance(false, false, null, 0);
            dialogFragment.show(getFragmentManager(), "add");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    public void onRefreshData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Groceries");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    DaListFragment fragment = (DaListFragment) getFragmentManager().findFragmentByTag("listFrag");
                    fragment.onList(parseObjects);
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
             }
        });
    }

    public static class OmniDialogFragment extends DialogFragment {
        private EditText grocery;
        private EditText number;
        private boolean mEdit;
        private boolean mLogout;
        private ParseObject mParseObject;
        private int mPosition;

        @Override
        public void setRetainInstance(boolean retain) {
            super.setRetainInstance(retain);
        }

        public static OmniDialogFragment newInstance(boolean edit, boolean logout, ParseObject parseObject, int position) {
            OmniDialogFragment newFragment = new OmniDialogFragment();
            newFragment.mEdit = edit;
            newFragment.mLogout = logout;
            newFragment.mParseObject = parseObject;
            newFragment.mPosition = position;

            return newFragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (mLogout) {
                builder.setMessage("Are you sure?")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ParseUser.logOut();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                OmniDialogFragment.this.getDialog().cancel();
                            }
                        });
                return builder.create();
            }

            View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add, null);
            grocery = (EditText) rootView.findViewById(R.id.grocery_name);
            number = (EditText) rootView.findViewById(R.id.grocery_number);

            if (mEdit) {
                grocery.setText(mParseObject.getString("grocery"));
                number.setText(String.valueOf(mParseObject.getInt("number")));
                builder.setView(rootView)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                mParseObject.put("grocery", grocery.getText().toString());
                                mParseObject.put("number", Integer.parseInt(number.getText().toString()));
                                mParseObject.saveInBackground();

                                DaListFragment fragment = (DaListFragment) getFragmentManager().findFragmentByTag("listFrag");
                                fragment.mAdapter.replaceObject(mParseObject, mPosition);
                                fragment.mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                OmniDialogFragment.this.getDialog().cancel();
                            }
                        });
            } else {
                builder.setView(rootView)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ParseObject groceries = new ParseObject("Groceries");
                                groceries.put("grocery", grocery.getText().toString());
                                groceries.put("number", Integer.parseInt(number.getText().toString()));
                                groceries.setACL(new ParseACL(ParseUser.getCurrentUser()));
                                groceries.saveInBackground();

                                DaListFragment fragment = (DaListFragment) getFragmentManager().findFragmentByTag("listFrag");
                                fragment.mAdapter.addObject(groceries);
                                fragment.mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                OmniDialogFragment.this.getDialog().cancel();
                            }
                        });
            }


            return builder.create();
        }
    }

    public static class DaListFragment extends Fragment {
        private ListView mainList;
        public MainListAdapter mAdapter;

        public static DaListFragment newInstance() {
            return new DaListFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_list, container, false);
            mainList = (ListView) rootView.findViewById(R.id.main_listView);
            return rootView;
        }

        public void onList(List<ParseObject> objects) {
            final ArrayList<ParseObject> myList = new ArrayList<ParseObject>();
            myList.addAll(objects);

            mAdapter = new MainListAdapter(getActivity(), myList);

            SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(mainList, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(int position) {
                    return true;
                }

                @Override
                public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                    for (int position : reverseSortedPositions) {
                        mAdapter.removeObject(position);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });

            mainList.setAdapter(mAdapter);
            mainList.setOnTouchListener(touchListener);
            mainList.setOnScrollListener(touchListener.makeScrollListener());
            mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    DialogFragment dialogFragment = OmniDialogFragment.newInstance(true, false, myList.get(position), position);
                    dialogFragment.show(getFragmentManager(), "edit");
                }
            });
        }
    }
}