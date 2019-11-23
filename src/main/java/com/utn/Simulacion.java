package com.utn;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Simulacion {

    private final String station = "Pueyrredón";
    private final ChronoUnit timeUnit = ChronoUnit.SECONDS;

    public void run() {
        //generateResult("2019");
        generateResult("2018");
        //generateResult("2017");

        System.out.println("Programa Ejecutado de forma exitosa");
    }

    private void generateResult(String year) {
        generateResultSeasonFile(year, Season.SUMMER);
        generateResultSeasonFile(year, Season.WINTER);
        generateResultSeasonFile(year, Season.FALL);
        generateResultSeasonFile(year, Season.SPRING);
    }

    private void generateResultSeasonFile(String year, Season season) {
        final String folder = "resultados/";
        System.out.println("Leyendo Archivo csv");
        List<String[]> rows = getRows("recorridos-realizados-" + year + ".csv", season);
        System.out.println("Archivo leído");

        System.out.println("Generando muestra para intervalo entre llegadas al puesto");
        generateSample(rows, folder + "partidas-" + year + "-" + season + ".txt", 4, 2);
        System.out.println("Muestra Generada con éxito");

        System.out.println("Generando muestra para intervalo entre partidas desde otro puesto");
        generateSample(rows, folder + "arribos-" + year + "-" + season + ".txt", 11, 9);
        System.out.println("Muestra Generada con éxito");

        System.out.println("Generando muestra para tiempo de viaje");
        generateSampleForTravelTime(rows, folder + "tiempoDeViaje-" + year + season + ".txt");
        System.out.println("Muestra Generada con éxito");
    }

    private void generateSampleForTravelTime(List<String[]> rows, String file) {
        List<LocalTime> transportsTime = getLocalTimeValueOfStation(rows, 4, 8);
        List<Long> timeInSecondsTransport = mapToTimeInSecondsLocalTime(transportsTime);
        writeInFile(timeInSecondsTransport, file);
    }

    private void generateSample(List<String[]> rows, String fileToWrite, int stationRow, int targetRow) {
        List<LocalDateTime> departuresFromAnotherStation = getSampleByFilter(rows, row -> row[stationRow].equals(station) , targetRow);
        List<Long> timeInSecondsDepartureFromAnotherStation = mapToTimeInSecondsLocalDateTime(departuresFromAnotherStation);
        List<Long> elapsedTimeArrivals = calculateElapsedTime(timeInSecondsDepartureFromAnotherStation);
        writeInFile(elapsedTimeArrivals, fileToWrite);
    }


    private List<Long> calculateElapsedTime(List<Long> timeInSecondsDeparture) {
        List<Long> elapsedTimeDepartures = new ArrayList<>(timeInSecondsDeparture.size());
        if(timeInSecondsDeparture.isEmpty()) return new ArrayList<>();
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
                .map(departure -> {
                    String departureWithoutDay = departure.replace("0 days ", "");
                    String cleanDeparture = departureWithoutDay.substring(0, departureWithoutDay.indexOf('.'));
                    return  LocalTime.parse(cleanDeparture, dateTimeFormat);
                })
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Long> mapToTimeInSecondsLocalDateTime(List<LocalDateTime> localDateTimes) {
        TemporalUnit temporalUnit = ChronoUnit.DAYS;
        LocalDateTime initialDate = localDateTimes.stream()
                .findFirst()
                .map(l -> l.truncatedTo(temporalUnit))
                .orElse(null);
        if(initialDate == null) return new ArrayList<>();
        return localDateTimes.stream()
                .map(localDateTime -> initialDate.until(localDateTime, timeUnit))
                .collect(Collectors.toList());
    }

    private List<Long> mapToTimeInSecondsLocalTime(List<LocalTime> localTimes) {
        TemporalUnit temporalUnit = ChronoUnit.DAYS;
        if(localTimes.isEmpty()) return new ArrayList<>();
        LocalTime timeZero = localTimes.get(0).truncatedTo(temporalUnit);
        return localTimes.stream()
                .map(localTime -> timeZero.until(localTime, timeUnit))
                .collect(Collectors.toList());
    }

    private List<LocalDateTime> getSampleByFilter(List<String[]> rows, Predicate<String[]> filter, int targetColumn) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return rows.stream()
                .filter(filter)
                .map(row -> row[targetColumn])
                .map(departure -> LocalDateTime.parse(departure, dateTimeFormat))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String[]> getRows(final String file, Season season) {
        List<String[]> rows = new LinkedList<>();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            double numberOfLines = 0;
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columnValues = line.split(",");
                if(columnValues.length == 15){
                    try{
                        LocalDateTime departureLocalDateTime = LocalDateTime.parse(columnValues[2], dateTimeFormat);
                        LocalDateTime arrivalLocalDateTime = LocalDateTime.parse(columnValues[9], dateTimeFormat);
                        if(season.getMonths().contains(departureLocalDateTime.getMonthValue())
                                && season.getMonths().contains(arrivalLocalDateTime.getMonthValue())){
                            rows.add(columnValues);
                            numberOfLines++;
                        }
                    } catch (DateTimeParseException e){
                        System.out.println("Fila rota");
                        continue;
                    }
                }
                if(numberOfLines % 1000 == 0 && numberOfLines != 0){
                    System.out.println("Se leyeron " + numberOfLines + " lineas");
                }
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
