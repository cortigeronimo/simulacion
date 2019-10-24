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

    private final String station = "Pueyrredón";
    private final String file = "recorridos-realizados-2019.csv";
    private final ChronoUnit unit = ChronoUnit.SECONDS;

    public void run() {
        List<String[]> rows = getRows(file);

        List<LocalDateTime> departures = getLocalDateTimeValueOfStation(rows, 3, 1);
        List<LocalDateTime> departuresFromAnotherStation = getLocalDateTimeValueOfStation(rows, 5, 1);

        List<Long> timeInSecondsDeparture = mapToTimeInSeconds(departures);
        List<Long> timeInSecondsDepartureFromAnotherStation = mapToTimeInSeconds(departuresFromAnotherStation);

        printSecondsTranscured(timeInSecondsDeparture, "Muestras para el retiro de bicicleta");
        System.out.println("");
        printSecondsTranscured(timeInSecondsDepartureFromAnotherStation, "Muestras para la partida de bicicletas desde otra estación");
    }

    private void printSecondsTranscured(List<Long> timeInSecondsDeparture, String messagePrePrint) {
        System.out.println("**************************************************");
        System.out.println("**************************************************");
        System.out.println("**************************************************");
        System.out.println(messagePrePrint);
        timeInSecondsDeparture.forEach(System.out::println);
        System.out.println("**************************************************");
        System.out.println("**************************************************");
        System.out.println("**************************************************");
    }

    private List<Long> mapToTimeInSeconds(List<LocalDateTime> localDateTimes) {
        TemporalUnit temporalUnit = ChronoUnit.HOURS;
        return localDateTimes.stream().map(localDateTime -> {
            LocalDateTime dateOfTheSameCheckin = localDateTime.truncatedTo(temporalUnit);
            return dateOfTheSameCheckin.until(localDateTime, unit);
        }).collect(Collectors.toList());
    }

    private List<LocalDateTime> getLocalDateTimeValueOfStation(List<String[]> rows, int stationColumn, int valueColumn) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return rows.stream()
                .filter(row -> row[stationColumn].equals(station))
                .map(row -> row[valueColumn])
                .map(departure -> LocalDateTime.parse(departure, dateTimeFormat))
                .collect(Collectors.toList());
    }

    private List<String[]> getRows(final String file) {
        List<String[]> rows = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            System.out.println("This are the columns:");
            System.out.println(line);
            while ((line = br.readLine()) != null) {
                String[] columnValues = line.split(",");
                rows.add(columnValues);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al abrir el archivo");
        }
        return rows;
    }
}
