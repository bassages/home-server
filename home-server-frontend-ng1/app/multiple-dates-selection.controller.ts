(function() {
    'use strict';

    angular
        .module('app')
        .controller('MultipleDateSelectionController', MultipleDateSelectionController);

    MultipleDateSelectionController.$inject = ['$filter', '$uibModalInstance', 'datepickerOptions', 'selectedDates', 'selectedDateFormat'];

    function MultipleDateSelectionController($filter, $uibModalInstance, datepickerOptions, selectedDates, selectedDateFormat) {
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.removeDate = removeDate;
        vm.datepickerChange = datepickerChange;
        vm.formatSelectedDate = formatSelectedDate;

        vm.selectedDates = _.clone(selectedDates);
        vm.datepickerOptions = datepickerOptions;
        vm.datepickerOptions.customClass = getCustomDayClass;
        vm.selectedDate = null;

        function getCustomDayClass(data) {
            if (containsDate(vm.selectedDates, data.date)) {
                return 'datepicker-selected-date';
            } else {
                return 'datepicker-not-selected-date';
            }
        }

        function ok() {
            $uibModalInstance.close(vm.selectedDates);
        }

        function cancel() {
            $uibModalInstance.dismiss('cancel');
        }

        function datepickerChange() {
            if (_.isDate(vm.selectedDate)) {
                if (containsDate(vm.selectedDates, vm.selectedDate)) {
                    removeDate(vm.selectedDate);
                } else {
                    addDate(vm.selectedDate);
                }
                vm.selectedDate = null;
            }
        }
        function formatSelectedDate(selectedDate) {
            return $filter('date')(selectedDate, selectedDateFormat);
        }

        function addDate(dateToAdd) {
            vm.selectedDates.push(dateToAdd);
            vm.selectedDates.sort(function(a,b){return a.getTime() - b.getTime();});
        }

        function removeDate(dateToRemove) {
            _.remove(vm.selectedDates, function(date: Date) {
                return date.getTime() === dateToRemove.getTime();
            });
        }

        function containsDate(selectedDates, date) {
            return _.isDate(_.find(selectedDates, function (o) {
                return o.getDate() === date.getDate() && o.getMonth() === date.getMonth() && o.getFullYear() === date.getFullYear();
            }));
        }
    }

})();

