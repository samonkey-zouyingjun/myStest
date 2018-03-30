package com.evideo.sambaprovider;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.evideo.sambaprovider.base.DirectoryEntry;
import com.evideo.sambaprovider.browsing.Iconfig;
import com.evideo.sambaprovider.browsing.NetworkBrowser;
import com.evideo.sambaprovider.browsing.ServiceHelper;
import com.evideo.sambaprovider.nativefacade.SmbFacade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity implements ServiceHelper.ServiesListListener {

    private ListView mServicesView, mDirsView;
    private List<String> mDevices = new ArrayList<>(), mDirs = new ArrayList<>();
    private ArrayAdapter<String> mAdapter, mAdapter2;
    private SmbFacade mClient;
    private EditText mTvPath;
    private EditText mName, mPwd;
    private String mServiceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initData();
        initView();
    }

    private void initData() {
        mClient = SambaProviderApplication.getSambaClient(this);
    }

    private void initView() {
        mName = (EditText) findViewById(R.id.uName);
        mPwd = (EditText) findViewById(R.id.uPwd);
        mTvPath = (EditText) findViewById(R.id.url);
        mServicesView = (ListView) findViewById(R.id.lv1);
        mDirsView = (ListView) findViewById(R.id.lv2);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDevices);
        mAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDirs);

        mServicesView.setAdapter(mAdapter);
        mDirsView.setAdapter(mAdapter2);

        ServiceHelper.getInstance().setListener(this);

        mServicesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String deviceInfo = mDevices.get(position);

                String[] split = deviceInfo.split(":");

                String ip = split[0];
                String service = split[1];
                if (TextUtils.isEmpty(mName.getText()) || TextUtils.isEmpty(mPwd.getText())) {
                    Toast.makeText(TestActivity.this, "用户名密码错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = mName.getText().toString();
                String pwd = mPwd.getText().toString();

                Iconfig iconfig = new Iconfig(ip, null, name, pwd, service);
                String urlByPwd = NetworkBrowser.creatUrlByPwd(iconfig);

                mTvPath.setText(urlByPwd);

            }
        });

        mDirsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = mDirs.get(position);
                mTvPath.setText(path);
            }
        });
    }

    private static final String TAG = "TestActivity";

    public void finish(View view) {
        onBackPressed();
    }

    public void findService(View view) {
        NetworkBrowser browser = new NetworkBrowser(mClient);
        browser.getServers();

    }

    public void openDir(View view) {
        final String path = mTvPath.getText().toString();
        if (TextUtils.isEmpty(path)) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkBrowser browser = new NetworkBrowser(mClient);

                final List<DirectoryEntry> childDirByUri = browser.getChildDirByUri(path);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(childDirByUri!=null){
                            mDirs.clear();
                            for (DirectoryEntry d:
                                 childDirByUri) {
                                String name = d.getName();
//                                int type = d.getType();
                                mDirs.add(mTvPath.getText().toString()+"/"+name);
                            }

                            mAdapter2.notifyDataSetChanged();
                        }else {
                            Toast.makeText(TestActivity.this, "childDirByUri is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    public void backDir(View view) {
        String path = mTvPath.getText().toString();
        String substring = path.substring(0, path.lastIndexOf("/"));

        mTvPath.setText(substring);

    }

    @Override
    public void dataChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshServiceData();
            }
        });
    }

    private void refreshServiceData() {
        mAdapter.clear();
        Map<String, List<String>> map = ServiceHelper.getInstance().getMap();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String ip = (String) entry.getKey();
            List<String> services = (List<String>) entry.getValue();

            if (services != null) {
                for (String serviceName : services) {
                    mDevices.add(ip + ":" + serviceName);
                }
            }

        }

        mAdapter.notifyDataSetChanged();
    }

    public void openVideo(View view) {
        String videoPath = mTvPath.getText().toString();
        if(TextUtils.isEmpty(videoPath) || videoPath.endsWith(".mp4")){
            Toast.makeText(this, "MP4路径错误", Toast.LENGTH_SHORT).show();
            return;
        }




    }
}
