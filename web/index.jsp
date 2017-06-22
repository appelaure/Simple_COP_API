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
        <title>KB API Demo</title>

        <!-- LEAFLET -->
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.0.3/dist/leaflet.css"
              integrity="sha512-07I2e+7D8p6he1SIM+1twR5TIrhUQn9+I6yjqD53JQjFiMf8EtC93ty0/5vJTZGF8aAocvHYNEDJajGdNx1IsQ=="
              crossorigin=""/>
        <link rel="stylesheet" href="/css/MarkerCluster.css"/>
        <script src="https://unpkg.com/leaflet@1.0.3/dist/leaflet.js"
                integrity="sha512-A7vV8IFfih/D732iSSKi20u/ooOfj/AGehOKq0f4vLT1Zr2Y+RX7C+w8A1gaSasGtRUZpF/NZgzSAu4/Gc41Lg=="
                crossorigin=""></script>
        <script src="/js/leaflet.markercluster-src.js"></script>

        <!-- JQUERY -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

        <!-- BOOTSTRAP -->
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
              integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
              crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
              integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp"
              crossorigin="anonymous">

        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
                integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
                crossorigin="anonymous"></script>


        <!-- Date range  -->
        <script src="/js/moment.min.js"></script>
        <script src="/js/daterangepicker.js"></script>
        <link rel="stylesheet" href="/css/daterangepicker.css"/>


    </head>
    <style>

        body {
            padding-top: 50px;
        }

        .starter-template {
            padding: 40px 15px;
            text-align: center;
        }

        #map {
            height: 680px;
        }

        .navbar-brand {
            background: transparent url(/images/logo_inverse.png) no-repeat top left !important;
            background-size: cover;
            width: 150px;
        }
    </style>

    <body>
        <nav class="navbar navbar-inverse navbar-fixed-top">
            <div class="container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                            aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="#"></a>
                </div>
                <div id="navbar" class="collapse navbar-collapse">
                    <ul class="nav navbar-nav">
                        <li class="active"><a href="/dsfl.html">DSFL</a></li>
                        <li><a href="/cop.html">COP</a></li>
                    </ul>
                </div>
            </div>
        </nav>

        <div class="container">
            <div class="starter-template">
                <span id="count"></span>
                <div id="url"></div>
                <br>
                <div id="map"></div>
            </div>
        </div>
    </body>

    <script>

        var map;
        var geojson;
        var xhr;
        var markers;

        function getData() {
            var bounds = map.getBounds()._northEast.lng + "," + map.getBounds()._northEast.lat + "," + map.getBounds()._southWest.lng + "," + map.getBounds()._southWest.lat;

            if (xhr != null){
                xhr.abort();
            }

            //Display the url
            var html = "<h3>URL</h3><strong>JSON: </strong> http://localhost:8080/rest/api/dsfl?bbo=" + bounds+ "&itemsPerPage=500";
            html += "<br><strong>RSS: </strong> http://www.kb.dk/cop/syndication/images/luftfo/2011/maj/luftfoto/subject203?format=rss&bbo=" + bounds+ "&itemsPerPage=500";
            html += "<br><strong>KML: </strong> http://www.kb.dk/cop/syndication/images/luftfo/2011/maj/luftfoto/subject203?format=kml&bbo=" + bounds+ "&itemsPerPage=500";
            html += "<br><strong>MODS: </strong> http://www.kb.dk/cop/syndication/images/luftfo/2011/maj/luftfoto/subject203?format=mods&bbo=" + bounds+ "&itemsPerPage=500";
            $('#url').html(html);



            xhr = $.ajax({
                dataType: "json",
                url: "rest/api/dsfl?bbo=" + bounds + "&itemsPerPage=500",
              /*  beforeSend: function(){
                    if (geojson != null) {
                        markers.removeLayer(geojson);
                        geojson = null;
                    }
                    $('#count').html("");
                },*/
                success: function (data, textStatus, request) {
                    if (geojson != null) {
                        markers.removeLayer(geojson);
                        geojson = null;
                    }
                    $('#count').html("Result " + data.length + "/" + request.getResponseHeader('Pagination-Count'));

                    geojson = L.geoJson(data, {
                        onEachFeature: function (feature, layer) {
                            var popup = L.popup({
                                keepInView: "true",
                                minWidth: 200,
                            }).setLatLng([feature.geometry.coordinates[0], feature.geometry.coordinates[1]])
                                .setContent('<h3>' + feature.properties.name + '</h3><img src="' + feature.properties.thumbnail + '">');
                            layer.bindPopup(popup);
                        }
                    });

                    if (geojson != null) {
                        markers.addLayer(geojson);
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

            markers = L.markerClusterGroup();
            markers.addTo(map);

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
