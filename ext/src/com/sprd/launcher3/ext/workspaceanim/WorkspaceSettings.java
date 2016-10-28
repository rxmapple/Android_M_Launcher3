/** Created by Spreadst */
package com.sprd.launcher3.ext.workspaceanim;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.launcher3.R;
import com.sprd.launcher3.ext.workspaceanim.effect.EffectInfo;
import com.sprd.launcher3.ext.workspaceanim.effect.NormalEffect;

public class WorkspaceSettings extends DialogFragment {
    private String TAG = "WSPA WorkspaceSettings";

    private ViewGroup mAnimDraw;
    private ListView mListView;
    private String[] animEffects;
    private SharedPreferences mSharedPref;

    /*Animation Preview Parameter*/
    protected float mDensity;
    private final float OFFSET = 0.0f;
    private final float SCALE_OFFSET = 1.0f;
    private final float INVISIBLE = 0.0f;
    private final float VISIBLE = 1.0f;

    /*Animation frame counter*/
    private int mFrameCount = 0;
    /*Preview Animation max frame*/
    private final float PREVIEW_MAXFRAME = 20.00f;
    /*Time between frame and frame*/
    private final long PREVIEW_ANIM_DELAY = 50l;

    private final int MSG_START_ANIM = 0;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case MSG_START_ANIM:
                    startPreviewAnimation();
                    return true;
                default:
                    break;
            }
            return false;
        }
    });
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDensity = getResources().getDisplayMetrics().density;
        animEffects = getResources().getStringArray(
                R.array.workspace_style_summaries);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(initDialogContentView())
                .setTitle(R.string.effect_button_text)
                // Add action buttons
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                mHandler.removeMessages(MSG_START_ANIM);
                                getDialog().dismiss();
                            }
                        }).setCancelable(false);
        return builder.create();
    }

    private View initDialogContentView(){
        int oldEffect;
        // Inflate and set the layout for the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.workspace_settings, null);
        mAnimDraw = (ViewGroup) view.findViewById(R.id.view_group);
        mListView = (ListView) view.findViewById(R.id.effect_lv);
        mListView.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_single_choice, animEffects));

        //listview init
        mSharedPref = FuncUtils.getmAnimSharePref(getContext());
        oldEffect = mSharedPref.getInt(FuncUtils.KEY_ANIMATION_STYLE, FuncUtils.ANIMATION_DEFAULT);
        mListView.setItemChecked(oldEffect,true);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    SharedPreferences.Editor mSharedEditor = mSharedPref.edit();
                    mSharedEditor.putInt(FuncUtils.KEY_ANIMATION_STYLE, position).commit();
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist animation setting", e);
                }
                mFrameCount = 0;
                mHandler.removeMessages(MSG_START_ANIM);
                mHandler.sendEmptyMessage(MSG_START_ANIM);
            }
        });
        return view;
    }

    public void startPreviewAnimation() {
        EffectInfo curentAnimInfo = FuncUtils.getCurentAnimInfo(getContext());

        if(mFrameCount > PREVIEW_MAXFRAME){
            mFrameCount = 0;
            return;
        }

        for (int i = 0; i < mAnimDraw.getChildCount(); i++) {
            boolean firstPage = (i == 0);
            View v = mAnimDraw.getChildAt(i);
            if (v != null) {
                float offset = (firstPage ? mFrameCount/PREVIEW_MAXFRAME : (mFrameCount/PREVIEW_MAXFRAME - 1));

                v.setCameraDistance(mDensity * FuncUtils.CAMERA_DISTANCE);
                int pageWidth = FuncUtils.getScaledMeasuredWidth(v);
                int pageHeight = v.getMeasuredHeight();

                if(mFrameCount < PREVIEW_MAXFRAME) {
                    v.setAlpha(VISIBLE);
                    if(curentAnimInfo == null) {
                        new NormalEffect(0).getTransformationMatrix(v, offset, pageWidth, pageHeight, mDensity * FuncUtils.CAMERA_DISTANCE, true, true);
                    }else{
                        curentAnimInfo.getTransformationMatrix(v, offset, pageWidth, pageHeight, mDensity * FuncUtils.CAMERA_DISTANCE, true, true);

                    }
                }else{//view reset
                    v.setAlpha((firstPage ? INVISIBLE:VISIBLE));
                    v.setTranslationX(OFFSET);
                    v.setPivotY(pageHeight / 2.0F);
                    v.setPivotX(pageWidth / 2.0F);
                    v.setScaleX(SCALE_OFFSET);
                    v.setScaleY(SCALE_OFFSET);
                    v.setRotationY(OFFSET);
                    v.setRotationX(OFFSET);
                    v.setRotation(OFFSET);
                }

            }
        }
        mFrameCount++;
        mHandler.sendEmptyMessageDelayed(MSG_START_ANIM,PREVIEW_ANIM_DELAY);
    }


}


