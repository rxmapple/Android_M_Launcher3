package com.sprd.launcher3.ext;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPREADTRUM\jin.xie on 10/20/16.
 */

public class AppListPreference extends ListPreference {

    public static final String ITEM_NONE_VALUE = "";
    private Drawable[] mEntryDrawables;
    private boolean mShowItemNone = false;
    private OnPreferenceCheckBoxClickListener mCheckBoxListener;

    private CheckBox mCheckBox;
    public static final String CHECKBOX_KEY = "_checked";

    public interface OnPreferenceCheckBoxClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param preference The view that was clicked.
         */
        void onPreferenceCheckboxClick(Preference preference);
    }



    public class AppArrayAdapter extends ArrayAdapter<CharSequence> {
        private Drawable[] mImageDrawables = null;
        private int mSelectedIndex = 0;
        public AppArrayAdapter(Context context, int textViewResourceId,
                               CharSequence[] objects, Drawable[] imageDrawables, int selectedIndex) {
            super(context, textViewResourceId, objects);
            mSelectedIndex = selectedIndex;
            mImageDrawables = imageDrawables;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            ViewHolders viewHolder;

            convertView = inflater.inflate(R.layout.app_preference_item, null);
            viewHolder = new ViewHolders();
            viewHolder.m_iv = (ImageView) convertView.findViewById(R.id.app_image);
            viewHolder.m_tv = (TextView) convertView.findViewById(R.id.app_label);
            viewHolder.m_tv_select = (TextView) convertView.findViewById(R.id.select_label);

            convertView.setTag(viewHolder);

            viewHolder.m_tv.setText(getItem(position));
            if (position == mSelectedIndex) {
                viewHolder.m_tv_select.setVisibility(View.VISIBLE);
            }
            viewHolder.m_iv.setImageDrawable(mImageDrawables[position]);
            return convertView;
        }
    }

    class ViewHolders {
        public ImageView m_iv;
        public TextView m_tv;
        public TextView m_tv_select;
    }


    public AppListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.listpref_widget_checkbox);
    }


    public void setShowItemNone(boolean showItemNone) {
        mShowItemNone = showItemNone;
    }

    public void setListValues(CharSequence[] listValues, CharSequence defaultPackageName) {
        // Look up all package names in PackageManager. Skip ones we can't find.
        PackageManager pm = getContext().getPackageManager();
        final int entryCount = listValues.length + (mShowItemNone ? 1 : 0);
        List<CharSequence> applicationNames = new ArrayList<>(entryCount);
        List<CharSequence> validatedNames = new ArrayList<>(entryCount);
        List<Drawable> entryDrawables = new ArrayList<>(entryCount);
        int selectedIndex = -1;
        for (int i = 0; i < listValues.length; i++) {
            try {
                String listValue = listValues[i].toString();
                String packageName = listValue.substring(0, listValue.indexOf("/"));
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                applicationNames.add(appInfo.loadLabel(pm));
                validatedNames.add(listValues[i]);
                entryDrawables.add(appInfo.loadIcon(pm));
                if (defaultPackageName != null &&
                        appInfo.packageName.contentEquals(defaultPackageName)) {
                    selectedIndex = i;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Skip unknown packages.
            }
        }
        if (mShowItemNone) {
            applicationNames.add(getContext().getResources().getText(R.string.app_list_preference_none));
            validatedNames.add(ITEM_NONE_VALUE);
            entryDrawables.add(getContext().getDrawable(R.drawable.ic_remove_circle));
        }
        setEntries(applicationNames.toArray(new CharSequence[applicationNames.size()]));
        setEntryValues(
                validatedNames.toArray(new CharSequence[validatedNames.size()]));
        mEntryDrawables = entryDrawables.toArray(new Drawable[entryDrawables.size()]);

        if(entryCount == 1) {
            setValueIndex(0);
            saveInitListValue();
            /*
            persistString(getEntryValues()[0].toString());
            notifyChanged();
            */
        } else {
            if (selectedIndex != -1) {
                setValueIndex(selectedIndex);
            } else {
                setValue(null);
            }
        }
    }

    private void saveInitListValue() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(getKey(), getEntryValues()[0].toString()).commit();
    }

    protected ListAdapter createListAdapter() {
        final String selectedValue = getValue();
        final boolean selectedNone = selectedValue == null ||
                (mShowItemNone && selectedValue.contentEquals(ITEM_NONE_VALUE));
        int selectedIndex = selectedNone ? -1 : findIndexOfValue(selectedValue);

        return new AppArrayAdapter(getContext(),
                R.layout.app_preference_item, getEntries(), mEntryDrawables, selectedIndex);
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
        setPreferenceChecked(isPreferenceChecked());

        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppListPreference.this.setPreferenceChecked(mCheckBox.isChecked());
                if(mCheckBoxListener != null){
                    mCheckBoxListener.onPreferenceCheckboxClick((Preference)AppListPreference.this);
                }

            }
        });
    }

    public void setOnPreferenceCheckBoxClickListener(OnPreferenceCheckBoxClickListener l){
        mCheckBoxListener = l;
    }


    public void setPreferenceChecked(boolean bool){
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharePref.edit();
        editor.putBoolean(getKey()+CHECKBOX_KEY, bool).commit();
        if(mCheckBox != null && mCheckBox.isChecked() != bool){
            mCheckBox.setChecked(bool);
        }
    }

    public boolean isPreferenceChecked(){
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getKey()+CHECKBOX_KEY, false);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setAdapter(createListAdapter(), this);
        super.onPrepareDialogBuilder(builder);
    }

}
