'use strict';

angular.module('appHomecontrol.afvalController', [])

    .controller('AfvalController', ['$scope', '$http', function($scope, $http) {
        //$http.get('rest/afvalinzameling/volgende/').success(function(data) {
        //    if (data) {
        //        var afvaltypes = [];
        //        for (var i=0; i<data.afvalTypes.length; i++) {
        //            afvaltypes.push({
        //                'type': data.afvalTypes[i],
        //                'omschrijving': getAfvalIconTitel(data.afvalTypes[i])});
        //        }
        //        $scope.afvalinzamelingdatum = formatDateWithdayname(new Date(data.datum));
        //        $scope.separator = ": ";
        //        $scope.afvaltypes = afvaltypes;
        //    } else {
        //        $scope.separator = "Kon niet worden bepaald door een technische fout";
        //    }
        //})
    }]);

function getAfvalIconTitel(afvalcode) {
    if (afvalcode == "REST") {
        return "Grijze container";
    } else if (afvalcode == "GFT") {
        return "Groene container";
    } else if (afvalcode == "SALLCON") {
        return "Papier, glas";
    } else if (afvalcode == "PLASTIC") {
        return "Oranje container (PMD)";
    }
}

// Returns a formatted date. Example: zondag 21-10-2015
function formatDateWithdayname(dateToFormat) {
    return weekday[dateToFormat.getDay()] + " " + pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}

var weekday = new Array(7);
weekday[0]=  "zondag";
weekday[1] = "maandag";
weekday[2] = "dinsdag";
weekday[3] = "woensdag";
weekday[4] = "donderdag";
weekday[5] = "vrijdag";
weekday[6] = "zaterdag";
