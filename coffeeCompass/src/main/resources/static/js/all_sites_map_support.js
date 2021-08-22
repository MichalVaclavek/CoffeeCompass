var m;
var layer;
 
var souradnice = []; /* souradnice vsech bodu/znacek v mape */
var allSites = [];
var markers = [];

/**
 * Creates map ...
 * inserted in the list of all points (CoffeeSites) to be shown in map.
 */
createMap = function(lat1, lon1) {
   
	 var center = SMap.Coords.fromWGS84(lon1, lat1);
	 
	 m = new SMap(JAK.gel("map"), center, 8);
	 m.addControl(new SMap.Control.Sync());
	 m.addDefaultLayer(SMap.DEF_BASE).enable();
	 m.addDefaultControls();

     var l = m.addDefaultLayer(SMap.DEF_BASE).enable();
	 layer = new SMap.Layer.Marker();

//     var clusterer = new SMap.Marker.Clusterer(m);
//     layer.setClusterer(clusterer);
}

/**
 * Main function to be called from html to create map in given div element
 */
map.create = function() {
	 var	 latInit = 49.8250401;
	 var	 lonInit = 15.4190817;
	 createMap(latInit, lonInit);
}

/*
 * Inserts all CoffeeSites geo coordinates into the map as markers/icons with CoffeeSite image and some
 * additional info to be shown on the mark's card or in table
 */
map.insertSites = function(mAllSites)
{
	if (mAllSites != null && mAllSites.length > 0)
	{	     		     	
		allSites = mAllSites;
		// vytvoreni markeru pro kazdy site
		allSites.forEach(function(site) {
			var c = SMap.Coords.fromWGS84(site.zemDelka, site.zemSirka); /* Souřadnice značky, z textového formátu souřadnic */
		  
			var base_url = window.location.origin;
			
			var options = {
				url: base_url + "/images/cup_basic.png", /* cesta k /src/resources/images/cup.png v resourcech */	
				title: site.siteName, 
				anchor: {left:10, bottom: 1}  /* Ukotvení značky za bod uprostřed dole */
			}
			
			// prirazeni id jednotlivemu markeru - vlastni id, jinak se generuje nahodne
			var znacka = new SMap.Marker(c, site.id, options);
			var card = new SMap.Card();
			
			var headerText = "<b><span style='display:block;text-align:center; margin-bottom:10px;'>" + site.siteName + "</span></b>";
			
			card.getHeader().innerHTML = headerText;
			
			var bodyImage = "";
			
			if (!(site.mainImageURL === "")) { 
				bodyImage = "<img style='height:240px;' src='" + site.mainImageURL + "'/>";
				bodyImage += "<br><br>"; // to create space above text, because of rotated image
			}
			
			var textVizitka = bodyImage + "hodnoceni: " + site.averageStarsWithNumOfHodnoceni.common + "<br>";

			card.getBody().innerHTML = textVizitka;
			
			// Inserts link to CoffeeSite details page
			var footerText = "<a href='" + base_url + "/showSite/"; 
			footerText += site.id + "'>Další detaily ...</a>";
			
			card.getFooter().innerHTML = footerText;
	
			znacka.decorate(SMap.Marker.Feature.Card, card);
			
			souradnice.push(c);
			markers.push(znacka);
		});

	    layer.addMarker(markers);
	    m.addLayer(layer);
		var cz = m.computeCenterZoom(souradnice); /* Spočítat pozici mapy tak, aby vsechny značky byly vidět */
		m.setCenterZoom(cz[0], cz[1]);

		// poslouchani na kliknuti u markeru
        m.getSignals().addListener(this, "marker-click", function(e) {
          // vybrany marker
          var marker = e.target;
          var id = marker.getId();
          // zobrazit data o lokaci v tab. - parovani vybraneho markeru pomoci jeho id a nasich vstupnich dat
          for (var i = 0; i < markers.length; i++) {
          	if (allSites[i].id == id) {
            	fillInTable(allSites[i]);
                break;
            }
          }
        });
	}
}

fillInTable = function(site) {

    var siteNameTableCell = document.getElementById('siteName');
    var siteTypeTableCell = document.getElementById('coffeeSiteType');
    var locationTypeTableCell = document.getElementById('locationType');
    var priceRangeTableCell = document.getElementById('priceRange');
    var pristupnostDnyHodTableCell = document.getElementById('pristupnostDnyHod');
    var mestoTableCell = document.getElementById('mesto');
    var uliceTableCell = document.getElementById('uliceCP');
    var avgStarsTableCell = document.getElementById('avgStars');

    var siteImageDiv = document.getElementById('siteImage');

    clearAll = function() {
        siteNameTableCell.innerHTML = '';
        siteTypeTableCell.innerHTML = '';
        locationTypeTableCell.innerHTML = '';
        priceRangeTableCell.innerHTML = '';
        pristupnostDnyHodTableCell.innerHTML = '';
        mestoTableCell.innerHTML = '';
        uliceTableCell.innerHTML = '';
        avgStarsTableCell.innerHTML = '';

        siteImageDiv.src = '';
    }

    clearAll();

    // fill table cells with site's data
    siteNameTableCell.innerHTML = site.siteName;
    siteTypeTableCell.innerHTML = site.typPodniku != null ? site.typPodniku.coffeeSiteType : '';
    locationTypeTableCell.innerHTML = site.typLokality != null ? site.typLokality.locationType : '';
    priceRangeTableCell.innerHTML = site.cena != null ? site.cena.priceRange : '';
    pristupnostDnyHodTableCell.innerHTML = site.pristupnostDny + ", " + site.pristupnostHod;
    mestoTableCell.innerHTML = site.mesto;
    uliceTableCell.innerHTML = site.uliceCP;
    avgStarsTableCell.innerHTML = site.averageStarsWithNumOfHodnoceni.common;

    siteImageDiv.src = site.mainImageURL;
}


map.enable = function() {
	layer.enable();
}

var changeNodeVisibility = function(node) {
		node.disabled = !node.disabled;
		if (node.disabled) node.style.visibility = "hidden";
		if (!node.disabled) node.style.visibility = "visible";
}
