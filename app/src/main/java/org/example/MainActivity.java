package org.example;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VrnTicketPrefs";

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBirthDate;
    private EditText etValidFrom;
    private EditText etValidTo;

    private LinearLayout layoutTicketHeader;
    private LinearLayout layoutTicketContainer;
    private LinearLayout cardInputForm;
    private TextView tvTicketHeader;
    private TextView tvPassengerName;
    private TextView tvPassengerDob;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private ImageView ivAztecCode;
    private BottomNavigationView bottomNavigation;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Инициализация
        layoutTicketHeader = findViewById(R.id.layoutTicketHeader);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etValidFrom = findViewById(R.id.etValidFrom);
        etValidTo = findViewById(R.id.etValidTo);

        layoutTicketContainer = findViewById(R.id.layoutTicketContainer);
        cardInputForm = findViewById(R.id.cardInputForm);
        tvTicketHeader = findViewById(R.id.tvTicketHeader);
        tvPassengerName = findViewById(R.id.tvPassengerName);
        tvPassengerDob = findViewById(R.id.tvPassengerDob);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        ivAztecCode = findViewById(R.id.ivAztecCode);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        Button btnGenerate = findViewById(R.id.btnGenerate);

        // Настройка выбора дат
        setupDatePicker(etBirthDate);
        setupDatePicker(etValidFrom);
        setupDatePicker(etValidTo);

        // Анимация вращения Aztec-кода по клику
        ivAztecCode.setOnClickListener(this::spinAztecCode);

        // Клик по шапке для открытия редактирования
        layoutTicketHeader.setOnClickListener(v -> toggleInputForm());

        btnGenerate.setOnClickListener(v -> generateAndSaveTicket());

        // Нижнее меню навигации
        bottomNavigation.setSelectedItemId(R.id.nav_ticket);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ticket) {
                if (layoutTicketContainer.getVisibility() == View.GONE) {
                    layoutTicketContainer.setVisibility(View.VISIBLE);
                    cardInputForm.setVisibility(View.GONE);
                }
                return true;
            } else if (id == R.id.nav_search || id == R.id.nav_profile) {
                // Вкладки заглушки
                return true;
            }
            return false;
        });

        // Загрузка
        loadSavedTicketData();
    }

    private void setupDatePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(v -> showDatePickerDialog(editText));
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.GERMANY, "%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear);
                    targetEditText.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void spinAztecCode(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        animator.setDuration(800);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void toggleInputForm() {
        if (cardInputForm.getVisibility() == View.VISIBLE) {
            cardInputForm.setVisibility(View.GONE);
            layoutTicketContainer.setVisibility(View.VISIBLE);
            clearFocusAndHideKeyboard();
        } else {
            cardInputForm.setVisibility(View.VISIBLE);
            layoutTicketContainer.setVisibility(View.GONE);
        }
    }

    private void clearFocusAndHideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private String generateNewTicketId() {
        int randomNum = 10000000 + new Random().nextInt(90000000);
        String newId = "VRN-DT-" + randomNum;
        sharedPreferences.edit().putString("ticketId", newId).apply();
        return newId;
    }

    /**
     * Расчет даты с 1-го по ПОСЛЕДНИЙ день ТЕКУЩЕГО месяца
     */
    private String[] getCurrentMonthDates() {
        Calendar cal = Calendar.getInstance();
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH); // 0-based

        // 1-й день текущего месяца
        String validFrom = String.format(Locale.GERMANY, "01.%02d.%04d", curMonth + 1, curYear);

        // Последний день текущего месяца
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        int lastDay = cal.get(Calendar.DAY_OF_MONTH);

        String validTo = String.format(Locale.GERMANY, "%02d.%02d.%04d", lastDay, curMonth + 1, curYear);

        return new String[]{validFrom, validTo, String.format(Locale.GERMANY, "%02d.%04d", curMonth + 1, curYear)};
    }

    private void generateAndSaveTicket() {
        clearFocusAndHideKeyboard();

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String validFrom = etValidFrom.getText().toString().trim();
        String validTo = etValidTo.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty()) {
            return;
        }

        if (validFrom.isEmpty() || validTo.isEmpty()) {
            String[] autoDates = getCurrentMonthDates();
            validFrom = autoDates[0];
            validTo = autoDates[1];
            etValidFrom.setText(validFrom);
            etValidTo.setText(validTo);
        }

        String ticketId = sharedPreferences.getString("ticketId", "");
        if (ticketId.isEmpty()) {
            ticketId = generateNewTicketId();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("birthDate", birthDate);
        editor.putString("validFrom", validFrom);
        editor.putString("validTo", validTo);
        editor.apply();

        displayTicket(firstName, lastName, birthDate, validFrom, validTo, ticketId);
        cardInputForm.setVisibility(View.GONE);
        layoutTicketContainer.setVisibility(View.VISIBLE);
    }

    private void loadSavedTicketData() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String birthDate = sharedPreferences.getString("birthDate", "");

        String[] currentMonthInfo = getCurrentMonthDates();
        String autoValidFrom = currentMonthInfo[0];
        String autoValidTo = currentMonthInfo[1];
        String currentMonthKey = currentMonthInfo[2];

        String lastProcessedMonth = sharedPreferences.getString("lastMonthKey", "");
        String ticketId = sharedPreferences.getString("ticketId", "");

        if (!currentMonthKey.equals(lastProcessedMonth)) {
            ticketId = generateNewTicketId();
            sharedPreferences.edit()
                    .putString("lastMonthKey", currentMonthKey)
                    .putString("validFrom", autoValidFrom)
                    .putString("validTo", autoValidTo)
                    .apply();
        }

        String validFrom = sharedPreferences.getString("validFrom", autoValidFrom);
        String validTo = sharedPreferences.getString("validTo", autoValidTo);

        etFirstName.setText(firstName);
        etLastName.setText(lastName);
        etBirthDate.setText(birthDate);
        etValidFrom.setText(validFrom);
        etValidTo.setText(validTo);

        if (!firstName.isEmpty() && !lastName.isEmpty() && !birthDate.isEmpty()) {
            displayTicket(firstName, lastName, birthDate, validFrom, validTo, ticketId);
            cardInputForm.setVisibility(View.GONE);
            layoutTicketContainer.setVisibility(View.VISIBLE);
        } else {
            cardInputForm.setVisibility(View.VISIBLE);
            layoutTicketContainer.setVisibility(View.GONE);
        }
    }

    private void displayTicket(String firstName, String lastName, String birthDate, String validFrom, String validTo, String ticketId) {
        tvTicketHeader.setText("VRN Ticket");
        tvPassengerName.setText(String.format("%s %s", firstName, lastName));
        tvPassengerDob.setText(birthDate);
        tvValidityInfo.setText(String.format("%s - %s", validFrom, validTo));
        tvTicketNumberInfo.setText(String.format("ID Ticket: %s", ticketId));

        String aztecContent = String.format(
                "VRN|DEUTSCHLANDTICKET|Name:%s %s|DOB:%s|ValidFrom:%s|ValidTo:%s|ID:%s",
                firstName, lastName, birthDate, validFrom, validTo, ticketId
        );

        Bitmap aztecBitmap = generateAztecCodeBitmap(aztecContent, 1200, 1200);
        if (aztecBitmap != null) {
            ivAztecCode.setImageBitmap(aztecBitmap);
            layoutTicketContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Генератор кода типа AZTEC
     */
    private Bitmap generateAztecCodeBitmap(String text, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    text, BarcodeFormat.AZTEC, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
