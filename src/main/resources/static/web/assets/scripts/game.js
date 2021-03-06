var shipsPos = [];
$(function () {

    //Getting GamePlayer parameter from URL
    let gpId = $.urlParam('gp');
    let loader = "../api/game_view/" + gpId;

    $.get(loader)
    .done(function(xhr) {
        let dataGame = xhr;
        let dataShips = dataGame.ships;
        let dataSalvos = dataGame.salvos;
        let dataGamePlayers = dataGame.gamePlayers;
        let gameStatus = dataGame.stateOfGame;

        //Filling Grids
        $("#grid-ships").html(drawGrid());
        $("#grid-salvos").html(drawGrid());
        $('#grid-salvos td').click(function () {
            setShoots(this);
        });

        //feature row/column red
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

        if(gameStatus == 1){
            //**********************PLACE SHIPS*************************
            placeShipsPage();
            var widthCell = $('.A5').outerWidth();
            var heightCell = $('.A10').outerHeight();
            setWidthShip(widthCell);
            $("#aircraft-carrier, #battleship, #submarine, #destroyer, #patrol-boat").css('height',heightCell-2);

            $(window).resize(function(){
                widthCell = $('.A5').outerWidth();
                heightCell = $('.A10').outerHeight();
                setWidthShip(widthCell);
                $("#aircraft-carrier, #battleship, #submarine, #destroyer, #patrol-boat").css('height',heightCell-2);
            });

            var rotated = false;
            $("#aircraft-carrier, #battleship, #submarine, #destroyer, #patrol-boat").click(function() {
                if (!rotated) {
                    $(this).css('height',widthCell-1);
                    $(this).addClass('vertical');
                    changeWidthShip(heightCell, this.id);
                    deg = 90;
                    setFitBehaviour(this.id,this.parentNode.id);
                }else {
                    $(this).css('height', (heightCell-1));
                    $(this).removeClass('vertical');
                    changeWidthShip(widthCell, this.id);
                    deg = 0;
                    setFitBehaviour(this.id,this.parentNode.id);
                }
                this.style.webkitTransform = 'rotate('+deg+'deg)';
                this.style.mozTransform = 'rotate('+deg+'deg)';
                this.style.msTransform = 'rotate('+deg+'deg)';
                this.style.oTransform = 'rotate('+deg+'deg)';
                this.style.transform = 'rotate('+deg+'deg)';
                rotated = !rotated;
            });
            //*********************************************************************
            $('#right-table-tittle').html('<h2>Drag your ships to the grid</h2>');
            $('#left-table-tittle').html('<h2>Drop your Ships here</h2>');
            $('#right-table-button').html('<button id="place-ships" type="button" class="btn btn-default btn-sm" disabled>Place Ships</button>');
            $('#place-ships').click(function() {placeShips(shipsPos)});
        }
        else {
            testStatus(dataSalvos, gpId, dataGamePlayers, gameStatus);
        }
        $("#info-game").html(paintInfoPlayers(dataGame, gpId));
        paintShips(dataShips);

    })
    .fail(function(xhr, status) {
        $('body').addClass('unauthorized').removeClass('authorized');
        $('#info-game, #grids').empty();
        $('h1').html(status + ': ' + xhr.responseJSON.error);
        //$('#div-button').removeClass('col-sm-6').addClass('col-sm-12');
    });

    //$('table tr td:last-child').css({ 'padding-left':'10px', 'padding-right':'10px' });

    //buttons listener
    $('#logout-button').click(logout);
    $('#return-button').click(backHome);
//    $('#grid-salvos td').click(function () {
//        setShoots(this);
//    });
    setInterval(function(){
        $.get(loader)
        .done(function(xhr) {
            let dataGame = xhr;
            let dataShips = dataGame.ships;
            let dataSalvos = dataGame.salvos;
            let dataGamePlayers = dataGame.gamePlayers;
            let gameStatus = dataGame.stateOfGame;

//            if(gameStatus != 5 && $('table tr td').hasClass('onClickable')) {
//                $('table tr td').removeClass('onClickable');
//                $('table tr td').addClass('offClickable');
//            }

            testStatus(dataSalvos, gpId, dataGamePlayers, gameStatus);

        })
        .fail(function(xhr, status) {
            $('body').addClass('unauthorized').removeClass('authorized');
            $('#info-game, #grids').empty();
            $('h1').html(status + ': ' + xhr.responseJSON.error);
        });
    }, 3000);
});

