/*

    Copyright (C) 2016 Mauricio Bustos (m@bustos.org)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

$(document).ready(function() {

    var http = location.protocol;
    var slashes = http.concat("//");
    var host = slashes.concat(window.location.host);

    $('.dynamicsparkline').sparkline.defaults.common.lineColor = 'black';
    $('.dynamicsparkline').sparkline.defaults.common.height = '30px';
    $('.dynamicsparkline').sparkline.defaults.common.width = '300px';
    $('.dynamicsparkline').sparkline.defaults.common.chartRangeMin = '0.0';
    $('.dynamicsparkline').sparkline.defaults.common.chartRangeMax = '100.0';

    function updateMetrics() {
		$.ajax({
			url: '/hostStatistics',
			cache: false
		}).success (function (statistics) {
            $('#hostMemorySparkline.dynamicsparkline').sparkline(statistics.memoryHistory);
            $('#hostCpuSparkline.dynamicsparkline').sparkline(statistics.cpuHistory);
            var timestamp = moment(statistics.startTime);
            $('#startTime').html(timestamp.tz('America/Los_Angeles').format('yyyyMMdd h:mm a'));
		}).error (function (xhr, ajaxOptions, thrownError) {
            window.location.replace(host);
        });
        $.ajax({
			url: '/heartbeat',
			cache: false
		}).success (function (heartbeat) {
            $('#pattern').html(heartbeat.patternName);
            var timestamp = moment(heartbeat.timestamp);
            $('#heartbeatTimestamp').html(timestamp.tz('America/Los_Angeles').format('yyyyMMdd h:mm a'));
		}).error (function (xhr, ajaxOptions, thrownError) {
            window.location.replace(host);
        });
    }

    updateMetrics();

});