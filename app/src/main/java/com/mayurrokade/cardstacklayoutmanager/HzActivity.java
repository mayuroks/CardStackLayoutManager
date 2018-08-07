package com.mayurrokade.cardstacklayoutmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mayurrokade.library.CardStackLayoutManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HzActivity extends AppCompatActivity {

    CardStackLayoutManager llm;
    RecyclerView rcv;
    HSAdapter2 adapter;
    int scrollToPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hz);

        llm = new CardStackLayoutManager(1.5f, 0.85f, CardStackLayoutManager.HORIZONTAL).
                setChildDecorateHelper(new CardStackLayoutManager.DefaultChildDecorateHelper(getResources().getDimension(R.dimen.item_max_elevation)));
        llm.setChildPeekSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                60, getResources().getDisplayMetrics()));
        llm.setMaxItemLayoutCount(5);

        rcv = (RecyclerView) findViewById(R.id.rcv);
        rcv.setLayoutManager(llm);
        adapter = new HSAdapter2();
        rcv.setAdapter(adapter);
    }

    private class HSAdapter2 extends RecyclerView.Adapter<HSAdapter2.ViewHolder> {
        private ArrayList<MyModel> mItems = new ArrayList<>();
        private Context mContext;

        public HSAdapter2() {
            init();
        }

        private void init() {
            mItems.add(new MyModel("Goodbye! Have a nice day", "6:00 PM", R.drawable.one));
            mItems.add(new MyModel("In the creative zone", "4:00 PM", R.drawable.two));
            mItems.add(new MyModel("Let the work begin", "2:00 PM", R.drawable.three));
            mItems.add(new MyModel("Office! Run for it", "12:00 PM", R.drawable.four));
            mItems.add(new MyModel("Time for your breakfast", "10:00 AM", R.drawable.five));
            mItems.add(new MyModel("How are you today?", "8:00 AM", R.drawable.six));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            mContext = parent.getContext();
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_hz_my_model, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MyModel model = mItems.get(position);
            holder.tv.setText(model.title);
            holder.tvDescription.setText(model.time);
            holder.image.setImageResource(model.drawable);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv, tvDescription;
            CircleImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                image = itemView.findViewById(R.id.image);
            }
        }
    }

    class MyModel {
        String title;
        String time;

        @DrawableRes
        int drawable;

        public MyModel(String title, String time, int drawable) {
            this.title = title;
            this.time = time;
            this.drawable = drawable;
        }
    }
}