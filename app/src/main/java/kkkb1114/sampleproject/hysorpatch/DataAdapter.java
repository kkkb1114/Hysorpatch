package kkkb1114.sampleproject.hysorpatch;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kkkb1114.sampleproject.hysorpatch.R;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    ArrayList<String> ad;
    ArrayList<String> af;
    Context context;
    HashMap<Integer,String> map = new HashMap<>();

    static int selectedPosition = 0;

    public static int getSelected()
    {
        return selectedPosition;
    }
    private List<ViewHolder> mViewHolderList = new ArrayList<>();


    public DataAdapter(ArrayList<String> asd,ArrayList<String> af,Context context){
        this.ad = asd;
        this.af=af;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_data, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mViewHolderList.add(holder);


        holder.data_name.setText(ad.get(position));
        holder.data_source.setText(af.get(position));
    }


    @Override
    public int getItemCount() {

        return ad.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout data_layout;
        TextView data_name;
        TextView data_source;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);

        }

        public void initView(View itemView){
            data_layout = itemView.findViewById(R.id.data_layout);
            data_name = itemView.findViewById(R.id.data_name);
            data_source = itemView.findViewById(R.id.data_source);
        }





    }


}
