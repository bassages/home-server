(function() {
    'use strict';

    angular
        .module('app')
        .service('EnergieHistorieService', EnergieHistorieService);

    EnergieHistorieService.$inject = ['BaseHistorieService'];

    function EnergieHistorieService(BaseHistorieService) {
        angular.extend(EnergieHistorieService.prototype, BaseHistorieService);

        this.getDataColors = function() {
            return {
                'stroom-verbruik': '#4575B3',
                'stroom-kosten': '#4575B3',
                'gas-verbruik': '#2ca02c',
                'gas-kosten': '#2ca02c'
            };
        };

        this.getChartPadding = function() {
            return {top: 10, bottom: 25, left: 55, right: 20};
        };

        this.getEmptyChartConfig = function() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: this.getChartPadding()
            };
        };

        this.getSupportedSoorten = function() {
            return [{'code': 'verbruik', 'omschrijving': 'Verbruik'}, {'code': 'kosten', 'omschrijving': 'Kosten'}];
        };

        this.getVerbruikLabel = function(energiesoort) {
            if (energiesoort == 'stroom') {
                return 'kWh';
            } else if (energiesoort == 'gas') {
                return 'm\u00B3';
            } else {
                return '?';
            }
        };

        this.formatWithoutUnitLabel = function(soortData, value) {
            return numbro(value).format('0.000');
        };

        this.formatWithUnitLabel = function(soortData, energieSoorten, value) {
            var withoutUnitLabel = this.formatWithoutUnitLabel(soortData, value);
            if (soortData == 'verbruik') {
                return withoutUnitLabel + ' ' + this.getVerbruikLabel(energieSoorten[0]);
            } else if (soortData == 'kosten') {
                return '\u20AC ' + withoutUnitLabel;
            }
        };

        this.getEnergieSoorten = function (locationSearch, verbruiksoort) {
            var result = null;
            if (locationSearch.energiesoort) {
                if (Array.isArray(locationSearch.energiesoort)) {
                    result = locationSearch.energiesoort;
                } else {
                    result = [locationSearch.energiesoort];
                }
            } else {
                result = this.getAllPossibleEnergiesoorten(verbruiksoort);
            }
            return result;
        };

        this.getAllPossibleEnergiesoorten = function(verbruiksoort) {
            if (verbruiksoort == 'kosten') {
                return ['stroom', 'gas'];
            } else {
                return ['stroom'];
            }
        };

        this.toggleEnergiesoort = function(energiesoorten, energiesoortToToggle, allowMultpleEnergiesoorten) {
            if (allowMultpleEnergiesoorten) {
                var index = energiesoorten.indexOf(energiesoortToToggle);
                if (index >= 0) {
                    energiesoorten.splice(index, 1);
                } else {
                    energiesoorten.push(energiesoortToToggle);
                }
                return true;
            } else {
                if (energiesoorten[0] != energiesoortToToggle) {
                    energiesoorten.splice(0, energiesoorten.length);
                    energiesoorten.push(energiesoortToToggle);
                    return true;
                }
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
                        if (value !== null) {
                            rowValue = this.formatWithUnitLabel(soort, energiesoorten, value);

                            if (rowTotal === null) { rowTotal = 0; }
                            rowTotal += value;
                        }
                        row[rowLabel] = rowValue;
                    }

                    if (energiesoorten.length > 1) {
                        if (rowTotal === null) {
                            row.Totaal = '';
                        } else {
                            row.Totaal = this.formatWithUnitLabel(soort, energiesoorten, rowTotal);
                        }
                    }
                    rows.push(row);
                }
            }

            if (rows.length > 0) {
                cols = Object.keys(rows[0]);
            }

            return {rows: rows, cols: cols};
        };

        this.transformServerdata = function(serverresponses, key) {
            var result = [];

            for (var i = 0; i < serverresponses.length; i++) {
                var responseRow = serverresponses[i];

                var transformedRow = {};
                transformedRow[key] = responseRow[key];

                transformedRow["gas-kosten"] = responseRow.gasKosten;
                transformedRow["gas-verbruik"] = responseRow.gasVerbruik;

                if (responseRow.stroomKostenDal !== null || responseRow.stroomKostenNormaal !== null) {
                    transformedRow["stroom-kosten"] = responseRow.stroomKostenDal + responseRow.stroomKostenNormaal;
                } else {
                    transformedRow["stroom-kosten"] = null;
                }

                if (responseRow.stroomVerbruikDal !== null || responseRow.stroomVerbruikNormaal !== null) {
                    transformedRow["stroom-verbruik"] = responseRow.stroomVerbruikDal + responseRow.stroomVerbruikNormaal;
                } else {
                    transformedRow["stroom-verbruik"] = null;
                }
                result.push(transformedRow);
            }
            return result;
        };

        this.getTooltipContent = function(c3, d, defaultTitleFormat, defaultValueFormat, color, soort, energiesoorten) {
            var $$ = c3;
            var config = $$.config;
            var CLASS = $$.CLASS;
            var tooltipContents;
            var total = 0;

            var orderAsc = false;
            if (config.data_groups.length === 0) {
                d.sort(function(a,b){
                    return orderAsc ? a.value - b.value : b.value - a.value;
                });
            } else {
                var ids = $$.orderTargets($$.data.targets).map(function (i) {
                    return i.id;
                });
                d.sort(function(a, b) {
                    if (a.value > 0 && b.value > 0) {
                        return orderAsc ? ids.indexOf(a.id) - ids.indexOf(b.id) : ids.indexOf(b.id) - ids.indexOf(a.id);
                    } else {
                        return orderAsc ? a.value - b.value : b.value - a.value;
                    }
                });
            }

            for (var i = 0; i < d.length; i++) {
                if (!(d[i] && (d[i].value || d[i].value === 0))) { continue; }

                if (!tooltipContents) {
                    var title = defaultTitleFormat(d[i].x);
                    tooltipContents = "<table class='" + $$.CLASS.tooltip + "'>" + "<tr><th colspan='2'>" + title + "</th></tr>";
                }

                var formattedName = (d[i].name.charAt(0).toUpperCase() + d[i].name.slice(1)).replace('-verbruik', '').replace('-kosten', '');
                var formattedValue = this.formatWithUnitLabel(soort, energiesoorten, d[i].value);
                var bgcolor = $$.levelColor ? $$.levelColor(d[i].value) : color(d[i].id);

                tooltipContents += "<tr class='" + CLASS.tooltipName + "-" + d[i].id + "'>";
                tooltipContents += "<td class='name'><span style='background-color:" + bgcolor + ";'></span>" + formattedName + "</td>";
                tooltipContents += "<td class='value'>" + formattedValue + "</td>";
                tooltipContents += "</tr>";

                total += d[i].value;
            }

            if (d.length > 1) {
                tooltipContents += "<tr class='" + CLASS.tooltipName + "'>";
                tooltipContents += "<td class='name'><strong>Totaal</strong></td>";
                tooltipContents += "<td class='value'><strong>" + this.formatWithUnitLabel(soort, energiesoorten, total) + "</strong></td>";
                tooltipContents += "</tr>";
            }
            tooltipContents += "</table>";

            return tooltipContents;
        };
    }
})();
