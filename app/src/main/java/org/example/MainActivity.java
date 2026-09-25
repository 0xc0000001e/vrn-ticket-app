package org.example;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etDob, etDocNum;
    private LinearLayout formLayout;
    private ScrollView ticketScrollView;
    
    // Поля билета
    private TextView tvPassengerName, tvTicketId, tvValidMonth, tvDob, tvDocNum, tvPrice;
    private ImageView ivQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#F4F6F9"));

        // ===== 1. ФОРМА ВВОДА ДАННЫХ =====
        ScrollView formScrollView = new ScrollView(this);
        formScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        formLayout = new LinearLayout(this);
        formLayout.setOrientation(LinearLayout.VERTICAL);
        formLayout.setPadding(50, 60, 50, 60);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("VRN Deutschlandticket");
        tvTitle.setTextSize(24);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#004B93"));
        tvTitle.setGravity(Gravity.CENTER);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Введите данные пассажира");
        tvSubtitle.setTextSize(14);
        tvSubtitle.setTextColor(Color.parseColor("#666666"));
        tvSubtitle.setGravity(Gravity.CENTER);
        tvSubtitle.setPadding(0, 10, 0, 50);

        formLayout.addView(tvTitle);
        formLayout.addView(tvSubtitle);

        etFirstName = createStyledInput("Имя", "z.B. Alex");
        etLastName = createStyledInput("Фамилия", "z.B. Müller");
        etDob = createStyledInput("Дата рождения", "ДД.ММ.ГГГГ");
        etDob.setInputType(InputType.TYPE_CLASS_DATETIME);
        etDocNum = createStyledInput("Номер документа / Ausweis", "z.B. DE987654321");

        Button btnGenerate = new Button(this);
        btnGenerate.setText("СФОРМИРОВАТЬ БИЛЕТ");
        btnGenerate.setTextSize(15);
        btnGenerate.setTypeface(Typeface.DEFAULT_BOLD);
        btnGenerate.setTextColor(Color.WHITE);
        
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#004B93"));
        btnBg.setCornerRadius(24);
        btnGenerate.setBackground(btnBg);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 140);
        btnParams.setMargins(0, 40, 0, 0);
        btnGenerate.setLayoutParams(btnParams);
        btnGenerate.setOnClickListener(v -> generateTicket());

        formLayout.addView(btnGenerate);
        formScrollView.addView(formLayout);
        rootLayout.addView(formScrollView);

        // ===== 2. ИНТЕРФЕЙС ЦИФРОВОГО БИЛЕТА =====
        ticketScrollView = new ScrollView(this);
        ticketScrollView.setVisibility(View.GONE);
        ticketScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout ticketContainer = new LinearLayout(this);
        ticketContainer.setOrientation(LinearLayout.VERTICAL);
        ticketContainer.setPadding(40, 50, 40, 60);

        // Карточка билета
        CardView cardView = new CardView(this);
        cardView.setRadius(32);
        cardView.setCardElevation(16);
        cardView.setCardBackgroundColor(Color.WHITE);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);

        // Шапка билета (Синяя VRN)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.parseColor("#004B93"));
        header.setPadding(40, 40, 40, 40);

        TextView tvHeaderTitle = new TextView(this);
        tvHeaderTitle.setText("DEUTSCHLANDTICKET");
        tvHeaderTitle.setTextSize(18);
        tvHeaderTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvHeaderTitle.setTextColor(Color.WHITE);

        TextView tvHeaderSub = new TextView(this);
        tvHeaderSub.setText("VRN • Verkehrsverbund Rhein-Neckar");
        tvHeaderSub.setTextSize(12);
        tvHeaderSub.setTextColor(Color.parseColor("#B3D4FF"));

        header.addView(tvHeaderTitle);
        header.addView(tvHeaderSub);
        cardContent.addView(header);

        // Тело билета
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(40, 30, 40, 40);

        // Статус-бейдж
        TextView tvStatus = new TextView(this);
        tvStatus.setText("● GÜLTIG / АКТИВЕН");
        tvStatus.setTextSize(13);
        tvStatus.setTypeface(Typeface.DEFAULT_BOLD);
        tvStatus.setTextColor(Color.parseColor("#2E7D32"));

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setColor(Color.parseColor("#E8F5E9"));
        statusBg.setCornerRadius(20);
        tvStatus.setBackground(statusBg);
        tvStatus.setPadding(30, 12, 30, 12);
        
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 0, 0, 30);
        tvStatus.setLayoutParams(statusParams);
        body.addView(tvStatus);

        // Пассажир
        tvPassengerName = createLabelValue(body, "Inhaber / Владелец", "", true);
        tvValidMonth = createLabelValue(body, "Gültigkeitsmonat / Месяц", "", false);
        tvTicketId = createLabelValue(body, "Ticket-ID", "", false);
        tvDob = createLabelValue(body, "Geburtsdatum / Дата рожд.", "", false);
        tvDocNum = createLabelValue(body, "Ausweis-Nr.", "", false);
        tvPrice = createLabelValue(body, "Preis", "58,00 €", false);

        // QR-код контейнер
        CardView qrCard = new CardView(this);
        qrCard.setRadius(20);
        qrCard.setCardElevation(4);
        qrCard.setCardBackgroundColor(Color.WHITE);
        
        LinearLayout.LayoutParams qrCardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qrCardParams.setMargins(0, 30, 0, 20);
        qrCard.setLayoutParams(qrCardParams);

        LinearLayout qrLayout = new LinearLayout(this);
        qrLayout.setOrientation(LinearLayout.VERTICAL);
        qrLayout.setGravity(Gravity.CENTER);
        qrLayout.setPadding(30, 30, 30, 30);

        ivQrCode = new ImageView(this);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(500, 500);
        ivQrCode.setLayoutParams(ivParams);
        ivQrCode.setOnClickListener(this::spinQrCode);

        TextView tvQrHint = new TextView(this);
        tvQrHint.setText("Нажмите на QR-код для вращения");
        tvQrHint.setTextSize(11);
        tvQrHint.setTextColor(Color.GRAY);
        tvQrHint.setPadding(0, 15, 0, 0);

        qrLayout.addView(ivQrCode);
        qrLayout.addView(tvQrHint);
        qrCard.addView(qrLayout);

        body.addView(qrCard);

        // Плашки видов транспорта
        TextView tvTransitTitle = new TextView(this);
        tvTransitTitle.setText("Разрешённый транспорт:");
        tvTransitTitle.setTextSize(12);
        tvTransitTitle.setTextColor(Color.GRAY);
        tvTransitTitle.setPadding(0, 10, 0, 10);
        body.addView(tvTransitTitle);

        LinearLayout transitBadges = new LinearLayout(this);
        transitBadges.setOrientation(LinearLayout.HORIZONTAL);
        addBadge(transitBadges, "🚌 Bus");
        addBadge(transitBadges, "🚊 Tram");
        addBadge(transitBadges, "🚆 S-Bahn");
        addBadge(transitBadges, "🚆 RE/RB");
        body.addView(transitBadges);

        cardContent.addView(body);
        cardView.addView(cardContent);
        ticketContainer.addView(cardView);

        // Кнопка Изменить данные
        Button btnBack = new Button(this);
        btnBack.setText("ИЗМЕНИТЬ ДАННЫЕ");
        btnBack.setTextSize(14);
        btnBack.setTextColor(Color.parseColor("#004B93"));
        
        GradientDrawable btnBackBg = new GradientDrawable();
        btnBackBg.setColor(Color.TRANSPARENT);
        btnBackBg.setStroke(3, Color.parseColor("#004B93"));
        btnBackBg.setCornerRadius(24);
        btnBack.setBackground(btnBackBg);

        LinearLayout.LayoutParams btnBackParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 120);
        btnBackParams.setMargins(0, 40, 0, 0);
        btnBack.setLayoutParams(btnBackParams);
        btnBack.setOnClickListener(v -> {
            ticketScrollView.setVisibility(View.GONE);
            formScrollView.setVisibility(View.VISIBLE);
        });

        ticketContainer.addView(btnBack);
        ticketScrollView.addView(ticketContainer);

        rootLayout.addView(ticketScrollView);
        setContentView(rootLayout);
    }

    private EditText createStyledInput(String label, String hint) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(Typeface.DEFAULT_BOLD);
        tvLabel.setTextColor(Color.parseColor("#333333"));
        tvLabel.setPadding(0, 15, 0, 8);
        formLayout.addView(tvLabel);

        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setTextSize(15);
        editText.setPadding(35, 30, 35, 30);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.WHITE);
        inputBg.setCornerRadius(16);
        inputBg.setStroke(2, Color.parseColor("#DDDDDD"));
        editText.setBackground(inputBg);

        formLayout.addView(editText);
        return editText;
    }

    private TextView createLabelValue(LinearLayout parent, String label, String value, boolean isMain) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, 18);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label.toUpperCase());
        tvLabel.setTextSize(10);
        tvLabel.setTextColor(Color.parseColor("#888888"));

        TextView tvVal = new TextView(this);
        tvVal.setText(value);
        tvVal.setTextSize(isMain ? 18 : 14);
        tvVal.setTypeface(Typeface.DEFAULT_BOLD);
        tvVal.setTextColor(Color.parseColor("#222222"));

        row.addView(tvLabel);
        row.addView(tvVal);
        parent.addView(row);

        return tvVal;
    }

    private void addBadge(LinearLayout parent, String text) {
        TextView badge = new TextView(this);
        badge.setText(text);
        badge.setTextSize(11);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(Color.parseColor("#004B93"));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E3F2FD"));
        bg.setCornerRadius(12);
        badge.setBackground(bg);
        badge.setPadding(20, 8, 20, 8);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 12, 0);
        badge.setLayoutParams(params);

        parent.addView(badge);
    }

    private void generateTicket() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dobStr = etDob.getText().toString().trim();
        String docNum = etDocNum.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || dobStr.isEmpty() || docNum.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate dob;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            dob = LocalDate.parse(dobStr, formatter);
        } catch (Exception e) {
            Toast.makeText(this, "Формат даты: ДД.ММ.ГГГГ (например: 12.04.1998)", Toast.LENGTH_LONG).show();
            return;
        }

        Passenger passenger = new Passenger(firstName, lastName, dob, docNum);
        DeutschlandTicket ticket = DeutschlandTicket.issueVrnTicket(passenger, YearMonth.now());

        tvPassengerName.setText(passenger.getFullName());
        tvValidMonth.setText(ticket.validMonth().toString());
        tvTicketId.setText(ticket.ticketId());
        tvDob.setText(dobStr);
        tvDocNum.setText(docNum);

        // Генерация настоящего QR-кода
        Bitmap qrBitmap = generateQrCodeBitmap(ticket.toQrPayload(), 600, 600);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
        }

        ((View) formLayout.getParent()).setVisibility(View.GONE);
        ticketScrollView.setVisibility(View.VISIBLE);
    }

    private Bitmap generateQrCodeBitmap(String text, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace塑造();
            return null;
        }
    }

    private void spinQrCode(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
}
