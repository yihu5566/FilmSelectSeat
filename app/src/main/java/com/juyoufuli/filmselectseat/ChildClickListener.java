package com.juyoufuli.filmselectseat;

import android.view.View;

/**
 * @Author : dongfang
 * @Created Time : 2019-01-08  10:40
 * @Description:
 */
public interface ChildClickListener {
    /**
     * @param view
     * @param object
     * @param position
     * @return
     */
    void onChildClick(View view, Object object, int position);
}
