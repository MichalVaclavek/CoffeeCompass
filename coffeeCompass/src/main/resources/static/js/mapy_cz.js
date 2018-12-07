 /* Get all coffeesites coordinates */
 var foundSites = [];
 

 /*<![CDATA[*/
 foundSites = /*[[${foundSites}]]*/ null;
 /*]]>*/

 var latSearch = document.getElementById("lat1");
 var lonSearch = document.getElementById("lon1");

 var latInit = latSearch.value;
 var lonInit = lonSearch.value;

 if (latInit == 0 && lonInit == 0) {
 	lat1 = 49.8250401;
    lon1 = 15.4190817;
 }


 var m;
 var layer;

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
   card.getHeader().innerHTML = "Střed hledání - přesuň mě!";
   /*card.getBody().innerHTML = "<strong>Stred hledani</strong> - presun me!";*/
   /*card.getBody().innerHTML = "Ahoj, presun me!</em>!";*/

   var marker = new SMap.Marker(center);
   marker.decorate(SMap.Marker.Feature.Card, card);
   marker.decorate(SMap.Marker.Feature.Draggable);
   layer.addMarker(marker);
   
  // layer.enable();

   function start(e) { /* Začátek tažení */
     var node = e.target.getContainer();
     node[SMap.LAYER_MARKER].style.cursor = "help";
   }

   function stop(e) {
     var node = e.target.getContainer();
     node[SMap.LAYER_MARKER].style.cursor = "";
     var coords = e.target.getCoords();

 	/* Vypsani do document */
     latSearch.value = coords.toWGS84()[1];
 	lonSearch.value = coords.toWGS84()[0];
   }

   var signals = m.getSignals();
   signals.addListener(window, "marker-drag-stop", stop);
   signals.addListener(window, "marker-drag-start", start);

 }

createMap(latInit, lonInit);  

if (foundSites != null && foundSites.length > 0)
{
 		     	
	var souradnice = [];
	// vytvoreni markeru
	foundSites.forEach(function(site) {
		var c = SMap.Coords.fromWGS84(site.zemDelka, site.zemSirka); /* Souřadnice značky, z textového formátu souřadnic */
	  
		var options = {
			url: "images/cup.png", /* cesta k /src/resources/images/kelimek.png  v mych resourcech */
			title: site.siteName, 
			anchor: {left:10, bottom: 1}  /* Ukotvení značky za bod uprostřed dole */
		}
		
		// duletize je prirazeni id jednotlivemu markeru - vlastni id, jinak se generuje nahodne
		var znacka = new SMap.Marker(c, site.id, options);
		var card = new SMap.Card();
		card.getHeader().innerHTML = site.siteName;
		/* card.getBody().innerHTML = site.DalsiInfo + ; */
		znacka.decorate(SMap.Marker.Feature.Card, card);
		
		souradnice.push(c);
		layer.addMarker(znacka);
	});

	// zobrazime a povolime vrstvu - pokud by se vrstva povolila pred vkladanim markeru, tak by se s kazdym vlozenym markerem prekreslovala mapa a pocitaly pozice vsech markeru
	/*m.addLayer(layer); */                         /* Přidat ji do mapy */
	/*layer.enable(); */                         /* A povolit */

	var cz = m.computeCenterZoom(souradnice); /* Spočítat pozici mapy tak, aby značky byly vidět */
	m.setCenterZoom(cz[0], cz[1]);        

	// poslouchani na kliknuti u markeru - Pro zakladni info mame prirazenu card - Jake dalsi vyuziti ... vedle mapy mala karta s detaily, ktera by se vyplnila udaji o situ
	m.getSignals().addListener(this, "marker-click", function(e) {
	  // vybrany marker
	  var marker = e.target;
	  var id = marker.getId();
	  // zobrazime jeho jmeno - parovani vybraneho markeru pomoci jeho id a nasich vstupnich dat
	  for (var i = 0; i < markers.length; i++) {
		if (foundSites[i].id == id) {
			/* alert(sites[i].name); */
		    break;
		}
	  }
	});     	     	
 	
}
 
layer.enable();