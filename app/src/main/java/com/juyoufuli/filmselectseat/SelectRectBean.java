package com.juyoufuli.filmselectseat;

import android.graphics.Rect;

/**
 * @Author : dongfang
 * @Created Time : 2019-01-09  17:58
 * @Description:
 */
public class SelectRectBean {
    private Rect rect;
    /**
     * 排
     */
    private int row;
    /**
     * 列
     */
    private int column;
    /**
     * 座位的状态
     */
    private int seatState;

    public int getSeatState() {
        return seatState;
    }

    public void setSeatState(int seatState) {
        this.seatState = seatState;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
