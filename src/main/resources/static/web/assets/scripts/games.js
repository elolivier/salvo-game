$(function () {
    $.getJSON("http://localhost:8080/api/games", function(dataGames) {
        if(dataGames.player != null) {
            $('#invalid-msg').addClass('hide');
            $('#logout-button').removeClass('hide');
            $('#new-game').removeClass('hide');
            $('#login-form').addClass('hide');
        }
        paintGames(dataGames);
    });
    $.getJSON("http://localhost:8080/api/leaderboard", function(players) {
        paintLeaderBoard(players);
    });
    $('#login-submit').click(login);
    $('#logout-button').click(logout);
    $('#new-player').click(newPlayer);
    $('#new-game').click(createGame);
});

function paintGames(dataGames) {
    $("#games-list").append('<thead><tr><th>Date</th><th>Players</th><th>Action</th></tr></thead>');
    $("#games-list").append('<tbody>');
    $(dataGames.games).each(function (i, game) {
        $("#games-list").append('<tr><td>' + getDate(game) + '<td>' + getPlayers(game) + "</td><td>" + getButton(game, dataGames.player) + "</td></tr>");
    });
    $('.join').click(joinGame);
    $('.play').click(playGame);
    $('.view').click(viewGame);
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
    })
    .columns.adjust();
}

function paintLeaderBoard(players) {
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
    })
    .columns.adjust();
}

function getDate(game) {
    let gameDate = "";
    gameDate += game.created.monthValue + "/" + game.created.dayOfMonth + "/" + game.created.year + " at ";
    gameDate += game.created.hour + ":" + game.created.minute + ":" + game.created.second;
    return gameDate;
}

function getPlayers(game) {
    let gamePlayers = "";
    if(game.gamePlayers.length == 1){
        gamePlayers += game.gamePlayers[0].player.userName + " <b>vs</b> " + '<span id="no-opponent">Waiting opponent</span>';
    }else {
        gamePlayers += game.gamePlayers[0].player.userName + " <b>vs</b> " + game.gamePlayers[1].player.userName;
    }
    return gamePlayers;
}

function getButton(game, player) {
    let gameButton = '';
    if(game.gamePlayers.length == 1 && player != null) {
        if(player.id != game.gamePlayers[0].player.playerId) {
                gameButton += '<button id="j' + game.game_id + player.id + '" type="button" class="btn btn-default btn-sm join">Join</button>';
        }else {
            gameButton += '<button id="p' + game.game_id + player.id + '" type="button" class="btn btn-default btn-sm play">Play</button>';
        }
    }else {
        if(player != null && (player.id == game.gamePlayers[0].player.playerId || player.id == game.gamePlayers[1].player.playerId)) {
                gameButton += '<button id="p' + game.game_id + player.id + '" type="button" class="btn btn-default btn-sm play">Play</button>';
        }else {
            gameButton += '<button id="v' + game.game_id + '" type="button" class="btn btn-default btn-sm view">View</button>';
        }
    }
    return gameButton;
}

function login(evt) {
  evt.preventDefault();
  let form = evt.target.form;
  let ids = ["login-user","login-pwd"];
  $.post("/api/login",
         { username: form["username"].value,
           password: form["password"].value })
  .done(function() {
    location.reload();
    clearFields(ids);
  })
  .fail(function(xhr, status) {
    $('#invalid-msg').removeClass('hide');
    clearFields(ids);
    //$('#welcome-msg').html('Welcome back ' + form["username"].value).removeClass('hide');
  });
}

function logout(evt) {
  evt.preventDefault();
  $.post("/api/logout")
   .done(function() {
     //$('#welcome-msg').html('See you soon Viking!');
     $('#logout-button').addClass('hide');
     $('#new-game').addClass('hide');
     $('#login-form').removeClass('hide');
     location.reload();
   })
   .fail(function(xhr, status, error) {

   });
}

function newPlayer(evt) {
    evt.preventDefault();
    let form = evt.target.form;
    $.post("/api/players",
    { username: form["username"].value,
      password: form["password"].value,
      email: form["email"].value})
    .done(function(msg) {
        $('#myModal').modal('toggle');
        $.post("/api/login",
        { username: form["username"].value,
          password: form["password"].value
        })
        .done(function() {
            //$('#welcome-msg').html('Welcome to the battle ' + form["username"].value).removeClass('hide');
            $('#logout-button').removeClass('hide');
            $('#login-form').addClass('hide');
        });
        location.reload();
    })
    .fail(function(xhr, status) {
        $('.modal-title').html(status + ': ' + xhr.responseJSON.error).css('color','red');
    });
}

function clearFields(listOfIds) {
    $(listOfIds).each(function (i, id) {
        $('#' + id).val('');
    });
}

function joinGame() {
    let gidFe = this.id.charAt(1);
    let pidFe = this.id.charAt(2);
    $.post("/api/join",{ gid: gidFe, pid: pidFe })
    .done(function(xhr) {
        window.location.assign("http://localhost:8080/web/game.html?gp=" + xhr.GamePlayerId);
    })
    .fail(function(xhr, status) {

    });
}

function playGame() {
    let gidFe = this.id.charAt(1);
    let pidFe = this.id.charAt(2);
    $.post("/api/play",{ gid: gidFe, pid: pidFe })
    .done(function(xhr) {
        window.location.assign("http://localhost:8080/web/game.html?gp=" + xhr.GamePlayerId);
    })
    .fail(function(xhr, status) {

    });
}

function viewGame() {
    let gidFe = this.id.charAt(1);
    console.log(gidFe);
}

function createGame() {
    $.post("/api/new")
    .done(function(xhr) {
        window.location.assign("http://localhost:8080/web/game.html?gp=" + xhr.GamePlayerId);
    })
    .fail(function(xhr, status) {

    });
}

