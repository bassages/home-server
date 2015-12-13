'use strict';

angular.module('appHomecontrol.d3LocalizationService', [])

    .service('D3LocalizationService', function() {

        var shortMonths = ["Jan", "Feb", "Maa", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"];

        this.getShortMonths = function() {
            return shortMonths;
        };

        this.localize = function() {
            var myFormatters = d3.locale({
                "decimal": ",",
                "thousands": ".",
                "grouping": [3],
                "currency": ["€", ""],
                "dateTime": "%a %b %e %X %Y",
                "date": "%d-%m-%Y",
                "time": "%H:%M:%S",
                "periods": ["AM", "PM"],
                "days": ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"],
                "shortDays": ["Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za"],
                "months": ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"],
                "shortMonths": shortMonths
            });
            d3.time.format = myFormatters.timeFormat;
            d3.format = myFormatters.numberFormat;
        };
    });
