$(function () {
    $.getJSON("http://localhost:8080/api/games", function(data) {
        var dataGames = data;
        $(dataGames).each(function (i,game) {
            $("#games-list").append("<li>" + getDate(game) + getPlayers(game) + "</li>");
        });
    });
});

function getDate(game) {
    let gameDate = "";
    gameDate += game.created.monthValue + "/" + game.created.dayOfMonth + "/" + game.created.year + ", ";
    gameDate += game.created.hour + ":" + game.created.minute + ":" + game.created.second + ": ";
    return gameDate;
}

function getPlayers(game) {
    let gamePlayers = "";
    gamePlayers += game.gamePlayers[0].player.userName + ", " + game.gamePlayers[1].player.userName;
    return gamePlayers;
}
