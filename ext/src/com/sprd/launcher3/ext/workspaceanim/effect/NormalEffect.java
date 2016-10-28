/** Created by Spreadtrum */
package com.sprd.launcher3.ext.workspaceanim.effect;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Scroller;

public class NormalEffect extends EffectInfo {

    private boolean flag;

    public NormalEffect(int id) {
        super(id);
    }

    public NormalEffect(int id, boolean flag) {
        super(id, flag);
        this.flag = flag;

    }

    @Override
    public boolean getCellLayoutChildStaticTransformation(ViewGroup viewGroup, View viewiew,
            Transformation transformation, Camera camera, float offset) {
        return false;
    }

    @Override
    public boolean getWorkspaceChildStaticTransformation(ViewGroup viewGroup, View viewiew,
            Transformation transformation, Camera camera, float offset) {
        Matrix tMatrix = transformation.getMatrix();
        float mViewHalfWidth = viewiew.getMeasuredWidth() / 2.0F;
        float mViewHalfHeight = viewiew.getMeasuredHeight() / 2.0F;
        if (offset == 0.0F || offset >= 1.0F || offset <= -1.0F)
            return false;
        transformation.setAlpha(1.0F - Math.abs(offset));
        // viewiew.setAlpha(1.0F - Math.abs(offset));
        camera.save();
        camera.translate(0.0F, 0.0F, mViewHalfWidth);
        camera.rotateY(-90.0F * offset);
        camera.translate(0.0F, 0.0F, -mViewHalfWidth);
        camera.getMatrix(tMatrix);
        camera.restore();
        tMatrix.preTranslate(-mViewHalfWidth, -mViewHalfHeight);
        float transOffset = (1.0F + 2.0F * offset) * mViewHalfWidth;
        tMatrix.postTranslate(transOffset, mViewHalfHeight);
        transformation.setTransformationType(Transformation.TYPE_BOTH);
        return true;
    }


    @Override
    public void getTransformationMatrix(View view, float offset, int pageWidth, int pageHeight,
            float distance, boolean overScroll, boolean overScrollLeft) {

        if (flag) {
            view.setCameraDistance(distance);
        }

        float xPost = 0f;
        if (overScroll) {
            if (overScrollLeft) {
                xPost = pageWidth * offset* -1f;
            } else {
                xPost = pageWidth * Math.abs(offset) * -1f;
            }
        }
        view.setTranslationX(xPost);
        view.setPivotX(offset < 0 ? 0 : pageWidth);
        view.setPivotY(pageHeight * 0.5f);
        view.invalidate();

    }

    @Override
    public Scroller getScroller(Context context) {
        return new Scroller(context, new DecelerateInterpolator());
    }

    @Override
    public int getSnapTime() {
        return 180;
    }

}
