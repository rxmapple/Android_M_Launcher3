/** Created by Spreadtrum */
package com.sprd.launcher3.ext.workspaceanim.effect;

import android.content.Context;
import android.content.SharedPreferences;

import com.sprd.launcher3.ext.workspaceanim.FuncUtils;

import java.util.ArrayList;
import java.util.List;

public class EffectFactory {

    private static List<EffectInfo> allEffects = new ArrayList<EffectInfo>();
    public static List<EffectInfo> getAllEffects(){
        return loadEffectsList();
    }
    public static EffectInfo getEffect(int id){
        if(id==0){
            return null; // if is is 0 , we return null , and mean we do not need animation for workspace
        }
        if(allEffects.isEmpty()){
            loadEffectsList();
        }
        for (int i = 0,count = allEffects.size(); i < count; i++) {
            EffectInfo eInfo = allEffects.get(i);
            if(eInfo.id == id){
                return eInfo;
            }
        }
        return null;
    }
    public static EffectInfo getCurrentEffect(Context context ){
        SharedPreferences mSpaceTypeShared = FuncUtils.getmAnimSharePref(context);
        int id = mSpaceTypeShared.getInt(FuncUtils.KEY_ANIMATION_STYLE, 0);

        for (int i = 0,count = allEffects.size(); i < count; i++) {
            EffectInfo eInfo = allEffects.get(i);
            if(eInfo.id == id){
                return eInfo;
            }
        }
        return null;
    }
    private static List<EffectInfo> loadEffectsList(){
        allEffects.clear();
        CrossEffect crossEffect = new CrossEffect(1);
        allEffects.add(crossEffect);

        PageEffect pageEffect = new PageEffect(2);
        allEffects.add(pageEffect);

        CubeEffect cubeInEffect = new CubeEffect(3, true);
        allEffects.add(cubeInEffect);

        CubeEffect cubeOutEffect = new CubeEffect(4, false);
        allEffects.add(cubeOutEffect);

        CarouselEffect carouselLeftEffect = new CarouselEffect(5, true);
        allEffects.add(carouselLeftEffect);

        CarouselEffect carouselRightEffect = new CarouselEffect(6, false);
        allEffects.add(carouselRightEffect);

//        CuboidEffect cuboidEffect = new CuboidEffect(7);
//        allEffects.add(cuboidEffect);

        RotateEffect rotateEffect = new RotateEffect(7);
        allEffects.add(rotateEffect);

        LayerEffect layerEffect = new LayerEffect(8);
        allEffects.add(layerEffect);

        FadeEffect fadeEffect = new FadeEffect(9);
        allEffects.add(fadeEffect);

        return allEffects;
    }
}
