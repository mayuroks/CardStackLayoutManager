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
            mItems.add(new MyModel("Goodbye! Have a nice day.", R.drawable.one));
            mItems.add(new MyModel("Hope to see you soon?", R.drawable.two));
            mItems.add(new MyModel("It was nice seeing you?", R.drawable.three));
            mItems.add(new MyModel("Where have you been?", R.drawable.four));
            mItems.add(new MyModel("What time is it?", R.drawable.five));
            mItems.add(new MyModel("How are you today?", R.drawable.six));
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
            holder.image.setImageResource(model.drawable);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            CircleImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.tv);
                image = (CircleImageView) itemView.findViewById(R.id.image);
            }
        }
    }

    class MyModel {
        String title;
        @DrawableRes
        int drawable;

        public MyModel(String title, int drawable) {
            this.title = title;
            this.drawable = drawable;
        }
    }
}