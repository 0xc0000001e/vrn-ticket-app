package org.example;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageView qrCodeImage;
    private TextView tvPassengerName;
    private TextView tvPassengerDob;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private View shimmerBar;
    private View securityBlock;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Инициализация View компонентов
            qrCodeImage = findViewById(R.id.qrCodeImage);
            tvPassengerName = findViewById(R.id.tvPassengerName);
            tvPassengerDob = findViewById(R.id.tvPassengerDob);
            tvValidityInfo = findViewById(R.id.tvValidityInfo);
            tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
            shimmerBar = findViewById(R.id.shimmerBar);
            securityBlock = findViewById(R.id.securityBlock);
            bottomNavigation = findViewById(R.id.bottomNavigation);

            // Обработка нижнего меню
            if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_fahrkarten);
                bottomNavigation.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    return itemId == R.id.nav_fahrkarten || itemId == R.id.nav_fahrplan || itemId == R.id.nav_profil;
                });
            }

            // Инициализация объектов моделей с безопасной обработкой
            setupTicketData();

            // Безопасный запуск анимации
            startSecurityShimmerAnimation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    private void setupTicketData() {
        try {
            LocalDate birthDate = LocalDate.of(1990, 1, 1);
            Passenger passenger = new Passenger("Max", "Mustermann", birthDate, "max.mustermann@example.com");

            TicketStatus status = TicketStatus.values().length > 0 ? TicketStatus.values()[0] : null;
            String ticketId = "VRN-DT-89230492";
            YearMonth validityMonth = YearMonth.of(2026, 10);
            BigDecimal price = new BigDecimal("49.00");
            String cardNumber = "DE89370001";
            EnumSet<TransitType> transitTypes = EnumSet.allOf(TransitType.class);

            DeutschlandTicket ticket = new DeutschlandTicket(
                    ticketId,
                    passenger,
                    validityMonth,
                    price,
                    cardNumber,
                    status,
                    transitTypes
            );

            displayTicketData(ticketId, "Max Mustermann", birthDate, validityMonth);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ticket data", e);
        }
    }

    private void displayTicketData(String ticketId, String passengerName, LocalDate birthDate, YearMonth validityMonth) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        String formattedDob = birthDate.format(dateFormatter);
        LocalDate validUntilDate = validityMonth.atEndOfMonth();
        String formattedValidUntil = validUntilDate.format(dateFormatter);

        if (tvPassengerName != null) tvPassengerName.setText(passengerName);
        if (tvPassengerDob != null) tvPassengerDob.setText(formattedDob);
        if (tvValidityInfo != null) tvValidityInfo.setText(formattedValidUntil);
        if (tvTicketNumberInfo != null) tvTicketNumberInfo.setText(ticketId);

        String barcodeData = String.format("VRN|%s|%s|%s",
                ticketId,
                passengerName,
                formattedValidUntil);

        generateDenseAztecCode(barcodeData);
    }

    private void generateDenseAztecCode(String data) {
        if (qrCodeImage == null) return;

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.AZTEC, 600, 600, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qrCodeImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error generating Aztec code", e);
        }
    }

    private void startSecurityShimmerAnimation() {
        if (securityBlock == null || shimmerBar == null) return;

        securityBlock.post(() -> {
            try {
                int parentWidth = securityBlock.getWidth();
                int shimmerWidth = shimmerBar.getWidth();

                if (parentWidth <= 0) parentWidth = 1000;
                if (shimmerWidth <= 0) shimmerWidth = 200;

                ObjectAnimator animator = ObjectAnimator.ofFloat(
                        shimmerBar,
                        "translationX",
                        -shimmerWidth,
                        parentWidth
                );
                animator.setDuration(2000);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.start();
            } catch (Exception e) {
                Log.e(TAG, "Error starting animation", e);
            }
        });
    }
}
