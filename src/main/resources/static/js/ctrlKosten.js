'use strict';

angular.module('appHomecontrol.kostenService', [])

    .factory('Kosten', function($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    });

angular.module('appHomecontrol.kostenController', [])

    .controller('KostenController', ['$scope', '$resource', '$log', 'Kosten', function($scope, $resource, $log, Kosten) {
        $scope.kosten = Kosten.query(function() {});

        $scope.startEdit = function(kosten) {
            $scope.selectedItem = kosten;
            $scope.item = angular.copy(kosten);
            $scope.detailsmode = 'edit';
            $scope.showDetails = true;
        };

        $scope.startAdd = function() {
            $scope.selectedItem = null;
            $scope.item = {van: (new Date()).getTime(), totEnMet: null, gasPerKuub: null, stroomPerkWh: null, omschrijving: ''};
            $scope.detailsmode = 'add';
            $scope.showDetails = true;
        };

        $scope.save = function() {
            if ($scope.detailsmode == 'add') {
                $scope.kosten.push($scope.item);
            } else {
                angular.copy($scope.item, $scope.selectedItem);
            }
            $scope.cancelEdit();
        };

        $scope.cancelEdit = function() {
            $scope.selectedItem = null;
            $scope.showDetails = false;
        };

        $scope.delete = function() {
            for (var i = 0; i < $scope.kosten.length; i++) {
                if ($scope.kosten[i].id == $scope.selectedItem.id) {
                    $scope.kosten.splice(i, 1);
                    break;
                }
            }
            $scope.cancelEdit();
        }

    }]);
