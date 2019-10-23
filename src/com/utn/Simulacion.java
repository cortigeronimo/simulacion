package com.utn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Simulacion {

    private final String station = "Estados Unidos";
    private final String file = "recorridos-realizados-2019.csv";
    private final ChronoUnit unit = ChronoUnit.SECONDS;

    public void run() {
        List<String> checkInData = getValuesOfColumn(file, station);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<LocalDateTime> checkinLocalDateTimeList = checkInData.stream()
                .map(cd -> LocalDateTime.parse(cd, dateFormat))
                .collect(Collectors.toList());

        List<Long> timeInSeconds = checkinLocalDateTimeList.stream().map(checkInLocalDateTime -> {
            TemporalUnit temporalUnit = ChronoUnit.HOURS;
            LocalDateTime dateOfTheSameCheckin = checkInLocalDateTime.truncatedTo(temporalUnit);
            return Long.valueOf(dateOfTheSameCheckin.until(checkInLocalDateTime, unit));
        }).collect(Collectors.toList());

        timeInSeconds.forEach(System.out::println);
    }

    private List<String> getValuesOfColumn(final String file, final String column) {
        List<String> values = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            System.out.println("This are the columns:");
            System.out.println(line);
            while ((line = br.readLine()) != null) {
                String[] columnValues = line.split(",");
                final String destinationStation = columnValues[3];
                if(destinationStation.equals(column)){
                    final String checkIn = columnValues[1];
                    values.add(checkIn);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }
}
