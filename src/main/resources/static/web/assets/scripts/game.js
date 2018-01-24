var shipsPos = [];
$(function () {
    //Getting GamePlayer parameter from URL
    let gpId = $.urlParam('gp');
    let loader = "http://localhost:8080/api/game_view/" + gpId;

    //Filling Grids
    $("#grid-ships").append(drawGrid());
    $("#grid-salvos").append(drawGrid());
    $('#grid-salvos td').click(function () {setShoots(this)});
    $.get(loader)
    .done(function(xhr) {
        let dataGame = xhr;
        let dataShips = dataGame.ships;
        if(dataShips.length == 0){
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
        }else {
            $('#right-table-tittle').html('<h2>Opponent field</h2>');
            $('#left-table-tittle').html('<h2>Your Ships</h2>');
            $('#right-table-button').html('<button id="fire-salvos" type="button" class="btn btn-default btn-sm" disabled>Fire!</button>');
            $('#fire-salvos').click(function() {setSalvoData()});
        }
        $("#info-game").append(paintInfoPlayers(dataGame, gpId));
        paintShips(dataShips);
        paintSalvos(dataGame.salvos, gpId, dataShips);
    })
    .fail(function(xhr, status) {
        $('body').addClass('unauthorized').removeClass('authorized');
        $('#info-game, #grids').empty();
        $('h1').html(status + ': ' + xhr.responseJSON.error);
        //$('#div-button').removeClass('col-sm-6').addClass('col-sm-12');
    });
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

    $('#logout-button').click(logout);
    $('#return-button').click(backHome);

});

function placeShipsPage() {
    $("#right-table").empty();
    $("#right-table").append(paintInfoGame());
    $("#left-table").empty();
    $("#left-table").append(drawDivsGrid());
}

function paintInfoGame() {
    let fillInfo = '';
    fillInfo += '<div id="grid-global">';
    fillInfo += '<img src="assets/images/boat1.jpg" id="aircraft-carrier" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/battleship.jpg" id="battleship" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/boat2.jpg" id="submarine" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/boat3.jpg" id="destroyer" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)">';
    fillInfo += '<img src="assets/images/boat4.jpg" id="patrol-boat" draggable="true" ondragstart="dragstart(this, event)" ondrag="drag(this, event)"></div>'
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
    $(dataShips).each(function(i, ship) {
        /*let dataLocation = ship.locations;
        console.log(ship);
        switch(ship.type) {
        		case 'aircraft-carrier':
        		console.log(dataLocation[0]);
        		    $('td .'+dataLocation[0]).appendChild('<img src="assets/images/boat1.jpg" id="aircraft-carrier">');
        		break;
        		case 'battleship':

        		break;
        		case 'submarine':

        		break;
        		case 'destroyer':

        		break;
        		case 'patrol-boat':

        		break;
        	}*/
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
}7

function logout(evt) {
   evt.preventDefault();
   $.post("/api/logout")
    .done(function() {
     backHome();
    });
}

function backHome() {
    window.location.assign("http://localhost:8080/web/games.html");
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
        let loader = "http://localhost:8080/api/game_view/" + gpId;
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
	//console.log(shipId,widthCell);
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
	    console.log('resultado final 1',shipsPos);
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
                console.log('before',shootsPos);
                $(shootCell).removeClass('shoot');
                shootsPos = shootsPos.filter(shoot => shoot !== arrayClasses[0]);
                console.log(shootsPos);
                shoots --;
            }else {
                $(shootCell).addClass('shoot');
                shoots ++;
                shootsPos.push(arrayClasses[0]);
            }
            if(shoots >= 5) {
                $("#fire-salvos").removeAttr("disabled");
            }
        }
    }
}
var turn = 0;
function setSalvoData() {
    turn ++;
    let salvoData = {'turn':turn,'locations':shootsPos};
    console.log(salvoData);
    $("#fire-salvos").attr("disabled", true);
    sendSalvo(salvoData);
}

function sendSalvo(salvoData) {
    let gpId = $.urlParam('gp');
    let poster = "/api/games/players/" + gpId + "/salvos";
    $.post({
      url: poster,
      data: JSON.stringify(salvoData),
      dataType: "text",
      contentType: "application/json"
    })
    .done(function (response, status, jqXHR) {
        let loader = "http://localhost:8080/api/game_view/" + gpId;
        $.get(loader)
        .done(function() {
            location.reload();
        });
    });
}