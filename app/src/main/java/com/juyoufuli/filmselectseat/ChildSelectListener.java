package com.juyoufuli.filmselectseat;

import java.util.List;

/**
 * @Author : dongfang
 * @Created Time : 2019-01-08  10:40
 * @Description:
 */
public interface ChildSelectListener {
    /**
     * @param stringList
     * @param position
     * @return
     */
    void onChildSelect(List<String> stringList, int position);

    /**
     * @param object
     * @param position
     */
    void onChildSelect(Object object, int position);

}
