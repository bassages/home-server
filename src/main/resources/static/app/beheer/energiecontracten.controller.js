(function() {
    'use strict';

    angular
        .module('app')
        .controller('EnergieContractenController', EnergieContractenController);

    EnergieContractenController.$inject = ['$log', 'EnergieContractenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function EnergieContractenController($log, EnergieContractenService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        activate();

        function activate() {
            LoadingIndicatorService.startLoading();

            vm.dateformat = 'EEEE dd-MM-yyyy';

            EnergieContractenService.query(
                function(data) {
                    vm.kosten = data;
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResponse) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Ophalen van gegevens is niet gelukt.', errorResponse);
                }
            );
        }

        vm.startEdit = function(energiecontract) {
            vm.item = angular.copy(energiecontract);
            vm.vanaf = new Date(energiecontract.van);
            vm.selectedId = vm.item.id;
            vm.detailsmode = 'edit';
            vm.showDetails = true;
        };

        vm.startAdd = function() {
            vm.item = new EnergieContractenService({van: Date.today().getTime(), gasPerKuub: null, stroomPerKwh: null, leverancier: ''});
            vm.vanaf = vm.item.van;
            vm.detailsmode = 'add';
            vm.showDetails = true;
        };

        vm.datepickerPopupOptions = {
        };

        vm.datepickerPopup = {
            opened: false
        };

        vm.toggleDatepickerPopup = function() {
            vm.datepickerPopup.opened = !vm.datepickerPopup.opened;
        };

        function saveAdd() {
            vm.item.$save(
                function(successResult) {
                    vm.item.id = successResult.id;
                    vm.kosten.push(vm.item);
                    vm.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );
        }

        function saveEdit() {
            vm.item.$save(
                function(successResult) {
                    var index = getIndexOfItemWithId(vm.item.id, vm.kosten);
                    angular.copy(vm.item, vm.kosten[index]);
                    vm.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );
        }

        vm.save = function() {
            LoadingIndicatorService.startLoading();

            vm.item.van = new Date(vm.vanaf).getTime();

            $log.info('Save kosten: ' + angular.toJson(vm.item));

            if (vm.detailsmode == 'add') {
                saveAdd();
            } else if (vm.detailsmode == 'edit') {
                saveEdit();
            } else {
                handleTechnicalError('Onverwachte waarde voor attribuut detailsmode: ' + vm.detailsmode);
            }
        };

        vm.cancelEdit = function() {
            vm.selectedId = null;
            vm.showDetails = false;
        };

        vm.delete = function() {
            LoadingIndicatorService.startLoading();

            $log.info('Delete energiecontract: ' + angular.toJson(vm.item));

            var index = getIndexOfItemWithId(vm.item.id, vm.kosten);

            EnergieContractenService.delete({id: vm.item.id},
                function(successResult) {
                    vm.kosten.splice(index, 1);
                    vm.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Verwijderen is niet gelukt.', errorResult);
                }
            );
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
            if (errorResult.data && errorResult.data.code == 'UNIQUE_KEY_CONSTRAINT_VIOLATION') {
                var userMessage = 'Er bestaat al een rij met dezelfde vanaf datum. Kies een andere datum a.u.b.';
                ErrorMessageService.showMessage(userMessage);
            } else {
                $log.error(message + ' Cause=' + angular.toJson(errorResult));
                ErrorMessageService.showMessage(message);
            }
        }

        function handleTechnicalError(details) {
            var message = 'Er is een onverwachte fout opgetreden.';
            $log.error(message + ' ' + details);
            ErrorMessageService.showMessage(message);
        }
    }
})();