function testStatus(dataSalvos, gpId, dataGamePlayers, gameStatus) {
    if(gameStatus == 2) {
        $('#grid-salvos').addClass('no-opponent');
    }else if(gameStatus == 3) {
        $('#grid-salvos').addClass('no-opponent-ships');
    }else if(gameStatus == 4) {
        $('#grid-salvos').addClass('not-your-turn');
        $('#left-table-tittle').html('<h2>Your field</h2>');
        $('#right-table-tittle').html('<h2>Opponent shooting</h2>');
        paintSalvos(dataSalvos, gpId, dataGamePlayers);
    }else if(gameStatus == 5) {
        $('#grid-salvos').removeClass('no-opponent');
        $('#grid-salvos').removeClass('no-opponent-ships');
        $('#grid-salvos').removeClass('not-your-turn');
//        $('table tr td').addClass('onClickable');
//        $('table tr td').removeClass('offClickable');

        $('#right-table-tittle').html('<h2>Shoot your Salvo</h2>');
        $('#left-table-tittle').html('<h2>Your field</h2>');
        $('#right-table-button').html('<button id="fire-salvos" type="button" class="btn btn-default btn-sm" disabled>Fire!</button>');
        $('#fire-salvos').click(function() {sendSalvo(gpId)});
        paintSalvos(dataSalvos, gpId, dataGamePlayers);
    }
    else if(gameStatus == 6) {
        window.location.assign("../web/games.html");
    }
    else if(gameStatus == 7) {
        window.location.assign("../web/games.html");
    }
}

function placeShipsPage() {
    $("#right-table").empty();
    $("#right-table").append(paintInfoGame());
    $("#left-table").empty();
    $("#left-table").append(drawDivsGrid());
}

