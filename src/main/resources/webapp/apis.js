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

function submit(url, data) {
    $.ajax({
      url: url,
      method: "POST",
      data: data,
      contentType: "application/json"
    }).success (function (ledResult) {
    });
}

$('#bodyOn').click(function() {
    submit("/bodyLights", JSON.stringify({"id": 1}));
    $('#bodyOn').addClass('active');
    $('#bodyOff').removeClass('active');
    $('#body1').removeClass('active');
    $('#body2').removeClass('active');
});

$('#bodyOff').click(function() {
    submit("/bodyLights", JSON.stringify({"id": 2}));
    $('#bodyOn').removeClass('active');
    $('#bodyOff').addClass('active');
    $('#body1').removeClass('active');
    $('#body2').removeClass('active');
});

$('#body1').click(function() {
    submit("/bodyLights", JSON.stringify({"id": 3}));
    $('#bodyOn').removeClass('active');
    $('#bodyOff').removeClass('active');
    $('#body1').addClass('active');
    $('#body2').removeClass('active');
});

$('#body2').click(function() {
    submit("/bodyLights", JSON.stringify({"id": 4}));
    $('#bodyOn').removeClass('active');
    $('#bodyOff').removeClass('active');
    $('#body1').removeClass('active');
    $('#body2').addClass('active');
});

$('#poofer1').click(function() {
    submit("/poofer", JSON.stringify({"id": 1}));
});

$('#poofer2').click(function() {
    submit("/poofer", JSON.stringify({"id": 2}));
});

$('#poofer3').click(function() {
    submit("/poofer", JSON.stringify({"id": 3}));
});

