/*
 * Copyright � 2013 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.ui.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet;
import org.dvbviewer.controller.ui.widget.DateField;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * The Class TimerDetails.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class TimerDetails extends SherlockDialogFragment implements OnDateSetListener, OnClickListener, OnLongClickListener {

	public static final int			TIMER_CHANGED		= 0;
	public static final int			RESULT_CHANGED		= 1;
	public static final int			RESULT_NO_CHANGE	= 2;

	public static final String		EXTRA_ID			= "_id";
	public static final String		EXTRA_TITLE			= "_title";
	public static final String		EXTRA_CHANNEL_NAME	= "_channel_name";
	public static final String		EXTRA_CHANNEL_ID	= "_channel_id";
	public static final String		EXTRA_START			= "_start";
	public static final String		EXTRA_END			= "_end";
	public static final String		EXTRA_ACTION		= "_action";
	public static final String		EXTRA_ACTIVE		= "_active";

	Timer							timer;
	private TextView				channelField;
	private TextView				titleField;
	private CheckBox				activeBox;
	private DateField				dateField;
	private DateField				startField;
	private DateField				stopField;
	private Button					cancelButton;
	private Button					okButton;
	private OnTimeSetListener		startTimeSetListener;
	private OnTimeSetListener		stopTimeSetListener;
	private Calendar				cal;
	private Spinner					postRecordSpinner;
	private OnTimerEditedListener	mOntimeredEditedListener;
	private DVBViewerPreferences	prefs;
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cal = GregorianCalendar.getInstance();
		Date now = new Date();
		prefs = new DVBViewerPreferences(getSherlockActivity());
		if (timer == null && savedInstanceState == null) {
			timer = new Timer();
			timer.setId(getArguments().getLong(EXTRA_ID, 0l));
			timer.setTitle(getArguments().getString(EXTRA_TITLE));
			timer.setChannelName(getArguments().getString(EXTRA_CHANNEL_NAME));
			timer.setChannelId(getArguments().getLong(EXTRA_CHANNEL_ID, 0));
			Date start = new Date(getArguments().getLong(EXTRA_START, now.getTime()));
			Date end = new Date(getArguments().getLong(EXTRA_END, now.getTime()));
			timer.setStart(start);
			timer.setEnd(end);
			if (!getArguments().getBoolean(EXTRA_ACTIVE)) {
				timer.setFlag(Timer.FLAG_DISABLED);
			}
			if (timer.getId() <= 0l) {
				timer.setTimerAction(prefs.getInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, 0));
			}else{
				timer.setTimerAction(getArguments().getInt(EXTRA_ACTION, 0));
			}
		}else if (savedInstanceState != null) {
			timer = new Timer();
			timer.setId(savedInstanceState.getLong(EXTRA_ID, 0));
			timer.setTitle(savedInstanceState.getString(EXTRA_TITLE));
			timer.setChannelName(savedInstanceState.getString(EXTRA_CHANNEL_NAME));
			timer.setChannelId(savedInstanceState.getLong(EXTRA_CHANNEL_ID, 0));
			timer.setStart(new Date(savedInstanceState.getLong(EXTRA_START, now.getTime())));
			timer.setEnd(new Date(savedInstanceState.getLong(EXTRA_END, now.getTime())));
			timer.setTimerAction(savedInstanceState.getInt(EXTRA_ACTION, 0));
			if (!savedInstanceState.getBoolean(EXTRA_ACTIVE)) {
				timer.setFlag(Timer.FLAG_DISABLED);
			}
		}

	}
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockDialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnTimerEditedListener) {
			mOntimeredEditedListener = (OnTimerEditedListener) activity;
		}
	}

	/**
	 * New instance.
	 *
	 * @return the timer details�
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static TimerDetails newInstance() {
		TimerDetails frag = new TimerDetails();
		return frag;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.details);
		if (timer != null) {
			titleField.setText(timer.getTitle());
			dateField.setDate(timer.getStart());
			activeBox.setChecked(!timer.isFlagSet(Timer.FLAG_DISABLED));
			startField.setTime(timer.getStart());
			stopField.setTime(timer.getEnd());
			postRecordSpinner.setSelection(timer.getTimerAction());
			if (!TextUtils.isEmpty(timer.getChannelName())) {
				channelField.setText(timer.getChannelName());
//				imageCahcer.getImage(channelLogo, ServerConsts.URL_CHANNEL_LOGO + URLEncoder.encode(timer.getChannelName()), null);
			}
		}
		if (getDialog() != null) {
			getDialog().setTitle(timer.getId() <= 0 ? R.string.createTimer : R.string.editTimer);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putLong(EXTRA_ID, timer.getId());
		arg0.putString(EXTRA_TITLE, timer.getTitle());
		arg0.putString(EXTRA_CHANNEL_NAME, timer.getChannelName());
		arg0.putLong(EXTRA_CHANNEL_ID, timer.getChannelId());
		arg0.putLong(EXTRA_START, timer.getStart().getTime());
		arg0.putLong(EXTRA_END, timer.getEnd().getTime());
		arg0.putInt(EXTRA_ACTION, timer.getTimerAction());
		arg0.putBoolean(EXTRA_ACTIVE, !timer.isFlagSet(Timer.FLAG_DISABLED));
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_timer_details, null);
		titleField = (TextView) v.findViewById(R.id.titleField);
		dateField = (DateField) v.findViewById(R.id.dateField);
		activeBox = (CheckBox) v.findViewById(R.id.activeBox);
		startField = (DateField) v.findViewById(R.id.startField);
		postRecordSpinner = (Spinner) v.findViewById(R.id.postRecordingSpinner);

		startTimeSetListener = new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				cal.setTime(startField.getDate());
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				startField.setTime(cal.getTime());
			}
		};
		stopTimeSetListener = new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				cal.setTime(stopField.getDate());
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				stopField.setTime(cal.getTime());
			}
		};

		stopField = (DateField) v.findViewById(R.id.stopField);
		cancelButton = (Button) v.findViewById(R.id.buttonCancel);
		okButton = (Button) v.findViewById(R.id.buttonOk);
		channelField = (TextView) v.findViewById(R.id.channelField);

		dateField.setOnClickListener(this);
		startField.setOnClickListener(this);
		stopField.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		okButton.setOnClickListener(this);

		dateField.setOnLongClickListener(this);
		startField.setOnLongClickListener(this);
		stopField.setOnLongClickListener(this);
		return v;
	}

	/**
	 * Gets the timer.
	 *
	 * @return the timer
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * Sets the timer.
	 *
	 * @param timer the new timer
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	/* (non-Javadoc)
	 * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
	 */
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(dateField.getDate());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		dateField.setDate(cal.getTime());
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		DateDialogFragment f;
		switch (v.getId()) {
		case R.id.dateField:
			f = DateDialogFragment.newInstance(getActivity(), (OnDateSetListener) TimerDetails.this, dateField.getDate());
			f.show(getSherlockActivity().getSupportFragmentManager(), "datepicker");
			break;
		case R.id.startField:
			f = DateDialogFragment.newInstance(getActivity(), startTimeSetListener, startField.getDate());
			f.show(getSherlockActivity().getSupportFragmentManager(), "startTimePicker");
			break;
		case R.id.stopField:
			f = DateDialogFragment.newInstance(getActivity(), stopTimeSetListener, stopField.getDate());
			f.show(getSherlockActivity().getSupportFragmentManager(), "stopTimePicker");
			break;
		case R.id.buttonCancel:
			if (mOntimeredEditedListener != null) {
				mOntimeredEditedListener.timerEdited(true);
			}
			dismiss();
			break;
		case R.id.buttonOk:
			StringBuffer url = new StringBuffer();
			url.append(timer.getId() < 0l ? ServerConsts.URL_TIMER_CREATE : ServerConsts.URL_TIMER_EDIT);
			String title = titleField.getText().toString();
			String days = String.valueOf(DateUtils.getDaysSinceDelphiNull(dateField.getDate()));
			String start = String.valueOf(DateUtils.getMinutesOfDay(startField.getDate()));
			String stop = String.valueOf(DateUtils.getMinutesOfDay(stopField.getDate()));
			String endAction = String.valueOf(postRecordSpinner.getSelectedItemPosition());
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("ch", String.valueOf(timer.getChannelId())));
			params.add(new BasicNameValuePair("dor", days));
			params.add(new BasicNameValuePair("encoding", "255"));
			params.add(new BasicNameValuePair("start", start));
			params.add(new BasicNameValuePair("stop", stop));
			params.add(new BasicNameValuePair("title", title));
			params.add(new BasicNameValuePair("endact", endAction));
			params.add(new BasicNameValuePair("enable", activeBox.isChecked() ? "1" : "0"));
			if (timer.getId() >= 0) {
				params.add(new BasicNameValuePair("id", String.valueOf(timer.getId())));
			}
			String query = URLEncodedUtils.format(params, "utf-8");
			url.append(query);
			RecordingServiceGet rsGet = new RecordingServiceGet(url.toString());
			Thread executionThread = new Thread(rsGet);
			executionThread.start();
			if (mOntimeredEditedListener != null) {
				mOntimeredEditedListener.timerEdited(true);
			}
			if (getDialog() != null && getDialog().isShowing()) {
				dismiss();
			}
			break;

		default:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		return true;
	}

	/**
	 * The listener interface for receiving onTimerEdited events.
	 * The class that is interested in processing a onTimerEdited
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnTimerEditedListener<code> method. When
	 * the onTimerEdited event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see OnTimerEditedEvent
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static interface OnTimerEditedListener{
		
		/**
		 * Timer edited.
		 *
		 * @param edited the edited
		 * @author RayBa
		 * @date 07.04.2013
		 */
		public void timerEdited(boolean edited);
		
	}
	
}
