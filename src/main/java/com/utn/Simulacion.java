package com.utn;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        List<LocalTime> transportsTime = getLocalTimeValueOfStation(rows, 5, 2);

        List<Long> timeInSecondsDeparture = mapToTimeInSecondsLocalDateTime(departures);
        List<Long> timeInSecondsDepartureFromAnotherStation = mapToTimeInSecondsLocalDateTime(departuresFromAnotherStation);
        List<Long> timeInSecondsTransport = mapToTimeInSecondsLocalTime(transportsTime);

        writeInFile(timeInSecondsDeparture, "partidas.txt");
        writeInFile(timeInSecondsDepartureFromAnotherStation, "arribos.txt");
        writeInFile(timeInSecondsTransport, "tiempoDeViaje.txt");
    }

    private List<LocalTime> getLocalTimeValueOfStation(List<String[]> rows, int stationColumn, int valueColumn) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return rows.stream()
                .filter(row -> row[stationColumn].equals(station))
                .map(row -> row[valueColumn])
                .map(departure -> LocalTime.parse(departure, dateTimeFormat))
                .collect(Collectors.toList());
    }

    private List<Long> mapToTimeInSecondsLocalDateTime(List<LocalDateTime> localDateTimes) {
        TemporalUnit temporalUnit = ChronoUnit.HOURS;
        return localDateTimes.stream().map(localDateTime -> {
            LocalDateTime dateOfTheSameCheckin = localDateTime.truncatedTo(temporalUnit);
            return dateOfTheSameCheckin.until(localDateTime, unit);
        }).collect(Collectors.toList());
    }

    private List<Long> mapToTimeInSecondsLocalTime(List<LocalTime> localTimes) {
        TemporalUnit temporalUnit = ChronoUnit.HOURS;
        return localTimes.stream().map(localTime -> {
            LocalTime timeZero = localTime.truncatedTo(temporalUnit);
            return timeZero.until(localTime, unit);
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

    private void writeInFile(List<Long> values, String file){
        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new FileWriter(file));
            for (Long value : values){
                writer.write(String.valueOf(value));
                writer.newLine();
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error al escribir en el archivo");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error cerrando el archivo");
            }
        }
    }
}
