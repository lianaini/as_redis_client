package com.jeme.jedis.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;


import com.jeme.jedis.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jeme
 * @date 2019/9/21
 */
public abstract class AbstractBindingAdapter<T extends ViewDataBinding,D> extends RecyclerView.Adapter<BindingViewHolder<T>> {

    protected Context mContext;
    protected List<D> mData;
    protected LayoutInflater mLayoutInflater;
    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    public AbstractBindingAdapter(Context context){
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }
    public AbstractBindingAdapter(Context context, List<D> datas){
        this(context);
        mData = datas;
    }
    public void addData(D data) {
        if(mData == null){
            mData = new ArrayList<>();
        }
        mData.add(data);
        notifyItemInserted(mData.size()-1);
    }
    public void addData(int index,D data) {
        if(mData == null){
            mData = new ArrayList<>();
        }
        mData.add(index,data);
        notifyItemInserted(index);
    }
    public void setData(List<D> data){
        setData(data,true);
    }
    public void setData(List<D> data,boolean isRefresh){
        if(isRefresh){
            mData = data;
        }else {
            if(mData == null){
                mData = new ArrayList<>();
            }
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public List<D> getData() {
        return mData;
    }


    protected abstract int getLayoutId(int viewType);
    protected abstract void onBindingView(@NonNull T binding, D item,int position);

    @NonNull
    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingViewHolder(mLayoutInflater.inflate(getLayoutId(viewType), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        if(itemClickListener != null || longItemClickListener != null) {
            holder.binding.getRoot().setTag(R.id.item_position, position);
            holder.binding.getRoot().setTag(R.id.item_value, mData.get(position));
        }
        if(itemClickListener != null) {
            if (!holder.binding.getRoot().hasOnClickListeners()) {
                holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(v, ((int) v.getTag(R.id.item_position)), v.getTag(R.id.item_value));
                        }
                    }
                });
            }
        }
        if(longItemClickListener != null) {
            holder.binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longItemClickListener != null) {
                        longItemClickListener.onLongItemClick(v, ((int) v.getTag(R.id.item_position)), v.getTag(R.id.item_value));
                    }
                    return true;
                }
            });
        }
        onBindingView((T) holder.binding,mData.get(position),position);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public void setLongItemClickListener(OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }


    public interface OnItemClickListener<D> {
        void onItemClick(View view, int position, D item);
    }
    public interface OnLongItemClickListener<D> {
        void onLongItemClick(View view, int position, D item);
    }
}
