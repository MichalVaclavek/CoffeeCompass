var m;
var layer;
var cityName;
var isMapVisible = false;
 
var coffeeSiteLatInput; /* input field for CoffeeSite's latitude */
var coffeeSiteLonInput; /* input field for CoffeeSite's longitude */

/**
 * Creates map with movable marker on the lat1, lon1 input coordinates
 */
createMap = function(lat1, lon1) {
   
	 var center;
	
	 if (lat1 == 0 && lon1 == 0) {
		 center = SMap.Coords.fromWGS84('15.4190817', '49.8250401');
		 m = new SMap(JAK.gel("map"), center, 8);
	 } else { // coordinates already inserted, let's zoom closer
		 center = SMap.Coords.fromWGS84(lon1, lat1);
		 m = new SMap(JAK.gel("map"), center, 16);
	 }
		
	 m.addControl(new SMap.Control.Sync());
	 m.addDefaultLayer(SMap.DEF_BASE).enable();
	 m.addDefaultControls();
	   
	 var mouse = new SMap.Control.Mouse(SMap.MOUSE_PAN | SMap.MOUSE_WHEEL | SMap.MOUSE_ZOOM); /* Ovládání myší */
	 m.addControl(mouse); 
	   
	 layer = new SMap.Layer.Marker();
	 m.addLayer(layer);
	   
	 var card = new SMap.Card();
	 card.getHeader().innerHTML = "Vyber polohu, přesuň mě!";
	
	 var marker = new SMap.Marker(center);
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
	
	 	/* Vypsani aktualni polohy do inputs of 'search' fomulare/documentu */
	    coffeeSiteLatInput.value = coords.toWGS84()[1];
	    coffeeSiteLonInput.value = coords.toWGS84()[0];
	 }
	
	 var signals = m.getSignals();
	 signals.addListener(window, "marker-drag-stop", stop);
	 signals.addListener(window, "marker-drag-start", start);
}


map.create = function(aLatSearchInput, aLonSearchInput, aCityName, aIsMapVisible) {

	coffeeSiteLatInput = aLatSearchInput;
	coffeeSiteLonInput = aLonSearchInput;
	 
	var latInit = coffeeSiteLatInput.value;
	var lonInit = coffeeSiteLonInput.value;
	
	isMapVisible = aIsMapVisible;
	
	// Show marker in the city only if aLatSearchInput and aLonSearchInput are empty
	if (aCityName.length > 1 && aIsMapVisible && latInit == 0 && lonInit == 0) {
		map.findCoordinatesAndShowInMap(aCityName);
	} else {
	    
		createMap(latInit, lonInit);
	}
}


map.enable = function() {
	layer.enable();
}

/* Naseptavac podle vyhledavaneho jmena lokality */

var inputEl = document.querySelector("input[id='cityInput']");
var suggest = new SMap.Suggest(inputEl);
suggest.urlParams({
	// omezeni pro celou CR
	//bounds: "48.5370786,12.0921668|51.0746358,18.8927040"
	locality:"Česko",
	category:"municipality_cz"
});
suggest.addListener("suggest", function(suggestData) {
    // vyber polozky z naseptavace
	if (isMapVisible) {
		map.findCoordinatesAndShowInMap(suggestData.phrase);
	}
});


/* Finding coordinates of the City/town from api.mapy.cz */

map.findCoordinatesAndShowInMap = function(city) {
   cityName = city;
   
   var geocode = new SMap.Geocoder(cityName, odpoved, {
	    // parametry pro omezeni mista - bounding box ceske republiky dle https://wiki.openstreetmap.org/wiki/WikiProject_Czech_Republic
	   /* bbox: [SMap.Coords.fromWGS84(12.09, 51.06), SMap.Coords.fromWGS84(18.87, 48.55)] */
	   locality:"Česko"
   });
}

/*
 * Callback function for SMap.Geocoder() call i.e. process the list
 * of found places and their geo coordinates. 
 */
function odpoved(geocoder) { /* Odpověď */
	
	var vysledky = geocoder.getResults()[0].results;
    var data = [];
    
    if (vysledky.length > 0) { // Take only first result, as it should be the one found earlier by 'Naseptavac' SMap.Suggest(inputEl);
    	
	
		var item = vysledky.shift()
		var latOfCity = item.coords.toWGS84()[1]; 
		var lonOfCity = item.coords.toWGS84()[0];
		
		createMap(latOfCity, lonOfCity);
		map.enable();
		// Coordinates of the city found, but they are not final coordinates of CoffeeSite
		// so lets zoom into the middle of zoom range
		m.setCenterZoom(item.coords, 13, false);
		
		if (isMapVisible) {
			// insert coordinates of found city into input fields, if map is visible
			coffeeSiteLatInput.value = latOfCity;
			coffeeSiteLonInput.value = lonOfCity;
		}
    } 
    return vysledky.length > 0;
}

/* To indicate if the map is to be refreshed when new city name is selected in suggest.addListener() */
map.setMapVisible = function(aIsMapVisible) {
	isMapVisible = aIsMapVisible;
}
