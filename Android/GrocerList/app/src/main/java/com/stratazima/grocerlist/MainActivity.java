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
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

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
                DialogFragment dialogFragment = LogoutDialogFragment.newInstance();
                dialogFragment.show(fragmentManager, "confirm");
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
            DialogFragment dialogFragment = AddDialogFragment.newInstance();
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

    public static class AddDialogFragment extends DialogFragment {
        private EditText grocery;
        private EditText number;

        public static AddDialogFragment newInstance() {
            return new AddDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add, null);
            grocery = (EditText) rootView.findViewById(R.id.grocery_name);
            number = (EditText) rootView.findViewById(R.id.grocery_number);

            builder.setView(rootView)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ParseObject groceries = new ParseObject("Groceries");
                            groceries.put("grocery", grocery.getText().toString());
                            groceries.put("number", Integer.parseInt(number.getText().toString()));
                            groceries.setACL(new ParseACL(ParseUser.getCurrentUser()));
                            groceries.saveInBackground();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AddDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class LogoutDialogFragment extends DialogFragment {
        public static LogoutDialogFragment newInstance() {
            return new LogoutDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                            LogoutDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class DeleteDialogFragment extends DialogFragment {
        static int mPosition;

        public static DeleteDialogFragment newInstance(int position) {
            mPosition = position;
            return new DeleteDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete?")
                    .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Groceries");
                            query.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> parseObjects, ParseException e) {
                                    if (e == null) {
                                        ParseObject toDelete = parseObjects.get(mPosition);
                                        toDelete.deleteInBackground();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DeleteDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class DaListFragment extends Fragment {
        private ListView mainList;

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
            ArrayList<ParseObject> myList = new ArrayList<ParseObject>();
            myList.addAll(objects);
            String[] length = new String[myList.size()];

            MainListAdapter listAdapter = new MainListAdapter(getActivity(), length, myList);

            mainList.setAdapter(listAdapter);
            mainList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    DialogFragment dialogFragment = DeleteDialogFragment.newInstance(position);
                    dialogFragment.show(getFragmentManager(), "confirm");
                    return false;
                }
            });
        }
    }
}