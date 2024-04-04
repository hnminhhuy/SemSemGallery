    package com.example.semsemgallery.activities;

    import android.content.Context;
    import android.os.Bundle;
    import android.os.Environment;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.exifinterface.media.ExifInterface;

    import com.example.semsemgallery.R;
    import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

    import java.io.File;
    import java.io.IOException;
    import java.text.DateFormat;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.GregorianCalendar;
    import java.util.Locale;
    import java.util.TimeZone;


    public class EditMetadataActivity extends AppCompatActivity {
        // Variables to store old data
        private String oldImageName;
        private String oldDateContent;
        private String oldTimeContent;
        private String oldLocation;

        // save InstanceState
        private Context context;

        // --------- Begin variable of DateTimePicker dialog ---------
        private static final String TAG = "Sample";
        private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
        private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";
        private TextView textView;
        private SwitchDateTimeDialogFragment dateTimeFragment;

        // --------- End variable of DateTimePicker dialog ---------

        public LinearLayout datetimeContainerClick;
        public TextView dateContent;
        public TextView timeContent;
        public EditText imageName;
        public TextView imageFormat;
        public TextView location;
        public ImageButton addLocationBtn;
        public ImageButton removeLocationBtn;
        public ImageButton backBtn;
        public Button saveBtn;
        public Button cancelBtn;
        public String path;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_meta_data);

            String nameFormatIntent = getIntent().getStringExtra("name");
            String nameIntent = nameFormatIntent.split("\\.")[0];
            String formatIntent = nameFormatIntent.split("\\.")[1];
            String dateIntent = getIntent().getStringExtra("date");
            String timeIntent = getIntent().getStringExtra("time");
            path = getIntent().getStringExtra("filePath");
            Log.d("DATE INTENT", dateIntent);






            context = getApplicationContext();
            datetimeContainerClick = (LinearLayout) findViewById(R.id.datetime_action_container);
            dateContent = (TextView) findViewById(R.id.date_TextView);
            timeContent = (TextView) findViewById(R.id.time_TextView);
            imageName = (EditText) findViewById(R.id.image_name_EditText);
            imageFormat = (TextView) findViewById(R.id.image_format);
            location = (TextView) findViewById(R.id.location_TextView);
            addLocationBtn = (ImageButton) findViewById(R.id.add_location_button);
            removeLocationBtn = (ImageButton) findViewById(R.id.remove_location_button);
            saveBtn = (Button) findViewById(R.id.edit_save_button);
            cancelBtn = (Button) findViewById(R.id.edit_cancel_button);
            backBtn = (ImageButton) findViewById(R.id.details_back_button);

            dateContent.setText(dateIntent);
            timeContent.setText(timeIntent);

            imageName.setText(nameIntent);
            imageFormat.setText(formatIntent);



            initDateTimePicker();

            datetimeContainerClick.setOnClickListener(onDateTimeClickListener);
            saveBtn.setOnClickListener(onSaveEdit);
            cancelBtn.setOnClickListener(onCancelEdit);
            removeLocationBtn.setOnClickListener(onRemoveLocationClickListener);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });


        }

        @Override
        protected void onStart() {
            super.onStart();
            Toast.makeText(this, "onStart", Toast.LENGTH_SHORT ).show();

        }

        private void initDateTimePicker()
        {
            // Construct SwitchDateTimePicker
            dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
            if (dateTimeFragment == null) {
                dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                        getString(com.kunzisoft.switchdatetime.R.string.label_datetime_dialog),
                        getString(android.R.string.ok),
                        getString(android.R.string.cancel),
                        getString(R.string.clean),// Optional
                        "en"
                );
            }

            // Optionally define a timezone
            dateTimeFragment.setTimeZone(TimeZone.getDefault());

            // Init format
            final SimpleDateFormat myDateFormat = new SimpleDateFormat("MMMM dd yyyy HH:mm", java.util.Locale.getDefault());
            // Assign unmodifiable values
            dateTimeFragment.set24HoursMode(true);
            dateTimeFragment.setHighlightAMPMSelection(false);
            dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime());
            dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2030, Calendar.DECEMBER, 31).getTime());

            // Define new day and month format
            try {
                dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
            } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
                Log.e(TAG, e.getMessage());
            }

            dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
                @Override
                public void onNeutralButtonClick(Date date) {
                    // Optional if neutral button does'nt exists
                }
                @Override
                public void onPositiveButtonClick(Date date) {
                    String[] dateAndTime = splitDateTime(myDateFormat.format(date));
                    dateContent.setText(dateAndTime[0]);
                    timeContent.setText(dateAndTime[1]);
                    Log.e("DateLogPoss", "POSITIVE");
                    String formattedDate = myDateFormat.format(date);

                    // Log the formatted date
                    Log.e("DateLog", "Formatted Date: " + formattedDate);
                }

                @Override
                public void onNegativeButtonClick(Date date) {

                }
            });
        }


        private View.OnClickListener onRemoveLocationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location.setText(R.string.location_default);
                addLocationBtn.setVisibility(View.VISIBLE);
                removeLocationBtn.setVisibility(View.GONE);
            }
        };

        private View.OnClickListener onSaveEdit = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
    //            updateStateData();
                try {
                    String datetime = dateContent.getText().toString() + " " + timeContent.getText().toString();
                    String saveDateTime = convertDatetime(datetime);




                    ExifInterface exifInterface = new ExifInterface(path);
                    exifInterface.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, saveDateTime);
