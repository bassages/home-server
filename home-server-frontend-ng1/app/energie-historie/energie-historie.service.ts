(function() {
    'use strict';

    angular
        .module('app')
        .service('EnergieHistorieService', EnergieHistorieService);

    EnergieHistorieService.$inject = ['$filter', 'BaseHistorieService', 'ApplicationSettingsService', '_'];

    function EnergieHistorieService($filter, BaseHistorieService, ApplicationSettingsService, _) {
        angular.extend(EnergieHistorieService.prototype, BaseHistorieService);

        this.getDefaultBarChartConfig = function(data) {
            var chartConfig: any = {};

            chartConfig.bindto = '#chart';

            chartConfig.data = {};
            chartConfig.data.type = 'bar';
            chartConfig.data.json = data;
            chartConfig.data.colors = this.getDataColors();

            chartConfig.data.order = function(data1, data2) { return data2.id.localeCompare(data1.id); };
            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 0.8}};

            chartConfig.transition = {duration: 0};
            chartConfig.padding = this.getChartPadding();

            chartConfig.grid = {y: {show: true}};

            return chartConfig;
        };

        this.getDataColors = function() {
            return {
                'stroom-verbruik-dal': '#4575b3',
                'stroom-verbruik-normaal': '#f4b649',
                'stroom-kosten-dal': '#4575b3',
                'stroom-kosten-normaal': '#f4b649',
                'stroom-verbruik': '#4575b3',
                'stroom-kosten': '#4575b3',
                'gas-verbruik': '#2ca02c',
                'gas-kosten': '#2ca02c'
            };
        };

        this.formatDateForLocationSearch = function(date) {
            return $filter('date')(date, "dd-MM-yyyy");
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

        this.getKeysGroups = function(energiesoorten, soort) {
            var keysGroups = [];
            for (var i = 0; i < energiesoorten.length; i++) {
                var energiesoort = energiesoorten[i];
                if (energiesoort === 'stroom' && ApplicationSettingsService.displayStroomDalNormaal === 'apart') {
                    keysGroups.push(energiesoort + '-' + soort + '-dal');
                    keysGroups.push(energiesoort + '-' + soort + '-normaal');
                } else {
                    keysGroups.push(energiesoort + '-' + soort);
                }
            }
            return keysGroups;
        };

        this.getSupportedSoorten = function() {
            return [{'code': 'verbruik', 'omschrijving': 'Verbruik'}, {'code': 'kosten', 'omschrijving': 'Kosten'}];
        };

        this.getVerbruikLabel = function(energiesoort) {
            if (energiesoort === 'stroom') {
                return 'kWh';
            } else if (energiesoort === 'gas') {
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
            if (soortData === 'verbruik') {
                return withoutUnitLabel + ' ' + this.getVerbruikLabel(energieSoorten[0]);
            } else if (soortData === 'kosten') {
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
            if (verbruiksoort === 'kosten') {
                return ['stroom', 'gas'];
            } else {
                return ['stroom'];
            }
        };

        this.toggleEnergiesoort = function(energiesoorten, energiesoortToToggle, allowMultpleEnergiesoorten) {
            if (allowMultpleEnergiesoorten) {
                const index = energiesoorten.indexOf(energiesoortToToggle);
                if (index >= 0) {
                    energiesoorten.splice(index, 1);
                } else {
                    energiesoorten.push(energiesoortToToggle);
                }
                return true;
            } else {
                if (energiesoorten[0] !== energiesoortToToggle) {
                    energiesoorten.splice(0, energiesoorten.length);
                    energiesoorten.push(energiesoortToToggle);
                    return true;
                }
            }
        };

        this.getTableData = function(data, energiesoorten, soort, labelFormatter, selectionKeyAttributeName) {
            var rows = [];
            var cols = [];

            if (energiesoorten.length === 0) {
                return {rows: rows, cols: cols};
            }

            for (var i = 0; i < data.length; i++) {
                var row: any = {};

                row[""] = labelFormatter(data[i]);
                row.selectionKey = data[i][selectionKeyAttributeName];

                var rowTotal = null;

                for (var j = 0; j < energiesoorten.length; j++) {
                    var energiesoort = energiesoorten[j];
                    var rowLabel = energiesoort.charAt(0).toUpperCase() + energiesoort.slice(1);
                    var value = data[i][energiesoort + '-' + soort];

                    var rowValue = '';
                    if (value !== null) {
                        rowValue = this.formatWithUnitLabel(soort, energiesoorten, value);

                        if (rowTotal === null) {
                            rowTotal = 0;
                        }
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
                transformedRow["stroom-kosten-dal"] = responseRow.stroomKostenDal;
                transformedRow["stroom-verbruik-dal"] = responseRow.stroomVerbruikDal;
                transformedRow["stroom-kosten-normaal"] = responseRow.stroomKostenNormaal;
                transformedRow["stroom-verbruik-normaal"] = responseRow.stroomVerbruikNormaal;

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

        function getTooltipLabelForKey(key) {
            if (_.endsWith(key, 'dal')) {
                return 'Stroom - Daltarief';
            } else if (_.endsWith(key, 'normaal')) {
                return 'Stroom - Normaaltarief';
            } else if (_.startsWith(key, 'stroom')) {
                return 'Stroom';
            } else if (_.startsWith(key, 'gas')) {
                return 'Gas';
            }
        }

        this.getTooltipContent = function(c3, data, defaultTitleFormat, defaultValueFormat, color, soort, energiesoorten) {
            var $$ = c3;
            var config = $$.config;
            var CLASS = $$.CLASS;
            var tooltipContents;
            var total = 0;

            data = _.sortBy(data, [function(o) { return o.name; }]);

            for (var i = 0; i < data.length; i++) {
                if (!(data[i] && (data[i].value || data[i].value === 0))) { continue; }

                if (!tooltipContents) {
                    var title = defaultTitleFormat(data[i].x);
                    tooltipContents = "<table class='" + $$.CLASS.tooltip + "'>" + "<tr><th colspan='2'>" + title + "</th></tr>";
                }

                var formattedLabel = getTooltipLabelForKey(data[i].name);
                var formattedValue = this.formatWithUnitLabel(soort, energiesoorten, data[i].value);
                var bgcolor = $$.levelColor ? $$.levelColor(data[i].value) : color(data[i].id);

                tooltipContents += "<tr class='" + CLASS.tooltipName + "-" + data[i].id + "'>";
                tooltipContents += "<td class='name'><span style='background-color:" + bgcolor + ";'></span>" + formattedLabel + "</td>";
                tooltipContents += "<td class='value'>" + formattedValue + "</td>";
                tooltipContents += "</tr>";

                total += data[i].value;
            }

            if (data.length > 1) {
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
