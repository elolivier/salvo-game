$(function () {
    $.getJSON("http://localhost:8080/api/games", function(data) {
        var dataGames = data;
        $("#games-list").append('<thead><tr><th>Date</th><th>Players</th></thead>');
        $("#games-list").append('<tbody>');
        $(dataGames).each(function (i, game) {
            $("#games-list").append('<tr><td>' + getDate(game) + '<td>' + getPlayers(game) + "</td></tr>");
        });
        $("#games-list > thead > tr > th").addClass('text-center');
        $('#games-list').DataTable({
            "scrollY": "190px",
            "dom": "ft",
            "ordering": false,
            "order": [],
            "columnDefs": [
            { "width": "45%", "targets": 0 }
            ],
            "paging": false,
            "columnDefs": [
            { "orderable": false, "targets": 0 }
            ]
        });
    });
    $.getJSON("http://localhost:8080/api/leaderboard", function(data) {
        var players = data;
        $("#leader-board").append('<thead><tr><th>Name</th><th>Total</th><th>Won</th><th>Lost</th><th>Tied</th></tr></thead>');
        $("#leader-board").append('<tbody>');
        $(players).each(function (i, eachOne) {
            var eachPlayer = eachOne.scores;
            $("#leader-board").append('<tr><td>' + eachOne.player + '</td>' + '<td>' + eachPlayer.score + '</td>'
            + '<td>' + eachPlayer.won + '</td>' + '<td>' + eachPlayer.lost + '</td>' + '<td>' + eachPlayer.tied + '</td>');
        });
        $("#leader-board").append('</tbody>');
        $("#leader-board > tbody > tr > td:not(:first-child)").addClass('text-center');
        $("#leader-board > thead > tr > th:not(:first-child)").addClass('text-center');
        $('#leader-board').DataTable({
            "scrollY": "190px",
            "dom": "ft",
            "ordering": true,
            "order": [1, 'desc'],
            "columnDefs": [
            { "width": "40%", "targets": 0 }
            ],
            "paging": false,
            "columnDefs": [
            { "orderable": false, "targets": 0 }
            ]
        });
    });
});

function getDate(game) {
    let gameDate = "";
    gameDate += game.created.monthValue + "/" + game.created.dayOfMonth + "/" + game.created.year + " at ";
    gameDate += game.created.hour + ":" + game.created.minute + ":" + game.created.second;
    return gameDate;
}

function getPlayers(game) {
    let gamePlayers = "";
    gamePlayers += game.gamePlayers[0].player.userName + " <b>vs</b> " + game.gamePlayers[1].player.userName;
    return gamePlayers;
}
