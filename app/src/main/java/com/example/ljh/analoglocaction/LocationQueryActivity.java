package com.example.ljh.analoglocaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ljh on 2017/12/27.
 */

public class LocationQueryActivity extends AppCompatActivity{
    private SearchView mSearchView;
    private ListView mListView;
    private ImageView ivBack;

    public static final String AK = "GveluuiZ3O1Es75DM6VxwYGxZ7NM4i9d";
    private String mLocationQueryUrl;
    private String mCity;
    private List<LocationQueryBean> datalist;
    private MyAdapter mMyAdapter;

    private final int CONNECT_ERROR = 0;    //网络错误
    private final int UPDATE_ADAPTER = 1;   //更新列表

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_query);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        mCity = intent.getStringExtra("city");
        datalist = new ArrayList<>();
        initView();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_ERROR:
                    Log.i("aaa", "网络错误");
                    break;
                case UPDATE_ADAPTER:
                    mMyAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * 匹配搜索位置数据
     */
    private void GetLocationFromSearchView(){
        ThreadPoolManager.getThreadPoolManager().execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(3000, TimeUnit.SECONDS)
                        .readTimeout(3000,TimeUnit.SECONDS)
                        .writeTimeout(3000,TimeUnit.SECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url(mLocationQueryUrl)
                        .get()
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handler.sendEmptyMessage(CONNECT_ERROR);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String result = response.body().string();
                            Log.i("bbb","response = " + result);
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject jsonObject1 = null;
                            if(jsonObject.getString("message").equals("ok")){
                                datalist.clear();
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
                                for(int i=0;i<jsonArray.length();i++){
                                    jsonObject = jsonArray.getJSONObject(i);
                                    jsonObject1 = jsonObject.getJSONObject("location");
                                    String name = jsonObject.getString("name");
                                    String address = jsonObject.getString("address");
                                    String latitude = jsonObject1.getString("lat");
                                    String longitude = jsonObject1.getString("lng");
                                    datalist.add(new LocationQueryBean(name,address,latitude,longitude));
                                }
                                    handler.sendEmptyMessage(UPDATE_ADAPTER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * 把定位坐标传回MainActivity
     */
    public void SendLocationToMain(String latitude,String longitude){
        Intent intent = new Intent();
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        setResult(MainActivity.LOCATION_QUERY_MODE,intent);
        finish();
    }

    private void initView(){
        mListView = findViewById(R.id.lvLocation);
        mMyAdapter = new MyAdapter();
        mListView.setAdapter(mMyAdapter);

        mSearchView = findViewById(R.id.SearchView);
        mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(datalist.size() != 0) {
                    String latitude = datalist.get(0).getLatitude();
                    String longitude = datalist.get(0).getLongitude();
                    SendLocationToMain(latitude,longitude);
                }else{
                    Toast.makeText(LocationQueryActivity.this,"找不到位置",Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && newText != ""){
                    mLocationQueryUrl = "http://api.map.baidu.com/place/v2/search?query=" + newText
                            + "&tag=" + "&region=" + mCity + "&output=json&ak="+AK;
                    Log.i("aaa",mLocationQueryUrl);
                    GetLocationFromSearchView();
                }
                return false;
            }
        });

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        datalist.clear();
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return datalist.size();
        }

        @Override
        public Object getItem(int position) {
            return datalist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            ViewHolder viewHolder;
            if(view == null){
                view = LayoutInflater.from(LocationQueryActivity.this).inflate(R.layout.item_location_query,null);
                viewHolder = new ViewHolder();
                viewHolder.tvName = view.findViewById(R.id.tvName);
                viewHolder.tvAddress = view.findViewById(R.id.tvAddress);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
                viewHolder.tvName.setText(datalist.get(position).getName());
                viewHolder.tvAddress.setText(datalist.get(position).getAddress());

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String latitude = datalist.get(position).getLatitude();
                        String longitude = datalist.get(position).getLongitude();
                        SendLocationToMain(latitude,longitude);
                    }
                });
            return view;
        }

        class ViewHolder{
            TextView tvName;
            TextView tvAddress;
        }
    }
}
