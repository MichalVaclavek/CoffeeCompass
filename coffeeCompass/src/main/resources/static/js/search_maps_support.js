var m;
var layer;
 
var latSearchInput; /* hidden input field for current searching point latitude */
var lonSearchInput; /* hidden input field for current searching point longitude */

var latSearchLabel; /* field to show current searching point latitude */
var lonSearchLabel; /* field to show current searching point longitude */

var rangeSearchLabel; /* field to show current searching range in m */ 

var souradnice = []; /* souradnice vsech bodu/znacek v mape */
var allSites = []; // all found sites
var markers = []; // all site's standard markers
var markersSelected = []; // all site's markers when selected
var markerCards = []; // cards - vizitka, for all sites (for their markers)
var markerCardsBodyText = []; // body text of the card - includes link to site's image

var base_url = window.location.origin;

var lastSelectedMarkerID = -1;

/**
 * Creates map with movable marker on the lat1, lon1 input coordinates
 * inserted in the list of all points (CoffeeSites) to be shown in map.
 */
createMap = function(lat1, lon1) {
   
	 var center = SMap.Coords.fromWGS84(lon1, lat1);
	 
	 m = new SMap(JAK.gel("map"), center, 8);
	 m.addControl(new SMap.Control.Sync());
	 m.addDefaultLayer(SMap.DEF_BASE).enable();
	 m.addDefaultControls();
	   
	 var mouse = new SMap.Control.Mouse(SMap.MOUSE_PAN | SMap.MOUSE_WHEEL | SMap.MOUSE_ZOOM); /* Ovládání myší */
	 m.addControl(mouse); 
	   
	 layer = new SMap.Layer.Marker();
	 m.addLayer(layer);

	 var card = new SMap.Card();
	 card.getHeader().innerHTML = "Střed hledání, přesuň mě!";
	
	 var marker = new SMap.Marker(center); // movable Marker to select center of searching
	 marker.decorate(SMap.Marker.Feature.Card, card);
	 marker.decorate(SMap.Marker.Feature.Draggable);
	 layer.addMarker(marker);
	   	
	 function start(e) { /* Začátek tažení */
	    var node = e.target.getContainer();
	    node[SMap.LAYER_MARKER].style.cursor = "help";
	 }
	
	 function stop(e) {
	    var node = e.target.getContainer();
	    node[SMap.LAYER_MARKER].style.cursor = "";
	    var coords = e.target.getCoords();
	
	 	/* Vypsani aktualni polohy do hidden inputs of 'search' fomulare/documentu a do */
	    /* kontrolnich labels pod mapou */
	    latSearchInput.value = coords.toWGS84()[1];
	 	lonSearchInput.value = coords.toWGS84()[0];
	 	
	 	latSearchLabel.innerHTML = latSearchInput.value.substring(0,10);
	 	lonSearchLabel.innerHTML = lonSearchInput.value.substring(0,10);
	 }
	
	 var signals = m.getSignals();
	 signals.addListener(window, "marker-drag-stop", stop);
	 signals.addListener(window, "marker-drag-start", start);
	 
	 souradnice.push(center);
}

/**
 * Main function to be called from html to create map in given div element
 */
map.create = function(aLatSearchInput, aLonSearchInput, aLatSearchLabel, aLonSearchLabel) {

	 latSearchInput = aLatSearchInput;
	 lonSearchInput = aLonSearchInput;
	 latSearchLabel = aLatSearchLabel;
	 lonSearchLabel = aLonSearchLabel;
	 
	 var latInit = latSearchInput.value;
	 var lonInit = lonSearchInput.value;

	 if (latInit == 0 && lonInit == 0) {
		 latInit = 49.8250401;
		 lonInit = 15.4190817;
	 }
	 
	 latSearchLabel.innerHTML = latInit.substring(0,10);
	 lonSearchLabel.innerHTML = lonInit.substring(0,10);
	 
	 createMap(latInit, lonInit);
}

