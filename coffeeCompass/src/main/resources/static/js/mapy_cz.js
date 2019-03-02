var m;
var layer;
 
var latSearch;
var lonSearch;

createMap = function(lat1, lon1) {
   
	 var center = SMap.Coords.fromWGS84(lon1, lat1);
	 m = new SMap(JAK.gel("map"), center, 8);
	 m.addDefaultLayer(SMap.DEF_BASE).enable();
	 m.addDefaultControls();
	   
	 var mouse = new SMap.Control.Mouse(SMap.MOUSE_PAN | SMap.MOUSE_WHEEL | SMap.MOUSE_ZOOM); /* Ovládání myší */
	 m.addControl(mouse); 
	   
	 layer = new SMap.Layer.Marker();
	 m.addLayer(layer);
	   
	 var card = new SMap.Card();
	 card.getHeader().innerHTML = "Střed hledání, přesuň mě!";
	
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
	
	 	/* Vypsani aktualni polohy do fomulare/documentu */
	    latSearch.value = coords.toWGS84()[1];
	 	lonSearch.value = coords.toWGS84()[0];
	 }
	
	 var signals = m.getSignals();
	 signals.addListener(window, "marker-drag-stop", stop);
	 signals.addListener(window, "marker-drag-start", start);
}


map.create =  function(aLatSearch, aLonSearch) {

	 latSearch = aLatSearch;
	 lonSearch = aLonSearch;
	
	 var latInit = latSearch.value;
	 var lonInit = lonSearch.value;

	 if (latInit == 0 && lonInit == 0) {
	 	lat1 = 49.8250401;
	    lon1 = 15.4190817;
	 }
	 
	 createMap(latInit, lonInit);
}

map.insertSites = function(foundSites)
{
	if (foundSites != null && foundSites.length > 0)
	{	     		     	
		var souradnice = [];
		// vytvoreni markeru
		foundSites.forEach(function(site) {
			var c = SMap.Coords.fromWGS84(site.zemDelka, site.zemSirka); /* Souřadnice značky, z textového formátu souřadnic */
		  
			var options = {
				url: "images/cup.png", /* cesta k /src/resources/images/cup.png  v mych resourcech */
				title: site.siteName, 
				anchor: {left:10, bottom: 1}  /* Ukotvení značky za bod uprostřed dole */
			}
			
			// prirazeni id jednotlivemu markeru - vlastni id, jinak se generuje nahodne
			var znacka = new SMap.Marker(c, site.id, options);
			var card = new SMap.Card();
			
			var headerText = "<b><span style='display:block;text-align:center; margin-bottom:10px;'>" + site.siteName + "</span></b>";
			
			card.getHeader().innerHTML = headerText;
			
			var siteId = site.id.toString();
			
			var bodyImage = "";
			
			if (!(site.mainImageURL === "")) { 
			
				bodyImage = "<img class='photoShow' style='height:240px;' src='http://coffeecompass.cz/rest/image/bytes/";
				bodyImage += siteId + "'/>";
				bodyImage += "<br>" + "<br>" + "<br>"; // to fill space, because of rotated image
			}
			
			var textVizitka = bodyImage 
							  + site.typPodniku.coffeeSiteType + ", " + site.typLokality.locationType + "<br>" 
							  + site.statusZarizeni.status + "<br>"
							  + "vzdálenost: " + site.distFromSearchPoint + " m <br>"
							  + "hodnoceni: " + site.averageStarsWithNumOfHodnoceni.common;
			
			card.getBody().innerHTML = textVizitka;
			
			var footerText = "<a href='http://coffeecompass.cz/showSite/";
			footerText += siteId + "'>Další detaily ...</a>";
			
			card.getFooter().innerHTML = footerText;
	
			znacka.decorate(SMap.Marker.Feature.Card, card);
			
			souradnice.push(c);
			layer.addMarker(znacka);
		});
	
		
		var cz = m.computeCenterZoom(souradnice); /* Spočítat pozici mapy tak, aby značky byly vidět */
		m.setCenterZoom(cz[0], cz[1]);
		
	
		// poslouchani na kliknuti u markeru - Pro zakladni info mame prirazenu card - Jake dalsi vyuziti ... vedle mapy mala karta s detaily, ktera by se vyplnila udaji o situ
		/*
		m.getSignals().addListener(this, "marker-click", function(e) {
		  // vybrany marker
		  var marker = e.target;
		  var id = marker.getId();
		  
		  for (var i = 0; i < markers.length; i++) {
			if (foundSites[i].id == id) {
				
			    break;
			}
		  }
		});  
		*/
	}

}


map.enable = function() {
	layer.enable();
}
 