function paintInfoGame() {
    let fillInfo = '';
    fillInfo += '<div id="grid-global">';
    fillInfo += '<img src="assets/images/aircraft-1.png" id="aircraft-carrier" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/battleship-2.png" id="battleship" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/submarine-3.png" id="submarine" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/submarine-3.png" id="destroyer" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/patrol-2.png" id="patrol-boat" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)"></div>'
    return fillInfo;
}

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
    row += '<td class="no-grid">' + rowName + "</td>";
    for(var j = 1; j < 11; j++) {
        if(i == 0) {
            row += '<td class="no-grid">' + j + '</td>';
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
    //console.log(dataShips);
    $(dataShips).each(function(i, ship) {
        //let dataLocation = ship.locations;
        let selectTable = "#grid-ships " + "."+ship.locations[0];
        //console.log(i);
        if(ship.type == 'aircraft-carrier') {
            if(ship.locations[0].charAt(0) == ship.locations[1].charAt(0)) {
                $(selectTable).append('<img src="assets/images/aircraft-1.png" id="aircraft-carrier1">');
            }else {
                $(selectTable).append('<img src="assets/images/aircraft-1.png" id="aircraft-carrier1">');
                $('#aircraft-carrier1').addClass('rotated');
                $('#aircraft-carrier1').css({ 'top': '78px', 'left': '-66px', 'max-height': '28px' });
            }
        }else if(ship.type == 'battleship') {
            if(ship.locations[0].charAt(0) == ship.locations[1].charAt(0)) {
                $(selectTable).append('<img src="assets/images/battleship-2.png" id="battleship1">');
            }else {
                $(selectTable).append('<img src="assets/images/battleship-2.png" id="battleship1">');
                $('#battleship1').addClass('rotated');
                $('#battleship1').css({ 'top': '63px', 'left': '-55px', 'max-height': '30px' });
            }
        }else if(ship.type == 'submarine' || ship.type == 'destroyer'){
            if(ship.locations[0].charAt(0) == ship.locations[1].charAt(0)) {
                $(selectTable).append('<img src="assets/images/submarine-3.png" id="submarine1">');
            }else {
                $(selectTable).append('<img src="assets/images/submarine-3.png" id="submarine1">');
                $('#submarine1').addClass('rotated');
                $('#submarine1').css({ 'top': '46px', 'left': '-39px', 'max-height': '27px' });
            }
        }else if(ship.type == 'patrol-boat'){
            if(ship.locations[0].charAt(0) == ship.locations[1].charAt(0)) {
                $(selectTable).append('<img src="assets/images/patrol-2.png" id="patrol-boat1">');
            }else {
                $(selectTable).append('<img src="assets/images/patrol-2.png" id="patrol-boat1">');
                $('#patrol-boat1').addClass('rotated');
                $('#patrol-boat1').css({ 'top': '26px', 'left': '-20px', 'max-height': '28px' });
            }
        }
    });
}

function paintSalvos(dataSalvos, ownerId, dataGamePlayers) {
    //console.log(dataGamePlayers);
    $(dataGamePlayers).each(function(i, eachGp) {
        if(eachGp.game_player_id == ownerId) {
            let requesterId = eachGp.player.playerId;
            for (const [turn, val] of Object.entries(dataSalvos)) {
                for (const [playerId, locations] of Object.entries(val)) {
                    if(requesterId == playerId) {
                        //console.log(turn, locations.shipsStatus);
                        hitShip(turn, locations.shipsStatus);
                        //console.log('own ships',locations.shipsStatus);
                        $(locations.salvo).each(function(i, shoot) {
                            //console.log(shoot.cell, shoot.hit);
                            let selectTable = "#grid-salvos " + "."+shoot.cell;
                            $(selectTable).html(turn);
                            if(shoot.hit) {
                                $(selectTable).addClass('hit');
                            }else {
                                $(selectTable).addClass('no-hit');
                            }
                        });
                        checkSunk(locations.shipsStatus, 'ships');
                    }
                    if(requesterId != playerId) {
                        //console.log(turn, locations.salvo);
                        //console.log('enemy ships',locations.shipsStatus);
                        checkSunk(locations.shipsStatus, 'salvos');
                    }
                }
            }
        }
    });
}

function hitShip(turn, ships) {
    //console.log(ships);
    $(ships).each(function(i, ship) {
        $(ship.cellsHitted).each(function(j, cell) {
            let selectTable2 = "#grid-ships " + "."+cell;
            $(selectTable2).addClass('hit-on-me');
            $(selectTable2).removeClass('background-blue');
            if ($(selectTable2).is(':empty')) {
                $(selectTable2).html(turn);
            }else if($(selectTable2).text() == '' && $(selectTable2).find('> img').length) {
                $(selectTable2).append(turn);
            }
        });
    });
}

function checkSunk(ShipsStatus, grid) {
    $(ShipsStatus).each(function(i, ship) {
        if(ship.shipSink){
            $(ship.cellsHitted).each(function(j, cell) {
                $('#grid-' + grid + ' .'+cell).addClass('sunk');
            });
        }
    });
}

function logout(evt) {
   evt.preventDefault();
   $.post("/api/logout")
    .done(function() {
     backHome();
    });
}

function backHome() {
    window.location.assign("../web/games.html");
}

function sendShips(shipsInfo) {
    let gpId = $.urlParam('gp');
    let poster = "/api/games/players/" + gpId + "/ships";
    $.post({
      url: poster,
      data: JSON.stringify(shipsInfo),
      dataType: "text",
      contentType: "application/json"
    })
    .done(function (response, status, jqXHR) {
        let loader = "../api/game_view/" + gpId;
        $.get(loader)
        .done(function() {
            location.reload();
        });
    });
}

//*********************************PLACE SHIPS*********************************************
function drawDivsGrid() {
    let grid = "";
    let rows = ["","A","B","C","D","E","F","G","H","I","J"];
    $(rows).each(function(i, rowName) {
        grid += '<div class="row">' + getDivsRow(i, rowName) + "</div>";
    });
    return grid;
}

function getDivsRow(i, rowName) {
    let row = "";
    row += '<div class="col-sm-1 rTableCell">' + rowName + "</div>";
    for(var j = 1; j < 11; j++) {
        if(i == 0) {
            row += '<div class="col-sm-1 rTableCell">' + j + '</div>';
        } else {
            row += '<div id="' + rowName + j + '" class="' + rowName + j + ' col-sm-1 rTableCell" ondrop="drop(this, event)" ondragenter="return false" ondragover="return false"></div>';
        }
    }
    return row;
}

function setWidthShip(widthCell) {
	$('#aircraft-carrier').css('width', (widthCell-1)*5);
	$('#battleship').css('width', (widthCell-1)*4);
	$('#submarine, #destroyer').css('width', (widthCell-1)*3);
	$('#patrol-boat').css('width', (widthCell-1)*2);
}

function changeWidthShip(widthCell, shipId) {
	if (shipId == 'aircraft-carrier') {
		$('#aircraft-carrier').css('width', (widthCell-1)*5);
	} else if (shipId == 'battleship') {
		$('#battleship').css('width', (widthCell-1)*4);
	} else if (shipId == 'submarine' || shipId == 'destroyer') {
		$('#' + shipId).css('width', (widthCell-1)*3);
	} else if (shipId == 'patrol-boat') {
		$('#patrol-boat').css('width', (widthCell-1)*2);
	}
}

function dragstart(ship, evento) {
    event.dataTransfer.setData('Data', ship.id);
}

function drag(ship, evento) {
	$('#'+ship.id).hide();
}

function drop(target, evento) {
    // obtenemos los datos
    let shipId = event.dataTransfer.getData('Data');
    let ship = document.getElementById(shipId);
    $('#'+shipId).css('z-index','2');
    target.appendChild(ship);
    setFitBehaviour(shipId, target.id);
    $('#'+shipId).show();
}

function setFitBehaviour(shipId, targetId) {
	let fitResult = fitOnGrid(shipId, targetId);
    if(fitResult.fit){
    	$('#'+shipId).addClass('fit');
	    $('#'+shipId).removeClass('no-fit');
	    if (shipsPos.length == 0) {
	    	shipsPos.push(fitResult.shipPos);
	    }else {
	    	shipsPos = checkShipPos(shipsPos, fitResult.shipPos);
	    	if (collision(shipsPos)) {
	    		$('#'+shipId).addClass('no-fit');
				$('#'+shipId).removeClass('fit');
	    	}
	    }
	}else {
		$('#'+shipId).addClass('no-fit');
		$('#'+shipId).removeClass('fit');
	}

	if (shipsPos.length==5 && !$('img').hasClass('no-fit')) {
	    //console.log('resultado final 1',shipsPos);
		$('#place-ships').removeAttr("disabled");
	}else {
	    $("#place-ships").attr("disabled", true);
	}
}

function fitOnGrid(shipId, targetId) {
	let fit=false;
	let cellNumber = getCellNumber(targetId);
	let cellLetter = targetId.charAt(0);
	let shipPos;
	switch(shipId) {
		case 'aircraft-carrier':
		shipPos = {'shipType':'aircraft-carrier','locations':fillShipPos(5, cellLetter, cellNumber, shipId)};
		if (!$('#'+shipId).hasClass('vertical')) {
			fit = fitHorizontal(5, cellNumber);
			$('#'+shipId).css({left:0, top:0,});
		}else {
			fit = fitVertical(5, cellLetter);
			$('#'+shipId).css({left:-77, top:77});
		}
		break;
		case 'battleship':
		shipPos = {'shipType':'battleship','locations':fillShipPos(4, cellLetter, cellNumber, shipId)};
		if (!$('#'+shipId).hasClass('vertical')) {
			fit = fitHorizontal(4, cellNumber);
			$('#'+shipId).css({left:0, top:0,});
		}else {
			fit = fitVertical(4, cellLetter);
			$('#'+shipId).css({left:-57, top:57});
		}
		break;
		case 'submarine':
		shipPos = {'shipType':'submarine','locations':fillShipPos(3, cellLetter, cellNumber, shipId)};
		if (!$('#'+shipId).hasClass('vertical')) {
			fit = fitHorizontal(3, cellNumber);
			$('#'+shipId).css({left:0, top:0,});
		}else {
			fit = fitVertical(3, cellLetter);
			$('#'+shipId).css({left:-37, top:37});
		}
		break;
		case 'destroyer':
		shipPos = {'shipType':'destroyer','locations':fillShipPos(3, cellLetter, cellNumber, shipId)};
		if (!$('#'+shipId).hasClass('vertical')) {
			fit = fitHorizontal(3, cellNumber);
			$('#'+shipId).css({left:0, top:0,});
		}else {
			fit = fitVertical(3, cellLetter);
			$('#'+shipId).css({left:-37, top:37});
		}
		break;
		case 'patrol-boat':
		shipPos = {'shipType':'patrol-boat','locations':fillShipPos(2, cellLetter, cellNumber, shipId)};
		if (!$('#'+shipId).hasClass('vertical')) {
			fit = fitHorizontal(2, cellNumber);
			$('#'+shipId).css({left:0, top:0,});
		}else {
			fit = fitVertical(2, cellLetter);
			$('#'+shipId).css({left:-17, top:17});
		}
		break;
	}
	return {fit, shipPos};
}

function getCellNumber(targetId) {
	let cellNumber;
	if (targetId.charAt(2)=="0") {
		cellNumber = 10;
	}else {
		cellNumber = targetId.charAt(1);
	}
	return cellNumber;
}

function fitHorizontal(shipSize, cellNumber) {
	let fit=false;
	if (11-cellNumber>=shipSize) {
		fit = true;
	}else {
		fit = false;
	}
	return fit;
}

function fitVertical(shipSize, cellLetter) {
	let fit=false;
	let letters = ["","A","B","C","D","E","F","G","H","I","J"];
	if (11-$.inArray(cellLetter, letters)>=shipSize) {
		fit = true;
	}else {
		fit = false;
	}
	return fit;
}

function fillShipPos(iterator, cellLetter, cellNumber, shipId) {
	let arrayPos = [];
	if(!$('#'+shipId).hasClass('vertical')) {
		for (var i = 0; i < iterator; i++) {
			arrayPos.push(cellLetter + (+cellNumber + i));
		}
	}else {
		arrayPos.push(cellLetter+cellNumber);
		for (var i = 1; i < iterator; i++) {
			arrayPos.push(nextLetter(cellLetter) + cellNumber);
			cellLetter = nextLetter(cellLetter);
		}
	}
	return arrayPos;
}

function checkShipPos(shipsPos, shipPos) {
	$(shipsPos).each(function(i, eachShipPos) {
		if (shipPos.shipType == eachShipPos.shipType) {
			shipsPos = shipsPos.filter((item) => item.locations !== eachShipPos.locations);
		}else {
			if (!contains(shipsPos, shipPos)) {
				shipsPos.push(shipPos);
			}
		}
	});
	return shipsPos;
}

function collision(shipsPos) {
	shipToCheck = shipsPos[shipsPos.length-1];
	for (var i = 0; i < shipsPos.length-1; i++) {
		for (var j = 0; j < shipsPos[i].locations.length; j++) {
			for (var k = 0; k < shipToCheck.locations.length; k++) {
				if(shipToCheck.locations[k]==shipsPos[i].locations[j]) {
					return true;
				}
			}
		}
	}
}

function placeShips(shipsPos) {
	sendShips(shipsPos);
}

//*******************************************************************

jQuery.expr[':'].regex = function(elem, index, match) {
    var matchParams = match[3].split(','),
        validLabels = /^(data|css):/,
        attr = {
            method: matchParams[0].match(validLabels) ?
                        matchParams[0].split(':')[0] : 'attr',
            property: matchParams.shift().replace(validLabels,'')
        },
        regexFlags = 'ig',
        regex = new RegExp(matchParams.join('').replace(/^\s+|\s+$/g,''), regexFlags);
    return regex.test(jQuery(elem)[attr.method](attr.property));
}

function nextLetter(s){
    return s.replace(/([a-zA-Z])[^a-zA-Z]*$/, function(a){
        var c= a.charCodeAt(0);
        switch(c){
            case 90: return 'A';
            case 122: return 'a';
            default: return String.fromCharCode(++c);
        }
    });
}

function contains(a, obj) {
    var i = a.length;
    while (i--) {
       if (a[i] === obj) {
           return true;
       }
    }
    return false;
}

//*******************************SALVOS********************************
var shoots = 0;
var shootsPos = [];
function setShoots(shootCell) {
    let arrayClasses = $(shootCell).prop('classList');
    if (!$(shootCell).hasClass('no-grid')) {
        if(shoots <= 5) {
            if($(shootCell).hasClass('shoot')) {
                $(shootCell).removeClass('shoot');
                shootsPos = shootsPos.filter(shoot => shoot !== arrayClasses[0]);
                shoots --;
            }else {
                $(shootCell).addClass('shoot');
                shoots ++;
                shootsPos.push(arrayClasses[0]);
            }

            if(shoots == 5) {
                //console.log(shootsPos);
                $("#fire-salvos").removeAttr("disabled");
            }
        }
    }
}

function sendSalvo(gpId) {
    $("#fire-salvos").attr("disabled", true);
    let poster = "/api/games/players/" + gpId + "/salvos";
    $.post({
      url: poster,
      data: JSON.stringify(shootsPos),
      dataType: "text",
      contentType: "application/json"
    })
    .done(function (response, status, jqXHR) {
        let loader = "../api/game_view/" + gpId;
        $.get(loader)
        .done(function(xhr) {
            location.reload();
        });
    });
}