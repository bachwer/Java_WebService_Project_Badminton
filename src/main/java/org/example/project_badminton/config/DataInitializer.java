package org.example.project_badminton.config;

import lombok.RequiredArgsConstructor;
import org.example.project_badminton.entity.TimeSlot;
import org.example.project_badminton.repository.TimeSlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    public void run(String... args) throws Exception {
        if (timeSlotRepository.count() == 0) {
            System.out.println("No TimeSlots found. Generating 48 slots (30 min each)...");
            LocalTime start = LocalTime.of(0, 0);
            for (int i = 0; i < 48; i++) {
                LocalTime end = start.plusMinutes(30);
                
                // For the last slot, 23:30 to 00:00 (next day) but LocalTime uses 00:00
                TimeSlot slot = TimeSlot.builder()
                        .startTime(start)
                        .endTime(end)
                        .isActive(true)
                        .build();
                timeSlotRepository.save(slot);
                start = end;
            }
            System.out.println("TimeSlots generation completed.");
        }
    }
}
