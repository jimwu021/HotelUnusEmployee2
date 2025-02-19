package android.com.hotelunusemployee2.delivery;

import android.app.Activity;
import android.com.hotelunusemployee2.R;
import android.com.hotelunusemployee2.main.Util;
import android.com.hotelunusemployee2.task.CommonTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class OngoingFragment extends Fragment {
    public static final String TAG = "OngoingFragment";
    private Activity activity;
    private RecyclerView rvOngoing;
    private List<RoomMealOrderMasterVO> orderList;
    private CommonTask getOrderTask;
    private RMOMAdapter mAdapter;

    public OngoingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();
        return inflater.inflate(R.layout.fragment_delivery_ongoing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvOngoing = view.findViewById(R.id.rvOngoing);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        rvOngoing.setLayoutManager(linearLayoutManager);

//        //加入每個item中間的分隔線
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvOngoing.getContext(),
//                linearLayoutManager.getOrientation());
//        rvOngoing.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.networkConnected(activity)) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getOngoingOrder");  //取得出餐中(1)的訂單
            String jsonOut = jsonObject.toString();

            updateUI(jsonOut);
        } else {
            Util.showToast(activity, R.string.msg_NoNetwork);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(getOrderTask != null){
            getOrderTask.cancel(true);
        }
    }

    private void updateUI(String jsonOut) {
        getOrderTask = new CommonTask(Util.URL + "RoomMealOrderServletAndroid", jsonOut);

        try {
            // 遇到日期格式資料，在創建gson物件同時也指定日期格式 (Client - Server需一致)
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

            String jsonIn = getOrderTask.execute().get();
            Type listType = new TypeToken<List<RoomMealOrderMasterVO>>() {
            }.getType();
            orderList = gson.fromJson(jsonIn, listType);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        if (orderList == null || orderList.isEmpty()) {
            Util.showToast(activity, R.string.msg_OrderNotFound);
        } else {
            mAdapter = new RMOMAdapter(activity, orderList);
            rvOngoing.setAdapter(mAdapter);
        }
    }
}
