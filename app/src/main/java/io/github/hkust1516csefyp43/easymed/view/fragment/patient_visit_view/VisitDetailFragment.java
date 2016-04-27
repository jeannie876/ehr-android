package io.github.hkust1516csefyp43.easymed.view.fragment.patient_visit_view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.hkust1516csefyp43.easymed.R;
import io.github.hkust1516csefyp43.easymed.listener.OnFragmentInteractionListener;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Consultation;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Document;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Patient;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Prescription;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.RelatedData;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Triage;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Visit;
import io.github.hkust1516csefyp43.easymed.utility.Const;
import io.github.hkust1516csefyp43.easymed.utility.v2API;
import io.github.hkust1516csefyp43.easymed.view.activity.PatientVisitEditActivity;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VisitDetailFragment extends Fragment {
  public final static String TAG = VisitDetailFragment.class.getSimpleName();
  private static String key1 = Const.BundleKey.VISIT_ID;
  private static String key2 = Const.BundleKey.ON_OR_OFF;
  private static String key3 = Const.BundleKey.EDIT_PATIENT;
  private static String key4 = Const.BundleKey.IS_TRIAGE;

  private OnFragmentInteractionListener mListener;

  private FloatingActionButton floatingActionButton;
  private ProgressBar progressBar;

  private int howManyStuffToGet = 2;
  private int stuffGot = 0;

  /**
   * Difference between this fabOn and the fabOn in PatientVisitViewActivity (PVVA): the code starts
   * PVVA, and the PVVA will be passed into this. However, if for some reason PVVA does not pass
   * fabOn, this would still works.
   * TODO what should the default be?
   */
  private Boolean fabOn = false;
  private Boolean isTriage = true;

  private Patient patient;
  private Visit visit;
  private Triage triage;
  private Consultation consultation;
  private List<Prescription> prescriptions;
  private List<RelatedData> physicalExaminations;
  private List<RelatedData> screenings;
  private List<RelatedData> allergies;
  private List<RelatedData> pmhs;
  private List<RelatedData> drugHistories;
  private List<RelatedData> ross;
  private List<RelatedData> redFlags;
  private List<RelatedData> investigations;
  private List<RelatedData> advices;
  private List<RelatedData> diagnosiss;
  private List<RelatedData> followups;
  private Document hpiDoc;
  private Document fhDoc;
  private Document shDoc;
  private LinearLayout linearLayout;


  public static VisitDetailFragment newInstance(Patient patient, Visit visit, Boolean fabOn, Boolean isTriage) {
    VisitDetailFragment fragment = new VisitDetailFragment();
    Bundle args = new Bundle();
    args.putSerializable(key1, visit);
    args.putBoolean(key2, fabOn);
    args.putSerializable(key3, patient);
    args.putBoolean(key4, isTriage);
    fragment.setArguments(args);
    return fragment;
  }

  public VisitDetailFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      Serializable serializable = getArguments().getSerializable(key1);
      if (serializable instanceof Visit) {
        visit = (Visit) serializable;
      }
      fabOn = getArguments().getBoolean(key2, false);
      //key3
      serializable = getArguments().getSerializable(key3);
      if (serializable instanceof Patient) {
        patient = (Patient) serializable;
      }
      isTriage = getArguments().getBoolean(key4, true);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_visit_detail, container, false);
    floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);

    if (!fabOn) {
      floatingActionButton.setVisibility(View.GONE);
    } else {
      floatingActionButton.setImageDrawable(new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_edit).actionBar().color(Color.WHITE));
      floatingActionButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(getContext(), PatientVisitEditActivity.class);
          intent.putExtra(Const.BundleKey.IS_TRIAGE, isTriage);
          if (patient != null) {                                //pass patient
            intent.putExtra(Const.BundleKey.EDIT_PATIENT, patient);
          }
          if (visit != null) {                                  //pass visit
            intent.putExtra(Const.BundleKey.WHOLE_VISIT, visit);
          }
          if (triage != null) {                                 //pass triage
            intent.putExtra(Const.BundleKey.WHOLE_TRIAGE, triage);
          }

          //TODO need to make sure
          //enter from post triage >> isTriage = true
          //enter from post consultation >> isTriage = false

          //pass consultation if exist
          //enter from post pharmacy >> never (fabOn should be false)
          //enter from not yet >> ?
          startActivity(intent);
        }
      });
    }
    linearLayout = (LinearLayout) view.findViewById(R.id.ll_visit_info);
    if (linearLayout != null){
      progressBar = new ProgressBar(getContext());
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
      params.weight = 1.0f;
      params.gravity = Gravity.CENTER;
      progressBar.setVisibility(View.VISIBLE);
      progressBar.setLayoutParams(params);
      linearLayout.addView(progressBar);

      //TODO fix sometimes triage first, sometimes consultation first (race condition)

      //Triage
      OkHttpClient.Builder ohc1 = new OkHttpClient.Builder();
      ohc1.readTimeout(1, TimeUnit.MINUTES);
      ohc1.connectTimeout(1, TimeUnit.MINUTES);
      final Retrofit retrofit = new Retrofit
          .Builder()
          .baseUrl(Const.Database.CLOUD_API_BASE_URL_121_dev)
          .addConverterFactory(GsonConverterFactory.create(Const.GsonParserThatWorksWithPGTimestamp))
          .client(ohc1.build())
          .build();
      v2API.triages triages = retrofit.create(v2API.triages.class);
      Call<List<Triage>> triageCall = triages.getTriages("1", visit.getId());
      triageCall.enqueue(new Callback<List<Triage>>() {
        @Override
        public void onResponse(Call<List<Triage>> call, Response<List<Triage>> response) {
          stuffGot++;
          Log.d(TAG, visit.getId());
          Log.d(TAG, response.body().toString());
          if (response.body() != null && response.body().size() != 0){
            triage = response.body().get(0);
            if (triage != null){
              Context context = getContext();
              if (context != null) {
                TextView tvTriageTitle = new TextView(context);
                tvTriageTitle.setText("Triage");
                tvTriageTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                tvTriageTitle.setTextColor(Color.BLACK);
                tvTriageTitle.setTypeface(null, Typeface.BOLD);
                linearLayout.addView(tvTriageTitle);

                if (triage.getUserId() != null){
                  TextView tvTriageUserId = new TextView(context);
                  tvTriageUserId.setText("User ID: " + triage.getUserId());
                  linearLayout.addView(tvTriageUserId);
                }
                if (triage.getId() != null){
                  TextView tvTriageId = new TextView(context);
                  tvTriageId.setText("Triage ID: " + triage.getId());
                  linearLayout.addView(tvTriageId);
                }
                if (triage.getVisitId() != null){
                  TextView tvTriageVisitId = new TextView(context);
                  tvTriageVisitId.setText("Visit ID: " + triage.getVisitId());
                  linearLayout.addView(tvTriageVisitId);
                }
                if (triage.getChiefComplaints() != null){
                  TextView tvTriageCC = new TextView(context);
                  tvTriageCC.setText("Chief complaint: " + triage.getChiefComplaints());
                  linearLayout.addView(tvTriageCC);
                }
                if (triage.getHeartRate() != null){
                  TextView tvTriageHeartRate = new TextView(context);
                  tvTriageHeartRate.setText("Heart rate: " + triage.getHeartRate());
                  linearLayout.addView(tvTriageHeartRate);
                }
                if (triage.getHeight() != null){
                  TextView tvTriageHeight = new TextView(context);
                  tvTriageHeight.setText("Height: " + String.valueOf(triage.getHeight()));
                  linearLayout.addView(tvTriageHeight);
                }
                if (triage.getWeight() != null){
                  TextView tvTriageWeight = new TextView(context);
                  tvTriageWeight.setText("Weight: " + String.valueOf(triage.getWeight()));
                  linearLayout.addView(tvTriageWeight);
                }
                if (triage.getRemark() != null){
                  TextView tvTriageRemark = new TextView(context);
                  tvTriageRemark.setText("Remark: " + triage.getRemark());
                  linearLayout.addView(tvTriageRemark);
                }
              }
            }
          } else {
            onFailure(null, null);
          }
          if (stuffGot >= howManyStuffToGet) {
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          }
        }

        @Override
        public void onFailure(Call<List<Triage>> call, Throwable t) {

        }
      });

      //Consultation
      final v2API.consultations consultations = retrofit.create(v2API.consultations.class);
      final Call<List<Consultation>> consultationCall = consultations.getConsultations("1", visit.getId());
      consultationCall.enqueue(new Callback<List<Consultation>>() {
        @Override
        public void onResponse(Call<List<Consultation>> call, Response<List<Consultation>> response) {
          stuffGot++;
          Log.d(TAG, visit.getId());
          Log.d(TAG, response.body().toString());
          if (response.body() != null && response.body().size() != 0){
            consultation = response.body().get(0);
            if (consultation != null){
              getOtherStuff(retrofit);
              Context context = getContext();
              if (context != null) {
                TextView tvConsultationTitle = new TextView(context);
                tvConsultationTitle.setText("Consultation");
                tvConsultationTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                tvConsultationTitle.setTextColor(Color.BLACK);
                tvConsultationTitle.setTypeface(null, Typeface.BOLD);
                linearLayout.addView(tvConsultationTitle);

              if (consultation.getUserId() != null){
                TextView tvConsultationUserId = new TextView(context);
                tvConsultationUserId.setText("User ID: " + consultation.getUserId());
                linearLayout.addView(tvConsultationUserId);
              }
                if (consultation.getId() != null){
                  TextView tvConsultationId = new TextView(context);
                  tvConsultationId.setText("Consultation ID: " + consultation.getId());
                  linearLayout.addView(tvConsultationId);
                }
              if (consultation.getVisitId() != null){
                TextView tvConsultationVisitId = new TextView(context);
                tvConsultationVisitId.setText("Visit ID: " + consultation.getVisitId());
                linearLayout.addView(tvConsultationVisitId);
              }
//              if (triage.getHeight() != null){
//                TextView textView4 = new TextView(context);
//                textView4.setText(String.valueOf(triage.getHeight()));
//                linearLayout.addView(textView4);
//              }
//              if (triage.getRemark() != null){
//                TextView textView5 = new TextView(context);
//                textView5.setText(triage.getRemark());
//                linearLayout.addView(textView5);
//              }
//              if (triage.getChiefComplaints() != null){
//                TextView textView7 = new TextView(context);
//                textView7.setText(triage.getChiefComplaints());
//                linearLayout.addView(textView7);
//              }
              }
            }
          } else {
            onFailure(null, null);
          }
          if (stuffGot >= howManyStuffToGet) {
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          }
        }

        @Override
        public void onFailure(Call<List<Consultation>> call, Throwable t) {

        }
      });
    }
    return view;
  }

  private void getOtherStuff(Retrofit retrofit) {
    if (consultation != null && patient != null) {
      //TODO get prescriptions
      v2API.prescriptions prescriptionService = retrofit.create(v2API.prescriptions.class);
      Call<List<Prescription>> prescriptions = prescriptionService.getPrescriptions("1", null, null, consultation.getId(), null, null, null, null);
      prescriptions.enqueue(new Callback<List<Prescription>>() {
        @Override
        public void onResponse(Call<List<Prescription>> call, Response<List<Prescription>> response) {
          //TODO fill the UI
        }

        @Override
        public void onFailure(Call<List<Prescription>> call, Throwable t) {

        }
      });

      //TODO get related data
      v2API.related_data relatedDataService = retrofit.create(v2API.related_data.class);
      Call<List<RelatedData>> screeningCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 1, null, null, null, null, null);
      Call<List<RelatedData>> allergyCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 2, null, null, null, null, null);
      Call<List<RelatedData>> diagnosisCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 3, null, null, null, null, null);
      Call<List<RelatedData>> adviceCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 4, null, null, null, null, null);
      Call<List<RelatedData>> followupCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 5, null, null, null, null, null);
      Call<List<RelatedData>> drughistoryCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 6, null, null, null, null, null);
      Call<List<RelatedData>> educationCall = relatedDataService.getRelatedDataPlural("1", consultation.getId(), 7, null, null, null, null, null);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }
}
