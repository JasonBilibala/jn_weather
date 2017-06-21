package com.jnweather.android.db;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jnweather.android.R;
import com.jnweather.android.util.HttpUtil;
import com.jnweather.android.util.Utility;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/6/21.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省
     */
    private Province selectProvince;

    /**
     * 选中的市
     */
    private City selectCity;

    /**
     * 当前等级
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectProvince = provinceList.get(position);
            } else if (currentLevel == LEVEL_CITY) {
                selectCity = cityList.get(position);
            }

            backButton.setOnClickListener(v -> {
                if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCity();
                }
            });
            queryProvince();
        }));
    }

    private void queryProvince() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);

        if (provinceList == null || provinceList.size() == 0) {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        } else {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
    }

    private void queryCity() {
        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectProvince.getProvinceCode())).find(City.class);

        if (cityList == null || cityList.size() == 0) {
            String address = "http://guolin.tech/api/china/" + selectProvince.getProvinceCode();
            queryFromServer(address, "city");
        } else {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
    }

    private void queryCounty() {
        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectProvince.getProvinceCode())).find(County.class);

        if (cityList == null || cityList.size() == 0) {
            String address = "http://guolin.tech/api/china/" + selectProvince.getProvinceCode();
            queryFromServer(address, "county");
        } else {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
    }

    private void queryFromServer(String address, String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败...", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;

                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectCity.getId());
                }

                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        if ("province".equals(type)) {
                            queryProvince();
                        } else if ("county".equals(type)) {
                            queryCity();
                        } else if ("county".equals(type)) {
                            queryCounty();
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }
}
