package com.yabinc.listpopupmenu;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.yabinc.logger.Log;

import java.util.ArrayList;

/**
 * Created by yabinc on 7/12/15.
 */
public class PopupListFragment extends ListFragment implements View.OnClickListener {

    public static String TAG = "PopupListFragment";

    public static String[] contents = new String[] {
            "Afghanistan",
            "Albania",
            "Algeria",
            "Andorra",
            "Angola",
            "Antigua and Barbuda",
            "Argentina",
            "Armenia",
            "Aruba",
            "Australia",
            "Austria",
            "Azerbaijan",
            "Bahamas, The",
            "Bahrain",
            "Bangladesh",
            "Barbados",
            "Belarus",
            "Belgium",
            "Belize",
            "Benin",
            "Bhutan",
            "Bolivia",
            "Bosnia and Herzegovina",
            "Botswana",
            "Brazil",
            "Brunei",
            "Bulgaria",
            "Burkina Faso",
            "Burma",
            "Burundi",
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < contents.length; ++i) {
            items.add(contents[i]);
        }
        setListAdapter(new PopupAdapter(items));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListView().getItemAtPosition(position);
        Log.d(TAG, "item " + item + " selected");
    }

    @Override
    public void onClick(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                showPopupMenu(view);
            }
        });
    }

    private void showPopupMenu(View view) {
        final PopupAdapter adapter = (PopupAdapter) getListAdapter();
        final String item = (String) view.getTag();
        PopupMenu popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_remove:
                        Log.d(TAG, "remove " + item + " from menu");
                        adapter.remove(item);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    class PopupAdapter extends ArrayAdapter<String> {
        PopupAdapter(ArrayList<String> items) {
            super(getActivity(), R.layout.list_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            View popupButton = view.findViewById(R.id.button_popup);
            popupButton.setTag(getItem(position));
            popupButton.setOnClickListener(PopupListFragment.this);
            return view;
        }
    }
}
