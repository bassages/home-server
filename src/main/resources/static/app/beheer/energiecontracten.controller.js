(function() {
    'use strict';

    angular
        .module('app')
        .controller('EnergieContractenController', EnergieContractenController);

    EnergieContractenController.$inject = ['$log', 'EnergieContractenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function EnergieContractenController($log, EnergieContractenService, LoadingIndicatorService, ErrorMessageService) {
        var vm = this;

        vm.startEdit = startEdit;
        vm.startAdd = startAdd;
        vm.toggleDatepickerPopup = toggleDatepickerPopup;
        vm.save = save;
        vm.cancelEdit = cancelEdit;
        vm.remove = remove;

        vm.dateformat = 'EEEE dd-MM-yyyy';
        vm.datepickerPopupOptions = { };
        vm.datepickerPopup = { opened: false };

        vm.tariffPattern = /^[0-9]{1}\,[0-9]{2}[0-9]{0,4}$/;
        vm.tariffPlaceholder = '0,000000';

        activate();

        function activate() {
            LoadingIndicatorService.startLoading();

            EnergieContractenService.query(
                function(data) {
                    vm.energieContracten = data;
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResponse) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Ophalen van gegevens is niet gelukt.', errorResponse);
                }
            );
        }

        function startAdd() {
            vm.energieContract = new EnergieContractenService({van: Date.today().getTime(), gasPerKuub: null, stroomPerKwh: null, leverancier: ''});
            vm.vanaf = vm.energieContract.van;
            vm.selectedId = null;
            vm.detailsmode = 'add';
            vm.showDetails = true;
        }

        function startEdit(energiecontract) {
            vm.energieContract = angular.copy(energiecontract);
            vm.vanaf = new Date(energiecontract.van);
            vm.selectedId = vm.energieContract.id;
            vm.detailsmode = 'edit';
            vm.showDetails = true;
        }

        function toggleDatepickerPopup() {
            vm.datepickerPopup.opened = !vm.datepickerPopup.opened;
        }

        function saveAdd() {
            vm.energieContract.$save(
                function(successResult) {
                    vm.energieContract.id = successResult.id;
                    vm.energieContracten.push(vm.energieContract);
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
            vm.energieContract.$save(
                function(successResult) {
                    var indexOfEnergieContractToEdit = _.findIndex(vm.energieContracten, {id: vm.energieContract.id});
                    angular.copy(vm.energieContract, vm.energieContracten[indexOfEnergieContractToEdit]);
                    vm.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );
        }

        function save() {
            LoadingIndicatorService.startLoading();

            vm.energieContract.van = new Date(vm.vanaf).getTime();

            $log.info('Save energieContract: ' + angular.toJson(vm.energieContract));

            if (vm.detailsmode === 'add') {
                saveAdd();
            } else if (vm.detailsmode === 'edit') {
                saveEdit();
            } else {
                handleTechnicalError('Onverwachte waarde voor attribuut detailsmode: ' + vm.detailsmode);
            }
        }

        function cancelEdit() {
            vm.selectedId = null;
            vm.showDetails = false;
        }

        function remove() {
            LoadingIndicatorService.startLoading();

            $log.info('Delete energiecontract: ' + angular.toJson(vm.energieContract));

            var indexOfEnergieContractToDelete = _.findIndex(vm.energieContracten, {id: vm.energieContract.id});

            EnergieContractenService.delete({id: vm.energieContract.id},
                function(successResult) {
                    vm.energieContracten.splice(indexOfEnergieContractToDelete, 1);
                    vm.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Verwijderen is niet gelukt.', errorResult);
                }
            );
        }

        function handleServiceError(message, errorResult) {
            if (errorResult.data && errorResult.data.code === 'UNIQUE_KEY_CONSTRAINT_VIOLATION') {
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