getNormalMarkerImage = function(site) {
    var options = {
				url: base_url + "/images/cup_basic_16px.png", /* cesta k /src/resources/images/cup.png v resourcech */
				title: site.siteName,
				anchor: {left:10, bottom: 10}  /* Ukotvení značky za bod uprostřed dole */
			}
	return options;
}

getSelectedMarkerImage = function(site) {
    var options = {
				url: base_url + "/images/cup_red2_16px.png", /* cesta k /src/resources/images/cup.png v resourcech */
				title: site.siteName,
				anchor: {left:10, bottom: 10}  /* Ukotvení značky za bod uprostřed dole */
			}
	return options;
}

/*
 * Inserts all CoffeeSites geo coordinates into the map as markers/icons
 * with CoffeeSite image and some additional info to be shown on
 * the mark's card.
 */
map.insertSites = function(foundSites)
{
	if (foundSites != null && foundSites.length > 0)
	{	     		     	
		allSites = foundSites;
		// vytvoreni markeru pro kazdy site
		foundSites.forEach(function(site) {
			var c = SMap.Coords.fromWGS84(site.zemDelka, site.zemSirka); /* Souřadnice značky, z textového formátu souřadnic */

		    // prirazeni id jednotlivemu markeru - vlastni id, jinak se generuje nahodne
			var znacka = new SMap.Marker(c, site.id, getNormalMarkerImage(site));
            var znackaSelected = new SMap.Marker(c, site.id, getSelectedMarkerImage(site));

			createMarkerCards(site);

			souradnice.push(c);

			markers.push(znacka);
            markersSelected.push(znackaSelected);
		});

	    layer.addMarker(markers);

		var cz = m.computeCenterZoom(souradnice); /* Spočítat pozici mapy tak, aby v3echny značky byly vidět */
		m.setCenterZoom(cz[0], cz[1]);

		// poslouchani na kliknuti u markeru
        m.getSignals().addListener(this, "marker-click", function(e) {
            // vybrany marker
            var marker = e.target;
            var id = marker.getId();

            // zobrazit data o lokaci v tab. - parovani vybraneho markeru pomoci jeho id a nasich vstupnich dat
            for (var i = 0; i < markers.length; i++) {
                if (allSites[i].id == id) {
                    var card = markerCards[i];
                    card.getBody().innerHTML = markerCardsBodyText[i];
                    // remove standard marker for currently selected marker
                    layer.removeMarker(markers[i]);
                    // insert selected marker for currently selected marker
                    layer.addMarker(markersSelected[i]);
                    markersSelected[i].decorate(SMap.Marker.Feature.Card, card);

                    if (lastSelectedMarkerID != -1) {
                        // remove selected marker for previously selected marker
                        layer.removeMarker(markersSelected[lastSelectedMarkerID]);
                        // insert standard marker for previously selected marker
                        layer.addMarker(markers[lastSelectedMarkerID]);
                    }

                    m.addCard(card, markersSelected[i].getCoords()); // to show card immediately after the click

                    lastSelectedMarkerID = i;
                    break;
                }
            }
        });
	}
}

createMarkerCards = function(site) {
    var card = new SMap.Card();

    var headerText = "<b><span style='display:block;text-align:center; margin-bottom:10px;'>" + site.siteName + "</span></b>";

    card.getHeader().innerHTML = headerText;

    var bodyImage = "";

    if (!(site.mainImageURL === "")) {
        bodyImage = "<img style='height:240px;' src='" + site.mainImageURL + "'/>";
        bodyImage += "<br><br>"; // to create space above text, because of rotated image
    }

    var textVizitka = bodyImage
                      + site.typPodniku.coffeeSiteType + ", " + site.typLokality.locationType + "<br>"
                      + site.statusZarizeni.status + "<br>"
                      + "hodnoceni: " + site.averageStarsWithNumOfHodnoceni.common + "<br>";
    if (site.distFromSearchPoint > 0) {
        textVizitka += "vzdálenost: " + site.distFromSearchPoint + " m";
    }

    // Inserts link to CoffeeSite details page
    var footerText = "<a href='" + base_url + "/showSite/";
    footerText += site.id + "'>Další detaily ...</a>";

    card.getFooter().innerHTML = footerText;

    markerCards.push(card);
    markerCardsBodyText.push(textVizitka);
}


