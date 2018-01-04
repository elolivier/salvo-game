$(function () {
    //Filling Grids
    $("#grid-ships").append(drawGrid());
    $("#grid-salvos").append(drawGrid());
    $('table tr td:last-child').css('padding-left', '3px');
    $('table tr td:last-child').css('padding-right', '3px');

    //background column and row of cell hover
    $('td').on("mouseover", function() {
        let tableName = $(this).parent().parent().parent().attr('id');
        tableName = '#' + tableName + ' td';
        let indCol = $(this).index();
        let indRow = $(this).parent().index();
        $(tableName).each(function(i, eachTd) {
            if(($(eachTd).index() == indCol && $(eachTd).parent().index() <= indRow) ||
            ($(eachTd).index() <= indCol && $(eachTd).parent().index() == indRow)) {
                $(eachTd).addClass('hover');
            } else {
                $(eachTd).removeClass('hover');
            }
        });
    });
    $('table').on("mouseleave", function() {
        $('td').removeClass('hover');
    });

    //Getting GamePlayer parameter from URL
    let gpId = $.urlParam('gp');
    let loader = "http://localhost:8080/api/game_view/" + gpId;

    //Showing ships and info in HTML
    $.getJSON(loader, function(data) {
        let dataGame = data;
        let dataShips = dataGame.ships;
        $("#info-game").append(paintInfoPlayers(dataGame, gpId));
        paintShips(dataShips);
        paintSalvos(dataGame.salvos, gpId, dataShips);

    });
    $('#logout-button').click(logout);
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
    infoPlayers += yourName + ' (you) <b>vs</b> ' + opponentName + '</p>';
    return infoPlayers;
}

function drawGrid() {
    let grid = "";
    let rows = ["","A","B","C","D","E","F","G","H","I","J"];
    grid += "<tbody>";
    $(rows).each(function(i, rowName) {
        grid += "<tr>" + getRow(i, rowName) + "</tr>";
    });
    grid += "</tbody>";
    return grid;
}

function getRow(i, rowName) {
    let row = "";
    row += "<td>" + rowName + "</td>";
    for(var j = 1; j < 11; j++) {
        if(i == 0) {
            row += '<td>' + j + '</td>';
        } else {
            row += '<td class="' + rowName + j + '"></td>';
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
            let selectTable = "#grid-ships " + "."+cell;
            $(selectTable).css('background-color', 'blue');
        });
    });
}

function paintSalvos(dataSalvos, ownerId, dataShips) {
    for (const [turn, val] of Object.entries(dataSalvos)) {
        let owner = false;
        for (const [playerId, locations] of Object.entries(val)) {
            if (playerId == ownerId) {
                $(locations).each(function(i, cell) {
                    let selectTable = "#grid-salvos " + "."+cell;
                    $(selectTable).css('background-color', 'green');
                    $(selectTable).append(turn);
                });
            } else {
//                $(locations).each(function(i, cell) {
//                    let selectTable = "#grid-ships " + "."+cell;
//                    $(selectTable).css('background-color', 'green');
//                    $(selectTable).append(turn);
//                });
                hitShip(turn, locations, dataShips);
            }
        }
    }
}

function hitShip(turn, locations, dataShips) {
    $(dataShips).each(function(i, ship) {
        $(ship.locations).each(function(j, cellShip) {
            $(locations).each(function(k, cellSalvo) {
                if (cellShip == cellSalvo) {
                    let selectTable = "#grid-ships " + "."+cellShip;
                    $(selectTable).css('background-color', 'red');
                    $(selectTable).html(turn);
                }
            });
        });
    });
}

function logout(evt) {
   evt.preventDefault();
   $.post("/api/logout")
    .done(function() {
     window.location.assign("http://localhost:8080/web/games.html");
    });
 }