package com.paraparp.realestatestats.services;

import com.paraparp.realestatestats.model.entity.Data;
import com.paraparp.realestatestats.model.entity.Item;
import com.paraparp.realestatestats.model.entity.MainObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.paraparp.realestatestats.utils.Utils.convertToInt;

@Service
@RequiredArgsConstructor
public class IdealistaService {
    final String path = "https://www.idealista.com/ajax/listingcontroller/listingmapajaxgrouped.ajax?";
    final String urlRiazorRosales = path +
            "locationUri=a-coruna/riazor-los-rosales&typology=1&operation=1&zoom=5&northEast=57.84598237584738,+23.051198959350607&southWest=24.409244860154534,+-39.922433853149414";
    final String urlCiudadVieja = path +
            "locationUri=a-coruna/ciudad-vieja-centro&typology=1&operation=1&zoom=5&northEast=58.502772676210604,+13.732917785644556&southWest=23.257310155975194,+-30.52001190185546";
    final String urlTotalCoruna = path +
            "locationUri=a-coruna-a-coruna&typology=1&operation=1&zoom=5&northEast=57.84598237584738,+23.051198959350607&southWest=24.409244860154534,+-39.922433853149414";


    private final IdealistaExtractorService extractorService;

    public List<Map<String, Object>> getStatistics() {

        MainObject responseRosales = extractorService.getIdealistaStats(urlRiazorRosales);
        MainObject responseCiudadVieja = extractorService.getIdealistaStats(urlCiudadVieja);
        MainObject responseTotalCoruna = extractorService.getIdealistaStats(urlTotalCoruna);

        Map<String, Object> statsRosales = getSimplifyStats(responseRosales);
        Map<String, Object> statsCiudadVieja = getSimplifyStats(responseCiudadVieja);
        Map<String, Object> statsTotalCoruna = getSimplifyStats(responseTotalCoruna);

        return List.of(statsRosales, statsCiudadVieja, statsTotalCoruna);
    }

    public List<String[]> getStatisticsAsArray() {

        MainObject responseRosales = extractorService.getIdealistaStats(urlRiazorRosales);
        MainObject responseCiudadVieja = extractorService.getIdealistaStats(urlCiudadVieja);
        MainObject responseTotalCoruna = extractorService.getIdealistaStats(urlTotalCoruna);

        String[] statsRosales = getSimplifyStatsAsArray(responseRosales);
        String[] statsCiudadVieja = getSimplifyStatsAsArray(responseCiudadVieja);
        String[] statsTotalCoruna = getSimplifyStatsAsArray(responseTotalCoruna);

        return List.of(statsRosales, statsCiudadVieja, statsTotalCoruna);
    }

    public List<RealEstateData> getStatisticsAsObject() {

        MainObject responseRosales = extractorService.getIdealistaStats(urlRiazorRosales);
        MainObject responseCiudadVieja = extractorService.getIdealistaStats(urlCiudadVieja);
        MainObject responseTotalCoruna = extractorService.getIdealistaStats(urlTotalCoruna);

        RealEstateData statsRosales = getSimplifyStatsObject(responseRosales);
        RealEstateData statsCiudadVieja = getSimplifyStatsObject(responseCiudadVieja);
        RealEstateData statsTotalCoruna = getSimplifyStatsObject(responseTotalCoruna);

        return List.of(statsRosales, statsCiudadVieja, statsTotalCoruna);
    }
    private Map<String, Object> getSimplifyStats(MainObject body) {

        Data data = body.getData();
        List<Item> items = data.map.items;

        List<Integer> list0 = items.stream().map(item -> item.ads).flatMap(Collection::stream).map(ad -> (int) ad.price).sorted().collect(Collectors.toList());
        int st0 = getMedianaStatistics(list0);
        int st1_3 = getMedianaStatistics(list0.subList(0, list0.size() / 3));
        int st3_3 = getMedianaStatistics(list0.subList(2 * list0.size() / 3, list0.size()));


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("Date", LocalDate.now());
        response.put("Location", getLocation(data));
        response.put("Amount", getAmount(data));
        response.put("Type0", st0);
        response.put("Percentil13", st1_3);
        response.put("Percentil33", st3_3);
        response.put("Price_m2", getListingPriceByArea(data));
        return response;
    }
    private RealEstateData getSimplifyStatsObject(MainObject body) {

        Data data = body.getData();
        List<Item> items = data.map.items;

        List<Integer> sortedList = items.stream().map(item -> item.ads).flatMap(Collection::stream).map(ad -> (int) ad.price).sorted().collect(Collectors.toList());
        int st0 = getMedianaStatistics(sortedList);
        int st1_3 = getMedianaStatistics(sortedList.subList(0, sortedList.size() / 3));
        int st3_3 = getMedianaStatistics(sortedList.subList(2 * sortedList.size() / 3, sortedList.size()));

        return new RealEstateData(LocalDate.now(), getLocation(data), getAmount(data),st0,st1_3,st3_3,getListingPriceByArea(data));
    }
    private String[] getSimplifyStatsAsArray(MainObject body) {

        Data data = body.getData();
        List<Item> items = data.map.items;

        List<Integer> list0 = items.stream().map(item -> item.ads).flatMap(Collection::stream).map(ad -> (int) ad.price).sorted().collect(Collectors.toList());
        double st0 = getMedianaStatistics(list0);
        double st1_3 = getMedianaStatistics(list0.subList(0, list0.size() / 3));
        double st3_3 = getMedianaStatistics(list0.subList(2 * list0.size() / 3, list0.size()));

        return new String[]{
                LocalDate.now().toString(),
                getLocation(data),
                getAmount(data).toString(),
                String.valueOf(st0),
                String.valueOf(st1_3),
                String.valueOf(st3_3),
                String.valueOf(getListingPriceByArea(data))
        };
    }


    private static Integer getListingPriceByArea(Data data) {
        return convertToInt(data.listingPriceByArea.split(" ")[2]);
    }

    private  String getLocation(Data data) {

        String location = data.valueH1.replace(",", "-").split(" en ")[1].trim();

        LinkedList<String> collect = Arrays.stream(location.split("-")).map(String::trim).collect(Collectors.toCollection(LinkedList::new));
        String first = collect.removeLast();
        collect.addFirst(first);
        return String.join(" - ", collect);
    }
    private Integer getAmount(Data data) {
        return convertToInt(data.valueH1.split(" ")[0].trim());
    }


    private int getMedianaStatistics(List<Integer> list) {
        int n = list.size();
        double median;
        if (n % 2 == 0) {
            int m1 = list.get(n/2 - 1);
            int m2 = list.get(n/2);
            median = (m1 + m2) / 2.0;
        } else {
            median = list.get((n-1)/2);
        }

        return convertToInt(String.valueOf(median));
    }
}
