package org.example;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

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

        // Инициализация верстки по корректным ID
        qrCodeImage = findViewById(R.id.qrCodeImage);
        tvPassengerName = findViewById(R.id.tvPassengerName);
        tvPassengerDob = findViewById(R.id.tvPassengerDob);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        shimmerBar = findViewById(R.id.shimmerBar);
        securityBlock = findViewById(R.id.securityBlock);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Активная вкладка Fahrkarten (по центру)
        bottomNavigation.setSelectedItemId(R.id.nav_fahrkarten);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_fahrkarten || itemId == R.id.nav_fahrplan || itemId == R.id.nav_profil) {
                return true;
            }
            return false;
        });

        // Создаем данные билета с использованием ваших классов данных
        Passenger passenger = new Passenger("Max", "Mustermann", "01.01.1990");
        DeutschlandTicket ticket = new DeutschlandTicket("VRN-DT-89230492", passenger, "01.10.2026", "31.10.2026");

        // Отображение билета
        displayTicketData(ticket);

        // Анимация защитного блока
        startSecurityShimmerAnimation();
    }

    private void displayTicketData(DeutschlandTicket ticket) {
        tvPassengerName.setText(ticket.getPassengerName());
        tvPassengerDob.setText(ticket.getPassengerDob());
        tvValidityInfo.setText(ticket.getValidTo());
        tvTicketNumberInfo.setText(ticket.getTicketId());

        String barcodeData = String.format("VRN|%s|%s|%s", 
                ticket.getTicketId(), 
                ticket.getPassengerName(), 
                ticket.getValidTo());
        
        generateDenseAztecCode(barcodeData);
    }

    private void generateDenseAztecCode(String data) {
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
            e.printStackTrace();
        }
    }

    private void startSecurityShimmerAnimation() {
        securityBlock.post(() -> {
            int parentWidth = securityBlock.getWidth();
            int shimmerWidth = shimmerBar.getWidth();

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
        });
    }
}
