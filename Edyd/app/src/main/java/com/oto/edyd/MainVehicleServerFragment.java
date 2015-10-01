package com.oto.edyd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yql on 2015/8/26.
 * 车辆服务
 */
public class MainVehicleServerFragment extends Fragment {

    private View vehicleServerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vehicleServerView = inflater.inflate(R.layout.main_vehicle_server, null);
        return vehicleServerView;
    }
}
