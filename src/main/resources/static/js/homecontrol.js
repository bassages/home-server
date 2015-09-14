angular.module('homecontrol', [])
    .controller('dashboard', function ($scope, $http) {
        $http.get('rest/afvalinzameling/volgende/').success(function(data) {
            $scope.afvalinzamelingdatum = formatDate(new Date(parseFloat(data[0].datum)));

            var afvaltypes = [];
            for (var i=0; i<data.length; i++) {
                afvaltypes.push({
                    'type': data[i].omschrijving.toLowerCase(),
                    'omschrijving': getAfvalIconTitle(data[i].omschrijving.toLowerCase())});
            }
            $scope.afvaltypes = afvaltypes;
            console.log($scope.afvaltypes);
        })
    })

function formatDate(dateToFormat) {
    return weekday[dateToFormat.getDay()] + " " + pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}

function getAfvalIconTitle(afvalcode) {
    if (afvalcode == "rest") {
        return "Grijze container";
    } else if (afvalcode == "gft") {
        return "Groene container";
    } else if (afvalcode == "sallcon") {
        return "Papier, glas, blik, drankkarton";
    } else if (afvalcode == "plastic") {
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
