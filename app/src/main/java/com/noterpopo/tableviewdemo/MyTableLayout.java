package com.noterpopo.tableviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class MyTableLayout extends ViewGroup {

    private TreeMap<Integer, Integer> rowMap = new TreeMap<>();
    private TreeMap<Integer, Integer> columnMap = new TreeMap<>();
    private boolean needMeasure = true;
    private int skipRow = 0;
    private int skipColumn = 0;
    private int skipRowIndex = -1;
    private int skipColumnIndex = -1;

    public MyTableLayout(Context context) {
        super(context);
    }

    public MyTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;


        if (needMeasure) {
            rowMap.clear();
            columnMap.clear();
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {

            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            LayoutParams childLayoutParam = (LayoutParams) child.getLayoutParams();
            int childRowIndex = childLayoutParam.getRowIndex();
            int childColumnIndex = childLayoutParam.getColumnIndex();

            if (null != rowMap && rowMap.containsKey(childRowIndex)) {
                if (needMeasure && child.getMeasuredHeight() > rowMap.get(childRowIndex)) {
                    rowMap.put(childRowIndex, child.getMeasuredHeight());
                }
            } else {
                if (needMeasure) {
                    rowMap.put(childRowIndex, child.getMeasuredHeight());
                }
            }

            if (null != columnMap && columnMap.containsKey(childColumnIndex)) {
                if (needMeasure && child.getMeasuredWidth() > columnMap.get(childColumnIndex)) {
                    columnMap.put(childColumnIndex, child.getMeasuredWidth());
                }
            } else {
                if (needMeasure) {
                    columnMap.put(childColumnIndex, child.getMeasuredWidth());
                }
            }

        }


        ArrayList<Integer> rowKeyArray = new ArrayList<>(rowMap.keySet());
        ArrayList<Integer> rowValueArray = new ArrayList<>(rowMap.values());

        ArrayList<Integer> columnKeyArray = new ArrayList<>(columnMap.keySet());
        ArrayList<Integer> columnValueArray = new ArrayList<>(columnMap.values());

        for (int j = 0; j < childCount; ++j) {
            View view = getChildAt(j);
            LayoutParams p = (LayoutParams) view.getLayoutParams();
            p.width = columnMap.get(p.getColumnIndex());
            p.height = rowMap.get(p.getRowIndex());
            int rowSpan = p.getRowSpan();
            int columnSpan = p.getColumnSpan();

            if (rowSpan > 1) {
                skipColumnIndex = p.getColumnIndex();
                int index = getIndexInArray(p.getRowIndex(), rowKeyArray);
                while (needMeasure && rowSpan-- > 1 && ++index < rowKeyArray.size()) {
                    p.height += rowValueArray.get(index);
                }
            }

            if (columnSpan > 1) {
                skipRowIndex = p.getRowIndex();
                int index = getIndexInArray(p.getColumnIndex(), columnKeyArray);
                while (needMeasure && columnSpan-- > 1 && ++index < columnKeyArray.size()) {
                    p.width += columnValueArray.get(index);
                }
            }

            while (needMeasure && skipRow > 0 && skipColumnIndex == p.getColumnIndex()) {
                skipRow--;
                p.height = 0;
            }

            while (needMeasure && skipColumn > 0 && skipRowIndex == p.getRowIndex()) {
                skipColumn--;
                p.width = 0;
            }

            skipRow = p.getRowSpan() - 1;
            skipColumn = p.getColumnSpan() - 1;

            view.setLayoutParams(p);
        }

        for (int row : rowMap.values()) {
            height += row;
        }

        for (int column : columnMap.values()) {
            width += column;
        }

        if (needMeasure) {
            needMeasure = false;
        }
        setMeasuredDimension(width, height);

    }

    private int getIndexInArray(int v, ArrayList<Integer> array) {
        int res = -1;
        for (int i = 0; i < array.size(); ++i) {
            if (v == array.get(i)) {
                res = i;
                break;
            }
        }
        return res;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cCount = getChildCount();
        int cWidth = 0;
        int cHeight = 0;
        LayoutParams cParams = null;


        for (int i = 0; i < cCount; i++) {
            View childView = getChildAt(i);
            cWidth = childView.getMeasuredWidth();
            cHeight = childView.getMeasuredHeight();
            cParams = (LayoutParams) childView.getLayoutParams();


            int cl = 0, ct = 0, cr = 0, cb = 0;

            for (int row : rowMap.keySet()) {
                if (cParams.rowIndex > row) {
                    ct += rowMap.get(row);
                }
            }


            for (int column : columnMap.keySet()) {
                if (cParams.columnIndex > column) {
                    cl += columnMap.get(column);
                }
            }

            cr = cl + cWidth;
            cb = cHeight + ct;
            childView.layout(cl, ct, cr, cb);
        }
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        Map.Entry<Integer,Integer> firstRow = rowMap.firstEntry();
        Map.Entry<Integer,Integer> firstColumn = columnMap.firstEntry();

        int cWidth = 0;
        int cHeight = 0;
        LayoutParams cParams = null;

        int childCount = getChildCount();
        for (int j = 0; j < childCount; ++j) {
            View childView = getChildAt(j);
            cWidth = childView.getMeasuredWidth();
            cHeight = childView.getMeasuredHeight();
            cParams = (LayoutParams) childView.getLayoutParams();

            if(cWidth ==0 || cHeight == 0){
                continue;
            }

            if(firstRow!=null && firstRow.getKey()==cParams.getRowIndex()){
                canvas.drawRect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getTop() + 2, paint);
                canvas.drawRect(childView.getLeft(), childView.getBottom()-2, childView.getRight(), childView.getBottom() , paint);
            }else {
                canvas.drawRect(childView.getLeft(), childView.getBottom()-2, childView.getRight(), childView.getBottom(), paint);
            }

            if(firstColumn!=null && firstColumn.getKey()==cParams.getColumnIndex()){
                canvas.drawRect(childView.getLeft(), childView.getTop(), childView.getLeft()+2, childView.getBottom(), paint);
                canvas.drawRect(childView.getRight()-2, childView.getTop(), childView.getRight(), childView.getBottom() , paint);
            }else {
                canvas.drawRect(childView.getRight()-2, childView.getTop(), childView.getRight(), childView.getBottom() , paint);
            }
        }

    }

    public class LayoutParams extends ViewGroup.LayoutParams {

        private int rowIndex;
        private int columnIndex;

        private int rowSpan;
        private int columnSpan;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.MyTableLayout);
            rowIndex = a.getInt(R.styleable.MyTableLayout_layout_row_index, 0);
            columnIndex = a.getInt(R.styleable.MyTableLayout_layout_column_index, 0);
            rowSpan = a.getInt(R.styleable.MyTableLayout_layout_row_span, 1);
            columnSpan = a.getInt(R.styleable.MyTableLayout_layout_column_span, 1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            rowIndex = source.getRowIndex();
            columnIndex = source.getColumnIndex();
            rowSpan = source.getRowSpan();
            columnSpan = source.getColumnSpan();
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        public int getRowSpan() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;
        }

        public int getColumnSpan() {
            return columnSpan;
        }

        public void setColumnSpan(int columnSpan) {
            this.columnSpan = columnSpan;
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
