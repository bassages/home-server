(function() {
    'use strict';

    angular
        .module('app')
        .service('GrafiekService', GrafiekService);

    function GrafiekService() {
        var MINIMUM_HEIGHT = 220;
        var MAXIMUM_HEIGHT = 475;

        numbro.culture('nl-NL');

        this.getDataColors = function() {
            return {
                'stroom-verbruik': '#4575B3',
                'stroom-kosten': '#4575B3',
                'gas-verbruik': '#BA2924',
                'gas-kosten': '#BA2924'
            };
        };

        this.getEnergiesoorten = function(soort) {
            if (soort == 'kosten') {
                return ['stroom', 'gas'];
            } else {
                return ['stroom'];
            }
        };

        this.getSupportedSoorten = function() {
            return [{'code': 'verbruik', 'omschrijving': 'Verbruik'}, {'code': 'kosten', 'omschrijving': 'Kosten'}];
        };

        this.getVerbruikLabel = function(energiesoort) {
            if (energiesoort == 'stroom') {
                return 'kWh'
            } else if (energiesoort == 'gas') {
                return 'M\u00B3';
            } else {
                return '???';
            }
        };

        this.formatWithoutUnitLabel = function(soortData, value) {
            if (soortData == 'verbruik') {
                return numbro(value).format('0.000');
            } else if (soortData == 'kosten') {
                return numbro(value).format('0.00');
            }
        };

        this.formatWithUnitLabel = function(soortData, energieSoorten, value) {
            var withoutUnitLabel = this.formatWithoutUnitLabel(soortData, value);
            if (soortData == 'verbruik') {
                return withoutUnitLabel + ' ' + this.getVerbruikLabel(energieSoorten[0]);
            } else if (soortData == 'kosten') {
                return '\u20AC ' + withoutUnitLabel;
            }
        };

        this.getTableData = function(data, energiesoorten, soort, labelFormatter) {
            var rows = [];
            var cols = [];

            if (energiesoorten.length > 0) {

                for (var i = 0; i < data.length; i++) {
                    var row = {};

                    row[""] = labelFormatter(data[i]);

                    var rowTotal = null;

                    for (var j = 0; j < energiesoorten.length; j++) {
                        var energiesoort = energiesoorten[j];
                        var rowLabel = energiesoort.charAt(0).toUpperCase() + energiesoort.slice(1);
                        var value = data[i][energiesoort + '-' + soort];

                        var rowValue = '';
                        if (value != null) {
                            rowValue = this.formatWithUnitLabel(soort, energiesoorten, value);

                            if (rowTotal == null) { rowTotal = 0; }
                            rowTotal += value;
                        }
                        row[rowLabel] = rowValue;
                    }

                    if (energiesoorten.length > 1 && rowTotal != null) {
                        row["Totaal"] = this.formatWithUnitLabel(soort, energiesoorten, rowTotal);
                    }
                    rows.push(row);
                }
            }

            if (rows.length > 0) {
                cols = Object.keys(rows[0]);
            }

            return {rows: rows, cols: cols}
        };

        this.transformServerdata = function(serverresponses, key, energiesoorten, supportedsoorten) {
            var result = [];

            for (var i = 0; i < energiesoorten.length; i++) {
                var energiesoort = energiesoorten[i];
                var serverdataForEnergiesoort = serverresponses[i].data;

                for (var j = 0; j < serverdataForEnergiesoort.length; j++) {
                    var dataOnKey = this.getByKey(result, serverdataForEnergiesoort[j][key], key);

                    if (dataOnKey == null) {
                        dataOnKey = {};
                        result.push(dataOnKey);
                    }
                    dataOnKey[key] = serverdataForEnergiesoort[j][key];

                    for (var k = 0; k < supportedsoorten.length; k++) {
                        var soort = supportedsoorten[k].code;
                        dataOnKey[energiesoort + '-' + soort] = serverdataForEnergiesoort[j][soort];
                    }
                }
            }
            return result;
        };

        this.getByKey = function(data, keyValue, key) {
            for (var i = 0; i < data.length; i++) {
                if (data[i][key] == keyValue) {
                    return data[i];
                }
            }
            return null;
        };

        function setGraphHeightMatchingWithAvailableWindowHeight(chart) {
            var height = window.innerHeight - 115;

            if (height < MINIMUM_HEIGHT) {
                height = MINIMUM_HEIGHT;
            } else if (height > MAXIMUM_HEIGHT) {
                height = MAXIMUM_HEIGHT;
            }
            chart.resize({height: height});
        }

        this.setGraphHeightMatchingWithAvailableWindowHeight = function(chart) {
            setGraphHeightMatchingWithAvailableWindowHeight(chart);
        };

        this.manageGraphSize = function(theScope) {
            $(window).on("resize.doResize", function () {
                if (typeof theScope.chart !== 'undefined') {
                    setGraphHeightMatchingWithAvailableWindowHeight(theScope.chart);
                }
            });
            theScope.$on("$destroy", function () {
                $(window).off("resize.doResize"); //remove the handler added earlier
            });
        };
    }
})();
