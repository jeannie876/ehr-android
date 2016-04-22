package io.github.hkust1516csefyp43.easymed.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.hkust1516csefyp43.easymed.R;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Medication;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Patient;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Prescription;
import io.github.hkust1516csefyp43.easymed.utility.Const;
import io.github.hkust1516csefyp43.easymed.utility.Util;
import io.github.hkust1516csefyp43.easymed.utility.v2API;
import mehdi.sakout.dynamicbox.DynamicBox;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PharmacyActivity extends AppCompatActivity {
  public static final String TAG = PharmacyActivity.class.getSimpleName();

  private RecyclerView rv;
  private DynamicBox box;
  private FloatingActionButton floatingActionButton;

  private Patient patient;

  private List<Prescription> prescriptions;

  private int updateQueue;
  private boolean updateError = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pharmacy);

    rv = (RecyclerView) findViewById(R.id.recycler_view);
    if (rv != null) {
      box = new DynamicBox(this,rv);
      box.showLoadingLayout();
    }

    //get extra patient (w/ visit_id)
    Intent intent = getIntent();
    if (intent != null) {
      Serializable serializable = intent.getSerializableExtra(Const.BundleKey.READ_ONLY_PATIENT);
      if (serializable instanceof Patient) {
        Log.d(TAG, "patient saved");
        patient = (Patient) serializable;
      }
    }

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();

    if (patient != null) {
      Log.d(TAG, "patient is not null: " + patient.toString());
      ImageView ivProfilePic = (ImageView) findViewById(R.id.profile_pic);
      if (ivProfilePic != null) {
        ivProfilePic.setImageDrawable(TextDrawable.builder().buildRect(Util.getTextDrawableText(patient), ColorGenerator.MATERIAL.getColor(patient.getLastNameSpaceFirstName())));
      }
      if (patient.getVisitId() != null) {
        Log.d(TAG, "vid exist: " + patient.getVisitId());

        OkHttpClient.Builder ohc1 = new OkHttpClient.Builder();
        ohc1.readTimeout(1, TimeUnit.MINUTES);
        ohc1.connectTimeout(1, TimeUnit.MINUTES);


        final Retrofit retrofit = new Retrofit
            .Builder()
            .baseUrl(Const.Database.CLOUD_API_BASE_URL_121_dev)
            .addConverterFactory(GsonConverterFactory.create(Const.GsonParserThatWorksWithPGTimestamp))
            .client(ohc1.build())
            .build();
        v2API.prescriptions prescriptionService = retrofit.create(v2API.prescriptions.class);
        Call<List<Prescription>> prescriptionsCall = prescriptionService.getPrescriptions("1", patient.getVisitId(), null, null, null, null, null, null);
        prescriptionsCall.enqueue(new Callback<List<Prescription>>() {
          @Override
          public void onResponse(Call<List<Prescription>> call, Response<List<Prescription>> response) {

            //TODO get name for each one
            if (response != null) {
              if (response.body() != null) {
                if (response.body().size() >= 1 ) {
                  prescriptions = response.body();
                  Log.d(TAG, prescriptions.toString());
                  for (int i = 0; i < prescriptions.size(); i++) {
                    Prescription p = prescriptions.get(i);
                    v2API.medication medicationService = retrofit.create(v2API.medication.class);
                    Call<Medication> medicationCall = medicationService.getMedication("1", p.getMedicationId());
                    final int j = i;
                    medicationCall.enqueue(new Callback<Medication>() {
                      @Override
                      public void onResponse(Call<Medication> call, Response<Medication> response) {
                        if (response != null) {
                          if (response.body() != null) {
                            if (response.body().getMedication() != null) {
                              prescriptions.get(j).setMedicationName(response.body().getMedication());
                              Log.d(TAG, "new name: " + response.body().getMedication());
                              if (theWholeListOfPrescriptionsGotName()) {
                                showUI(null);
                              }
                            } else {
                              onFailure(null, new Throwable("Medication have no name -_-"));
                            }
                          } else {
                            onFailure(null, new Throwable("Empty body"));
                          }
                        } else {
                          onFailure(null, new Throwable("No response"));
                        }
                      }

                      @Override
                      public void onFailure(Call<Medication> call, Throwable t) {
                        //How to handle if some medication cannot get medication (no name)
                        //>>solution: set the medication name to Const.EMPTY_STRING then check again
                        t.printStackTrace();
                        prescriptions.get(j).setMedicationName(Const.EMPTY_STRING);
                        Prescription tempP = prescriptions.get(j);
                        tempP.setMedicationName(Const.EMPTY_STRING);
                        prescriptions.set(j, tempP);
                        if (theWholeListOfPrescriptionsGotName()) {
                          showUI(null);
                        }
                      }
                    });
                  }
                } else {    //the list id empty
                  onFailure(null, new Throwable("No prescriptions"));
                  //TODO set next station back to 1 + some dialog explain what happened.
                }
              } else {  //the list is null
                onFailure(null, new Throwable("No prescriptions"));
                //TODO set next station back to 1 + some dialog explain what happened.
              }
            } else {
              onFailure(null, new Throwable("No response"));
            }
          }

          @Override
          public void onFailure(Call<List<Prescription>> call, Throwable t) {
            t.printStackTrace();
            showUI(t);
          }
        });
      }

      if (actionBar != null) {
        actionBar.setTitle(patient.getLastNameSpaceFirstName());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
      }

    }

    floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
    if (floatingActionButton != null) {
      floatingActionButton.setVisibility(View.GONE);
      floatingActionButton.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_check).color(Color.WHITE).actionBar());
      floatingActionButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          //TODO click to confirm >> retrofit
          if (prescriptions.size() > 0) {
            updateQueue = prescriptions.size();
            for (final Prescription p: prescriptions) {
              OkHttpClient.Builder ohc1 = new OkHttpClient.Builder();

              HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override public void log(String message) {
                  Log.d(TAG, "from put prescription: " + message);
                }
              });
              logging.setLevel(HttpLoggingInterceptor.Level.BODY);
              ohc1.readTimeout(1, TimeUnit.MINUTES);
              ohc1.connectTimeout(1, TimeUnit.MINUTES);
              ohc1.addInterceptor(logging);

              Retrofit retrofit = new Retrofit
                  .Builder()
                  .baseUrl(Const.Database.CLOUD_API_BASE_URL_121_dev)
                  .addConverterFactory(GsonConverterFactory.create(Const.GsonParserThatWorksWithPGTimestamp))
                  .client(ohc1.build())
                  .build();
              v2API.prescriptions prescriptionService = retrofit.create(v2API.prescriptions.class);
              Log.d(TAG, "will update to this: " + p.toString());
              Call<Prescription> prescriptionCall = prescriptionService.editPrescription("1", p.getId(), p);
              prescriptionCall.enqueue(new Callback<Prescription>() {
                @Override
                public void onResponse(Call<Prescription> call, Response<Prescription> response) {
                  if (response != null) {
                    Log.d(TAG, "code: " + response.code());
                    if (response.body() != null) {
                      Log.d(TAG, "PUT response: " + response.body() + " vs " + p.getId());
                      if (response.body().getId().compareTo(p.getId()) == 0) {
                        updateQueue--;
                        ifFinishLeave();
                      } else {
                        onFailure(null, new Throwable("wrong id??? WTH?"));
                      }
                    } else {
                      onFailure(null, new Throwable("response body is null"));
                    }
                  } else {
                    onFailure(null, new Throwable("empty response"));
                  }
                }

                @Override
                public void onFailure(Call<Prescription> call, Throwable t) {
                  t.printStackTrace();
                  updateQueue--;
                  updateError = true;
                  ifFinishLeave();
                }
              });
            }
          }
        }
      });
    }
  }

  private void ifFinishLeave() {
    if (updateQueue <= 0) {
      if (!updateError) {
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            imDone();
          }
        }, 2000);
      } else {
        //TODO notify user, ask them to try again

      }
    }
    //else >> not yet, do nothing and just wait for another response to trigger this
  }

  private void imDone() {
    finish();
  }

  private void showUI(@Nullable Throwable throwable) {
    if (throwable == null) {
      if (prescriptions != null) {
        Log.d(TAG, "before cleanup" + prescriptions.size());
        //clean up invalid prescriptions
        if (prescriptions.size() >= 1) {
          List<Prescription> newPrescriptions = new ArrayList<>();
          for (Prescription p: prescriptions) {
            if (p.getMedicationName() != null) {
              if (p.getMedicationName() != Const.EMPTY_STRING) {
                newPrescriptions.add(p);
              }
            }
          }
          prescriptions = newPrescriptions;
          Log.d(TAG, "after cleanup" + prescriptions.size());
          if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.VISIBLE);
          }
          if (box != null) {
            //TODO set adapters and stuff
            if (rv != null) {
              rv.setAdapter(new prescriptionRVAdapter());
              rv.setLayoutManager(new LinearLayoutManager(getBaseContext()));
              box.hideAll();
            }
          }
        } else {
          showNoPrescriptions();
        }
      }
    } else {
      showNoPrescriptions();
    }
  }

  private void showNoPrescriptions() {
    if (box != null) {
//      box.setClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//          //TODO PUT next_station to 1
//          Log.d(TAG, "PUT next_station to 1");
//        }
//      });
      box.addCustomView(getLayoutInflater().inflate(R.layout.exception_failure_report, null, false), "report");
      //TODO user view to findviewbyid >> button onclick
      box.showCustomView("report");
      if (floatingActionButton != null) {
        floatingActionButton.setVisibility(View.GONE);
      }
    }
  }

  /**
   * How to handle if some medication cannot get medication (no name)
   * >>solution: set the medication name to Const.EMPTY_STRING then check again
   * (Because remove it mess up the for loop -_-)
   * @return true if the whole list have have
   */
  private boolean theWholeListOfPrescriptionsGotName () {
    if (prescriptions != null) {
      Log.d(TAG, "Checking if the list have been processed: " + prescriptions.toString());
      for (Prescription p: prescriptions) {
        if (p.getMedicationName() == null) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // app icon in action bar clicked; goto parent activity.
        this.finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private class prescriptionsRVViewHolder extends RecyclerView.ViewHolder {
    AppCompatCheckBox appCompatCheckBox;
    TextView tvMedication;
    TextView tvPrescriptionDetail;
    RelativeLayout rlTheWholeThing;

    public prescriptionsRVViewHolder(View itemView) {
      super(itemView);
      appCompatCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.accbPrescription);
      tvMedication = (TextView) itemView.findViewById(R.id.tvMedication);
      tvPrescriptionDetail = (TextView) itemView.findViewById(R.id.tvPrescriptionDetail);
      rlTheWholeThing = (RelativeLayout) itemView.findViewById(R.id.rlPrescription);
      rlTheWholeThing.setLongClickable(true);
    }

  }

  private class prescriptionRVAdapter extends RecyclerView.Adapter<prescriptionsRVViewHolder> {

    public prescriptionRVAdapter() {

    }

    @Override
    public prescriptionsRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new prescriptionsRVViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prescription, parent, false));
    }

    @Override
    public void onBindViewHolder(final prescriptionsRVViewHolder holder, int position) {
      if (holder != null && prescriptions != null) {
        if (position < prescriptions.size()) {
          if (prescriptions.get(holder.getAdapterPosition()).getMedicationName() != Const.EMPTY_STRING) {
            holder.tvMedication.setText(prescriptions.get(holder.getAdapterPosition()).getMedicationName());
            holder.tvPrescriptionDetail.setText(prescriptions.get(holder.getAdapterPosition()).getDetail());
            holder.appCompatCheckBox.setChecked(prescriptions.get(holder.getAdapterPosition()).getPrescribed());
            holder.appCompatCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, prescriptions.get(holder.getAdapterPosition()) + " :" + String.valueOf(isChecked));
                Prescription tempP = prescriptions.get(holder.getAdapterPosition());
                tempP.setPrescribed(isChecked);
                prescriptions.set(holder.getAdapterPosition(), tempP);
              }
            });
            holder.rlTheWholeThing.setOnLongClickListener(new View.OnLongClickListener() {
              @Override
              public boolean onLongClick(View v) {
                //TODO long click >> flag quantity (dialog maybe)
                return false;
              }
            });

          }
        }
      }
    }

    @Override
    public int getItemCount() {
      Log.d("qqq370", String.valueOf(prescriptions.size()));
      return prescriptions.size();
    }
  }

}