map.enable = function() {
	layer.enable();
}


/* Finding coordinates of the City/town from api.mapy.cz */
var cityName;

map.findCoordinatesAndInsertToForm = function(city) {
   cityName = city;
   
   var geocode = new SMap.Geocoder(cityName, odpoved, {
	    // parametry pro omezeni mista - bounding box ceske republiky dle https://wiki.openstreetmap.org/wiki/WikiProject_Czech_Republic
	   /* bbox: [SMap.Coords.fromWGS84(12.09, 51.06), SMap.Coords.fromWGS84(18.87, 48.55)] */
	   locality:"Česko"
   });
}

var changeNodeVisibility = function(node) {
		node.disabled = !node.disabled;
		if (node.disabled) node.style.visibility = "hidden";
		if (!node.disabled) node.style.visibility = "visible";
	}

/*
 * Callback function for SMap.Geocoder() call looking for city/place geo coordinates
 */
function odpoved(geocoder) { /* Odpověď */
	
	var vysledky = geocoder.getResults()[0].results;
    var data = [];
    var selectCity = document.getElementById('selectCity');
    var cityNameInput = document.getElementById("cityName");
    var additionalCityLabel = document.getElementById("selectfromlistSpan");
	
    if (!geocoder.getResults()[0].results.length) {
        /* alert("Tohle neznáme."); */
		// nahradit select inputem
		swapDivsWithClick(cityNameInput, selectCity);
		additionalCityLabel.style.visibility = "hidden";
		
		latSearchInput.value = ""; // Coordinates not known, try to find against cityName only in CoffeeSites's 'poloha_mesto' column
		lonSearchInput.value = "";
		
		$(".se-pre-con").fadeIn("fast");
    	document.getElementById("searchByCoordinatesForm").submit();
        return;
    }
    
    if (vysledky.length === 1) {
    	// nahradit select inputem
		swapDivsWithClick(cityNameInput, selectCity);
		additionalCityLabel.style.visibility = "hidden";
	
		var item = vysledky.shift()
		latSearchInput.value = item.coords.toWGS84()[1]; /* Zobrazi se tedy zatim jen posledni souradnice */
		lonSearchInput.value = item.coords.toWGS84()[0];
		/* data.push(item.label + " (" + item.coords.toWGS84(2).reverse().join(", ") + ")"); */
		/* document.getElementById("cityName").value = item.label; */
		cityNameInput.value = item.label;
		
		$(".se-pre-con").fadeIn("fast");
		document.getElementById("searchByCoordinatesForm").submit();
    	
    } else  if (vysledky.length > 1) { // More then 1 result returned from mapy.cz, select options has to be shown to user
		// nahradit input selectem
		swapDivsWithClick(cityNameInput, selectCity);
		additionalCityLabel.style.visibility = "visible";
	
		var i = 0;
		// naplnit select vysledky
		while (vysledky.length) { /* Zobrazit všechny výsledky hledání */
    		var item = vysledky.shift();
	        
	        var opt = document.createElement('option');
	        opt.value = item.label;
	        opt.dataset[i+'0'] = item.coords.toWGS84()[0]; // i.e. opt.dataset[00] means id of the 1st item's longitude, opt.dataset[10] means id of the 2nd item's longitude
	        opt.dataset[i+'1'] = item.coords.toWGS84()[1]; // i.e. opt.dataset[01] means id of the 1st item's latitude, opt.dataset[11] means id of the 2nd item's latitude
	        opt.innerHTML = item.label;
	        selectCity.appendChild(opt);
	        i++;
	    }
    }
    return vysledky.length > 0;
}
