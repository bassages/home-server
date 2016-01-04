'use strict';

angular.module('appHomecontrol.sharedDataService', [])

    .service('SharedDataService', function() {
        var soortData = 'verbruik'; // Default value

        this.getSoortData = function() {
            return soortData;
        };

        this.setSoortData = function(aSoortData) {
            soortData = aSoortData;
        };
    });
