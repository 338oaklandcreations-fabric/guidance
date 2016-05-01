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

    $('#controlNav').click(function() {
        $('#controlPage').removeClass('hide');
        $('#statusPage').addClass('hide');
        $('#controlNav').addClass('active');
        $('#statusNav').removeClass('active');
        updateControl();
    });

    $('#statusNav').click(function() {
        $('#controlPage').addClass('hide');
        $('#statusPage').removeClass('hide');
        $('#controlNav').removeClass('active');
        $('#statusNav').addClass('active');
        updateStatus();
    });

    $('#patternSubmit').click(function() {
        var patternName = Number($('#patternName').val().split(' ')[0]);
        var rValue = r.getValue();
        var gValue = g.getValue();
        var bValue = b.getValue();
        var speedValue = speed.getValue();
        var intensityValue = intensity.getValue();
        var patternSelect = {"id": patternName, "red": rValue, "green": gValue, "blue": bValue, "speed": speedValue, "intensity": intensityValue};
        $.ajax({
            type: "POST",
            url: '/pattern',
            data: JSON.stringify(patternSelect),
            contentType: "application/json"
        }).done(function(results) {
            var timestamp = moment(new Date());
            var timestampString = timestamp.tz('America/Los_Angeles').format('h:mm a');
            if (results.result == 0) {
                $('#submitResult').addClass('alert-success');
                $('#submitResult').removeClass('alert-danger');
                $('#submitResult').html('<strong>' + timestampString + ' (Pacific):</strong> Submission Successful');
            } else {
                $('#submitResult').removeClass('alert-success');
                $('#submitResult').addClass('alert-danger');
                $('#submitResult').html('<strong>' + timestampString + ' (Pacific):</strong> Submission Failed');
            }
            updateHeartbeat();
        });
    });

    $('#ledOn').click(function() {
        $.ajax({
          url: "/ledPower/on",
          method: "POST",
		}).success (function (ledResult) {
		    if (ledResult.result == 1) {
                $('#ledOff').removeClass('active');
                $('#ledOn').addClass('active');
            }
        });
    });
    $('#ledOff').click(function() {
        $('#ledOn').removeClass('active');
        $('#ledOff').addClass('active');
        $.ajax({
          url: "/ledPower/off",
          method: "POST",
		}).success (function (ledResult) {
		    if (ledResult.result == 1) {
                $('#ledOff').removeClass('active');
                $('#ledOn').addClass('active');
            }
        });
    });
    var RGBChange = function() {
        $('#RGB').css('background', 'rgb('+r.getValue()+','+g.getValue()+','+b.getValue()+')')
    };

    var r = $('#R').slider()
            .on('slide', RGBChange)
            .data('slider');
    var g = $('#G').slider()
            .on('slide', RGBChange)
            .data('slider');
    var b = $('#B').slider()
            .on('slide', RGBChange)
            .data('slider');
    var speed = $('#speed').slider().data('slider');
    var intensity = $('#intensity').slider().data('slider');

    function updateHeartbeat() {
        $.ajax({
			url: '/heartbeat',
			cache: false
		}).success (function (heartbeat) {
            $('#pattern').html(heartbeat.patternName);
            var timestamp = moment(heartbeat.timestamp);
            $('#heartbeatTimestamp').html(timestamp.tz('America/Los_Angeles').format('YYYY-MM-DD h:mm a'));
        });
    };

	function updateStatus() {
		$.ajax({
			url: '/hostStatistics',
			cache: false
		}).success (function (statistics) {
            $('#hostMemorySparkline.dynamicsparkline').sparkline(statistics.memoryHistory);
            $('#hostCpuSparkline.dynamicsparkline').sparkline(statistics.cpuHistory);
            var timestamp = moment(statistics.startTime);
            $('#startTime').html(timestamp.tz('America/Los_Angeles').format('YYYY-MM-DD h:mm a'));
            var lumenessenceLogs = "Warn: " + statistics.concerning[0].warn + ", Error: " + statistics.concerning[0].error + ", Fatal: " + statistics.concerning[0].fatal;
            var serverLogs = "Warn: " + statistics.concerning[1].warn + ", Error: " + statistics.concerning[1].error + ", Fatal: " + statistics.concerning[1].fatal;
            var opcLogs = "Warn: " + statistics.concerning[2].warn + ", Error: " + statistics.concerning[2].error + ", Fatal: " + statistics.concerning[2].fatal;
            $('#serverLogs').html(serverLogs);
            $('#ledControllerLogs').html(lumenessenceLogs);
            $('#opcLogs').html(opcLogs);
		}).error (function (xhr, ajaxOptions, thrownError) {
            window.location.replace(host);
        });
		$.ajax({
			url: '/version/ledController',
			cache: false
		}).success (function (version) {
            $('#ledControllerVersion').html(version.versionId);
            $('#ledControllerBuildTime').html(version.buildTime);
        });
		$.ajax({
			url: '/version/server',
			cache: false
		}).success (function (version) {
		    var versionTime = moment(version.builtAt).tz('America/Los_Angeles').format('YYYY-MM-DD h:mm a')
            $('#serverVersion').html(version.version);
            $('#serverBuildTime').html(versionTime);
        });
        updateHeartbeat();
    };

    function updateControl() {
        $.ajax({
			url: '/pattern/names',
			cache: false
		}).success (function (patternNames) {
            $.each(patternNames.names, function(key, name) {
                $('#patternName').append(
                    '<option value =\"' + name.split(' ')[0] + '\">' + name + '</option>'
                );
            });
        });
        updateHeartbeat();
    }

    updateControl();

});