<%--
  Created by IntelliJ IDEA.
  User: laap
  Date: 03-05-2017
  Time: 13:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>KB API Simple example</title>
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.0.3/dist/leaflet.css"
              integrity="sha512-07I2e+7D8p6he1SIM+1twR5TIrhUQn9+I6yjqD53JQjFiMf8EtC93ty0/5vJTZGF8aAocvHYNEDJajGdNx1IsQ=="
              crossorigin=""/>
        <link rel="stylesheet" href="/markerCluster/MarkerCluster.css"/>
        <script src="https://unpkg.com/leaflet@1.0.3/dist/leaflet.js"
                integrity="sha512-A7vV8IFfih/D732iSSKi20u/ooOfj/AGehOKq0f4vLT1Zr2Y+RX7C+w8A1gaSasGtRUZpF/NZgzSAu4/Gc41Lg=="
                crossorigin=""></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
        <script src="/markerCluster/leaflet.markercluster-src.js"></script>


    </head>
    <style>
        body, html, #map {
            height: 100%;
        }
    </style>

    <body>
        <h1>API Demo</h1>
        <div id="map"></div>
    </body>

    <script>

        var map;
        var geojson;

        function getData() {

            var bounds = map.getBounds()._northEast.lng + "," + map.getBounds()._northEast.lat + "," + map.getBounds()._southWest.lng + "," + map.getBounds()._southWest.lat;

            $.ajax({
                dataType: "json",
                data: "bbo=" + bounds + "&limit=" + 80 + "&page=" + 8,
                beforeSend: function () {
                    if (geojson != null) {
                        geojson.removeFrom(map);
                    }

                },
                url: "/rest/api/dsfl",
                success: function (data) {
                    geojson = L.geoJson(data, {
                        onEachFeature: function (feature, layer) {
                            var popup = L.popup({
                                keepInView: "true",
                                minWidth: 200,
                            }).setLatLng([feature.geometry.coordinates[0], feature.geometry.coordinates[1]])
                                .setContent('<h3>' + feature.properties.name + '</h3><img src="' + feature.properties.thumbnail + '">')


                            layer.bindPopup(popup);
                        }
                    });

                    if (geojson != null) {
                        var markers = L.markerClusterGroup();
                        markers.addLayer(geojson);
                        markers.addTo(map);
                    }
                }
            });
        }

        $(document).ready(function () {

            map = L.map('map').setView([55.48, 10.4], 10);

            var OpenStreetMap_BlackAndWhite = L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
                maxZoom: 18,
                attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            }).addTo(map);

            map.on('moveend', function () {
                getData();
            });

            map.on('zoomend', function () {
                getData();
            });

            getData();
        });

    </script>

</html>
