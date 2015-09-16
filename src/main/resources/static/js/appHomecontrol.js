angular.module('appHomecontrol', [])
    .controller('DashboardController', function ($scope, $http) {
        $http.get('rest/afvalinzameling/volgende/').success(function(data) {
            $scope.afvalinzamelingdatum = formatDate(new Date(data.datum));
            var afvaltypes = [];
            for (var i=0; i<data.afvalTypes.length; i++) {
                afvaltypes.push({
                    'type': data.afvalTypes[i],
                    'omschrijving': getAfvalIconTitel(data.afvalTypes[i])});
            }
            $scope.afvaltypes = afvaltypes;
        })
    });

function formatDate(dateToFormat) {
    return weekday[dateToFormat.getDay()] + " " + pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}

function getAfvalIconTitel(afvalcode) {
    if (afvalcode == "REST") {
        return "Grijze container";
    } else if (afvalcode == "GFT") {
        return "Groene container";
    } else if (afvalcode == "SALLCON") {
        return "Papier, glas, blik, drankkarton";
    } else if (afvalcode == "PLASTIC") {
        return "Oranje container";
    }
}

function pad2(number) {
    return (number < 10 ? '0' : '') + number;
}

var weekday = new Array(7);
weekday[0]=  "zondag";
weekday[1] = "maandag";
weekday[2] = "dinsdag";
weekday[3] = "woensdag";
weekday[4] = "donderdag";
weekday[5] = "vrijdag";
weekday[6] = "zaterdag";
