package nl.appsrus.vhack2012;

import java.net.URI;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import nl.appsrus.vhack2012.api.AbcApi;
import nl.appsrus.vhack2012.api.ApiFactory;
import nl.appsrus.vhack2012.data.UserProfile;
import nl.appsrus.vhack2012.ui.RemoteImageView;

import org.json.JSONObject;

import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class MyProfileFragment extends SherlockFragment {
	
	private static final String TAG = MyProfileFragment.class.getSimpleName();
	
	private UserProfile profile;
	
	private View profileEditor;
	private View loadingScreen;
	
	private EditText firstName;
	private EditText lastName;
	
	private RemoteImageView avatar;
	
	private TextView birthDay;
	
	private TextView tagline;
	
	private TextView phoneModel;
	private TextView osVersion;
	
	private Button saveButton;
	private Button dateButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_profile_edit, null);
		
		profileEditor = view.findViewById(R.id.profile_editor);
		loadingScreen = view.findViewById(R.id.layout_loading);
		
		firstName = (EditText) view.findViewById(R.id.first_name);
		lastName = (EditText) view.findViewById(R.id.last_name);
		
		avatar = (RemoteImageView) view.findViewById(R.id.avatar);
		avatar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("http://www.gravatar.com/"));
				startActivity(i);
			}
		});
		
		birthDay = (TextView) view.findViewById(R.id.date);
		
		tagline = (EditText) view.findViewById(R.id.tagline);
		
		phoneModel = (TextView) view.findViewById(R.id.device_name);
		osVersion = (TextView) view.findViewById(R.id.device_os);
		
		
		saveButton = (Button) view.findViewById(R.id.save);
		saveButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveProfile();
			}
		});
		
		dateButton = (Button) view.findViewById(R.id.change_date);
		dateButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Clicking date change button!!!");
				
				final DatePickerFragment datePicker = new DatePickerFragment(profile.year, profile.month, profile.day, new DatePickerFragment.DatePickerListener() {
					@Override
					public void datePicked(int year, int month, int day) {
						profile.year = year;
						profile.month = month;
						profile.day = day;
						setProfile(profile);
					}
				});
				datePicker.show(getFragmentManager(), "datePicker");
			}
		});
		profileEditor.setVisibility(View.GONE);
		loadingScreen.setVisibility(View.VISIBLE);
		// Animation
		ImageView iv = (ImageView) view.findViewById(R.id.loading_animation);
		iv.setImageResource(R.drawable.loading_animated);
		AnimationDrawable ad = (AnimationDrawable) iv.getDrawable();
		ad.start();
		
		new UserProfile();
		ApiFactory.getInstance().updateUserProfile(new UserProfile(), new AbcApi.ApiListener() {
			@Override
			public void onSuccess(JSONObject response) {
				try {
					UserProfile profile = UserProfile.parse(response);
					setProfile(profile);
					Log.e(TAG, "onSuccess: " + response.toString());
				} catch (JSONException e) {
					Log.e(TAG, "Could not parse profile", e);
				}
				profileEditor.setVisibility(View.VISIBLE);
				loadingScreen.setVisibility(View.GONE);
			}

			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "onError: " + errorCode + " = " + errorMessage);
			}
		});		
		return view;
	}
	
	public void setProfile(UserProfile profile) {
		this.profile = profile;
		
		firstName.setText(profile.firstName);
		lastName.setText(profile.lastName);
		tagline.setText(profile.tagLine);
		
		phoneModel.setText(profile.phoneName);
		osVersion.setText(profile.osVersion);
		
		if (profile.day == 0 || profile.month == 0 || profile.year == 0) {
			birthDay.setText(R.string.birthday_not_set);
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(profile.year, profile.month-1, profile.day);
			
			birthDay.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime()));
		}
		
		profileEditor.setVisibility(View.VISIBLE);
		loadingScreen.setVisibility(View.GONE);
		
		URI uri = URI.create("http://www.gravatar.com/avatar/" + profile.gravatarUrl);
		avatar.loadURI(uri);
	}
	
	private void saveProfile() {
		profile.firstName = firstName.getText().toString();
		profile.lastName = lastName.getText().toString();
		profile.tagLine= tagline.getText().toString();
		
		final ProgressDialog progress = new ProgressDialog(getActivity());
		progress.setMessage(getText(R.string.saving_profile));
		progress.show();
		
		ApiFactory.getInstance().updateUserProfile(profile, new AbcApi.ApiListener() {
			
			@Override
			public void onSuccess(JSONObject response) {
				Log.d(TAG, "onSuccess: " + response.toString());
				progress.dismiss();
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "onError: " + errorMessage);
				progress.dismiss();
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
			}
		});
	}
}
