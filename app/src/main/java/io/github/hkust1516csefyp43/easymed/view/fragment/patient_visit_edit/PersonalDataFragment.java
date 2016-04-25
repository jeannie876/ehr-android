package io.github.hkust1516csefyp43.easymed.view.fragment.patient_visit_edit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.Serializable;

import io.github.hkust1516csefyp43.easymed.R;
import io.github.hkust1516csefyp43.easymed.listener.OnSendData;
import io.github.hkust1516csefyp43.easymed.pojo.patient_visit_edit.PersonalData;
import io.github.hkust1516csefyp43.easymed.pojo.server_response.Patient;

public class PersonalDataFragment extends Fragment implements OnSendData{
  public final static String TAG = PersonalDataFragment.class.getSimpleName();

  private static Patient patient;
  private EditText etTag;
  private EditText etFirstName;
  private EditText etMiddleName;
  private EditText etLastName;
  private EditText etNativeName;
  private EditText etAddress;
  //TODO phone country code spinner
  private EditText etPhoneNumber;
  private ImageView ivProfilePic;
  private TextView tvBirthday;
  private Spinner sGender;
  private Spinner sStatus;
  private String[] genderArray;
  private String[] statusArray;
  private int[] birthday = new int[3];
  private int[] preFillBirthday = new int[3];

  private boolean anyError = false;

  public static PersonalDataFragment newInstance(Patient p) {
    PersonalDataFragment fragment = new PersonalDataFragment();
    patient = p;
    return fragment;
  }

  public PersonalDataFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_personal_data, container, false);
    inflateEveryBoxes(view);

    ImageView ivBirthdayRemove = (ImageView) view.findViewById(R.id.ivRemoveBirthday);
    ivBirthdayRemove.setImageDrawable(new IconicsDrawable(getContext()).actionBar().color(ResourcesCompat.getColor(getResources(), R.color.secondary_text_color, null)).icon(FontAwesome.Icon.faw_trash_o));
    ivBirthdayRemove.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        birthday[0] = 0;
        birthday[1] = 0;
        birthday[2] = 0;
        tvBirthday.setText("Click to select date");
      }
    });
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  public void inflateEveryBoxes(View view) {
    etTag = (EditText) view.findViewById(R.id.etTag);
    if (patient != null && etTag != null) {
      if (patient.getTag() != null) {
        etTag.setText(patient.getTag().toString());
      }
    }

    etFirstName = (EditText) view.findViewById(R.id.first_name);
    if (patient != null && patient.getFirstName() != null) {
      etFirstName.setText(patient.getFirstName());
    }
    etFirstName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.toString().length() < 1) {
          anyError = true;
          etFirstName.setError("Patient must have a first name");
        } else {
          anyError = false;
        }
      }
    });

    etMiddleName = (EditText) view.findViewById(R.id.middle_name);
    if (patient != null && patient.getMiddleName() != null)
      etMiddleName.setText(patient.getMiddleName());

    etLastName = (EditText) view.findViewById(R.id.last_name);
    if (patient != null && patient.getLastName() != null)
      etLastName.setText(patient.getLastName());

    etNativeName = (EditText) view.findViewById(R.id.native_name);
    if (patient != null && patient.getNativeName() != null)
      etNativeName.setText(patient.getNativeName());

    etAddress = (EditText) view.findViewById(R.id.etAddress);
    if (patient != null && patient.getAddress() != null)
      etAddress.setText(patient.getAddress());

    //TODO phone country code

    etPhoneNumber = (EditText) view.findViewById(R.id.etPhoneNumber);
    if (patient != null && patient.getPhoneNumber() != null){
      etPhoneNumber.setText(patient.getPhoneNumber());
    }

    ivProfilePic = (ImageView) view.findViewById(R.id.iv_profile_pic);
    //TODO get attachment by id


    //TODO from DB
    sGender = (Spinner) view.findViewById(R.id.sGender);
    genderArray = new String[]{
        "Male", "Female", "Disclosed", "Custom"
    };
    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, genderArray);
    sGender.setAdapter(adapter1);
//    if(patient != null && patient.getGenderId() != null){
//
//    }
    sGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    //TODO from keywords
    sStatus = (Spinner) view.findViewById(R.id.sStatus);
    statusArray = new String[]{
        "Single", "Married", "Divorced", "Widowed", "Custom"
    };
    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, statusArray);
    sStatus.setAdapter(adapter2);
    sStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    if (tvBirthday != null) {
      tvBirthday.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
              if (tvBirthday != null) {
                String date = "" + year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;
                birthday[0] = year;
                birthday[1] = monthOfYear;
                birthday[2] = dayOfMonth;
                tvBirthday.setText(date);
              }
            }
          }, preFillBirthday[0], preFillBirthday[1], preFillBirthday[2]);
          dpd.showYearPickerFirst(true);
          dpd.show(getActivity().getFragmentManager(), TAG);
        }
      });
    }

    tvBirthday = (TextView) view.findViewById(R.id.tvBirthday);
    birthday[0] = 0;
    birthday[1] = 0;
    birthday[2] = 0;
    preFillBirthday[0] = 1992;
    preFillBirthday[1] = 8;
    preFillBirthday[2] = 14;
    if (patient != null && patient.getBirthYear() != null && patient.getBirthMonth() != null &&patient.getBirthDate() != null) {
      preFillBirthday[0] = patient.getBirthYear();
      preFillBirthday[1] = patient.getBirthMonth();
      preFillBirthday[2] = patient.getBirthDate();
      String date = "" + preFillBirthday[0] + "/" + (preFillBirthday[1] + 1) + "/" + preFillBirthday[2];
      tvBirthday.setText(date);
    }

  }

  @Override
  public Serializable onSendData() {

    PersonalData pd = new PersonalData();
    if (etFirstName != null) {
      //TODO
    }
    if (etMiddleName != null) {
      pd.setMiddleName(etMiddleName.getText().toString());
    }
    if (etLastName != null) {
      pd.setLastName(etLastName.getText().toString());
    }
    if (etNativeName != null) {
      pd.setNativeName(etNativeName.getText().toString());
    }
    if (etTag != null) {
      //TODO
    }
    if (tvBirthday != null) {
      pd.setBirthYear(birthday[0]);
      pd.setBirthMonth(birthday[1]);
      pd.setBirthDate(birthday[2]);
    }
    if (etAddress != null) {
      pd.setAddress(etAddress.getText().toString());
    }
    if (etPhoneNumber != null) {
      pd.setPhoneNumber(etPhoneNumber.getText().toString());
    }
    return pd;
  }
}
