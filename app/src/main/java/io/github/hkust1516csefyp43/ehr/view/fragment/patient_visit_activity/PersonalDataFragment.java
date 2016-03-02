package io.github.hkust1516csefyp43.ehr.view.fragment.patient_visit_activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.GregorianCalendar;

import io.github.hkust1516csefyp43.ehr.R;
import io.github.hkust1516csefyp43.ehr.Utils;
import io.github.hkust1516csefyp43.ehr.listener.OnCameraRespond;
import io.github.hkust1516csefyp43.ehr.listener.OnFragmentInteractionListener;
import io.github.hkust1516csefyp43.ehr.pojo.Patient;
import io.github.hkust1516csefyp43.ehr.value.Const;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalDataFragment extends Fragment implements OnCameraRespond {
    private static Patient patient;
    private OnFragmentInteractionListener mListener;
    private EditText etFirstName;
    private EditText etLastName;
    private ImageView ivProfilePic;
    private TextView tvBirthday;

    public PersonalDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PersonalDataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalDataFragment newInstance() {
        return new PersonalDataFragment();
    }

    public static PersonalDataFragment newInstance(Patient p) {
        if (p != null) {
            patient = p;
        }
        return new PersonalDataFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme2);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View v = localInflater.inflate(R.layout.fragment_personal_data, container, false);
        //TODO findViewById
        etFirstName = (EditText) v.findViewById(R.id.first_name);
        etLastName = (EditText) v.findViewById(R.id.last_name);
        ivProfilePic = (ImageView) v.findViewById(R.id.iv_profile_pic);
        tvBirthday = (TextView) v.findViewById(R.id.tvBirthday);
        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add new image dialog
                new MaterialDialog.Builder(getContext())
                        .title("Patient picture")
                        .items(R.array.image_array)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                switch (which) {
                                    case Const.ACTION_TAKE_PICTURE:
                                        openCamera();
                                        break;
                                    case Const.ACTION_SELECT_PICTURE:
                                        break;
                                    case Const.ACTION_REMOVE_PICTURE:
                                        //save as default
                                    default:
                                        //TODO remove picture & put a Text Drawable
                                }
                            }
                        })
                        .theme(Theme.LIGHT)
                        .show();
            }
        });
        return v;
    }

    @Override
    public void onStop() {
        //TODO save all inputs
        super.onStop();
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
    public void onResume() {
        super.onResume();
        if (tvBirthday != null) {
            tvBirthday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GregorianCalendar gc = new GregorianCalendar();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            //TODO
                        }
                    }, 1992, 9, 14);
                    dpd.showYearPickerFirst(true);
                    dpd.show(getActivity().getFragmentManager(), "qqq");
                }
            });
        }
        //TODO fill ui with patient data
        if (patient != null) {
            if (etFirstName != null && patient.getFirstName() != null)
                etFirstName.setText(patient.getFirstName());
            if (etLastName != null && patient.getLastName() != null)
                etLastName.setText(patient.getLastName());
            if (ivProfilePic != null && patient.getProfilePictureUrl() != null) {
                String t = Utils.getTextDrawableText(patient);
                Drawable backup = TextDrawable.builder().buildRound(t, Utils.getTextDrawableColor(t));
                Glide.with(this).load(patient.getProfilePictureUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.01f)
                        .placeholder(backup)
                        .fallback(backup)
                        .into(ivProfilePic);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    public void OnCameraRespond(Intent t) {
        if (t != null && ivProfilePic != null) {
            Bundle extras = t.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
//        mImageView.setImageBitmap(imageBitmap);
            Glide.with(this).load(imageBitmap).diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.1f).into(ivProfilePic);
        }
    }
}
