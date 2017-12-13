$(function () {
    //Filling Grid
    $("#grid-table").append(drawGrid());

    //Getting GamePlayer parameter from URL
    let gpId = $.urlParam('gp');
    let loader = "http://localhost:8080/api/game_view/" + gpId;

    //Showing ships and info in HTML
    $.getJSON(loader, function(data) {
        let dataGame = data;
        let dataShips = dataGame.ships;

        paintShips(dataShips);

        $("#info-game").append(paintInfoPlayers(dataGame, gpId));
    });
});

function paintInfoPlayers(dataGame, gpId) {
    let yourName;
    let opponentName;
    let infoPlayers = '';
    infoPlayers += '<p>'
    $(dataGame.gamePlayers).each(function(i, gp) {
        if(gp.game_player_id == gpId) {
            yourName = gp.player.userName;
        } else {
            opponentName = gp.player.userName;
        }
    });
    infoPlayers += yourName + ' (you) vs. ' + opponentName + '</p>';
    return infoPlayers;
}

function drawGrid() {
    let grid = "";
    let rows = ["","A","B","C","D","E","F","G","H","I","J"];
    $(rows).each(function(i, rowName) {
        grid += "<tr>" + getRow(i, rowName) + "</tr>";
    });
    return grid;
}

function getRow(i, rowName) {
    let row = "";
    row += "<td>" + rowName + "</td>";
    for(var j = 1; j < 11; j++) {
        if(i == 0) {
            row += '<td>' + j + '</td>';
        } else {
            row += '<td id="' + rowName + j + '"></td>';
        }
    }
    return row;
}

$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^]*)').exec(window.location.href);
    if (results==null){
       return null;
    }
    else{
       return results[1] || 0;
    }
}

function paintShips(dataShips) {
    $(dataShips).each(function(i, ship) {
        let dataLocation = ship.locations;
        $(dataLocation).each(function(j, cell) {
            $('#' + cell).css('background-color', 'blue');
        });
    });
}