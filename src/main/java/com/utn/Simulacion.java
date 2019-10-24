package com.utn;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Simulacion {

    private final String station = "Pueyrredón";
    private final String file = "recorridos-realizados-2019.csv";
    private final ChronoUnit timeUnit = ChronoUnit.SECONDS;

    public void run() {
        System.out.println("Leyendo Archivo csv");
        List<String[]> rows = getRows(file);
        System.out.println("Archivo leído");

        System.out.println("Generando muestra para intervalo entre llegadas al puesto");
        generateSampleForArrivals(rows, "partidas.txt", 3);
        System.out.println("Muestra Generada con éxito");

        System.out.println("Generando muestra para intervalo entre partidas desde otro puesto");
        generateSampleForArrivals(rows, "arribos.txt", 5);
        System.out.println("Muestra Generada con éxito");

        //generateSampleForAtentionTime(rows);

        System.out.println("Programa Ejecutado de forma exitosa");
    }

    private void generateSampleForAtentionTime(List<String[]> rows) {
        List<LocalTime> transportsTime = getLocalTimeValueOfStation(rows, 5, 2);
        List<Long> timeInSecondsTransport = mapToTimeInSecondsLocalTime(transportsTime);
        writeInFile(timeInSecondsTransport, "tiempoDeViaje.txt");
    }

    private void generateSampleForArrivals(List<String[]> rows, String fileToWrite, int rowToCheck) {
        List<LocalDateTime> departuresFromAnotherStation = getSampleByFilter(rows, row -> row[rowToCheck].equals(station), 1);
        List<Long> timeInSecondsDepartureFromAnotherStation = mapToTimeInSecondsLocalDateTime(departuresFromAnotherStation);
        List<Long> elapsedTimeArrivals = calculateElapsedTime(timeInSecondsDepartureFromAnotherStation);
        writeInFile(elapsedTimeArrivals, fileToWrite);
    }


    private List<Long> calculateElapsedTime(List<Long> timeInSecondsDeparture) {
        List<Long> elapsedTimeDepartures = new ArrayList<>(timeInSecondsDeparture.size());
        elapsedTimeDepartures.add(timeInSecondsDeparture.get(0));
        for (int i = 1; i < timeInSecondsDeparture.size(); i++ ){
            elapsedTimeDepartures.add(timeInSecondsDeparture.get(i) - timeInSecondsDeparture.get(i - 1));
        }
        return elapsedTimeDepartures;
    }

    private List<LocalTime> getLocalTimeValueOfStation(List<String[]> rows, int stationColumn, int valueColumn) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return rows.stream()
                .filter(row -> row[stationColumn].equals(station))
                .map(row -> row[valueColumn])
                .map(departure -> LocalTime.parse(departure, dateTimeFormat))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Long> mapToTimeInSecondsLocalDateTime(List<LocalDateTime> localDateTimes) {
        TemporalUnit temporalUnit = ChronoUnit.DAYS;
        LocalDateTime initialDate = localDateTimes.get(0).truncatedTo(temporalUnit);
        return localDateTimes.stream().map(localDateTime -> initialDate.until(localDateTime, timeUnit))
                .collect(Collectors.toList());
    }

    private List<Long> mapToTimeInSecondsLocalTime(List<LocalTime> localTimes) {
        TemporalUnit temporalUnit = ChronoUnit.HOURS;
        return localTimes.stream().map(localTime -> {
            LocalTime timeZero = localTime.truncatedTo(temporalUnit);
            return timeZero.until(localTime, timeUnit);
        }).collect(Collectors.toList());
    }

    private List<LocalDateTime> getSampleByFilter(List<String[]> rows, Predicate<String[]> filter, int valueColumn) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return rows.stream()
                .filter(filter)
                .map(row -> row[valueColumn])
                .map(departure -> LocalDateTime.parse(departure, dateTimeFormat))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String[]> getRows(final String file) {
        List<String[]> rows = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine();
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
            throw new RuntimeException("Error al escribir en el archivo " + file);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error cerrando el archivo" + file);
            }
        }
    }
}
