package io.github.hkust1516csefyp43.ehr.view.fragment.two_recycler_view_patients_activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.hkust1516csefyp43.ehr.R;
import io.github.hkust1516csefyp43.ehr.Utils;
import io.github.hkust1516csefyp43.ehr.adapter.PatientCardRecyclerViewAdapter;
import io.github.hkust1516csefyp43.ehr.listener.ListCounterChangedListener;
import io.github.hkust1516csefyp43.ehr.listener.OnChangeStationListener;
import io.github.hkust1516csefyp43.ehr.pojo.server_response.v2.Patient;
import io.github.hkust1516csefyp43.ehr.v2API;
import io.github.hkust1516csefyp43.ehr.value.Cache;
import io.github.hkust1516csefyp43.ehr.value.Const;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class PatientsRecyclerViewFragment extends android.support.v4.app.Fragment implements OnChangeStationListener {
    public final String TAG = this.getClass().getSimpleName();
    private RecyclerView rv;
    private SwipeRefreshLayout srl;
    private TextView fail;
    private ProgressBar pb;
    private ListCounterChangedListener lListener;
    private int whichPage;                           //which station

    public PatientsRecyclerViewFragment() {
        // Required empty public constructor
    }

    public static PatientsRecyclerViewFragment newInstance() {
        return new PatientsRecyclerViewFragment();
    }

    public static PatientsRecyclerViewFragment newInstance(int which) {
        PatientsRecyclerViewFragment ptrvf = new PatientsRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putInt(Const.EXTRA_WHICH_ONE, which);
        ptrvf.setArguments(args);
        return ptrvf;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("qqq41", "onAttach");
        try {
            lListener = (ListCounterChangedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            whichPage = getArguments().getInt(Const.EXTRA_WHICH_ONE);
            Log.d("qqq42", "which one = " + whichPage);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("qqq43", "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("qqq44", "onResume");
        final Context c = getContext();
        View rootView = getView();
        if (rootView != null) {
            if (rv == null)
                rv = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            if (srl == null)
                srl = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
            if (fail == null)
                fail = (TextView) rootView.findViewById(R.id.fail);
            if (pb == null)
                pb = (ProgressBar) rootView.findViewById(R.id.loading_wheel);
            fail.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(c, LinearLayoutManager.VERTICAL, false));
            rv.setAdapter(new PatientCardRecyclerViewAdapter(c, whichPage));
            rv.setVisibility(View.GONE);
            Log.d("qqq", "rv lm and a all set");
        }

        OkHttpClient ohc = new OkHttpClient();
        ohc.setReadTimeout(2, TimeUnit.MINUTES);
        ohc.setConnectTimeout(2, TimeUnit.MINUTES);
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(Const.API_ONE2ONE_HEROKU)
                .addConverterFactory(GsonConverterFactory.create(Const.GsonParserThatWorksWithPGTimestamp))
                .client(ohc)
                .build();
        v2API apiService2 = retrofit.create(v2API.class);
        //TODO different Call depending on the station variable >>1/2/3
        final Call<List<Patient>> call22;
        switch (whichPage) {
            case Const.PATIENT_LIST_ALL_PATIENTS:
                Log.d(TAG, "all patients");
                call22 = apiService2.getPatients("1", null, null, null, null, null, null, null, null, null, null);
                break;
            case Const.PATIENT_LIST_ALL_TODAYS_PATIENT:
                Log.d(TAG, "all today patients");
                call22 = apiService2.getPatients("1", null, null, null, null, null, null, null, null, null, Utils.todayString());
                break;
            case Const.PATIENT_LIST_POST_CONSULTATION:
                Log.d(TAG, "post consultation");
                call22 = apiService2.getPatients("1", null, "3", null, null, null, null, null, null, null, Utils.todayString()); //3 == next station is pharmacy
                break;
            case Const.PATIENT_LIST_POST_TRIAGE:
                Log.d(TAG, "post triage");
                call22 = apiService2.getPatients("1", null, "2", null, null, null, null, null, null, null, Utils.todayString()); //2 == next station is consultation i.e. post triage
                break;
            case Const.PATIENT_LIST_PRE_PHARMACY:
                Log.d(TAG, "pre pharmacy ");
                call22 = apiService2.getPatients("1", null, "1", null, null, null, null, null, null, null, Utils.todayString()); //1 == next station is triage i.e. ended, loop back to 1
                break;
            default:
                Log.d(TAG, "default = ?");
                call22 = apiService2.getPatients("1", null, null, null, null, null, null, null, null, null, null);
        }
        final Callback<List<Patient>> cb2 = new Callback<List<Patient>>() {
            @Override
            public void onResponse(Response<List<Patient>> response, Retrofit retrofit) {
                Log.d("qqq27", "receiving: " + response.code() + " " + response.message());
                if (response.code() >= 500 && response.code() < 600)
                    onFailure(new Throwable(response.code() + "/" + response.message()));
                else
                    receiving(c, response);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("qqq27", "receives nothing/error");
                failing(t);
            }
        };
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUI(call22, cb2);
            }
        });
        refreshUI(call22, cb2);
    }

    private void refreshUI(Call<List<Patient>> call2, Callback<List<Patient>> cb) {
        rv.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        Log.d("qqq29", "refreshing");
        Call<List<Patient>> call = call2.clone();
        call.enqueue(cb);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        lListener = null;
    }

    private void receiving(Context c, Response<List<Patient>> response) {
        if (response != null && response.body() != null && response.body().size() > 0) {
            Cache.setPostTriagePatients2(c, response.body());
            changeTabCounter(response.body().size());
            pb.setVisibility(View.GONE);

            if (rv != null && c != null && srl != null) {
//            if (rv != null && c != null) {
//                rv.getAdapter().notifyDataSetChanged();
                rv.swapAdapter(new PatientCardRecyclerViewAdapter(c, whichPage), false);            //i don't know what would happen if set to true
                fail.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                srl.setVisibility(View.VISIBLE);
                srl.setRefreshing(false);
                Log.d("qqq25", "" + response.body().toString());
                Log.d("qqq23", "rv/srl not showing occasionally");
            } else {
                Log.d("qqq23", "sth wrong");
            }
        } else {
            //TODO receives nothing
            Log.d("qqq", "error");
            changeTabCounter(-1);
            failing(new Throwable("Fetched nothing"));
        }
    }

    private void failing(Throwable t) {
        Log.d("qqq19", "failing");
        changeTabCounter(0);
        pb.setVisibility(View.GONE);
        fail.setText(t.getMessage());
        fail.setVisibility(View.VISIBLE);
        srl.clearAnimation();
        srl.setRefreshing(false);
        rv.setVisibility(View.GONE);
    }


    /**
     * Signal TwoRecyclerViewPatientsActivity to change the number of patients on the tab
     * @param count = how many patients
     */
    public void changeTabCounter(int count) {
        if (lListener != null) {
            lListener.onCounterChangedListener(0, count);
        }
    }

    public void scrollToTop() {
        rv.getLayoutManager().scrollToPosition(0);
    }

    /**
     * TODO depends on whichPage to fetch from different API
     */
    private void fetchFromServer() {
//        switch (whichPage) {
//            case
//        }
    }

    @Override
    public void onStationChange(int newPageId) {
        //TODO call API and refresh the UI
        Log.d("qqq280", "" + newPageId);
        whichPage = newPageId;
        if (newPageId == Const.PATIENT_LIST_POST_TRIAGE || newPageId == Const.PATIENT_LIST_ALL_PATIENTS)
            Cache.setWhichStation(getContext(), Const.ID_TRIAGE);
        else if (newPageId == Const.PATIENT_LIST_PRE_CONSULTATION || newPageId == Const.PATIENT_LIST_POST_CONSULTATION)
            Cache.setWhichStation(getContext(), Const.ID_CONSULTATION);
        //TODO modify patientListCall and patientListCallback, and then call refreshUI

        OkHttpClient ohc = new OkHttpClient();
        ohc.setReadTimeout(2, TimeUnit.MINUTES);
        ohc.setConnectTimeout(2, TimeUnit.MINUTES);
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(Const.API_ONE2ONE_HEROKU)
                .addConverterFactory(GsonConverterFactory.create(Const.GsonParserThatWorksWithPGTimestamp))
                .client(ohc)
                .build();
        v2API apiService2 = retrofit.create(v2API.class);
        //TODO different Call depending on the station variable >>1/2/3
        final Call<List<Patient>> call22;
        switch (whichPage) {
            case Const.PATIENT_LIST_ALL_PATIENTS:
                Log.d(TAG, "all patients");
                call22 = apiService2.getPatients("1", null, null, null, null, null, null, null, null, null, null);
                break;
            case Const.PATIENT_LIST_ALL_TODAYS_PATIENT:
                Log.d(TAG, "all today patients");
                call22 = apiService2.getPatients("1", null, null, null, null, null, null, null, null, null, Utils.todayString());
                break;
            case Const.PATIENT_LIST_POST_CONSULTATION:
                Log.d(TAG, "post consultation");
                call22 = apiService2.getPatients("1", null, "3", null, null, null, null, null, null, null, Utils.todayString()); //3 == next station is pharmacy
                break;
            case Const.PATIENT_LIST_POST_TRIAGE:
                Log.d(TAG, "post triage");
                call22 = apiService2.getPatients("1", null, "2", null, null, null, null, null, null, null, Utils.todayString()); //2 == next station is consultation i.e. post triage
                break;
            case Const.PATIENT_LIST_PRE_PHARMACY:
                Log.d(TAG, "pre pharmacy ");
                call22 = apiService2.getPatients("1", null, "1", null, null, null, null, null, null, null, Utils.todayString()); //1 == next station is triage i.e. ended, loop back to 1
                break;
            default:
                Log.d(TAG, "default = ?");
                call22 = apiService2.getPatients("1", null, null, null, null, null, null, null, null, null, null);
        }
        final Callback<List<Patient>> cb2 = new Callback<List<Patient>>() {
            @Override
            public void onResponse(Response<List<Patient>> response, Retrofit retrofit) {
                Log.d("qqq27", "receiving: " + response.code() + " " + response.message());
                if (response.code() >= 500 && response.code() < 600)
                    onFailure(new Throwable(response.code() + "/" + response.message()));
                else
                    receiving(getContext(), response);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("qqq27", "receives nothing/error");
                failing(t);
            }
        };
        refreshUI(call22, cb2);
        //give rv new adapter instead of just notifychange >> pass the station id so that the viewholder can handle click correctly
//        rv.setAdapter(new PatientCardRecyclerViewAdapter(getContext(), whichPage));
    }
}
