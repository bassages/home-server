(function() {
    'use strict';

    angular
        .module('app')
        .controller('KostenController', ['$scope', '$resource', '$log', 'LoadingIndicatorService', 'KostenService', KostenController]);

    function KostenController($scope, $resource, $log, LoadingIndicatorService, KostenService) {
        LoadingIndicatorService.startLoading();
        KostenService.query(function(data){
            $scope.kosten = data;
            LoadingIndicatorService.stopLoading();
        }, function(errorResponse){
            LoadingIndicatorService.stopLoading();
            handleServiceError('Ophalen van gegevens is niet gelukt.', errorResult);
        });

        $scope.startEdit = function(kosten) {
            $scope.item = angular.copy(kosten);
            $scope.detailsmode = 'edit';
            $scope.showDetails = true;
        };

        $scope.startAdd = function() {
            $scope.item = new KostenService({van: (new Date()).getTime(), gasPerKuub: null, stroomPerKwh: null, leverancier: ''});
            $scope.detailsmode = 'add';
            $scope.showDetails = true;
        };

        $scope.save = function() {
            LoadingIndicatorService.startLoading();

            $log.info('Save kosten: ' + JSON.stringify($scope.item));

            if ($scope.detailsmode == 'add') {
                $scope.item.$save(
                    function(successResult) {
                        $scope.item.id = successResult.id;
                        $scope.kosten.push($scope.item);
                        $scope.cancelEdit();
                        LoadingIndicatorService.stopLoading();
                    },
                    function(errorResult) {
                        LoadingIndicatorService.stopLoading();
                        $scope.cancelEdit();
                        handleServiceError('Opslaan is niet gelukt.', errorResult);
                    }
                );
            } else if ($scope.detailsmode == 'edit') {
                $scope.item.$save(
                    function(successResult) {
                        var index = getIndexOfItemWithId($scope.item.id, $scope.kosten);
                        angular.copy($scope.item, $scope.kosten[index]);
                        $scope.cancelEdit();
                        LoadingIndicatorService.stopLoading();
                    },
                    function(errorResult) {
                        LoadingIndicatorService.stopLoading();
                        $scope.cancelEdit();
                        handleServiceError('Opslaan is niet gelukt.', errorResult);
                    }
                );
            } else {
                handleTechnicalError('Onverwachte waarde voor attribuut detailsmode: ' + $scope.detailsmode);
            }
        };

        $scope.cancelEdit = function() {
            $scope.selectedItem = null;
            $scope.showDetails = false;
        };

        $scope.delete = function() {
            LoadingIndicatorService.startLoading();

            $log.info('Delete kosten: ' + JSON.stringify($scope.item));

            var index = getIndexOfItemWithId($scope.item.id, $scope.kosten);

            KostenService.delete({id: $scope.item.id},
                function(successResult) {
                    $scope.kosten.splice(index, 1);
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Verwijderen is niet gelukt.', errorResult);
                }
            );
            $scope.cancelEdit();
        };

        function getIndexOfItemWithId(id, items) {
            var result = null;
            for (var i = 0; i < items.length; i++) {
                if (items[i].id == id) {
                    result = i;
                }
            }
            return result;
        }

        function handleServiceError(message, errorResult) {
            $log.error(message + ' Cause=' + JSON.stringify(errorResult));
            alert(message);
        }

        function handleTechnicalError(details) {
            var message = 'Er is een onverwachte fout opgetreden.';
            $log.error(message + ' ' + details);
            alert(message);
        }
    }
})();
