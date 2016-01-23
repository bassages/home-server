(function() {
    'use strict';

    angular
        .module('app')
        .service('LocalizationService', LocalizationService);

    function LocalizationService() {
        var shortMonths = ["Jan.", "Feb.", "Maa.", "Apr.", "Mei.", "Jun.", "Jul.", "Aug.", "Sep.", "Okt.", "Nov.", "Dec."];
        var shortDays = ["Zo.", "Ma.", "Di.", "Wo.", "Do.", "Vr.", "Za."];
        var fullMonths = ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"];

        this.getFullMonths = function() {
            return fullMonths;
        };

        this.getShortMonths = function() {
            return shortMonths;
        };

        this.getShortDays = function() {
            return shortDays;
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
                "shortDays": shortDays,
                "months": fullMonths,
                "shortMonths": shortMonths
            });
            d3.time.format = myFormatters.timeFormat;
            d3.format = myFormatters.numberFormat;
        };
    }
})();