//
//                    Log.d(TAG, "Updated date metadata: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME));


                    exifInterface.saveAttributes();
                    Toast.makeText(context, "Date metadata updated", Toast.LENGTH_SHORT).show();
//                    finish();
                    exifInterface.saveAttributes();


                }  catch (IOException e) {
                    Log.e("ERROR SAVE", "Error saving date time", e);
                    Toast.makeText(context, "Failed to save date time: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace(); // Print stack trace for detailed error analysis
                }
                Toast.makeText(context, "Data Saved",Toast.LENGTH_SHORT).show();
            }
        };

        public boolean renameFile(String oldFilePath, String newFileName) {
            File oldFile = new File(oldFilePath);

            // Ensure the old file exists
            if (!oldFile.exists()) {
                return false; // File does not exist
            }

            // Construct the new file path
            String parentDirectory = oldFile.getParent();
            String newFilePath = parentDirectory + File.separator + newFileName;

            File newFile = new File(newFilePath);

            // Rename the file
            if (newFile.exists()) {
                Log.e("renameFile", "File already exists: " + newFilePath);
                return false;
            }

            // Rename the file
            if (oldFile.renameTo(newFile)) {
                // File renamed successfully
                Log.d("renameFile", "File renamed successfully to: " + newFilePath);

                // If you want to update metadata, do it here

                return true;
            } else {
                // Failed to rename file
                Log.e("renameFile", "Failed to rename file: " + oldFilePath);
                return false;
            }
        }

        private boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }

        private View.OnClickListener onCancelEdit = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Cancel Edit",Toast.LENGTH_SHORT).show();
                finish();
            }
        };


        public static String[] splitDateTime(String formattedDate) {
            // Split the formatted date string into date and time components
            String[] parts = formattedDate.split(" ");

            // Extract date and time components
            String date = parts[0] + " " + parts[1] + ", " + parts[2]; // Date component
            String time = parts[3]; // Time component

            return new String[]{date, time};
        }

        public String convertDatetime(String datetime)
        {
            DateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());
            Date date;
            try {
                date = inputFormat.parse(datetime);
            } catch (Exception e) {
                return "Error parsing datetime";  // Return empty strings in case of parsing error
            }

            DateFormat outputFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
            String formattedDateTime = outputFormat.format(date);

            return formattedDateTime;


        }
        private View.OnClickListener onDateTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Click", "CLICK: ");
                dateTimeFragment.startAtCalendarView();
                dateTimeFragment.setDefaultDateTime(new GregorianCalendar(2017, Calendar.MARCH, 4, 15, 20).getTime());
                dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
            }
        };



    }