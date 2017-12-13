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

    $('#myNavbar a').on('click', function(){
        $('.navbar-toggle').click()
    });

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
        updateExternalMessages();
    });

    $('#patternName').change(function() {
        checkSubmit();
    });

    function checkSubmit() {
        if ($("input#autoSubmit")[0].checked) {
           submitPattern();
        }
    }

    $('#patternSubmit').click(function() {
        submitPattern();
    });

    function submitPattern() {
        var patternId = Number($('#patternName').val().split('-')[0]);
        var patternName = $('#patternName').val().split('-')[1];
        if (patternName == "Starfield") {
            r.setValue(255);
            g.setValue(255);
            b.setValue(255);
            speed.setValue(10);
            intensity.setValue(255);
        }
        var rValue = r.getValue();
        var gValue = g.getValue();
        var bValue = b.getValue();
        var speedValue = speed.getValue();
        var intensityValue = intensity.getValue();
        var patternSelect = {"id": patternId,
                             "red": rValue,
                             "green": gValue,
                             "blue": bValue,
                             "speed": speedValue,
                             "intensity": intensityValue};
        updateWellLight();
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
    };

    $('#ledOn').click(function() {
        $.ajax({
          url: "/ledPower/on",
          method: "POST",
		}).success (function (ledResult) {
		    if (ledResult.result == 1) {
                $('#ledOff').removeClass('active');
                $('#ledOn').addClass('active');
            }
            updateHeartbeat();
        });
    });
    $('#ledOff').click(function() {
        $.ajax({
          url: "/ledPower/off",
          method: "POST",
		}).success (function (ledResult) {
		    if (ledResult.result == 0) {
                $('#ledOff').addClass('active');
                $('#ledOn').removeClass('active');
            }
            updateHeartbeat();
        });
    });
    $('#wellLightOn').click(function() {
        $('#wellLightOff').removeClass('active');
        $('#wellLightOn').addClass('active');
        updateWellLight();
    });
    $('#wellLightOff').click(function() {
        $('#wellLightOff').addClass('active');
        $('#wellLightOn').removeClass('active');
        updateWellLight();
    });
    function updateWellLight() {
        var wellLevel = {"powerOn": $('#wellLightOn').hasClass('active'), "level": wellLightLevel.getValue()};
        $.ajax({
            type: "POST",
            url: '/wellLightSettings',
            data: JSON.stringify(wellLevel),
            contentType: "application/json"
        }).done(function(results) {
        });
    };
    $('#logInfo').click(function() {
        $.ajax({
          url: "/logLevel/INFO",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#logInfo').addClass('active');
            $('#logDebug').removeClass('active');
            $('#logWarn').removeClass('active');
        });
    });
    $('#logDebug').click(function() {
        $.ajax({
          url: "/logLevel/DEBUG",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#logInfo').removeClass('active');
            $('#logDebug').addClass('active');
            $('#logWarn').removeClass('active');
        });
    });
    $('#logWarn').click(function() {
        $.ajax({
          url: "/logLevel/WARN",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#logInfo').removeClass('active');
            $('#logDebug').removeClass('active');
            $('#logWarn').addClass('active');
        });
    });
    $('#dataLoopbackOn').click(function() {
        $.ajax({
          url: "/dataLoopback/ON",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#dataLoopbackOn').toggleClass('active');
            $('#dataLoopbackOff').toggleClass('active');
        });
    });
    $('#dataLoopbackOff').click(function() {
        $.ajax({
          url: "/dataLoopback/OFF",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#dataLoopbackOn').toggleClass('active');
            $('#dataLoopbackOff').toggleClass('active');
        });
    });
    $('#shutdownDetectOn').click(function() {
        $.ajax({
          url: "/shutdownDetect/ON",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#shutdownDetectOn').toggleClass('active');
            $('#shutdownDetectOff').toggleClass('active');
        });
    });
    $('#shutdownDetectOff').click(function() {
        $.ajax({
          url: "/shutdownDetect/OFF",
          method: "POST",
		}).success (function (loglevelResult) {
            $('#shutdownDetectOn').toggleClass('active');
            $('#shutdownDetectOff').toggleClass('active');
        });
    });
    var RGBChange = function() {
        $('#RGB').css('background', 'rgb('+r.getValue()+','+g.getValue()+','+b.getValue()+')')
    	$("#redSliderVal").text("Red (" + r.getValue() + ")");
    	$("#greenSliderVal").text("Green (" + g.getValue() + ")");
    	$("#blueSliderVal").text("Blue (" + b.getValue() + ")");
    	$("#speedSliderVal").text("Speed (" + speed.getValue() + ")");
    	$("#intensitySliderVal").text("Intensity (" + intensity.getValue() + ")");
    	$("#dimmingSliderVal").text("Dimming (" + wellLightLevel.getValue() + ")");
    };

    var r = $('#R').slider()
            .on('slide', RGBChange)
            .on('slideStop', checkSubmit)
            .data('slider');
    var g = $('#G').slider()
            .on('slide', RGBChange)
            .on('slideStop', checkSubmit)
            .data('slider');
    var b = $('#B').slider()
            .on('slide', RGBChange)
            .on('slideStop', checkSubmit)
            .data('slider');
    var speed = $('#speed').slider()
            .on('slide', RGBChange)
            .on('slideStop', checkSubmit)
            .data('slider');
    var intensity = $('#intensity').slider()
            .on('slide', RGBChange)
            .on('slideStop', checkSubmit)
            .data('slider');
    var wellLightLevel = $('#wellDimming').slider()
            .on('slide', RGBChange)
            .on('slideStop', checkSubmit)
            .data('slider');

    function updateHeartbeat() {
        $.ajax({
            url: '/heartbeat',
            cache: false
        }).success (function (heartbeat) {
            $('#pattern').html(heartbeat.patternName);
            $('#patternName').val(heartbeat.currentPattern + "-" + heartbeat.patternName);
            if (heartbeat.patternName != 'Off') {
                r.setValue(heartbeat.red);
                g.setValue(heartbeat.green);
                b.setValue(heartbeat.blue);
                speed.setValue(heartbeat.speed);
                intensity.setValue(heartbeat.intensity);
                RGBChange();
                $('#ledOff').removeClass('active');
                $('#ledOn').addClass('active');
            } else {
                $('#ledOn').removeClass('active');
                $('#ledOff').addClass('active');
            }
            var timestamp = moment(heartbeat.timestamp);
            $('#heartbeatTimestamp').html(timestamp.tz('America/Los_Angeles').format('YYYY-MM-DD h:mm a z'));
        });
    };

    function updateWellLightSettings() {
        $.ajax({
            url: '/wellLightSettings',
			cache: false
		}).success (function (heartbeat) {
            wellLightLevel.setValue(heartbeat.level);
            if (heartbeat.powerOn) {
                $('#wellLightOn').addClass('active');
                $('#wellLightOff').removeClass('active');
            } else {
                $('#wellLightOn').removeClass('active');
                $('#wellLightOff').addClass('active');
            }
        });
    };

    function updateExternalMessages() {
        $.ajax({
            url: '/externalMessages',
            cache: false
        }).done (function (externals) {
			$('tbody#externalMessages_table_body').empty();
			$.each(externals.messages, function(key, currentMessage) {
			    var stripped = currentMessage.payload.replace(/\\"/g, '"')
                stripped = stripped.slice(1, stripped.length - 1);
			    if (stripped[0] != '{') stripped = '{' + stripped + '}';
                var obj = JSON.parse(stripped);
                var str = JSON.stringify(obj, undefined, 2);
                var timestamp = moment(currentMessage.timeStamp).tz('America/Los_Angeles').format('YYYY-MM-DD h:mm:ss a z');
                $('#externalMessages_table_body').append(
                    '<tr>' +
                    '<td>' + timestamp + '</td>' +
                    '<td>' + currentMessage.channel + '</td>' +
                    '<td><pre>' + str + '</pre></td>' +
                    '</tr>'
				);
            });
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
            $('#startTime').html(timestamp.tz('America/Los_Angeles').format('YYYY-MM-DD h:mm a z'));
            var lumenessenceLogs = "Warn: " + statistics.concerning[0].warn + ", Error: " + statistics.concerning[0].error + ", Fatal: " + statistics.concerning[0].fatal;
            var serverLogs = "Warn: " + statistics.concerning[1].warn + ", Error: " + statistics.concerning[1].error + ", Fatal: " + statistics.concerning[1].fatal;
            var opcLogs = "Warn: " + statistics.concerning[2].warn + ", Error: " + statistics.concerning[2].error + ", Fatal: " + statistics.concerning[2].fatal;
            $('#serverLogs').html(serverLogs);
            $('#ledControllerLogs').html(lumenessenceLogs);
            $('#opcLogs').html(opcLogs);
		    var startTime = moment.tz(statistics.timing.startup, "UTC").tz("America/Los_Angeles").format('h:mm a z');
            $('#illuminationStartTime').html(startTime);
		    var endTime = moment.tz(statistics.timing.shutdown, "UTC").tz("America/Los_Angeles").format('h:mm a z');
            $('#illuminationStopTime').html(endTime);
            if (statistics.dataLoopback) {
                $('#dataLoopbackOn').addClass('active');
                $('#dataLoopbackOff').removeClass('active');
            } else {
                $('#dataLoopbackOn').removeClass('active');
                $('#dataLoopbackOff').addClass('active');
            }
            if (statistics.shutdownDetect) {
                $('#shutdownDetectOn').addClass('active');
                $('#shutdownDetectOff').removeClass('active');
            } else {
                $('#shutdownDetectOn').removeClass('active');
                $('#shutdownDetectOff').addClass('active');
            }
        });
		$.ajax({
			url: '/version/ledController',
			cache: false
		}).success (function (version) {
            $('#ledControllerVersion').html(version.versionId);
            $('#ledControllerBuildTime').html(version.buildTime);
            if (version.buildTime != '<Unknown>') {
                var versionTime = moment.tz(version.buildTime, "UTC").tz("America/Los_Angeles").format('YYYY-MM-DD h:mm a z');
                $('#ledControllerBuildTime').html(versionTime);
            }
        });
		$.ajax({
			url: '/version/server',
			cache: false
		}).success (function (version) {
		    var versionTime = moment.tz(version.builtAt, "UTC").tz("America/Los_Angeles").format('YYYY-MM-DD h:mm a z');
            $('#serverVersion').html(version.version);
            $('#serverBuildTime').html(versionTime);
        });
        updateHeartbeat();
        updateWellLightSettings();
    };

    function updateControl() {
        $.ajax({
			url: '/pattern/names',
			cache: false
		}).success (function (patternNames) {
            $('#patternName').empty();
            $.each(patternNames.names, function(key, name) {
                $('#patternName').append(
                    '<option value =\"' + name + '\">' + name.split('-')[1]  + '</option>'
                );
            });
        });
        updateHeartbeat();
        updateWellLightSettings();
    }

    function updateHostName() {
        $.ajax({
            url: '/hostName',
            cache: false
        }).success (function (currentHostName) {
            if (currentHostName.hostName.includes("apis")) {
                $('#bodyLightsForm').removeClass('hide');
                $('#poofersForm').removeClass('hide');
            }
            $('#siteTitle').text('Fabric - ' + currentHostName.hostName.charAt(0).toUpperCase() + currentHostName.hostName.slice(1, currentHostName.hostName.length));
        });
    }

    wellLightLevel.setValue(0);
    updateControl();
    updateHostName();

    $('#illuminationStartTime')[0].value = "00:00";
    $('#illuminationStopTime')[0].value = "00:00";

});