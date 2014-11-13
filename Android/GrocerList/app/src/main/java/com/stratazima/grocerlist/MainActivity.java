package com.stratazima.grocerlist;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.stratazima.grocerlist.processes.AlarmMangerPoller;
import com.stratazima.grocerlist.processes.MainListAdapter;
import com.stratazima.grocerlist.processes.NavigationDrawerFragment;
import com.stratazima.grocerlist.processes.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isOnline()) onRefreshData();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        mTitle = getTitle();
        onSetPoller();
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
                DialogFragment dialogFragment = OmniDialogFragment.newInstance(false, null, -1);
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
            DialogFragment dialogFragment = OmniDialogFragment.newInstance(false, null, 0);
            dialogFragment.show(getFragmentManager(), "add");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    public void onSetPoller() {
        Intent intent = new Intent(this, AlarmMangerPoller.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        long scTime = 30000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + scTime, scTime, pendingIntent);

        registerReceiver(broadcastReceiver, new IntentFilter("Connect"));
    }

    public void onRefreshData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Groceries");
        if (!isOnline()) query.fromLocalDatastore();

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
        private ParseObject mParseObject;
        private int mPosition;

        @Override
        public void setRetainInstance(boolean retain) {
            super.setRetainInstance(retain);
        }

        public static OmniDialogFragment newInstance(boolean edit, ParseObject parseObject, int position) {
            OmniDialogFragment newFragment = new OmniDialogFragment();
            newFragment.mEdit = edit;
            newFragment.mParseObject = parseObject;
            newFragment.mPosition = position;

            return newFragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (mPosition == -1) {
                builder.setMessage("Are you sure?")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ParseUser.logOut();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("Cancel", null);
                return builder.create();
            }

            View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add, null);
            final DaListFragment fragment = (DaListFragment) getFragmentManager().findFragmentByTag("listFrag");

            grocery = (EditText) rootView.findViewById(R.id.grocery_name);
            number = (EditText) rootView.findViewById(R.id.grocery_number);
            String isAdd = "Add";
            builder.setView(rootView).setNegativeButton("Cancel", null);

            if (mEdit) {
                grocery.setText(mParseObject.getString("grocery"));
                number.setText(String.valueOf(mParseObject.getInt("number")));
                isAdd = "Update";
            }

            builder.setPositiveButton(isAdd, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Integer x;
                    if (grocery.getText().toString().equals("") || number.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Input Missing", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        try {
                            x = Integer.parseInt(number.getText().toString());
                        } catch (NumberFormatException e) {
                            Toast.makeText(getActivity(), "Invalid Number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (mEdit) {
                        mParseObject.put("grocery", grocery.getText().toString());
                        mParseObject.put("number", x);

                        fragment.mAdapter.replaceObject(mParseObject, mPosition);
                    } else {
                        ParseObject groceries = new ParseObject("Groceries");
                        groceries.put("grocery", grocery.getText().toString());
                        groceries.put("number", x);
                        groceries.setACL(new ParseACL(ParseUser.getCurrentUser()));

                        fragment.mAdapter.addObject(groceries);
                    }

                    fragment.mAdapter.notifyDataSetChanged();
                }
            });

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
                    DialogFragment dialogFragment = OmniDialogFragment.newInstance(true, myList.get(position), position);
                    dialogFragment.show(getFragmentManager(), "edit");
                }
            });
        }
    }
}